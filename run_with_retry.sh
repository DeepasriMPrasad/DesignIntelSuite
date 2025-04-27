#!/bin/bash

# Kill any existing processes
ps aux | grep java
pkill -f "java" || echo "No Java processes to kill"
pkill -f "mvn" || echo "No Maven processes to kill"
sleep 2

# Set application properties directly
export SPRING_PROFILES_ACTIVE=dev

# Start the application in background with explicit port binding
echo "Starting Spring Boot application on port 5000..."
SPRING_OPTS="-Dserver.port=5000 -Dserver.address=0.0.0.0 -Dserver.servlet.context-path=/quizmaster"

# Run Maven in the background
nohup mvn spring-boot:run -Dspring-boot.run.jvmArguments="$SPRING_OPTS" > spring-boot.log 2>&1 &
MAVEN_PID=$!

# Check if Maven process is running
if ! ps -p $MAVEN_PID > /dev/null; then
  echo "Failed to start Maven process."
  exit 1
fi

echo "Maven started with PID $MAVEN_PID"

# Wait for server to start - with patience
echo "Waiting for Spring Boot to start... (this might take a minute or two)"
MAX_WAIT=120
COUNTER=0

while [ $COUNTER -lt $MAX_WAIT ]; do
  if curl -s http://localhost:5000/quizmaster/api/quiz/health > /dev/null 2>&1; then
    echo "✅ Spring Boot started successfully on port 5000!"
    echo "Application is running at http://localhost:5000/quizmaster"
    exit 0
  fi
  
  # Check if Maven is still running
  if ! ps -p $MAVEN_PID > /dev/null; then
    echo "❌ Maven process has died. Check spring-boot.log for details:"
    cat spring-boot.log | tail -20
    exit 1
  fi
  
  echo "Waiting for Spring Boot... ($COUNTER/$MAX_WAIT seconds)"
  sleep 2
  COUNTER=$((COUNTER + 2))
done

echo "Could not detect application startup after $MAX_WAIT seconds."
echo "Check spring-boot.log for details or try again."
exit 1