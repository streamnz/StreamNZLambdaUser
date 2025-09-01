#!/bin/bash

# 简化版自动化部署脚本
set -e

echo "🚀 开始简化自动化部署..."

# 配置
FUNCTION_NAME="user-crud-lambda"
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

# 步骤 1: 检查现有角色
echo "🔐 步骤 1: 检查 IAM 角色..."

# 尝试查找现有的 Lambda 执行角色
EXISTING_ROLE=$(aws iam list-roles --query 'Roles[?contains(RoleName, `lambda`) && contains(RoleName, `execution`)].RoleName' --output text 2>/dev/null | head -1)

if [ -n "$EXISTING_ROLE" ]; then
    echo "✅ 找到现有角色: $EXISTING_ROLE"
    ROLE_ARN=$(aws iam get-role --role-name "$EXISTING_ROLE" --query 'Role.Arn' --output text)
else
    echo "⚠️  未找到合适的现有角色"
    echo "📋 请先在 AWS Console 中创建 Lambda 执行角色："
    echo "1. 进入 IAM 控制台"
    echo "2. 创建角色，选择 Lambda 服务"
    echo "3. 附加 AWSLambdaBasicExecutionRole 策略"
    echo "4. 角色名称: user-lambda-execution-role"
    echo ""
    read -p "请输入现有角色的 ARN（或按回车跳过）: " ROLE_ARN
fi

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
    if [ -n "$ROLE_ARN" ]; then
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
    else
        echo "❌ 无法创建函数：缺少执行角色"
        echo "请先在 AWS Console 中创建 Lambda 执行角色"
        exit 1
    fi
fi

# 步骤 3: 创建 API Gateway
echo "🌐 步骤 3: 创建 API Gateway..."

# 检查是否已有 API
EXISTING_API=$(aws apigatewayv2 get-apis --region "$REGION" --query 'Items[?Name==`user-api`].ApiId' --output text 2>/dev/null)

if [ -n "$EXISTING_API" ]; then
    echo "✅ 找到现有 API: $EXISTING_API"
    API_ID="$EXISTING_API"
else
    echo "🆕 创建 HTTP API..."
    API_ID=$(aws apigatewayv2 create-api \
        --name "user-api" \
        --protocol-type "HTTP" \
        --target "arn:aws:lambda:$REGION:$ACCOUNT_ID:function:$FUNCTION_NAME" \
        --region "$REGION" \
        --query 'ApiId' \
        --output text)
    
    echo "✅ API Gateway 创建完成: $API_ID"
fi

# 获取 API 端点
API_ENDPOINT=$(aws apigatewayv2 get-api \
    --api-id "$API_ID" \
    --region "$REGION" \
    --query 'ApiEndpoint' \
    --output text)

echo "✅ API 端点: $API_ENDPOINT"

# 步骤 4: 添加 Lambda 权限
echo "🔐 步骤 4: 添加 Lambda 权限..."

# 检查权限是否已存在
if ! aws lambda get-policy --function-name "$FUNCTION_NAME" --region "$REGION" 2>/dev/null | grep -q "apigateway-invoke"; then
    aws lambda add-permission \
        --function-name "$FUNCTION_NAME" \
        --statement-id "apigateway-invoke" \
        --action "lambda:InvokeFunction" \
        --principal "apigateway.amazonaws.com" \
        --source-arn "arn:aws:execute-api:$REGION:$ACCOUNT_ID:$API_ID/*/*/*" \
        --region "$REGION"
    
    echo "✅ Lambda 权限配置完成"
else
    echo "✅ Lambda 权限已存在"
fi

# 清理
rm -f function.zip

echo ""
echo "🎉 部署完成！"
echo ""
echo "📋 部署信息："
echo "  Lambda 函数: $FUNCTION_NAME"
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
