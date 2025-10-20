FROM openjdk:17-jdk-slim

WORKDIR /app

COPY presence-service/target/presence-service-1.0.0.jar app.jar

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "app.jar"]
