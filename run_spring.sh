#!/bin/bash

# Cleanup any existing processes
echo "Cleaning up any existing processes..."
pkill -f java || echo "No Java processes to clean up"
pkill -f mvn || echo "No Maven processes to clean up"
sleep 2

echo "Starting Spring Boot application with optimization..."
# Set strict memory limits
export MAVEN_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=100 -XX:+UseStringDeduplication"
export _JAVA_OPTIONS="-Xms256m -Xmx512m -XX:+UseG1GC -Dspring.config.location=classpath:/application.properties"

# Use production mode (disable devtools)
export SPRING_PROFILES_ACTIVE=production

# Run with JVM optimizations for faster startup
echo "Running with optimized JVM settings..."
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-XX:TieredStopAtLevel=1 -Djava.security.egd=file:/dev/./urandom" &

# Store the PID
SPRING_PID=$!

# Start a background process to indicate that the app is starting
(
  echo "Starting Spring Boot (this may take 20-30 seconds)..."
  for i in {1..6}; do
    echo -n "."
    sleep 5
  done
) &
LOADING_PID=$!

# Wait for the application to start - increased timeout for first start
echo "Waiting for Spring Boot to start (max 120 seconds)..."
MAX_WAIT=120
COUNTER=0
while [ $COUNTER -lt $MAX_WAIT ]; do
  # Check if the port is open OR if the app has started by checking the logs
  if curl -s http://localhost:5000/quizmaster/api/quiz/health > /dev/null 2>&1 || 
     grep -q "Started QuizMasterApplication" quizmaster.log 2>/dev/null; then
    
    # Kill the loading indicator
    kill $LOADING_PID 2>/dev/null
    
    echo "✅ Spring Boot started successfully on port 5000!"
    
    # The app is running, wait to make sure port 5000 is actually open
    sleep 5
    
    # Exit successfully - the actual server is already running on port 5000
    # The workflow system should detect it automatically
    echo "Application is running and serving port 5000"
    
    # Stay alive to keep the process running
    while true; do
      sleep 10
      if ! ps -p $SPRING_PID > /dev/null; then
        echo "❌ Spring Boot process has died, exiting"
        exit 1
      fi
      echo "🟢 Spring Boot is running... (health check)"
    done
  fi
  
  # Check if Spring Boot is still running
  if ! ps -p $SPRING_PID > /dev/null; then
    echo "❌ Spring Boot process died during startup"
    cat quizmaster.log | tail -n 20
    exit 1
  fi
  
  echo "Waiting for Spring Boot... ($COUNTER/$MAX_WAIT)"
  sleep 1
  COUNTER=$((COUNTER + 1))
done

# If we get here, startup failed
echo "❌ Spring Boot failed to start within $MAX_WAIT seconds"
echo "Showing last 20 lines of the log:"
cat quizmaster.log | tail -n 20
kill $SPRING_PID 2>/dev/null
exit 1