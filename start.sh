#!/bin/bash

# Kill any existing Java processes
pkill -f "java" || echo "No Java processes to kill"
sleep 1

# Set environment variables
export SPRING_PROFILES_ACTIVE=dev

# Start the application using the built JAR
echo "Starting QuizMaster application..."
nohup mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dserver.port=5000" > spring-boot.log 2>&1 &

echo "Application started in background. Check spring-boot.log for details."
echo "The application should be available at http://localhost:5000/quizmaster"