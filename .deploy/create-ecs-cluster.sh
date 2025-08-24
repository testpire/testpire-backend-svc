#!/bin/bash

# Configuration
CLUSTER_NAME="testpire-fargate-cluster"
SERVICE_NAME="testpire-service"
TASK_DEFINITION_NAME="testpire-task"
CONTAINER_NAME="testpire"
CONTAINER_PORT=8081  # Changed to match your security group rule
CPU="256"
MEMORY="512"
REGION=$(aws configure get region)
ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)

# Replace with your actual Docker image
DOCKER_IMAGE="340234701501.dkr.ecr.ap-south-1.amazonaws.com/testpire:latest"

# Create ECS cluster
echo "Creating ECS cluster..."
aws ecs create-cluster --cluster-name $CLUSTER_NAME

# Check if execution role already exists, if not create it
echo "Checking/Creating execution role..."
EXECUTION_ROLE_ARN=$(aws iam get-role --role-name "ecsTaskExecutionRole" --query 'Role.Arn' --output text 2>/dev/null || echo "NOT_EXISTS")

if [ "$EXECUTION_ROLE_ARN" = "NOT_EXISTS" ]; then
    echo "Creating execution role..."
    cat > trust-policy.json << EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "Service": "ecs-tasks.amazonaws.com"
      },
      "Action": "sts:AssumeRole"
    }
  ]
}
EOF

    EXECUTION_ROLE_ARN=$(aws iam create-role \
        --role-name "ecsTaskExecutionRole" \
        --assume-role-policy-document file://trust-policy.json \
        --query 'Role.Arn' \
        --output text)

    # Attach policy to execution role
    aws iam attach-role-policy \
        --role-name "ecsTaskExecutionRole" \
        --policy-arn "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
else
    echo "Execution role already exists: $EXECUTION_ROLE_ARN"
fi

# Create CloudWatch log group for ECS
echo "Creating CloudWatch log group..."
aws logs create-log-group --log-group-name "/ecs/$TASK_DEFINITION_NAME" 2>/dev/null || echo "Log group may already exist"

# Create task definition JSON
cat > task-definition.json << EOF
{
    "family": "$TASK_DEFINITION_NAME",
    "networkMode": "awsvpc",
    "requiresCompatibilities": ["FARGATE"],
    "cpu": "$CPU",
    "memory": "$MEMORY",
    "executionRoleArn": "$EXECUTION_ROLE_ARN",
    "taskRoleArn": "$EXECUTION_ROLE_ARN",
    "containerDefinitions": [
        {
            "name": "$CONTAINER_NAME",
            "image": "$DOCKER_IMAGE",
            "portMappings": [
                {
                    "containerPort": $CONTAINER_PORT,
                    "protocol": "tcp"
                }
            ],
            "essential": true,
            "logConfiguration": {
                "logDriver": "awslogs",
                "options": {
                    "awslogs-group": "/ecs/$TASK_DEFINITION_NAME",
                    "awslogs-region": "$REGION",
                    "awslogs-stream-prefix": "ecs"
                }
            }
        }
    ]
}
EOF

# Register task definition
echo "Registering task definition..."
TASK_DEF_ARN=$(aws ecs register-task-definition --cli-input-json file://task-definition.json --query 'taskDefinition.taskDefinitionArn' --output text)
echo "Task definition registered: $TASK_DEF_ARN"

# Create VPC and networking (with multiple subnets for ALB)
echo "Creating VPC and networking resources..."
VPC_ID=$(aws ec2 create-vpc --cidr-block 10.0.0.0/16 --query 'Vpc.VpcId' --output text)
aws ec2 modify-vpc-attribute --vpc-id $VPC_ID --enable-dns-support "{\"Value\":true}"
aws ec2 modify-vpc-attribute --vpc-id $VPC_ID --enable-dns-hostnames "{\"Value\":true}"

# Create two subnets in different availability zones
echo "Creating subnets..."
SUBNET_ID_1=$(aws ec2 create-subnet --vpc-id $VPC_ID --cidr-block 10.0.1.0/24 --availability-zone "${REGION}a" --query 'Subnet.SubnetId' --output text)
SUBNET_ID_2=$(aws ec2 create-subnet --vpc-id $VPC_ID --cidr-block 10.0.2.0/24 --availability-zone "${REGION}b" --query 'Subnet.SubnetId' --output text)

# Create internet gateway
IGW_ID=$(aws ec2 create-internet-gateway --query 'InternetGateway.InternetGatewayId' --output text)
aws ec2 attach-internet-gateway --vpc-id $VPC_ID --internet-gateway-id $IGW_ID

# Create route table
ROUTE_TABLE_ID=$(aws ec2 create-route-table --vpc-id $VPC_ID --query 'RouteTable.RouteTableId' --output text)
aws ec2 create-route --route-table-id $ROUTE_TABLE_ID --destination-cidr-block 0.0.0.0/0 --gateway-id $IGW_ID

# Associate route table with both subnets
aws ec2 associate-route-table --route-table-id $ROUTE_TABLE_ID --subnet-id $SUBNET_ID_1
aws ec2 associate-route-table --route-table-id $ROUTE_TABLE_ID --subnet-id $SUBNET_ID_2

# Create security group
SG_ID=$(aws ec2 create-security-group --group-name "ecs-security-group" --description "ECS Security Group" --vpc-id $VPC_ID --query 'GroupId' --output text)
aws ec2 authorize-security-group-ingress --group-id $SG_ID --protocol tcp --port 80 --cidr 0.0.0.0/0
aws ec2 authorize-security-group-ingress --group-id $SG_ID --protocol tcp --port 443 --cidr 0.0.0.0/0
aws ec2 authorize-security-group-ingress --group-id $SG_ID --protocol tcp --port $CONTAINER_PORT --cidr 0.0.0.0/0

# Create load balancer with two subnets
echo "Creating Application Load Balancer..."
ALB_ARN=$(aws elbv2 create-load-balancer \
    --name "ecs-alb" \
    --subnets $SUBNET_ID_1 $SUBNET_ID_2 \
    --security-groups $SG_ID \
    --query 'LoadBalancers[0].LoadBalancerArn' \
    --output text)

# Check if target group exists, delete if it does
EXISTING_TG=$(aws elbv2 describe-target-groups --names "ecs-target-group" --query 'TargetGroups[0].TargetGroupArn' --output text 2>/dev/null || echo "NOT_EXISTS")
if [ "$EXISTING_TG" != "NOT_EXISTS" ]; then
    echo "Deleting existing target group..."
    aws elbv2 delete-target-group --target-group-arn $EXISTING_TG
    sleep 5
fi

# Create target group
TARGET_GROUP_ARN=$(aws elbv2 create-target-group \
    --name "ecs-target-group" \
    --protocol HTTP \
    --port 80 \
    --vpc-id $VPC_ID \
    --target-type ip \
    --health-check-path "/actuator/health" \
    --health-check-interval-seconds 30 \
    --health-check-timeout-seconds 5 \
    --healthy-threshold-count 2 \
    --unhealthy-threshold-count 2 \
    --query 'TargetGroups[0].TargetGroupArn' \
    --output text)

# Create listener
LISTENER_ARN=$(aws elbv2 create-listener \
    --load-balancer-arn $ALB_ARN \
    --protocol HTTP \
    --port 80 \
    --default-actions Type=forward,TargetGroupArn=$TARGET_GROUP_ARN \
    --query 'Listeners[0].ListenerArn' \
    --output text)

# Wait for resources to be ready
echo "Waiting for resources to be ready..."
sleep 30

# Create service
echo "Creating ECS service..."
aws ecs create-service \
    --cluster $CLUSTER_NAME \
    --service-name $SERVICE_NAME \
    --task-definition $TASK_DEF_ARN \
    --desired-count 1 \
    --launch-type FARGATE \
    --network-configuration "awsvpcConfiguration={subnets=[$SUBNET_ID_1,$SUBNET_ID_2],securityGroups=[$SG_ID],assignPublicIp=ENABLED}" \
    --load-balancers targetGroupArn=$TARGET_GROUP_ARN,containerName=$CONTAINER_NAME,containerPort=$CONTAINER_PORT

# Get ALB DNS name
ALB_DNS=$(aws elbv2 describe-load-balancers --load-balancer-arns $ALB_ARN --query 'LoadBalancers[0].DNSName' --output text)

echo "=============================================="
echo "Setup complete! Your Spring Boot application is being deployed."
echo "Load Balancer URL: http://$ALB_DNS"
echo "Health check URL: http://$ALB_DNS/actuator/health"
echo "=============================================="
echo "Note: It may take 2-5 minutes for the application to be fully deployed and healthy."