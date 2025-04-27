#!/bin/bash

# Kill previous instances
pkill -f "java" || echo "No Java processes to kill"
pkill -f "mvn" || echo "No Maven processes to kill"
sleep 2

# Set environment variables
export SPRING_PROFILES_ACTIVE=dev
export SERVER_PORT=5000
export SERVER_ADDRESS=0.0.0.0
export SERVER_SERVLET_CONTEXT_PATH=/quizmaster

# Enable logging
echo "Starting Spring Boot application on port $SERVER_PORT with context path $SERVER_SERVLET_CONTEXT_PATH"

# Run Maven in foreground mode
exec mvn spring-boot:run \
  -Dspring-boot.run.jvmArguments="-Xms256m -Xmx512m" \
  -Dspring-boot.run.arguments="--server.port=5000 --server.address=0.0.0.0 --server.servlet.context-path=/quizmaster"