#!/bin/bash

# Configuration (must match create script)
CLUSTER_NAME="testpire-fargate-cluster"
SERVICE_NAME="testpire-service"
TASK_DEFINITION_NAME="testpire-task"

echo "Starting ECS cluster..."

# Check if cluster exists
CLUSTER_STATUS=$(aws ecs describe-clusters --clusters $CLUSTER_NAME --query 'clusters[0].status' --output text 2>/dev/null || echo "MISSING")

if [ "$CLUSTER_STATUS" = "MISSING" ]; then
    echo "Cluster doesn't exist. Please run the create script first."
    exit 1
fi

# Update service to desired count 1
echo "Starting service..."
aws ecs update-service \
    --cluster $CLUSTER_NAME \
    --service $SERVICE_NAME \
    --desired-count 1

echo "Service is starting. It may take a few minutes for the application to be available."