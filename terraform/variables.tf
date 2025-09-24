# Variables for User CRUD Lambda Terraform configuration

variable "aws_region" {
  description = "AWS region for resources"
  type        = string
  default     = "ap-southeast-2"
}

variable "function_name" {
  description = "Name of the Lambda function"
  type        = string
  default     = "user-crud-lambda"
}

variable "api_name" {
  description = "Name of the API Gateway"
  type        = string
  default     = "user-api"
}

variable "stage_name" {
  description = "Name of the API Gateway stage"
  type        = string
  default     = "prod"
}

variable "db_url" {
  description = "Database connection URL"
  type        = string
  default     = "jdbc:mysql://ai-game.cfkuy6mi4nng.ap-southeast-2.rds.amazonaws.com:3306/ai-game?useSSL=true&serverTimezone=UTC&characterEncoding=utf8"
}

variable "db_user" {
  description = "Database username"
  type        = string
  default     = "chenghao"
}

variable "db_password" {
  description = "Database password"
  type        = string
  sensitive   = true
  default     = ""
}

variable "tags" {
  description = "Tags to apply to all resources"
  type        = map(string)
  default = {
    Project     = "UserLambda"
    Environment = "Production"
    ManagedBy   = "Terraform"
  }
}

variable "lambda_timeout" {
  description = "Lambda function timeout in seconds"
  type        = number
  default     = 30
}

variable "lambda_memory_size" {
  description = "Lambda function memory size in MB"
  type        = number
  default     = 512
}

variable "log_retention_days" {
  description = "CloudWatch log retention in days"
  type        = number
  default     = 14
}
