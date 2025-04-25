#!/bin/bash
set -e

# This script deploys the QuizMaster application to SAP BTP Cloud Foundry environment

echo "========================================================"
echo "Deploying QuizMaster to SAP BTP Cloud Foundry"
echo "========================================================"

# 1. Build the application with the prod profile
echo "Building application JAR with Maven..."
./mvnw clean package -DskipTests

# 2. Log in to Cloud Foundry (this will prompt for credentials if not already logged in)
echo "Logging into Cloud Foundry..."
echo "If you're not logged in, you'll be prompted for your SAP BTP credentials."
cf login

# 3. Check if the PostgreSQL service exists, create it if it doesn't
echo "Checking for database service..."
if ! cf service quizmaster-db > /dev/null 2>&1; then
    echo "Creating PostgreSQL service instance 'quizmaster-db'..."
    cf create-service postgresql-db trial quizmaster-db
    
    echo "Waiting for service to be created..."
    while cf service quizmaster-db | grep -q "create in progress"; do
        sleep 5
    done
    
    echo "Database service created."
else
    echo "Database service 'quizmaster-db' already exists."
fi

# Optional: Create XSUAA service if needed
# Uncomment the following lines if you want to use XSUAA authentication
#echo "Checking for XSUAA service..."
#if ! cf service quizmaster-xsuaa > /dev/null 2>&1; then
#    echo "Creating XSUAA service instance 'quizmaster-xsuaa'..."
#    cf create-service xsuaa application quizmaster-xsuaa -c xs-security.json
#    
#    echo "Waiting for XSUAA service to be created..."
#    while cf service quizmaster-xsuaa | grep -q "create in progress"; do
#        sleep 5
#    done
#    
#    echo "XSUAA service created."
#else
#    echo "XSUAA service 'quizmaster-xsuaa' already exists."
#fi

# 4. Initialize questions Excel file and include it in the deployment
echo "Preparing the external Excel file..."
cp src/main/resources/questions.xlsx ./target/

# 5. Deploy the application using the manifest
echo "Deploying application to Cloud Foundry..."
cf push

# 6. Show the application status
echo "Checking application status..."
cf app quizmaster

# 7. Display the application URL
echo "========================================================"
echo "Deployment completed!"
app_url=$(cf app quizmaster | grep routes | awk '{print $2}')
echo "QuizMaster is now available at: https://$app_url/quizmaster"
echo "========================================================"