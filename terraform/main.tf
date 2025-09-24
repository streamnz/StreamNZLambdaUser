# Terraform configuration for User CRUD Lambda with API Gateway
terraform {
  required_version = ">= 1.0"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
    archive = {
      source  = "hashicorp/archive"
      version = "~> 2.0"
    }
  }
}

# Configure the AWS Provider
provider "aws" {
  region = var.aws_region
}

# Data source to get current AWS account ID
data "aws_caller_identity" "current" {}

# Data source to get current AWS region
data "aws_region" "current" {}

# Archive the JAR file for Lambda deployment
data "archive_file" "lambda_zip" {
  type        = "zip"
  source_file = "../target/user-lambda-1.0.0.jar"
  output_path = "lambda_function.zip"
}

# IAM role for Lambda execution
resource "aws_iam_role" "lambda_execution_role" {
  name = "${var.function_name}-execution-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "lambda.amazonaws.com"
        }
      }
    ]
  })

  tags = var.tags
}

# Attach basic execution policy to Lambda role
resource "aws_iam_role_policy_attachment" "lambda_basic_execution" {
  role       = aws_iam_role.lambda_execution_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
}

# Lambda function
resource "aws_lambda_function" "user_function" {
  filename         = data.archive_file.lambda_zip.output_path
  function_name    = var.function_name
  role             = aws_iam_role.lambda_execution_role.arn
  handler          = "com.example.lambda.UserHandler::handleRequest"
  source_code_hash = data.archive_file.lambda_zip.output_base64sha256
  runtime          = "java17"
  timeout          = 30
  memory_size      = 512

  environment {
    variables = {
      DB_URL      = var.db_url
      DB_USER     = var.db_user
      DB_PASSWORD = var.db_password
    }
  }

  tags = var.tags

  depends_on = [
    aws_iam_role_policy_attachment.lambda_basic_execution,
    aws_cloudwatch_log_group.lambda_logs
  ]
}

# CloudWatch Log Group for Lambda
resource "aws_cloudwatch_log_group" "lambda_logs" {
  name              = "/aws/lambda/${var.function_name}"
  retention_in_days = 14
  tags              = var.tags
}

# API Gateway HTTP API
resource "aws_apigatewayv2_api" "user_api" {
  name          = var.api_name
  protocol_type = "HTTP"
  description   = "User CRUD API"

  cors_configuration {
    allow_credentials = false
    allow_headers     = ["content-type", "x-amz-date", "authorization", "x-api-key", "x-amz-security-token"]
    allow_methods     = ["GET", "POST", "PUT", "DELETE", "OPTIONS"]
    allow_origins     = ["*"]
    expose_headers    = ["date", "keep-alive"]
    max_age           = 86400
  }

  tags = var.tags
}

# API Gateway Stage
resource "aws_apigatewayv2_stage" "user_api_stage" {
  api_id      = aws_apigatewayv2_api.user_api.id
  name        = var.stage_name
  auto_deploy = true

  access_log_settings {
    destination_arn = aws_cloudwatch_log_group.api_gateway_logs.arn
    format = jsonencode({
      requestId      = "$context.requestId"
      ip             = "$context.identity.sourceIp"
      requestTime    = "$context.requestTime"
      httpMethod     = "$context.httpMethod"
      routeKey       = "$context.routeKey"
      status         = "$context.status"
      protocol       = "$context.protocol"
      responseLength = "$context.responseLength"
    })
  }

  tags = var.tags
}

# CloudWatch Log Group for API Gateway
resource "aws_cloudwatch_log_group" "api_gateway_logs" {
  name              = "/aws/apigateway/${var.api_name}"
  retention_in_days = 14
  tags              = var.tags
}

# API Gateway Integration
resource "aws_apigatewayv2_integration" "lambda_integration" {
  api_id                 = aws_apigatewayv2_api.user_api.id
  integration_type       = "AWS_PROXY"
  integration_uri        = aws_lambda_function.user_function.invoke_arn
  integration_method     = "POST"
  payload_format_version = "2.0"
}

# API Gateway Routes
resource "aws_apigatewayv2_route" "users_route" {
  api_id    = aws_apigatewayv2_api.user_api.id
  route_key = "ANY /users"
  target    = "integrations/${aws_apigatewayv2_integration.lambda_integration.id}"
}

resource "aws_apigatewayv2_route" "users_with_id_route" {
  api_id    = aws_apigatewayv2_api.user_api.id
  route_key = "ANY /users/{id}"
  target    = "integrations/${aws_apigatewayv2_integration.lambda_integration.id}"
}

# Lambda permission for API Gateway
resource "aws_lambda_permission" "api_gateway_lambda" {
  statement_id  = "AllowExecutionFromAPIGateway"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.user_function.function_name
  principal     = "apigateway.amazonaws.com"
  source_arn    = "${aws_apigatewayv2_api.user_api.execution_arn}/*/*"
}
