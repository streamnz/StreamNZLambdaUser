# Terraform Infrastructure as Code for User CRUD Lambda

This directory contains Terraform configuration files to deploy the User CRUD Lambda function and API Gateway using Infrastructure as Code (IaC).

## Prerequisites

1. **Terraform** (>= 1.0)
   ```bash
   # Install via Homebrew (macOS)
   brew install terraform
   
   # Or download from https://terraform.io/downloads
   ```

2. **AWS CLI** configured with appropriate credentials
   ```bash
   aws configure
   ```

3. **Java JAR file** built and available at `../target/user-lambda-1.0.0.jar`
   ```bash
   mvn clean package
   ```

## File Structure

```
terraform/
├── main.tf                    # Main Terraform configuration
├── variables.tf               # Variable definitions
├── outputs.tf                 # Output definitions
├── terraform.tfvars.example   # Example variables file
├── terraform.tfvars          # Actual variables (not in git)
├── .gitignore                # Git ignore rules
├── deploy.sh                 # Deployment script
├── destroy.sh                # Destroy script
└── README.md                 # This file
```

## Quick Start

### 1. Setup Variables

Copy the example variables file and update with your values:

```bash
cp terraform.tfvars.example terraform.tfvars
```

Edit `terraform.tfvars` and update:
- `db_password`: Your actual database password
- `aws_region`: Your preferred AWS region
- Any other configuration values

### 2. Deploy

Run the deployment script:

```bash
./deploy.sh
```

Or manually:

```bash
# Initialize Terraform
terraform init

# Plan the deployment
terraform plan

# Apply the configuration
terraform apply
```

### 3. Test

After deployment, test the API using the provided curl commands in the output.

### 4. Destroy (when needed)

To remove all resources:

```bash
./destroy.sh
```

## Configuration

### Variables

Key variables you can customize in `terraform.tfvars`:

- `aws_region`: AWS region (default: ap-southeast-2)
- `function_name`: Lambda function name (default: user-crud-lambda)
- `api_name`: API Gateway name (default: user-api)
- `stage_name`: API Gateway stage (default: prod)
- `db_url`: Database connection URL
- `db_user`: Database username
- `db_password`: Database password (sensitive)
- `lambda_timeout`: Lambda timeout in seconds (default: 30)
- `lambda_memory_size`: Lambda memory in MB (default: 512)
- `log_retention_days`: CloudWatch log retention (default: 14)

### Resources Created

This Terraform configuration creates:

1. **Lambda Function**
   - Java 17 runtime
   - Environment variables for database connection
   - CloudWatch logging

2. **IAM Role**
   - Lambda execution role
   - Basic execution policy attached

3. **API Gateway HTTP API**
   - CORS configuration
   - Routes for `/users` and `/users/{id}`
   - Integration with Lambda function

4. **CloudWatch Log Groups**
   - Lambda function logs
   - API Gateway logs

## Security Notes

- `terraform.tfvars` contains sensitive information and is excluded from git
- Database password is marked as sensitive in Terraform
- IAM roles follow least privilege principle

## Troubleshooting

### Common Issues

1. **JAR file not found**
   ```bash
   mvn clean package
   ```

2. **AWS credentials not configured**
   ```bash
   aws configure
   ```

3. **Terraform not installed**
   ```bash
   brew install terraform  # macOS
   ```

4. **Permission denied on scripts**
   ```bash
   chmod +x deploy.sh destroy.sh
   ```

### Useful Commands

```bash
# Check Terraform version
terraform version

# Validate configuration
terraform validate

# Format code
terraform fmt -recursive

# Show current state
terraform show

# List resources
terraform state list

# Import existing resource (if needed)
terraform import aws_lambda_function.user_function user-crud-lambda
```

## Migration from Shell Scripts

This Terraform configuration replaces the shell scripts (`deploy-auto.sh`, `deploy-simple-auto.sh`) with:

- **Better state management**: Terraform tracks resource state
- **Dependency management**: Automatic resource ordering
- **Rollback capability**: Easy to revert changes
- **Version control**: Infrastructure changes are tracked
- **Reusability**: Easy to deploy to multiple environments

## Next Steps

1. **Environment Management**: Create separate `.tfvars` files for dev/staging/prod
2. **Remote State**: Use S3 backend for team collaboration
3. **Modules**: Extract reusable components
4. **CI/CD**: Integrate with GitHub Actions or similar
5. **Monitoring**: Add CloudWatch alarms and dashboards
