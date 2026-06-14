# super-trading

A scheduled web-automation platform. On a configurable interval a bot logs into an
external website, navigates it, clicks **GREEN** or **RED** per a pluggable strategy,
waits, and logs out. Every run and click is persisted; a React admin console lets
multiple users manage credentials/schedules and monitor activity.

> Implemented per the project spec, following Domain-Driven Design and a multi-user
> ownership model (`app_user` ──< `site_credential` >── `site`, with
> `schedule ──< execution ──< action`).

## Repository layout (monorepo, Maven multi-module)

```
super-trading/
├── pom.xml            parent aggregator + dependency management
├── domain/            shared library: entities, repositories, choice strategy, DB migrations
├── scheduling/        AWS EventBridge Scheduler adapter for the ScheduleProvisioner port
├── api/               Spring Boot app — REST + WebSocket (consumes domain + scheduling)
├── worker/            Spring Boot bot — SQS consumer + Playwright (consumes domain)
├── frontend/          React + Vite + TS admin console (TanStack Query, Tailwind, Recharts)
└── infra/             Terraform IaC — VPC, RDS, ECS/Fargate, ALB, SQS, CloudFront, Cognito
```

The `domain` module is a plain library shared by both the `api` service and the
`worker` task, exactly as the spec requires.

### Structural decisions

- **Monorepo, multi-module Maven** — single clone, atomic FE/BE commits, shared
  `domain` as a library; each app still deploys independently.
- **Base package** `com.rick.supertrading` (reusing the existing groupId).
- **Migrations live in `domain`** (`classpath:db/migration`) so every app that uses
  the domain gets the same schema.

## Current status (Build Order steps 1–8 — complete)

- ✅ **Step 1 — Database & migrations:** Flyway `V1__init.sql` with all 7 tables,
  constraints and indexes (spec §5.4).
- ✅ **Step 2 — Domain module:** JPA entities, ownership-aware repositories,
  `ChoiceStrategy` + `RandomChoiceStrategy` + `ChoiceStrategyResolver`, unit tests.
- ✅ **Step 3 — Backend API:** application services (ownership filtering + ADMIN
  bypass, audit on every mutation, JIT user provisioning), Cognito OIDC resource
  server with `cognito:groups`→`ROLE_*` mapping and method security, REST
  controllers (`/api/sites|credentials|schedules|executions|stats|admin`),
  validation + RFC 7807 errors, and a STOMP `/ws/monitoring` endpoint with a
  `MonitoringPublisher` adapter.
- ✅ **Step 4 — EventBridge integration:** `scheduling` module with
  `EventBridgeScheduleManager` (AWS SDK v2 Scheduler) implementing
  `ScheduleProvisioner` — creates/updates/deletes one EventBridge schedule per
  `schedule` row, targeting the SQS queue. On fire it delivers
  `{"scheduleId":…,"idempotencyKey":"<aws.scheduler.execution-id>"}`, giving the
  worker a unique idempotency key per invocation (spec §8.2). Gated by
  `supertrading.scheduling.enabled`; off by default (local no-op provisioner).
- ✅ **Step 5 — Bot worker:** `worker` Spring Boot module (Fargate-task model:
  drains SQS then exits). `BotJobProcessor` orchestrates idempotent recording →
  Secrets Manager read → strategy decision → Playwright session (login → navigate →
  GREEN/RED → wait → logout) → action/outcome recording → monitoring publish; on
  failure it captures a screenshot to S3. `BotRunner` is abstracted so the
  orchestration is unit-tested without a browser. Idempotency is enforced via the
  unique `execution.idempotency_key` (duplicate SQS deliveries are skipped).

- ✅ **Step 6 — React frontend** (`frontend/`): Vite + TypeScript + TanStack Query +
  Tailwind (shadcn-style primitives) + Recharts + react-router. Cognito OIDC (PKCE)
  auth via `react-oidc-context`; access token attached to REST (axios) and STOMP
  (SockJS) calls. Screens: Sites, Credentials, Schedules, live Monitoring (WebSocket),
  History + GREEN/RED analytics, and an Admin users view (shown only to the ADMIN
  Cognito group). Typechecks and builds clean (`npm run build`).

- ✅ **Step 7 — Terraform IaC** (`infra/`): full AWS stack — VPC (2 AZs, NAT),
  RDS PostgreSQL (private, password in Secrets Manager), SQS jobs queue + DLQ,
  EventBridge schedule group + delivery role, ECS Fargate cluster with the API
  service behind an ALB and a worker task definition, ECR repos, S3 (frontend +
  screenshots), CloudFront (S3 default origin, `/api/*` + `/ws/*` → ALB, OAC),
  Cognito user pool + PKCE SPA client + ADMIN group, and least-privilege IAM roles.
  `terraform fmt` + `terraform validate` pass. (Added `spring-boot-starter-actuator`
  to the API for a real ALB `/actuator/health` check.)
- ✅ **Step 8 — End-to-end test** (CI-gated harness): `WorkerEndToEndIT` seeds a
  user → credential → schedule in a real PostgreSQL (Testcontainers) and drives the
  worker's `BotJobProcessor` (browser stubbed) to assert the execution + click are
  recorded, a duplicate delivery is skipped (idempotency), and a bot failure is
  recorded with a screenshot key. `PlaywrightMockSiteIT` exercises the real browser
  flow against an embedded mock site. Both run under `mvn verify` (Failsafe);
  Docker-gated / flag-gated so they skip cleanly when unavailable.

**All 12 build-order steps are implemented.** Remaining items are the documented
follow-ups below, not new features.

### Remaining follow-ups (not blockers)

- API-side Secrets Manager **write** adapter (API still uses the local `SecretStore`;
  the worker already reads via `SecretReader`).
- SQS→Fargate **trigger** for the worker (task definition exists; no auto-start wired).
- ACM cert + custom domain + HTTPS ALB listener (CloudFront→ALB is HTTP in v1).
- Cross-process live monitoring: bridge the worker's status to the API's WebSocket.

### Frontend dev

```bash
cd frontend
cp .env.example .env.local   # fill in Cognito authority/client id/redirect
npm install
npm run dev                  # proxies /api and /ws to http://localhost:8080
```

### Ports & adapters

The domain defines outbound **ports** — `SecretStore`, `ScheduleProvisioner`,
`MonitoringPublisher`. The API ships local stand-in beans (`LocalAdaptersConfig`,
`@ConditionalOnMissingBean`) so it runs without AWS; Steps 4–5 add the real
EventBridge/Secrets Manager adapters, which transparently take over.

### Runtime config

- `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI` — **required** at runtime,
  e.g. `https://cognito-idp.<region>.amazonaws.com/<userPoolId>`.
- `DB_URL` / `DB_USERNAME` / `DB_PASSWORD` — datasource (Flyway applies the schema).
- `SCHEDULING_ENABLED=true` plus `AWS_REGION`, `SCHEDULING_QUEUE_ARN`,
  `SCHEDULING_ROLE_ARN` (and optional `SCHEDULING_GROUP`) — to provision real
  EventBridge schedules. Left off, schedules use the local no-op provisioner.
- **Worker:** `WORKER_QUEUE_URL`, `WORKER_SCREENSHOT_BUCKET`, `AWS_REGION`
  (optional `WORKER_NAVIGATE_MILLIS`, `WORKER_WAIT_SECONDS`, `WORKER_MAX_MESSAGES`).
  The worker needs Playwright browsers installed in its image
  (`mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args=install`,
  or the Playwright Docker base image).

## Build & test

```bash
./mvnw clean test     # unit tests (Surefire) — always green, no Docker needed
./mvnw clean verify   # also runs *IT integration tests (Failsafe)
```

- **Unit tests** (choice strategy/resolver, JWT authority mapping, schedule-expression
  mapping, bot-job orchestration with mocks) always run — no Docker.
- **Integration tests** (`*IT`: repository ownership, API context load, worker
  end-to-end) spin up a PostgreSQL **Testcontainer** with the Flyway schema and
  Hibernate in `validate` mode, run under **`mvn verify`**. They **skip automatically
  when Docker is unavailable**, so the local build stays green; they run fully in CI.
- The real-browser `PlaywrightMockSiteIT` runs only with `-De2e.playwright=true` and
  Playwright browsers installed (CI:
  `mvn -pl worker exec:java -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args=install`).

### Continuous integration

`.github/workflows/ci.yml` runs on pushes to `main`/`master` and on PRs:

- **backend** — `./mvnw -B clean verify`. The GitHub ubuntu runner's Docker daemon lets
  Testcontainers run the `*IT` integration tests for real; a follow-up step installs
  Chromium and runs the Playwright browser e2e (`-De2e.playwright=true`).
- **frontend** — `npm ci` + `npm run build` (type-check + Vite bundle).

Requires `frontend/package-lock.json` to be committed (used by `npm ci`).

### Running the API locally

Requires a PostgreSQL reachable at the configured datasource (defaults to
`jdbc:postgresql://localhost:5432/supertrading`, user/pass `supertrading`). Override
via `DB_URL` / `DB_USERNAME` / `DB_PASSWORD`. Flyway applies the schema on startup.

## Local simulation (run the whole system without AWS) — spec §14

The AWS edges sit behind the domain ports and are selected by **Spring profile**
(`local` vs `aws`), so the domain and bot/worker flow are identical to production. Locally,
AWS-API-compatible substitutes (ElasticMQ for SQS, LocalStack for Secrets Manager + S3) are
reached by endpoint override; the one piece with no emulator — EventBridge — is replaced by
an in-JVM `LocalScheduleTrigger` that enqueues the *same* SQS message shape (no 1-minute
floor). Auth is dev no-auth with a seeded demo ADMIN user.

### Config source of truth

Keep these identical across `docker-compose.local.yml`, `application-local.yaml`
(api + worker), `LocalSchedulingProperties`, and `LocalSecretsConfig`:

| Service    | Endpoint              | Identifier |
|------------|-----------------------|------------|
| Postgres   | `localhost:5432`      | `supertrading` / `supertrading` / `supertrading` |
| ElasticMQ  | `localhost:9324`      | queue `supertrading-jobs` |
| LocalStack | `localhost:4566`      | `s3` + `secretsmanager`; bucket `supertrading-screenshots` |
| Mock site  | `localhost:8088`      | `/login.html`, `/dashboard.html` |

### Run steps

**Terminal 1: Start Docker infrastructure (once)**

```bash
docker compose -f docker-compose.local.yml up -d
```

This starts Postgres, ElasticMQ, LocalStack, and the mock target site. The database is persisted
in a Docker volume (`pgdata`), so data survives container restarts (`docker compose down`). To
delete the volume and start fresh: `docker compose down -v`.

**Terminal 2: Run Flyway migrations (first time only, or after schema changes)**

```powershell
cd C:\job_local\repo_trading\super-trading
.\mvnw -pl domain flyway:migrate `
  "-Dflyway.url=jdbc:postgresql://localhost:5432/supertrading" `
  "-Dflyway.user=supertrading" `
  "-Dflyway.password=supertrading"
```

After the first migration, Flyway automatically runs on API startup, so you won't need this
command again unless you add new migrations.

**Terminal 3: Run the API (Flyway migrates automatically; LocalSeedRunner inserts demo user/site/credential+secret)**

```powershell
cd C:\job_local\repo_trading\super-trading
$env:SPRING_PROFILES_ACTIVE='local'
.\mvnw -pl api spring-boot:run
```

The API listens on `http://localhost:8080`. The local profile auto-seeds a demo ADMIN user
with a mock site and credential.

**Terminal 4: Run the Worker (host JVM, for local Chromium). Install browsers once:**

```bash
./mvnw -pl worker exec:java -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args=install
$env:SPRING_PROFILES_ACTIVE='local'
./mvnw -pl worker spring-boot:run   # continuous local poller
```

**Terminal 5: Run the Frontend (dev no-auth)**

```bash
cd frontend
cp .env.example .env.local   # set VITE_AUTH_DISABLED=true
npm install
npm run dev
```

### Using the local system

In the console create a schedule with a short interval (e.g. 30s) on the seeded credential.
`LocalScheduleTrigger` enqueues every interval → the worker consumes → `execution`/`action`
rows appear → the Monitoring screen streams status live over WebSocket. To watch the browser,
run the worker with Playwright headed mode.

### What local simulation does NOT validate (spec §14.6)

IAM / least-privilege, VPC networking + security groups + ALB routing, Fargate cold-start /
scaling latency, Cognito token/claim specifics, and EventBridge timing/throttling. Validate
these on a real staging environment before production.
