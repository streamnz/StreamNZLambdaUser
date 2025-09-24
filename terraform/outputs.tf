# Outputs for User CRUD Lambda Terraform configuration

output "lambda_function_name" {
  description = "Name of the Lambda function"
  value       = aws_lambda_function.user_function.function_name
}

output "lambda_function_arn" {
  description = "ARN of the Lambda function"
  value       = aws_lambda_function.user_function.arn
}

output "lambda_function_invoke_arn" {
  description = "Invoke ARN of the Lambda function"
  value       = aws_lambda_function.user_function.invoke_arn
}

output "api_gateway_id" {
  description = "ID of the API Gateway"
  value       = aws_apigatewayv2_api.user_api.id
}

output "api_gateway_endpoint" {
  description = "API Gateway endpoint URL"
  value       = aws_apigatewayv2_api.user_api.api_endpoint
}

output "api_gateway_stage_url" {
  description = "API Gateway stage URL"
  value       = "${aws_apigatewayv2_api.user_api.api_endpoint}/${aws_apigatewayv2_stage.user_api_stage.name}"
}

output "lambda_execution_role_arn" {
  description = "ARN of the Lambda execution role"
  value       = aws_iam_role.lambda_execution_role.arn
}

output "cloudwatch_log_group_name" {
  description = "Name of the CloudWatch log group"
  value       = aws_cloudwatch_log_group.lambda_logs.name
}

# Test commands output
output "test_commands" {
  description = "Sample test commands for the API"
  value = {
    create_user = "curl -X POST ${aws_apigatewayv2_api.user_api.api_endpoint}/${aws_apigatewayv2_stage.user_api_stage.name}/users -H \"Content-Type: application/json\" -d '{\"username\":\"test_user\",\"email\":\"test@example.com\",\"password\":\"password123\",\"score\":100}'"
    list_users  = "curl -X GET \"${aws_apigatewayv2_api.user_api.api_endpoint}/${aws_apigatewayv2_stage.user_api_stage.name}/users?limit=5&offset=0\""
    get_user    = "curl -X GET ${aws_apigatewayv2_api.user_api.api_endpoint}/${aws_apigatewayv2_stage.user_api_stage.name}/users/1"
  }
}
