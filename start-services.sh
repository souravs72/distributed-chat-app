#!/bin/bash

echo "Starting Distributed Chat Application..."

# Build all services
echo "Building all services..."
mvn clean install -DskipTests

# Start infrastructure services
echo "Starting infrastructure services..."
docker-compose up -d postgres-auth postgres-users cassandra redis zookeeper kafka

# Wait for infrastructure to be ready
echo "Waiting for infrastructure services to be ready..."
sleep 30

# Start application services
echo "Starting application services..."
docker-compose up -d auth-service user-service presence-service chat-service websocket-service api-gateway

# Start monitoring services
echo "Starting monitoring services..."
docker-compose up -d prometheus grafana

echo "All services started successfully!"
echo ""
echo "Service URLs:"
echo "API Gateway: http://localhost:8080"
echo "Auth Service: http://localhost:8081"
echo "User Service: http://localhost:8082"
echo "Presence Service: http://localhost:8083"
echo "Chat Service: http://localhost:8084"
echo "WebSocket Service: ws://localhost:8085/ws"
echo "Grafana Dashboard: http://localhost:3000 (admin/admin)"
echo "Prometheus: http://localhost:9090"
echo ""
echo "To view logs: docker-compose logs -f [service-name]"
echo "To stop all services: docker-compose down"
