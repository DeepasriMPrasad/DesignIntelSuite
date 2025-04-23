
#!/bin/bash

# Ensure Java is in PATH
export PATH=$PATH:/nix/var/nix/profiles/default/bin:/nix/store/*/bin

# Build the WAR file
echo "Building the WAR file..."
mvn clean package -DskipTests

# Start the Spring Boot application
echo "Starting Spring Boot application..."
java -jar target/quiz-master.war --server.port=5000 --server.address=0.0.0.0 --server.servlet.context-path=/quizmaster
