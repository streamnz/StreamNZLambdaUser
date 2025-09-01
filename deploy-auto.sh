#!/bin/bash

# 完整的自动化部署脚本
set -e

echo "🚀 开始自动化部署..."

# 配置
FUNCTION_NAME="user-crud-lambda"
ROLE_NAME="user-lambda-execution-role"
REGION="ap-southeast-2"
JAR_FILE="target/user-lambda-1.0.0.jar"
ACCOUNT_ID="463470976263"

# 检查 JAR 文件
if [ ! -f "$JAR_FILE" ]; then
    echo "❌ JAR 文件不存在: $JAR_FILE"
    echo "请先运行: mvn clean package"
    exit 1
fi

echo "✅ JAR 文件检查通过"

# 创建部署包
echo "📦 创建部署包..."
zip -j function.zip "$JAR_FILE"

# 步骤 1: 创建 IAM 角色
echo "🔐 步骤 1: 创建 IAM 角色..."

# 信任策略文档
TRUST_POLICY='{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "Service": "lambda.amazonaws.com"
      },
      "Action": "sts:AssumeRole"
    }
  ]
}'

# 检查角色是否存在
if aws iam get-role --role-name "$ROLE_NAME" >/dev/null 2>&1; then
    echo "✅ 角色已存在: $ROLE_NAME"
    ROLE_ARN=$(aws iam get-role --role-name "$ROLE_NAME" --query 'Role.Arn' --output text)
else
    echo "🆕 创建新角色: $ROLE_NAME"
    aws iam create-role \
        --role-name "$ROLE_NAME" \
        --assume-role-policy-document "$TRUST_POLICY" \
        --description "Execution role for user CRUD Lambda function"
    
    # 等待角色创建完成
    echo "⏳ 等待角色创建完成..."
    aws iam wait role-exists --role-name "$ROLE_NAME"
    
    ROLE_ARN=$(aws iam get-role --role-name "$ROLE_NAME" --query 'Role.Arn' --output text)
fi

# 附加基本执行策略
echo "📋 附加执行策略..."
aws iam attach-role-policy \
    --role-name "$ROLE_NAME" \
    --policy-arn "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"

echo "✅ IAM 角色配置完成: $ROLE_ARN"

# 步骤 2: 创建 Lambda 函数
echo "🔧 步骤 2: 创建 Lambda 函数..."

# 检查函数是否存在
if aws lambda get-function --function-name "$FUNCTION_NAME" --region "$REGION" >/dev/null 2>&1; then
    echo "📝 更新现有函数..."
    aws lambda update-function-code \
        --function-name "$FUNCTION_NAME" \
        --zip-file fileb://function.zip \
        --region "$REGION"
    
    # 更新配置
    aws lambda update-function-configuration \
        --function-name "$FUNCTION_NAME" \
        --runtime "java17" \
        --handler "com.example.lambda.UserHandler::handleRequest" \
        --timeout 30 \
        --memory-size 512 \
        --environment Variables='{DB_URL="jdbc:mysql://ai-game.cfkuy6mi4nng.ap-southeast-2.rds.amazonaws.com:3306/ai-game?useSSL=true&serverTimezone=UTC&characterEncoding=utf8",DB_USER="chenghao",DB_PASSWORD="C1h2E3n4G5^&"}' \
        --region "$REGION"
    
    echo "✅ 函数更新完成"
else
    echo "🆕 创建新函数..."
    aws lambda create-function \
        --function-name "$FUNCTION_NAME" \
        --runtime "java17" \
        --role "$ROLE_ARN" \
        --handler "com.example.lambda.UserHandler::handleRequest" \
        --zip-file fileb://function.zip \
        --timeout 30 \
        --memory-size 512 \
        --environment Variables='{DB_URL="jdbc:mysql://ai-game.cfkuy6mi4nng.ap-southeast-2.rds.amazonaws.com:3306/ai-game?useSSL=true&serverTimezone=UTC&characterEncoding=utf8",DB_USER="chenghao",DB_PASSWORD="C1h2E3n4G5^&"}' \
        --region "$REGION"
    
    echo "✅ 函数创建完成"
fi

# 步骤 3: 创建 API Gateway
echo "🌐 步骤 3: 创建 API Gateway..."

# 创建 HTTP API
echo "🆕 创建 HTTP API..."
API_ID=$(aws apigatewayv2 create-api \
    --name "user-api" \
    --protocol-type "HTTP" \
    --target "arn:aws:lambda:$REGION:$ACCOUNT_ID:function:$FUNCTION_NAME" \
    --region "$REGION" \
    --query 'ApiId' \
    --output text)

echo "✅ API Gateway 创建完成: $API_ID"

# 获取 API 端点
API_ENDPOINT=$(aws apigatewayv2 get-api \
    --api-id "$API_ID" \
    --region "$REGION" \
    --query 'ApiEndpoint' \
    --output text)

echo "✅ API 端点: $API_ENDPOINT"

# 步骤 4: 添加 Lambda 权限
echo "🔐 步骤 4: 添加 Lambda 权限..."

# 为 API Gateway 添加调用权限
aws lambda add-permission \
    --function-name "$FUNCTION_NAME" \
    --statement-id "apigateway-invoke" \
    --action "lambda:InvokeFunction" \
    --principal "apigateway.amazonaws.com" \
    --source-arn "arn:aws:execute-api:$REGION:$ACCOUNT_ID:$API_ID/*/*/*" \
    --region "$REGION"

echo "✅ Lambda 权限配置完成"

# 清理
rm -f function.zip

echo ""
echo "🎉 部署完成！"
echo ""
echo "📋 部署信息："
echo "  Lambda 函数: $FUNCTION_NAME"
echo "  IAM 角色: $ROLE_NAME"
echo "  API Gateway ID: $API_ID"
echo "  API 端点: $API_ENDPOINT"
echo ""
echo "🧪 测试命令："
echo "  # 创建用户"
echo "  curl -X POST $API_ENDPOINT/users \\"
echo "    -H \"Content-Type: application/json\" \\"
echo "    -d '{\"username\":\"test_user\",\"email\":\"test@example.com\",\"password\":\"password123\",\"score\":100}'"
echo ""
echo "  # 列表用户"
echo "  curl -X GET \"$API_ENDPOINT/users?limit=5&offset=0\""
echo ""
echo "  # 获取用户"
echo "  curl -X GET $API_ENDPOINT/users/1"
