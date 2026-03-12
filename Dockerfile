# Build stage
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app

# Copy all source files
# Explicitly copy required parts
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY gradle.properties .
COPY backend backend

# Grant execution permissions
RUN chmod +x gradlew

# Build backend
# We use --no-daemon to avoid issues in containerized environments
RUN ./gradlew :backend:build -x test --no-daemon

# Run stage
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copy the built jar from the build stage
# Find the jar in the build/libs directory of the backend module
# Using a glob to pick the executable jar
COPY --from=build /app/backend/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
