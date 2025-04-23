# Quiz Master API Collection

This is a Bruno collection for testing the Quiz Master API. It contains all the requests needed to interact with the API.

## Getting Started

1. Install Bruno from [usebruno.com](https://www.usebruno.com/)
2. Open Bruno and select "Open Collection"
3. Navigate to the `bruno-collection/QuizMaster` folder and open it

## Using the Collection

The requests are numbered in the order they should be executed:

1. **Health Check**: Verify the API is running
2. **Create Quiz Session**: Start a new quiz (copy the `sessionId` from the response)
3. **Get Question**: Get a question (copy the `questionId` from the response)
4. **Validate Answer**: Submit an answer (set `answerId` to one of the option IDs)
5. **Get Score**: Check your current score
6. **End Quiz**: End the quiz and get your final score

## Environment Variables

After receiving the `sessionId` from the Create Quiz Session request, you need to:

1. Click on the "Environments" tab in Bruno
2. Select the "local" environment
3. Update the `sessionId` variable with the value from the response
4. Do the same for `questionId` and `answerId` as you progress through the quiz

## Testing on Replit

To test on Replit:

1. Select the "replit" environment in Bruno
2. Replace the placeholders in the URL with your actual Replit values
3. Follow the same steps as for local testing

## Troubleshooting

If you encounter any issues:

1. Ensure the Spring Boot application is running on port 5000
2. Verify that the correct `sessionId` is being used in all requests
3. Check the application logs for any errors