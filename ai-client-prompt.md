# Quiz Master API Integration Guide for AI Agents

This guide explains how to integrate with the Quiz Master API to create a quiz experience for users.

## API Endpoints

The Quiz Master provides the following RESTful endpoints:

1. **Start a Quiz Session**
   - `POST /api/quiz/start`
   - Creates a new quiz session for a user
   - Request Body: `{ "userName": "string" }`
   - Response: `{ "sessionId": "string", "message": "string", "userName": "string", "maxQuestions": number }`

2. **Get a Random Question**
   - `GET /api/quiz/question?sessionId={sessionId}`
   - Returns a random question with multiple choice options
   - Response: 
     ```json
     {
       "questionId": "string", 
       "text": "string", 
       "options": [
         { "id": "string", "text": "string" }
       ],
       "attempts": number,
       "maxAttempts": number,
       "completedQuestions": number,
       "totalQuestions": number
     }
     ```

3. **Validate an Answer**
   - `POST /api/quiz/validate`
   - Checks if the provided answer is correct
   - Request Body: 
     ```json
     {
       "sessionId": "string", 
       "questionId": "string", 
       "answerId": "string"
     }
     ```
   - Response: 
     ```json
     {
       "questionId": "string",
       "correct": boolean,
       "message": "string",
       "attempts": number,
       "maxAttempts": number,
       "remainingAttempts": number,
       "correctAnswerId": "string",
       "remainingQuestions": number
     }
     