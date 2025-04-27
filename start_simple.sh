#!/bin/bash

# Cleanup any running processes
pkill -f "java -jar" || echo "No Java processes found"
sleep 2

# Set development profile
export SPRING_PROFILES_ACTIVE=dev

# Run with main application class directly
echo "Starting Spring Boot application..."
mvn spring-boot:run -Dspring-boot.run.profiles=dev -Dspring-boot.run.jvmArguments="-Dserver.port=5000"