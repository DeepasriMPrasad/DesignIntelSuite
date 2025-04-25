# SAP BTP Deployment Quick Reference

## Prerequisites
- SAP BTP account with Cloud Foundry subscription
- Cloud Foundry CLI installed
- Java 17+ and Maven installed

## Quick Commands

### Login
```bash
cf login -a https://api.cf.eu10.hana.ondemand.com
```

### Select Region
```bash
./btp-regions.sh eu10  # Replace eu10 with your region
```

### Create Database
```bash
cf create-service postgresql-db trial quizmaster-db
```

### Deploy Application
```bash
./deploy_to_btp.sh
```

### Check Application Status
```bash
cf app quizmaster
```

### View Logs
```bash
cf logs quizmaster --recent
```

### Scale Application
```bash
cf scale quizmaster -i 2  # Scale to 2 instances
cf scale quizmaster -m 2G  # Increase memory to 2GB
```

### Restart Application
```bash
cf restart quizmaster
```

### Delete Application
```bash
cf delete quizmaster -r -f  # Delete app and routes
```

### Delete Services
```bash
cf delete-service quizmaster-db -f
```

## Database Service Details

### View Service Details
```bash
cf service quizmaster-db
```

### Create Service Key
```bash
cf create-service-key quizmaster-db quizmaster-key
```

### View Service Key
```bash
cf service-key quizmaster-db quizmaster-key
```

## Environment Variables

### View Environment Variables
```bash
cf env quizmaster
```

### Set Environment Variable
```bash
cf set-env quizmaster QUESTIONS_EXCEL_PATH file:./questions.xlsx
cf restart quizmaster  # Required to apply changes
```

## Troubleshooting

### Application Crashes
1. Check logs: `cf logs quizmaster --recent`
2. Increase memory: `cf scale quizmaster -m 1.5G`
3. Verify service bindings: `cf services`

### Database Connection Issues
1. Verify service exists: `cf service quizmaster-db`
2. Check service key credentials
3. Ensure app is bound to service: `cf bind-service quizmaster quizmaster-db`

### Help Resources
- SAP BTP Documentation: https://help.sap.com/docs/btp
- Cloud Foundry CLI Reference: https://cli.cloudfoundry.org/en-US/v8/