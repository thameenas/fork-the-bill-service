# Stage 1: Build the application
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Copy gradle wrapper and related files first for caching
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Ensure gradlew has execute permissions
RUN chmod +x ./gradlew

# Copy the source code
COPY src src

# Build the application
# -x test skips the tests during the docker build to speed it up and avoid needing a db.
RUN ./gradlew bootJar -x test

# Stage 2: Create a lightweight runtime image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Run as non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy over the built application from the builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Expose the standard port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
