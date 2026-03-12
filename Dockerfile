# Build stage
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app

# Copy the entire project structure
# This includes app, backend, gradle, gradlew, settings.gradle.kts, etc.
COPY . .

# Grant execution permissions
RUN chmod +x gradlew

# Build only the backend module
# Using --no-daemon is important for CI/Docker environments
RUN ./gradlew :backend:build -x test --no-daemon

# Run stage
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copy the built jar from the backend build directory
# It should be in backend/build/libs/
COPY --from=build /app/backend/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
