output "cloudfront_domain" {
  description = "Public console URL host"
  value       = aws_cloudfront_distribution.main.domain_name
}

output "alb_dns_name" {
  description = "API load balancer DNS name"
  value       = aws_lb.main.dns_name
}

output "cognito_user_pool_id" {
  value = aws_cognito_user_pool.main.id
}

output "cognito_client_id" {
  value = aws_cognito_user_pool_client.spa.id
}

output "cognito_issuer_uri" {
  value = local.cognito_issuer
}

output "cognito_domain" {
  value = "${aws_cognito_user_pool_domain.main.domain}.auth.${var.region}.amazoncognito.com"
}

output "jobs_queue_url" {
  value = aws_sqs_queue.jobs.url
}

output "jobs_queue_arn" {
  value = aws_sqs_queue.jobs.arn
}

output "scheduler_role_arn" {
  value = aws_iam_role.scheduler.arn
}

output "scheduler_group_name" {
  value = aws_scheduler_schedule_group.main.name
}

output "ecr_api_repository_url" {
  value = aws_ecr_repository.api.repository_url
}

output "ecr_worker_repository_url" {
  value = aws_ecr_repository.worker.repository_url
}

output "rds_endpoint" {
  value = aws_db_instance.main.address
}

output "frontend_bucket" {
  value = aws_s3_bucket.frontend.id
}

output "screenshots_bucket" {
  value = aws_s3_bucket.screenshots.id
}
