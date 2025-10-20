FROM openjdk:17-jdk-slim

WORKDIR /app

COPY media-service/target/media-service-1.0.0.jar app.jar

EXPOSE 8086

ENTRYPOINT ["java", "-jar", "app.jar"]
