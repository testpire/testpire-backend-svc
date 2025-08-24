#!/bin/bash

CLUSTER_NAME="testpire-cluster"
SERVICE_NAME="testpire-service"
TASK_FAMILY="testpire-task"
CONTAINER_NAME="testpire-container"
ECR_IMAGE="340234701501.dkr.ecr.ap-south-1.amazonaws.com/testpire:latest"  # Replace with your ECR image URI
REGION="ap-south-1"  # Replace with your AWS region
SECURITY_GROUP_NAME="testpire-sg"
TASK_DEF_FILE="task-definition.json"

echo "Starting deployment of $SERVICE_NAME on ECS Fargate..."

# Get default VPC ID
VPC_ID=$(aws ec2 describe-vpcs --filters "Name=isDefault,Values=true" --query "Vpcs[0].VpcId" --output text --region $REGION)
if [ -z "$VPC_ID" ]; then
  echo "Error: Could not find default VPC in region $REGION"
  exit 1
fi
echo "Default VPC: $VPC_ID"

# Get at least two default subnets for ALB and ECS tasks
SUBNETS_ARR=($(aws ec2 describe-subnets --filters "Name=vpc-id,Values=$VPC_ID" "Name=default-for-az,Values=true" --query "Subnets[*].SubnetId" --output text --region $REGION))
if [ ${#SUBNETS_ARR[@]} -lt 2 ]; then
  echo "Warning: Less than 2 default subnets found. Found: ${#SUBNETS_ARR[@]}"
fi

SUBNETS="${SUBNETS_ARR[0]},${SUBNETS_ARR[1]}"
echo "Using subnets: $SUBNETS"

# Create ECS Cluster
echo "Creating ECS Cluster: $CLUSTER_NAME"
aws ecs create-cluster --cluster-name $CLUSTER_NAME --region $REGION

# Create Security Group
echo "Creating Security Group: $SECURITY_GROUP_NAME"
SG_ID=$(aws ec2 create-security-group --group-name $SECURITY_GROUP_NAME --description "Allow HTTP inbound for $SERVICE_NAME" --vpc-id $VPC_ID --query 'GroupId' --output text --region $REGION)
echo "Created Security Group ID: $SG_ID"

# Authorize inbound HTTP port 80
aws ec2 authorize-security-group-ingress --group-id $SG_ID --protocol tcp --port 80 --cidr 0.0.0.0/0 --region $REGION
echo "Inbound HTTP port 80 open to 0.0.0.0/0"

# Create Application Load Balancer
echo "Creating Application Load Balancer"
ALB_ARN=$(aws elbv2 create-load-balancer --name testpire-alb --subnets $SUBNETS --security-groups $SG_ID --scheme internet-facing --type application --query "LoadBalancers[0].LoadBalancerArn" --output text --region $REGION)
ALB_DNS=$(aws elbv2 describe-load-balancers --names testpire-alb --query "LoadBalancers[0].DNSName" --output text --region $REGION)
echo "ALB ARN: $ALB_ARN"
echo "ALB DNS Name: $ALB_DNS"

# Create Target Group
echo "Creating Target Group"
TARGET_GROUP_ARN=$(aws elbv2 create-target-group --name testpire-tg --protocol HTTP --port 80 --vpc-id $VPC_ID --target-type ip --query "TargetGroups[0].TargetGroupArn" --output text --region $REGION)
echo "Target Group ARN: $TARGET_GROUP_ARN"

# Create Listener
echo "Creating ALB Listener on port 80"
aws elbv2 create-listener --load-balancer-arn $ALB_ARN --protocol HTTP --port 80 --default-actions Type=forward,TargetGroupArn=$TARGET_GROUP_ARN --region $REGION

# Create task definition JSON file
echo "Creating task definition JSON file: $TASK_DEF_FILE"
cat > $TASK_DEF_FILE << EOF
{
  "family": "$TASK_FAMILY",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "256",
  "memory": "1024",
  "containerDefinitions": [
    {
      "name": "$CONTAINER_NAME",
      "image": "$ECR_IMAGE",
      "portMappings": [
        {
          "containerPort": 8080,
          "protocol": "tcp"
        }
      ],
      "essential": true
    }
  ]
}
EOF

# Register task definition
echo "Registering Task Definition"
aws ecs register-task-definition --cli-input-json file://$TASK_DEF_FILE --region $REGION

# Create ECS service
echo "Creating ECS service: $SERVICE_NAME"
aws ecs create-service \
  --cluster $CLUSTER_NAME \
  --service-name $SERVICE_NAME \
  --task-definition $TASK_FAMILY \
  --launch-type FARGATE \
  --desired-count 1 \
  --network-configuration "awsvpcConfiguration={subnets=[$SUBNETS],securityGroups=[$SG_ID],assignPublicIp=ENABLED}" \
  --load-balancers "targetGroupArn=$TARGET_GROUP_ARN,containerName=$CONTAINER_NAME,containerPort=8080" \
  --region $REGION

echo "Deployment complete!"
echo "Access your Spring Boot app at: http://$ALB_DNS"

# Instructions to cleanup resources
echo -e "\nTo cleanup resources, run this script with 'destroy' argument:"
echo "  bash $0 destroy"
exit 0

# Destroy block
if [[ "$1" == "destroy" ]]; then
  echo "Starting cleanup of resources..."

  echo "Deleting ECS service..."
  aws ecs delete-service --cluster $CLUSTER_NAME --service $SERVICE_NAME --force --region $REGION

  echo "Deregistering task definition..."
  aws ecs list-task-definitions --family-prefix $TASK_FAMILY --region $REGION --query "taskDefinitionArns[]" --output text | \
  xargs -n 1 -I {} aws ecs deregister-task-definition --task-definition {} --region $REGION

  echo "Deleting ECS cluster..."
  aws ecs delete-cluster --cluster $CLUSTER_NAME --region $REGION

  echo "Deleting ALB Listener and Load Balancer..."
  LISTENER_ARN=$(aws elbv2 describe-listeners --load-balancer-arn $ALB_ARN --region $REGION --query "Listeners[0].ListenerArn" --output text)
  aws elbv2 delete-listener --listener-arn $LISTENER_ARN --region $REGION
  aws elbv2 delete-load-balancer --load-balancer-arn $ALB_ARN --region $REGION

  echo "Deleting Target Group..."
  aws elbv2 delete-target-group --target-group-arn $TARGET_GROUP_ARN --region $REGION

  echo "Deleting Security Group..."
  aws ec2 delete-security-group --group-id $SG_ID --region $REGION

  echo "Cleanup complete."
  exit 0
fi
