FROM openjdk:17-jdk-slim

WORKDIR /app

COPY api-gateway/target/api-gateway-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
