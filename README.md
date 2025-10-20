# Distributed Chat Application

A production-ready, highly scalable Spring Boot microservices chat application supporting one-to-one messaging with real-time communication.

## System Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Client App    │────│   API Gateway   │────│  Auth Service   │
│   (Web/Mobile)  │    │   (Port 8080)   │    │  (Port 8081)    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                              │
                              ├─────────────────┐
                              │                 │
                    ┌─────────────────┐ ┌─────────────────┐
                    │  User Service   │ │Presence Service │
                    │  (Port 8082)    │ │  (Port 8083)    │
                    └─────────────────┘ └─────────────────┘
                              │                 │
                    ┌─────────────────┐ ┌─────────────────┐
                    │  Chat Service   │ │WebSocket Service│
                    │  (Port 8084)    │ │  (Port 8085)    │
                    └─────────────────┘ └─────────────────┘
                              │                 │
                    ┌─────────────────┐ ┌─────────────────┐
                    │ Media Service   │ │Notification Svc │
                    │  (Port 8086)    │ │  (Port 8087)    │
                    └─────────────────┘ └─────────────────┘
                              │
                    ┌─────────────────┐
                    │   Kafka Broker  │
                    │   (Port 9092)   │
                    └─────────────────┘
                              │
        ┌─────────────────────┼─────────────────────┐
        │                     │                     │
┌───────────────┐    ┌───────────────┐    ┌───────────────┐
│ PostgreSQL    │    │    Redis      │    │  Cassandra    │
│ (Port 5432)   │    │ (Port 6379)   │    │ (Port 9042)   │
└───────────────┘    └───────────────┘    └───────────────┘
```

## Core Services

| Service              | Port | Technology             | Database    | Purpose                                        |
| -------------------- | ---- | ---------------------- | ----------- | ---------------------------------------------- |
| API Gateway          | 8080 | Spring Cloud Gateway   | -           | Request routing, JWT validation, rate limiting |
| Auth Service         | 8081 | Spring Boot + Security | PostgreSQL  | User authentication, JWT tokens                |
| User Service         | 8082 | Spring Boot + JPA      | PostgreSQL  | User profiles, contacts, privacy settings      |
| Presence Service     | 8083 | Spring Boot            | Redis       | Online status, typing indicators               |
| Chat Service         | 8084 | Spring Boot            | Cassandra   | Message handling, chat history                 |
| WebSocket Service    | 8085 | Spring WebSocket       | Redis       | Real-time communication                        |
| Media Service        | 8086 | Spring Boot            | File System | File upload, thumbnails                        |
| Notification Service | 8087 | Spring Boot            | -           | Push notifications, email                      |

## Prerequisites

- Java 17+
- Docker & Docker Compose
- Maven 3.8+
- 8GB+ RAM recommended

## Quick Start

### 1. Clone and Build

```bash
git clone <repository-url>
cd distributed-chat-app
mvn clean install
```

### 2. Start Infrastructure

```bash
docker-compose up -d postgres-auth postgres-users cassandra redis zookeeper kafka
```

### 3. Start Application

```bash
./start-services.sh
```

### 4. Access Services

- **API Gateway**: http://localhost:8080
- **Grafana Dashboard**: http://localhost:3000 (admin/admin)
- **Prometheus**: http://localhost:9090

## API Usage

### Authentication

```bash
# Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"phoneNumber":"+1234567890","password":"password123","displayName":"John Doe"}'

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"phoneNumber":"+1234567890","password":"password123"}'
```

### Send Message

```bash
curl -X POST http://localhost:8080/api/chat/messages \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"senderId":"user1","recipientId":"user2","content":"Hello!","messageType":"TEXT"}'
```

### WebSocket Connection

```javascript
const socket = new SockJS("http://localhost:8080/ws");
const stompClient = Stomp.over(socket);

stompClient.connect({}, function (frame) {
  console.log("Connected: " + frame);

  // Subscribe to messages
  stompClient.subscribe("/user/queue/messages", function (message) {
    console.log("Received: " + message.body);
  });

  // Send message
  stompClient.send(
    "/app/chat.sendMessage",
    {},
    JSON.stringify({
      senderId: "user1",
      recipientId: "user2",
      content: "Hello via WebSocket!",
      messageType: "TEXT",
    })
  );
});
```

## Data Models

### User

```json
{
  "userId": "uuid",
  "phoneNumber": "+1234567890",
  "displayName": "John Doe",
  "avatarUrl": "https://example.com/avatar.jpg",
  "lastSeen": "2023-12-01T10:00:00Z",
  "privacySettings": {
    "showLastSeen": true,
    "showProfilePhoto": true,
    "lastSeenPolicy": "EVERYONE"
  }
}
```

### Message

```json
{
  "messageId": "uuid",
  "senderId": "user-uuid",
  "recipientId": "recipient-uuid",
  "content": "Hello, how are you?",
  "messageType": "TEXT",
  "timestamp": "2023-12-01T10:00:00Z",
  "status": "SENT"
}
```

## Event-Driven Architecture

The system uses Apache Kafka for inter-service communication:

- **message-events**: Message lifecycle (sent, delivered, seen)
- **presence-events**: User presence changes (online, offline)
- **typing-events**: Typing indicators

## Scaling & Performance

### Horizontal Scaling

- Stateless service design
- Load balancer distribution
- Database sharding strategies
- Message broker partitioning

### Expected Performance

- **Throughput**: 10,000+ messages/second
- **Concurrent Users**: 100,000+ active users
- **Latency**: <100ms for message delivery

## Security Features

- JWT-based authentication
- Password encryption (BCrypt)
- Rate limiting (100 req/min default)
- Input validation and sanitization
- CORS configuration
- HTTPS enforcement

## Monitoring

### Metrics

- Prometheus for metrics collection
- Grafana for visualization
- Custom business metrics
- Health check endpoints

### Logging

- Structured JSON logging
- Centralized log collection
- Error tracking and alerting

## Development

### Running Individual Services

```bash
cd auth-service && mvn spring-boot:run
cd user-service && mvn spring-boot:run
cd chat-service && mvn spring-boot:run
```

### Testing

```bash
mvn test                    # Unit tests
mvn verify -P integration   # Integration tests
```

### Docker Commands

```bash
docker-compose build        # Build all images
docker-compose up -d        # Start all services
docker-compose logs -f      # View logs
docker-compose down         # Stop all services
```

## Production Deployment

### Kubernetes

```bash
kubectl apply -f k8s/       # Deploy to Kubernetes
kubectl get pods -n chatapp # Check status
```

### Environment Variables

```bash
export SPRING_PROFILES_ACTIVE=prod
export SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/chatapp
export SPRING_REDIS_HOST=redis
export SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092
```

## Troubleshooting

### Common Issues

```bash
# Check service health
curl http://localhost:8080/actuator/health

# View service logs
docker-compose logs -f api-gateway

# Check database connections
docker-compose exec postgres-auth psql -U chatapp -d chatapp_auth
```

### Performance Issues

- Monitor JVM memory usage
- Check database query performance
- Verify Kafka consumer lag
- Review Redis memory usage

## License

MIT License
