#!/bin/bash

# Build the Spring Boot application
echo "Building Spring Boot application for production..."
mvn clean package -DskipTests

# Check if build was successful
if [ $? -ne 0 ]; then
  echo "Maven build failed! Aborting deployment."
  exit 1
fi

echo "Spring Boot application built successfully."
echo "JAR file created at: target/quizmaster.jar"

# Create data directory if it doesn't exist
mkdir -p ./data

# Run the application with production profile
echo "Starting Spring Boot application in PRODUCTION mode on port 5000..."
java -jar target/quizmaster.jar --spring.profiles.active=prod