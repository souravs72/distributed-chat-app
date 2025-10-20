FROM openjdk:17-jdk-slim

WORKDIR /app

COPY chat-service/target/chat-service-1.0.0.jar app.jar

EXPOSE 8084

ENTRYPOINT ["java", "-jar", "app.jar"]
