# ── EventBridge Scheduler group ────────────────────────────────────────────────
# Individual schedules (one per `schedule` row) are created at runtime by the API
# via the AWS SDK; Terraform only provisions the group and the delivery role.

resource "aws_scheduler_schedule_group" "main" {
  name = "${local.name_prefix}-schedules"
}

# Role EventBridge Scheduler assumes to send job messages to SQS.
resource "aws_iam_role" "scheduler" {
  name = "${local.name_prefix}-scheduler"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect    = "Allow"
      Principal = { Service = "scheduler.amazonaws.com" }
      Action    = "sts:AssumeRole"
      Condition = {
        StringEquals = { "aws:SourceAccount" = data.aws_caller_identity.current.account_id }
      }
    }]
  })
}

resource "aws_iam_role_policy" "scheduler_send" {
  name = "send-to-jobs-queue"
  role = aws_iam_role.scheduler.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect   = "Allow"
      Action   = "sqs:SendMessage"
      Resource = aws_sqs_queue.jobs.arn
    }]
  })
}
