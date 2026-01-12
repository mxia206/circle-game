# Build stage with Maven
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app
COPY . .
RUN mvn clean package

# Runtime stage with only Java
FROM openjdk:17.0.1-jdk-slim
WORKDIR /app
COPY --from=builder /app/target/sahur-1.0-SNAPSHOT.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
