#!/bin/bash

# Configuration (must match create script)
CLUSTER_NAME="testpire-fargate-cluster"
SERVICE_NAME="testpire-service"
TASK_DEFINITION_NAME="testpire-task"
ALB_NAME="ecs-alb"
TARGET_GROUP_NAME="ecs-target-group"
SG_NAME="ecs-security-group"
ROLE_NAME="ecsTaskExecutionRole"
LOG_GROUP_NAME="/ecs/$TASK_DEFINITION_NAME"

echo "Starting cleanup of ECS resources..."

# Get region
REGION=$(aws configure get region)

# Scale service to 0 tasks and delete service
echo "Stopping and deleting ECS service..."
SERVICE_EXISTS=$(aws ecs describe-services --cluster $CLUSTER_NAME --services $SERVICE_NAME --query 'services[0].status' --output text 2>/dev/null || echo "MISSING")

if [ "$SERVICE_EXISTS" != "MISSING" ] && [ "$SERVICE_EXISTS" != "INACTIVE" ]; then
    echo "Scaling service to 0 tasks..."
    aws ecs update-service \
        --cluster $CLUSTER_NAME \
        --service $SERVICE_NAME \
        --desired-count 0

    echo "Waiting for tasks to stop..."
    sleep 60

    echo "Deleting service..."
    aws ecs delete-service \
        --cluster $CLUSTER_NAME \
        --service $SERVICE_NAME \
        --force
else
    echo "Service already deleted or doesn't exist."
fi

# Deregister all task definitions
echo "Deregistering task definitions..."
TASK_DEFINITION_ARNS=$(aws ecs list-task-definitions --family-prefix $TASK_DEFINITION_NAME --query 'taskDefinitionArns' --output text 2>/dev/null || echo "NONE")

if [ "$TASK_DEFINITION_ARNS" != "NONE" ]; then
    for TASK_DEFINITION_ARN in $TASK_DEFINITION_ARNS; do
        echo "Deregistering: $TASK_DEFINITION_ARN"
        aws ecs deregister-task-definition --task-definition $TASK_DEFINITION_ARN
    done
else
    echo "No task definitions found to deregister."
fi

# Delete load balancer resources
echo "Deleting load balancer resources..."
ALB_ARN=$(aws elbv2 describe-load-balancers --names $ALB_NAME --query 'LoadBalancers[0].LoadBalancerArn' --output text 2>/dev/null || echo "NONE")

if [ "$ALB_ARN" != "NONE" ]; then
    # Delete listeners first
    echo "Deleting listeners..."
    LISTENER_ARNS=$(aws elbv2 describe-listeners --load-balancer-arn $ALB_ARN --query 'Listeners[].ListenerArn' --output text 2>/dev/null || echo "NONE")
    if [ "$LISTENER_ARNS" != "NONE" ]; then
        for LISTENER_ARN in $LISTENER_ARNS; do
            echo "Deleting listener: $LISTENER_ARN"
            aws elbv2 delete-listener --listener-arn $LISTENER_ARN
        done
    fi

    # Delete load balancer
    echo "Deleting load balancer: $ALB_ARN"
    aws elbv2 delete-load-balancer --load-balancer-arn $ALB_ARN
else
    echo "Load balancer not found."
fi

# Delete target group
echo "Deleting target group..."
TARGET_GROUP_ARN=$(aws elbv2 describe-target-groups --names $TARGET_GROUP_NAME --query 'TargetGroups[0].TargetGroupArn' --output text 2>/dev/null || echo "NONE")
if [ "$TARGET_GROUP_ARN" != "NONE" ]; then
    echo "Deleting target group: $TARGET_GROUP_ARN"
    aws elbv2 delete-target-group --target-group-arn $TARGET_GROUP_ARN
else
    echo "Target group not found."
fi

# Delete security group
echo "Deleting security group..."
SG_ID=$(aws ec2 describe-security-groups --group-names $SG_NAME --query 'SecurityGroups[0].GroupId' --output text 2>/dev/null || echo "NONE")
if [ "$SG_ID" != "NONE" ]; then
    echo "Deleting security group: $SG_ID"
    aws ec2 delete-security-group --group-id $SG_ID
else
    echo "Security group not found."
fi

# Delete VPC resources (subnets, IGW, route tables, VPC)
echo "Cleaning up VPC resources..."

# Get VPC ID by looking for VPCs with 10.0.0.0/16 CIDR (from our create script)
VPC_ID=$(aws ec2 describe-vpcs --filters Name=cidr,Values=10.0.0.0/16 --query 'Vpcs[0].VpcId' --output text 2>/dev/null || echo "NONE")

if [ "$VPC_ID" != "NONE" ]; then
    echo "Found VPC: $VPC_ID"

    # Delete subnets
    echo "Deleting subnets..."
    SUBNET_IDS=$(aws ec2 describe-subnets --filters Name=vpc-id,Values=$VPC_ID --query 'Subnets[].SubnetId' --output text 2>/dev/null || echo "NONE")
    if [ "$SUBNET_IDS" != "NONE" ]; then
        for SUBNET_ID in $SUBNET_IDS; do
            echo "Deleting subnet: $SUBNET_ID"
            aws ec2 delete-subnet --subnet-id $SUBNET_ID
        done
    fi

    # Delete internet gateway
    echo "Deleting internet gateway..."
    IGW_ID=$(aws ec2 describe-internet-gateways --filters Name=attachment.vpc-id,Values=$VPC_ID --query 'InternetGateways[0].InternetGatewayId' --output text 2>/dev/null || echo "NONE")
    if [ "$IGW_ID" != "NONE" ]; then
        aws ec2 detach-internet-gateway --internet-gateway-id $IGW_ID --vpc-id $VPC_ID
        aws ec2 delete-internet-gateway --internet-gateway-id $IGW_ID
    fi

    # Delete route tables (except main)
    echo "Deleting route tables..."
    ROUTE_TABLE_IDS=$(aws ec2 describe-route-tables --filters Name=vpc-id,Values=$VPC_ID --query 'RouteTables[?Associations[?Main!=`true`]].RouteTableId' --output text 2>/dev/null || echo "NONE")
    if [ "$ROUTE_TABLE_IDS" != "NONE" ]; then
        for ROUTE_TABLE_ID in $ROUTE_TABLE_IDS; do
            echo "Deleting route table: $ROUTE_TABLE_ID"
            aws ec2 delete-route-table --route-table-id $ROUTE_TABLE_ID
        done
    fi

    # Delete VPC
    echo "Deleting VPC: $VPC_ID"
    aws ec2 delete-vpc --vpc-id $VPC_ID
else
    echo "VPC not found."
fi

# Delete IAM role
echo "Cleaning up IAM role..."
ROLE_EXISTS=$(aws iam get-role --role-name $ROLE_NAME --query 'Role.RoleName' --output text 2>/dev/null || echo "NONE")
if [ "$ROLE_EXISTS" != "NONE" ]; then
    echo "Detaching policies from IAM role..."
    aws iam detach-role-policy --role-name $ROLE_NAME --policy-arn "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy" 2>/dev/null || true

    echo "Deleting IAM role: $ROLE_NAME"
    aws iam delete-role --role-name $ROLE_NAME
else
    echo "IAM role not found."
fi

# Delete CloudWatch log group
echo "Deleting CloudWatch log group..."
aws logs delete-log-group --log-group-name $LOG_GROUP_NAME 2>/dev/null || echo "Log group not found or already deleted."

# Delete ECS cluster
echo "Deleting ECS cluster..."
CLUSTER_EXISTS=$(aws ecs describe-clusters --clusters $CLUSTER_NAME --query 'clusters[0].status' --output text 2>/dev/null || echo "MISSING")
if [ "$CLUSTER_EXISTS" != "MISSING" ]; then
    aws ecs delete-cluster --cluster $CLUSTER_NAME
    echo "Cluster deleted."
else
    echo "Cluster not found."
fi

echo "=============================================="
echo "Cleanup complete! All resources have been deleted."
echo "You can now run ./create-ecs-cluster.sh to create a fresh setup."
echo "=============================================="