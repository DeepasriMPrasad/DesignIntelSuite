# Quiz Master API - Bruno Collection

This repository contains a Bruno collection for testing the Quiz Master API. Bruno is an open-source API client that makes it easy to test and debug APIs.

## Getting Started

To use this collection:

1. Install Bruno from [https://www.usebruno.com/](https://www.usebruno.com/)
2. Open Bruno and import the collection from the `bruno-collection` folder
3. Make sure the Quiz Master application is running on port 5000
4. Follow the testing workflow described below

## Testing Workflow

1. First, run the **Health Check** request to verify that the API is up and running
2. Create a new quiz session using the **Create Quiz Session** request
3. Copy the `sessionId` from the response and update the collection variable
4. Get the first question using the **Get Question** request
5. Copy the `questionId` from the response and update the collection variable
6. Choose an answer and update the `answerId` variable
7. Submit your answer using the **Validate Answer** request
8. Repeat steps 4-7 until you've completed all questions
9. Check your score at any time using the **Get Score** request
10. End the quiz using the **End Quiz** request to get your final score

## API Endpoints

The collection includes the following endpoints:

- **Health Check** (`GET /api/quiz/health`): Verify that the API is running
- **Create Quiz Session** (`POST /api/quiz/start`): Start a new quiz
- **Get Question** (`GET /api/quiz/question`): Get the current or next question
- **Validate Answer** (`POST /api/quiz/validate`): Submit an answer
- **Get Score** (`GET /api/quiz/score`): Check the current score
- **End Quiz** (`POST /api/quiz/end`): End the quiz and get the final score

## Variables

The collection uses the following variables:

- `host`: The host where the API is running (default: `localhost:5000`)
- `sessionId`: The session ID returned from the Create Quiz Session endpoint
- `questionId`: The ID of the current question
- `answerId`: The ID of the selected option/answer

You need to update these variables manually after receiving the corresponding responses.

## Environments

Two environments are provided:

1. **Local**: For testing on your local machine
2. **Replit**: For testing on Replit

## Troubleshooting

If you encounter any issues:

1. Make sure the Quiz Master application is running on port 5000
2. Check that you've updated the variables correctly
3. Verify that the API is accessible by running the Health Check endpoint
4. Check the application logs for any errors

Happy testing!