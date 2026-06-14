# CLAUDE.md

Guidance for Claude working in this repo. Keep it accurate; update it when facts change.

## What this is

A **scheduled web-automation platform**: on an interval it logs into an external site,
clicks **GREEN** or **RED** via a pluggable strategy, waits, logs out, and records every
run/click. Built per a detailed spec following **Domain-Driven Design**. All 12 spec
build-order steps plus "§14 local simulation" are implemented.

- **Stack:** Java 21, Spring Boot **4.1.0**, PostgreSQL + Flyway, AWS SDK v2, Playwright,
  React 18 + Vite + TS. Maven multi-module monorepo. Base package `com.rick.supertrading`.

## Module layout

```
domain/      shared library: JPA entities, repositories, ChoiceStrategy, application
             services, outbound PORTS, Flyway migrations. No AWS/web deps. Never put
             framework/AWS specifics here.
scheduling/  ScheduleProvisioner adapters: EventBridgeScheduleManager (aws) +
             LocalScheduleTrigger (local, in-JVM). Has AWS SDK `scheduler` + `sqs`.
api/         Spring Boot app: REST + STOMP WebSocket, Cognito OIDC resource server,
             ownership filtering, audit. Depends on domain + scheduling.
worker/      Spring Boot Fargate-task bot: SQS consumer + Playwright. Depends on domain.
frontend/    React + Vite + TS admin console (TanStack Query, Tailwind, Recharts, OIDC).
infra/       Terraform (VPC, RDS, ECS/Fargate, ALB, SQS, CloudFront, Cognito, IAM).
mock-target-site/  static HTML for local simulation.
```

The **domain** module is the shared library; the worker/api consume it. Per spec the
domain + worker bot flow must stay **byte-for-byte identical** across environments — don't
leak AWS SDK types into them.

## Architecture: ports & adapters

Domain defines outbound ports (`domain/.../port/`): `SecretStore` (write), `SecretReader`
(read), `ScheduleProvisioner`, `ScreenshotStore`, `MonitoringPublisher`. Adapters are
selected by **Spring profile**: `@Profile("!local")` = cloud/default (so "no profile"
behaves like aws and existing tests are unaffected), `@Profile("local")` = local sim.
ElasticMQ/LocalStack are AWS-API-compatible, so local SQS/S3/Secrets reuse the same SDK
clients with an **endpoint override** — no new interfaces.

Data model is multi-user: `app_user ──< site_credential >── site`, `schedule ──< execution
──< action`. Reads filter by owner; ADMIN bypasses. Idempotency: unique
`execution.idempotency_key` (the scheduler injects it per fire).

## Build & test

```bash
./mvnw clean test     # unit tests only (Surefire) — always green, NO Docker needed
./mvnw clean verify   # adds *IT integration tests (Failsafe + Testcontainers/Docker)
cd frontend && npm ci && npm run build   # type-check + Vite build
```

- Unit tests never need Docker. `*IT` tests boot a real PostgreSQL via **Testcontainers**
  and **self-skip when Docker is unavailable** (a `dockerAvailable()` guard), so the local
  build stays green without a daemon. They run for real in CI (Linux).
- The real-browser `PlaywrightMockSiteIT` runs only with `-De2e.playwright=true` and
  installed browsers.
- CI: `.github/workflows/ci.yml` runs `mvn verify` (Testcontainers on the runner's Docker)
  + the Playwright e2e + the frontend build.

## CRITICAL gotchas (these cost real time — read before debugging)

1. **Spring Boot 4 relocated the test-slice annotations.** `@DataJpaTest`, `@WebMvcTest`,
   `@AutoConfigureTestDatabase`, and `@EntityScan` are NOT on the default test classpath.
   Don't use them. Patterns used here instead:
   - Repository/integration tests: plain `@SpringBootTest` with a minimal
     `@SpringBootConfiguration @EnableAutoConfiguration` class placed at the
     `com.rick.supertrading.domain` package root (default entity/repo scanning covers
     `.model`/`.repository`).
   - Web/security: unit-test the logic directly (e.g. the JWT authority converter) — no
     `@WebMvcTest`.

2. **AWS SDK v2 builder types are top-level**, not nested: use `SqsClientBuilder`,
   `S3ClientBuilder`, `SchedulerClientBuilder` — NOT `SqsClient.Builder` etc. (compile error).

3. **Docker Desktop 29.x on Windows is incompatible with docker-java** (the client
   Testcontainers embeds): it returns HTTP 400 over both named pipe and TCP, so the `*IT`
   tests can't run locally on such a setup. They run fine on Linux CI. To run locally, use
   WSL2 native Docker engine / Rancher Desktop, or Docker Desktop with Engine ≤ 28.x.
   (Bumping Testcontainers/docker-java did NOT fix it — don't bother.)

4. **`aws` profile requires `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI`** at
   startup (Cognito). It's intentionally not defaulted (avoids an empty-issuer trap). The
   api context test supplies a stub `JwtDecoder` via `TestSecurityConfig`.

## Local simulation (no AWS) — see README §"Local simulation"

`docker compose -f docker-compose.local.yml up -d` (Postgres + ElasticMQ + LocalStack +
mock site), then run api & worker with `SPRING_PROFILES_ACTIVE=local` and the frontend with
`VITE_AUTH_DISABLED=true`. Local profile: dev no-auth (fixed demo ADMIN), in-JVM
`LocalScheduleTrigger` (no 1-min floor), `LocalSqsPoller` (continuous), `LocalSeedRunner`
(demo user/site/credential). Keep config identical across compose,
`application-local.yaml` (api+worker), `LocalSchedulingProperties`, `LocalSecretsConfig`
(README has the source-of-truth table).

## Conventions

- Match surrounding code style; constructor injection; Javadoc on public types.
- Mutating endpoints write an `audit_log` row; reads are ownership-scoped.
- Commits: **NEVER** add a `Co-Authored-By: Claude` trailer (or any AI co-author/attribution).
  Author commits solely as the user's git identity. Branch off `master` before committing if
  asked to commit on the default branch.
- Remote `origin` is `git@github.com:sourcelight/super-trading.git` (SSH; the github key is
  pinned in `~/.ssh/config` under `Host github.com`).

## Known follow-ups (not yet done)

API-side Secrets Manager **write** adapter for the `aws` profile (worker already reads);
SQS→Fargate trigger for the worker; ACM/custom domain + HTTPS ALB listener (CloudFront→ALB
is HTTP in v1); cross-process live monitoring bridge (worker status → API WebSocket).
