data "aws_availability_zones" "available" {
  state = "available"
}

data "aws_caller_identity" "current" {}

locals {
  name_prefix = "${var.project}-${var.environment}"

  azs            = slice(data.aws_availability_zones.available.names, 0, 2)
  public_subnets = [for i in range(2) : cidrsubnet(var.vpc_cidr, 8, i)]
  # Offset private subnets so they never overlap with the public ones.
  private_subnets = [for i in range(2) : cidrsubnet(var.vpc_cidr, 8, i + 10)]

  api_image    = var.api_image != "" ? var.api_image : "${aws_ecr_repository.api.repository_url}:latest"
  worker_image = var.worker_image != "" ? var.worker_image : "${aws_ecr_repository.worker.repository_url}:latest"

  common_tags = {
    Project     = var.project
    Environment = var.environment
    ManagedBy   = "terraform"
  }
}
