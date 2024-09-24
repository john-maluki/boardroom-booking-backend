# Use an official Gradle runtime as a parent image
FROM gradle:7.6-jdk17 AS build

# Set the working directory
WORKDIR /app

# Copy the Gradle wrapper and build files
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

# Download the dependencies
RUN ./gradlew dependencies

## Copy the rest of the application code
#COPY src src
# Copy the rest of the project files
COPY . /app/

# Copy the .env file to the /app directory in the container

# Build the application
RUN ./gradlew build -x test

# Use an official OpenJDK runtime as a parent image
FROM openjdk:17-jdk-alpine

# Set the working directory
WORKDIR /app

# Copy the jar file from the build stage
COPY --from=build /app/build/libs/boardroom-booking-backend-0.0.1-SNAPSHOT.jar app.jar
COPY .env /app/
COPY uploads /app/
# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]




