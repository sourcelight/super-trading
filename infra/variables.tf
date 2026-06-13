variable "project" {
  description = "Project name, used as a resource name prefix"
  type        = string
  default     = "super-trading"
}

variable "environment" {
  description = "Deployment environment (e.g. dev, prod)"
  type        = string
  default     = "dev"
}

variable "region" {
  description = "AWS region"
  type        = string
  default     = "eu-west-1"
}

variable "vpc_cidr" {
  description = "CIDR block for the VPC"
  type        = string
  default     = "10.0.0.0/16"
}

variable "db_name" {
  description = "PostgreSQL database name"
  type        = string
  default     = "supertrading"
}

variable "db_username" {
  description = "PostgreSQL master username"
  type        = string
  default     = "supertrading"
}

variable "db_instance_class" {
  description = "RDS instance class"
  type        = string
  default     = "db.t4g.micro"
}

variable "db_allocated_storage" {
  description = "RDS allocated storage (GiB)"
  type        = number
  default     = 20
}

variable "api_image" {
  description = "Container image for the API service (defaults to the project ECR repo :latest)"
  type        = string
  default     = ""
}

variable "worker_image" {
  description = "Container image for the bot worker (defaults to the project ECR repo :latest)"
  type        = string
  default     = ""
}

variable "api_desired_count" {
  description = "Number of API tasks to run"
  type        = number
  default     = 1
}

variable "api_cpu" {
  description = "API task CPU units"
  type        = number
  default     = 512
}

variable "api_memory" {
  description = "API task memory (MiB)"
  type        = number
  default     = 1024
}

variable "worker_cpu" {
  description = "Worker task CPU units"
  type        = number
  default     = 1024
}

variable "worker_memory" {
  description = "Worker task memory (MiB)"
  type        = number
  default     = 2048
}
