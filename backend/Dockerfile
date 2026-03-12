# Build stage
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app

# Copy gradle wrapper from root to backend folder
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY src src

# Grant execution permissions
RUN chmod +x gradlew

# Build the application
RUN ./gradlew build -x test --no-daemon

# Run stage
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copy the built jar
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
