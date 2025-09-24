#!/bin/bash

# Terraform deployment script for User CRUD Lambda
set -e

echo "🚀 开始Terraform自动化部署..."

# 配置
TERRAFORM_DIR="terraform"
JAR_FILE="../target/user-lambda-1.0.0.jar"
REGION="ap-southeast-2"

# 检查JAR文件是否存在
if [ ! -f "$JAR_FILE" ]; then
    echo "❌ JAR文件不存在: $JAR_FILE"
    echo "请先运行: mvn clean package"
    exit 1
fi

echo "✅ JAR文件检查通过"

# 检查Terraform是否已安装
if ! command -v terraform &> /dev/null; then
    echo "❌ Terraform未安装"
    echo "请访问 https://terraform.io/downloads 下载并安装Terraform"
    exit 1
fi

echo "✅ Terraform已安装: $(terraform version -json | jq -r '.terraform_version')"

# 检查AWS CLI是否已安装
if ! command -v aws &> /dev/null; then
    echo "❌ AWS CLI未安装"
    echo "请访问 https://aws.amazon.com/cli/ 下载并安装AWS CLI"
    exit 1
fi

echo "✅ AWS CLI已安装"

# 检查AWS凭证是否配置
if ! aws sts get-caller-identity &> /dev/null; then
    echo "❌ AWS凭证未配置"
    echo "请运行: aws configure"
    exit 1
fi

echo "✅ AWS凭证已配置"

# 进入Terraform目录
cd "$TERRAFORM_DIR"

# 初始化Terraform
echo "🔧 初始化Terraform..."
terraform init

# 验证Terraform配置
echo "🔍 验证Terraform配置..."
terraform validate

# 格式化Terraform代码
echo "📝 格式化Terraform代码..."
terraform fmt -recursive

# 显示执行计划
echo "📋 显示执行计划..."
terraform plan

# 询问是否继续部署
echo ""
read -p "是否继续部署? (y/N): " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "❌ 部署已取消"
    exit 1
fi

# 执行部署
echo "🚀 开始部署..."
terraform apply -auto-approve

# 显示输出
echo ""
echo "🎉 部署完成！"
echo ""
echo "📋 部署信息："
terraform output

# 返回项目根目录
cd ..

echo ""
echo "🧪 测试命令："
API_ENDPOINT=$(cd terraform && terraform output -raw api_gateway_stage_url)
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
