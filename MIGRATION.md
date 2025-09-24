# Migration Guide: From Shell Scripts to Terraform

This guide helps you migrate from the existing shell script deployment to the new Terraform Infrastructure as Code approach.

## Why Migrate to Terraform?

### Current Shell Script Limitations
- ❌ No state management
- ❌ Difficult to track changes
- ❌ Hard to rollback
- ❌ Manual dependency management
- ❌ No version control for infrastructure
- ❌ Difficult to manage multiple environments

### Terraform Benefits
- ✅ **Infrastructure as Code**: Version controlled infrastructure
- ✅ **State Management**: Track resource changes and dependencies
- ✅ **Rollback Capability**: Easy to revert changes
- ✅ **Environment Management**: Easy to deploy to multiple environments
- ✅ **Team Collaboration**: Shared infrastructure state
- ✅ **Automation**: CI/CD integration ready
- ✅ **Dependency Management**: Automatic resource ordering
- ✅ **Plan Before Apply**: Review changes before deployment

## Migration Steps

### Step 1: Backup Current Resources

Before migrating, ensure you have backups of your current deployment:

```bash
# List current Lambda functions
aws lambda list-functions --query 'Functions[?FunctionName==`user-crud-lambda`]'

# List current API Gateways
aws apigatewayv2 get-apis --query 'Items[?Name==`user-api`]'

# Export current configuration
aws lambda get-function --function-name user-crud-lambda > lambda-backup.json
```

### Step 2: Install Terraform

```bash
# macOS
brew install terraform

# Verify installation
terraform version
```

### Step 3: Configure Terraform Variables

```bash
# Navigate to terraform directory
cd terraform

# Copy example variables
cp terraform.tfvars.example terraform.tfvars

# Edit with your actual values
nano terraform.tfvars
```

Key variables to update:
- `db_password`: Your database password
- `aws_region`: Your AWS region
- `tags`: Add your project tags

### Step 4: Test Terraform Configuration

```bash
# Initialize Terraform
terraform init

# Validate configuration
terraform validate

# Review the plan (this shows what will be created)
terraform plan
```

### Step 5: Deploy with Terraform

#### Option A: Using the deployment script (Recommended)
```bash
./deploy.sh
```

#### Option B: Manual deployment
```bash
# Apply the configuration
terraform apply

# Confirm with 'yes' when prompted
```

### Step 6: Verify Deployment

After deployment, verify that everything works:

```bash
# Get the API endpoint
terraform output api_gateway_stage_url

# Test the API
curl -X GET "$(terraform output -raw api_gateway_stage_url)/users?limit=5&offset=0"
```

### Step 7: Clean Up Old Resources (Optional)

If you want to remove the old shell-script deployed resources:

```bash
# Delete old Lambda function (if different name)
aws lambda delete-function --function-name user-crud-lambda-old

# Delete old API Gateway (if different name)
aws apigatewayv2 delete-api --api-id YOUR_OLD_API_ID
```

## Resource Mapping

| Shell Script Resource | Terraform Resource | Notes |
|----------------------|-------------------|-------|
| `user-crud-lambda` Lambda function | `aws_lambda_function.user_function` | Same configuration |
| `user-lambda-execution-role` IAM role | `aws_iam_role.lambda_execution_role` | Same permissions |
| `user-api` API Gateway | `aws_apigatewayv2_api.user_api` | HTTP API with CORS |
| Lambda permissions | `aws_lambda_permission.api_gateway_lambda` | Automatic management |
| CloudWatch logs | `aws_cloudwatch_log_group.lambda_logs` | Automatic creation |

## Environment Management

### Development Environment

Create a separate variables file for development:

```bash
# Create dev variables
cp terraform.tfvars.example terraform.dev.tfvars

# Edit dev-specific values
nano terraform.dev.tfvars
```

Deploy to development:
```bash
terraform apply -var-file="terraform.dev.tfvars"
```

### Production Environment

Use the default `terraform.tfvars` for production:
```bash
terraform apply
```

## Rollback Strategy

If you need to rollback to the shell script deployment:

1. **Destroy Terraform resources**:
   ```bash
   cd terraform
   ./destroy.sh
   ```

2. **Redeploy with shell script**:
   ```bash
   cd ..
   ./deploy-simple-auto.sh
   ```

## Troubleshooting

### Common Issues

1. **Resource already exists**
   ```bash
   # Import existing resource
   terraform import aws_lambda_function.user_function user-crud-lambda
   ```

2. **State file conflicts**
   ```bash
   # Refresh state
   terraform refresh
   ```

3. **Permission errors**
   ```bash
   # Check AWS credentials
   aws sts get-caller-identity
   ```

### Getting Help

- Check Terraform documentation: https://terraform.io/docs
- Review the Terraform README: `terraform/README.md`
- Check AWS provider documentation: https://registry.terraform.io/providers/hashicorp/aws/latest/docs

## Next Steps

After successful migration:

1. **Set up remote state** (for team collaboration):
   ```bash
   # Configure S3 backend
   terraform {
     backend "s3" {
       bucket = "your-terraform-state-bucket"
       key    = "user-lambda/terraform.tfstate"
       region = "ap-southeast-2"
     }
   }
   ```

2. **Add CI/CD integration**:
   - GitHub Actions
   - GitLab CI
   - Jenkins

3. **Set up monitoring**:
   - CloudWatch alarms
   - Custom dashboards
   - Log aggregation

4. **Implement security best practices**:
   - Use AWS Secrets Manager for passwords
   - Enable VPC configuration
   - Add API authentication

## Support

If you encounter issues during migration:

1. Check the troubleshooting section above
2. Review Terraform logs: `terraform apply -auto-approve`
3. Check AWS CloudTrail for API errors
4. Verify AWS permissions and quotas

The Terraform approach provides a more robust, maintainable, and scalable infrastructure management solution for your Lambda application.
