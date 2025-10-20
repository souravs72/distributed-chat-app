FROM openjdk:17-jdk-slim

WORKDIR /app

COPY websocket-service/target/websocket-service-1.0.0.jar app.jar

EXPOSE 8085

ENTRYPOINT ["java", "-jar", "app.jar"]
