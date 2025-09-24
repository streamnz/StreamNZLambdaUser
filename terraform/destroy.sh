#!/bin/bash

# Terraform destroy script for User CRUD Lambda
set -e

echo "⚠️  准备销毁Terraform资源..."

# 配置
TERRAFORM_DIR="terraform"

# 检查Terraform是否已安装
if ! command -v terraform &> /dev/null; then
    echo "❌ Terraform未安装"
    exit 1
fi

# 进入Terraform目录
cd "$TERRAFORM_DIR"

# 显示将要销毁的资源
echo "📋 将要销毁的资源："
terraform plan -destroy

# 确认销毁
echo ""
echo "⚠️  警告：此操作将销毁所有Terraform管理的资源！"
echo "包括："
echo "  - Lambda函数"
echo "  - API Gateway"
echo "  - IAM角色和策略"
echo "  - CloudWatch日志组"
echo ""
read -p "确认要销毁所有资源吗? 请输入 'yes' 确认: " -r
if [[ ! $REPLY == "yes" ]]; then
    echo "❌ 销毁操作已取消"
    exit 1
fi

# 执行销毁
echo "🗑️  开始销毁资源..."
terraform destroy -auto-approve

echo ""
echo "✅ 资源销毁完成！"
