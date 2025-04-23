#!/bin/bash

# Stop any running processes
echo "Stopping any running processes..."
pkill -f streamlit || true
pkill -f java || true

# Wait for ports to be released
sleep 2

# Build the WAR file
echo "Building the WAR file..."
mvn clean package

# Start the Spring Boot application on port 5000
echo "Starting Spring Boot application on port 5000..."
java -jar target/quiz-master.war --server.port=5000 --server.address=0.0.0.0 --server.servlet.context-path=/quizmaster