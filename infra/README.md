# Infrastructure (Terraform)

Provisions the full AWS stack for super-trading (spec §11):

- **Network** — VPC, 2 public + 2 private subnets across 2 AZs, IGW, single NAT.
- **Data** — RDS PostgreSQL (private), master password in Secrets Manager.
- **Messaging** — SQS jobs queue + dead-letter queue, EventBridge Scheduler group.
- **Compute** — ECS Fargate cluster; API service behind an ALB; worker task definition.
- **Edge** — S3 (frontend + screenshots), CloudFront (S3 default origin, `/api/*` and
  `/ws/*` → ALB), Origin Access Control.
- **Auth** — Cognito user pool, PKCE SPA client, hosted-UI domain, `ADMIN` group.
- **Registries** — ECR repos for the API and worker images.
- **IAM** — least-privilege roles: ECS execution (pull/logs/inject DB secret),
  API task (manage credential secrets + EventBridge schedules + PassRole),
  worker task (consume SQS, read secrets, write screenshots), scheduler delivery role.

## Usage

```bash
cd infra
cp terraform.tfvars.example terraform.tfvars   # edit as needed
terraform init
terraform plan
terraform apply
```

Then build & push the API/worker images to the created ECR repos and force a new
ECS deployment. The `terraform output` values feed the frontend `.env`
(`cognito_*`) and confirm the queue/role ARNs the API uses for scheduling.

## Notes / deliberate simplifications (v1)

- **TLS terminates at CloudFront**; CloudFront → ALB is HTTP. A custom domain +
  ACM certificate (in `us-east-1`) and an HTTPS ALB listener are a follow-up.
- The **worker** has a task definition but no always-on service or SQS→Fargate
  trigger wired (e.g. an EventBridge Pipe or a Lambda starter) — left as a follow-up.
- State is local; add a remote backend (S3 + DynamoDB lock) for real environments.
- The API container expects `spring-boot-starter-actuator` so the ALB health check
  on `/actuator/health` succeeds.
