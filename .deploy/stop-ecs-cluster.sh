#!/bin/bash

# Configuration (must match create script)
CLUSTER_NAME="testpire-fargate-cluster"
SERVICE_NAME="testpire-service"
TASK_DEFINITION_NAME="testpire-task"
ALB_NAME="ecs-alb"
TARGET_GROUP_NAME="ecs-target-group"
SG_NAME="ecs-security-group"
ROLE_NAME="ecsTaskExecutionRole"

# Get region
REGION=$(aws configure get region)

echo "Stopping ECS cluster and cleaning up resources..."

# Scale service to 0 tasks
echo "Scaling service to 0 tasks..."
aws ecs update-service \
    --cluster $CLUSTER_NAME \
    --service $SERVICE_NAME \
    --desired-count 0

# Wait for tasks to stop
echo "Waiting for tasks to stop..."
sleep 60

# Delete service
echo "Deleting service..."
aws ecs delete-service \
    --cluster $CLUSTER_NAME \
    --service $SERVICE_NAME \
    --force

# Delete task definition
echo "Deregistering task definitions..."
TASK_DEFINITION_ARNS=$(aws ecs list-task-definitions --family-prefix $TASK_DEFINITION_NAME --query 'taskDefinitionArns' --output text)
for TASK_DEFINITION_ARN in $TASK_DEFINITION_ARNS; do
    echo "Deregistering: $TASK_DEFINITION_ARN"
    aws ecs deregister-task-definition --task-definition $TASK_DEFINITION_ARN
done

# Delete load balancer resources
echo "Deleting load balancer resources..."
ALB_ARN=$(aws elbv2 describe-load-balancers --names $ALB_NAME --query 'LoadBalancers[0].LoadBalancerArn' --output text 2>/dev/null || echo "None")
if [ "$ALB_ARN" != "None" ]; then
    # Delete listeners first
    LISTENER_ARNS=$(aws elbv2 describe-listeners --load-balancer-arn $ALB_ARN --query 'Listeners[].ListenerArn' --output text)
    for LISTENER_ARN in $LISTENER_ARNS; do
        echo "Deleting listener: $LISTENER_ARN"
        aws elbv2 delete-listener --listener-arn $LISTENER_ARN
    done

    # Delete load balancer
    echo "Deleting load balancer: $ALB_ARN"
    aws elbv2 delete-load-balancer --load-balancer-arn $ALB_ARN
fi

# Delete target group
TARGET_GROUP_ARN=$(aws elbv2 describe-target-groups --names $TARGET_GROUP_NAME --query 'TargetGroups[0].TargetGroupArn' --output text 2>/dev/null || echo "None")
if [ "$TARGET_GROUP_ARN" != "None" ]; then
    echo "Deleting target group: $TARGET_GROUP_ARN"
    aws elbv2 delete-target-group --target-group-arn $TARGET_GROUP_ARN
fi

# Delete security group
SG_ID=$(aws ec2 describe-security-groups --group-names $SG_NAME --query 'SecurityGroups[0].GroupId' --output text 2>/dev/null || echo "None")
if [ "$SG_ID" != "None" ]; then
    echo "Deleting security group: $SG_ID"
    aws ec2 delete-security-group --group-id $SG_ID
fi

# Delete IAM role
echo "Deleting IAM role..."
aws iam detach-role-policy --role-name $ROLE_NAME --policy-arn "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy" 2>/dev/null || true
aws iam delete-role --role-name $ROLE_NAME 2>/dev/null || true

# Delete cluster
echo "Deleting cluster..."
aws ecs delete-cluster --cluster $CLUSTER_NAME

echo "Cleanup complete! All resources have been stopped/deleted."