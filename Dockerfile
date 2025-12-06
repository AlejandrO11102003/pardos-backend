
# Stage 1: Build the application
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy the pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the source code and build the application
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the jar file from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the port (Render sets the PORT environment variable, Spring Boot should pick it up if configured, 
# or we can pass it as a system property)
# Default to 8080
EXPOSE 8080

# Run the application
# We use -Dserver.port=${PORT} so that it listens on the port Render assigns, defaulting to 8080 if not set.
ENTRYPOINT ["java", "-Dserver.port=${PORT:8080}", "-jar", "app.jar"]
