# User CRUD Lambda API

A Java 17 AWS Lambda function that provides CRUD operations for users through API Gateway HTTP API.

## Features

- **Java 17 + Maven** project structure
- **AWS Lambda** with **API Gateway HTTP API** integration
- **MySQL RDS** database integration
- **Complete CRUD operations** for users
- **Pagination support** for list operations
- **PreparedStatement** usage for SQL injection prevention
- **Structured logging** with SLF4J
- **Unified JSON response** format
- **Error handling** with appropriate HTTP status codes

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/users` | Create new user |
| GET | `/users` | List users (with pagination) |
| GET | `/users/{id}` | Get user by ID |
| PUT | `/users/{id}` | Update user |
| DELETE | `/users/{id}` | Delete user |

### Query Parameters for List Users

- `limit`: Number of records to return (default: 10, max: 100)
- `offset`: Number of records to skip (default: 0)

Example: `GET /users?limit=5&offset=10`

### Response Format

All responses follow this unified JSON format:

```json
{
  "success": true,
  "data": {
    // Response data here
  }
}
```

Error responses:

```json
{
  "success": false,
  "error": "Error message here"
}
```

## Prerequisites

- **JDK 17** installed and configured
- **Maven 3.6+** installed
- **AWS CLI** installed and configured
- **AWS SAM CLI** installed (for deployment)
- **MySQL RDS** instance accessible

### Verify Prerequisites

```bash
# Check Java version
java -version

# Check Maven version
mvn -version

# Check AWS CLI configuration
aws sts get-caller-identity

# Check SAM CLI version
sam --version
```

## Database Setup

### 1. Connect to MySQL RDS

```bash
mysql -h ai-game.cfkuy6mi4nng.ap-southeast-2.rds.amazonaws.com -P 3306 -u chenghao -p ai-game
```

### 2. Execute SQL Script

```bash
mysql -h ai-game.cfkuy6mi4nng.ap-southeast-2.rds.amazonaws.com -P 3306 -u chenghao -p ai-game < sql/users.sql
```

Or copy and paste the contents of `sql/users.sql` into your MySQL client.

## Security Notice

⚠️ **Important**: This repository contains sensitive configuration files that should not be committed to version control.

### Protected Files
- `env.json` - Contains database credentials and is excluded from Git
- `samconfig.toml` - Contains AWS deployment configuration
- `deploy-auto.sh` - Contains hardcoded credentials

### Setup Instructions
1. Copy `env.example.json` to `env.json`
2. Update `env.json` with your actual database credentials
3. Never commit `env.json` to version control

## Local Development

### 1. Set Environment Variables

First, create your environment configuration:

```bash
# Copy the example configuration
cp env.example.json env.json

# Edit the configuration with your actual values
nano env.json
```

```bash
export DB_URL="jdbc:mysql://ai-game.cfkuy6mi4nng.ap-southeast-2.rds.amazonaws.com:3306/ai-game?useSSL=true&serverTimezone=UTC&characterEncoding=utf8"
export DB_USER="chenghao"
export DB_PASSWORD="<<YOUR_PASSWORD>>"
```

### 2. Build the Project

```bash
mvn clean package
ls target/*-shaded.jar
```

### 3. Test Database Connection

```bash
# Create a simple test class or use the Db utility
java -cp target/user-lambda-1.0.0-shaded.jar com.example.lambda.util.Db
```

## Deployment

### Option 1: Using AWS SAM (Recommended)

#### 1. Build SAM Application

```bash
sam build
```

#### 2. Deploy to AWS

```bash
sam deploy --guided
```

When prompted, provide:
- **Stack Name**: `user-lambda-stack`
- **AWS Region**: `ap-southeast-2`
- **Database Password**: `<<YOUR_PASSWORD>>`
- **Confirm changes**: `y`
- **Deploy**: `y`

#### 3. Get API Endpoint

```bash
sam list endpoints
```

Or check the CloudFormation outputs:

```bash
aws cloudformation describe-stacks --stack-name user-lambda-stack --query 'Stacks[0].Outputs[?OutputKey==`UserApi`].OutputValue' --output text
```

### Option 2: Manual Deployment

#### 1. Create Lambda Function

```bash
# Create Lambda function
aws lambda create-function \
  --function-name user-crud-lambda \
  --runtime java17 \
  --role arn:aws:iam::YOUR_ACCOUNT_ID:role/lambda-execution-role \
  --handler com.example.lambda.UserHandler::handleRequest \
  --zip-file fileb://target/user-lambda-1.0.0-shaded.jar \
  --timeout 30 \
  --memory-size 512 \
  --environment Variables='{DB_URL="jdbc:mysql://ai-game.cfkuy6mi4nng.ap-southeast-2.rds.amazonaws.com:3306/ai-game?useSSL=true&serverTimezone=UTC&characterEncoding=utf8",DB_USER="chenghao",DB_PASSWORD="<<YOUR_PASSWORD>>"}'
```

#### 2. Create API Gateway

```bash
# Create HTTP API
aws apigatewayv2 create-api \
  --name user-api \
  --protocol-type HTTP \
  --target arn:aws:lambda:ap-southeast-2:YOUR_ACCOUNT_ID:function:user-crud-lambda
```

## Testing

### 1. Create User

```bash
curl -X POST https://YOUR_API_ID.execute-api.ap-southeast-2.amazonaws.com/prod/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "test_user",
    "email": "test@example.com",
    "password": "password123",
    "score": 100,
    "wallet_address": "0x1234567890abcdef",
    "wallet_type": "ETH"
  }'
```

### 2. List Users

```bash
curl -X GET "https://YOUR_API_ID.execute-api.ap-southeast-2.amazonaws.com/prod/users?limit=5&offset=0"
```

### 3. Get User by ID

```bash
curl -X GET https://YOUR_API_ID.execute-api.ap-southeast-2.amazonaws.com/prod/users/1
```

### 4. Update User

```bash
curl -X PUT https://YOUR_API_ID.execute-api.ap-southeast-2.amazonaws.com/prod/users/1 \
  -H "Content-Type: application/json" \
  -d '{
    "username": "updated_user",
    "email": "updated@example.com",
    "password": "newpassword123",
    "score": 200,
    "wallet_address": "0xabcdef1234567890",
    "wallet_type": "ETH"
  }'
```

### 5. Delete User

```bash
curl -X DELETE https://YOUR_API_ID.execute-api.ap-southeast-2.amazonaws.com/prod/users/1
```

## Monitoring and Logs

### View Lambda Logs

```bash
aws logs describe-log-groups --log-group-name-prefix /aws/lambda/user-crud-lambda
```

### Stream Logs

```bash
aws logs tail /aws/lambda/user-crud-lambda --follow
```

### CloudWatch Metrics

Monitor the following metrics:
- **Invocations**: Number of function invocations
- **Duration**: Function execution time
- **Errors**: Number of errors
- **Throttles**: Number of throttled requests

## Security Considerations

1. **Database Password**: Store securely in AWS Systems Manager Parameter Store or AWS Secrets Manager
2. **VPC Configuration**: Configure Lambda to run in VPC for RDS access
3. **IAM Roles**: Use least privilege principle for Lambda execution role
4. **API Gateway**: Consider adding authentication/authorization
5. **Input Validation**: All inputs are validated before processing

## Troubleshooting

### Common Issues

1. **Database Connection Failed**
   - Check RDS security group allows Lambda access
   - Verify database credentials
   - Ensure Lambda is in correct VPC

2. **Timeout Errors**
   - Increase Lambda timeout (max 15 minutes)
   - Check database performance
   - Optimize queries

3. **Memory Issues**
   - Increase Lambda memory allocation
   - Check for memory leaks in code

### Debug Mode

Enable debug logging by setting the log level:

```bash
aws lambda update-function-configuration \
  --function-name user-crud-lambda \
  --environment Variables='{LOG_LEVEL=DEBUG}'
```

## Project Structure

```
/
├── pom.xml                          # Maven configuration
├── template.yaml                    # SAM template
├── README.md                        # This file
├── sql/
│   └── users.sql                   # Database schema
└── src/main/java/com/example/lambda/
    ├── UserHandler.java            # Main Lambda handler
    ├── dao/
    │   └── UserDao.java           # Data access object
    ├── model/
    │   └── User.java              # User entity
    └── util/
        └── Db.java                # Database utility
```

## License

This project is licensed under the MIT License.
