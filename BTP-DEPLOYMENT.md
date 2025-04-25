# SAP BTP Deployment Guide for QuizMaster

This document explains how to deploy the QuizMaster application to SAP Business Technology Platform (BTP) using Cloud Foundry.

## Prerequisites

Before you begin, make sure you have:

1. An SAP BTP account with Cloud Foundry entitlement
2. The [Cloud Foundry CLI](https://github.com/cloudfoundry/cli) installed
3. Access to a terminal or command prompt
4. Maven installed (or use the included Maven wrapper)
5. Java 17 or later installed

## Deployment Steps

### Option 1: Using the Automated Script

1. Make sure the deployment script is executable:
   ```bash
   chmod +x deploy_to_btp.sh
   ```

2. Run the deployment script:
   ```bash
   ./deploy_to_btp.sh
   ```

3. The script will:
   - Build the application with Maven
   - Log you into Cloud Foundry (if not already logged in)
   - Create a PostgreSQL service instance if it doesn't exist
   - Deploy the application using the manifest.yml file
   - Display the application URL when complete

### Option 2: Manual Deployment

If you prefer to deploy manually, follow these steps:

1. **Build the Application**:
   ```bash
   ./mvnw clean package -DskipTests
   ```

2. **Log in to Cloud Foundry**:
   ```bash
   cf login -a https://api.cf.eu10.hana.ondemand.com
   ```
   Replace the API endpoint with your region's endpoint.

3. **Create a PostgreSQL Service**:
   ```bash
   cf create-service postgresql-db trial quizmaster-db
   ```
   Wait for the service to be created:
   ```bash
   cf service quizmaster-db
   ```

4. **Deploy the Application**:
   ```bash
   cf push
   ```

5. **Verify the Deployment**:
   ```bash
   cf app quizmaster
   ```

## Accessing the Application

After successful deployment, you can access QuizMaster at:
```
https://quizmaster-[random-word].cfapps.eu10.hana.ondemand.com/quizmaster
```

Replace `[random-word]` with the value generated during deployment and `eu10` with your region.

## Configuration Management

### Environment Variables

The QuizMaster application uses the following environment variables in Cloud Foundry:

- `SPRING_PROFILES_ACTIVE`: Set to `prod` to use production configuration
- `QUESTIONS_EXCEL_PATH`: Set to `file:./questions.xlsx` to use the Excel file for questions
- `QUIZMASTER_RESULTS_STORAGE_TYPE`: Set to `database` to store results in database

You can modify these values using the cf CLI:
```bash
cf set-env quizmaster ENV_VAR_NAME ENV_VAR_VALUE
cf restart quizmaster
```

### Database Management

The application uses the PostgreSQL service bound through `quizmaster-db`. To access the database:

1. Create a service key:
   ```bash
   cf create-service-key quizmaster-db quizmaster-db-key
   ```

2. View the credentials:
   ```bash
   cf service-key quizmaster-db quizmaster-db-key
   ```

3. Connect using your preferred PostgreSQL client.

## Monitoring and Logging

### Viewing Application Logs

```bash
cf logs quizmaster --recent
```

For streaming logs:
```bash
cf logs quizmaster
```

### Monitoring Application Health

Check the application health using:
```bash
cf app quizmaster
```

## Troubleshooting

### Common Issues and Resolutions

1. **Application crashes on startup**:
   - Check the logs: `cf logs quizmaster --recent`
   - Verify the service binding: `cf services`

2. **Database connection issues**:
   - Ensure the database service is properly created: `cf service quizmaster-db`
   - Verify the application is bound to the service: `cf env quizmaster`

3. **Excel file not found**:
   - Confirm the file was included in the deployment
   - Check the logs for file path issues

4. **Memory issues**:
   - Increase memory allocation in manifest.yml if necessary
   - Monitor the application's memory usage: `cf app quizmaster`

## Scaling the Application

To scale the application horizontally:
```bash
cf scale quizmaster -i 2
```

To scale vertically (increase memory):
```bash
cf scale quizmaster -m 2G
```

## Updates and Maintenance

To update the application:

1. Make your code changes
2. Build the updated JAR
3. Deploy using `cf push`

For zero-downtime deployment, use the blue-green deployment strategy with:
```bash
cf push quizmaster-new
# Test the new version
cf map-route quizmaster-new cfapps.eu10.hana.ondemand.com --hostname quizmaster-[random-word]
cf unmap-route quizmaster cfapps.eu10.hana.ondemand.com --hostname quizmaster-[random-word]
cf stop quizmaster
cf rename quizmaster quizmaster-old
cf rename quizmaster-new quizmaster
cf delete quizmaster-old -f
```