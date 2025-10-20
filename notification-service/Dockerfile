FROM openjdk:17-jdk-slim

WORKDIR /app

COPY notification-service/target/notification-service-1.0.0.jar app.jar

EXPOSE 8087

ENTRYPOINT ["java", "-jar", "app.jar"]
