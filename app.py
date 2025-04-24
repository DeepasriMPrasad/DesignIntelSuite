import streamlit as st
import requests
import json
import time
import socket

# Set up the page first
st.set_page_config(
    page_title="Quiz Master",
    page_icon="❓",
    layout="centered",
    initial_sidebar_state="expanded",
)

# Add a script to suppress WebSocket errors in the browser console
st.markdown('''
<script>
// Override WebSocket to suppress connection errors in console
(function() {
    // Store original WebSocket
    const OrigWebSocket = window.WebSocket;
    
    // Create a no-op console error handler
    const noopConsoleError = function() {};
    
    // Replace WebSocket with our version
    window.WebSocket = function(url, protocols) {
        const ws = new OrigWebSocket(url, protocols);
        
        // Override the onerror handler to suppress console errors
        const origOnError = ws.onerror;
        ws.onerror = function(event) {
            // Ignore WebSocket errors in the console
            console.error = noopConsoleError;
            
            // Call original handler if it exists
            if (origOnError) origOnError.call(ws, event);
            
            // Restore console.error
            setTimeout(function() {
                console.error = console.__error || console.error;
            }, 100);
        };
        
        return ws;
    };
    
    // Copy properties from original WebSocket
    for (let prop in OrigWebSocket) {
        if (OrigWebSocket.hasOwnProperty(prop)) {
            window.WebSocket[prop] = OrigWebSocket[prop];
        }
    }
})();
</script>
''', unsafe_allow_html=True)

# Set fixed API URL for the external server with Replit-compatible URLs
# Updated to use correct port (5000) and context path (/quizmaster)
API_BASE_URL = "http://0.0.0.0:5000/quizmaster/api/quiz"
SPRING_APP_URL = "http://0.0.0.0:5000/quizmaster"

try:
    # Try to get Replit environment variables
    import os
    repl_slug = os.environ.get("REPL_SLUG", "")
    repl_owner = os.environ.get("REPL_OWNER", "")

    # If we're in a Replit environment, use the Replit URLs
    if repl_slug and repl_owner:
        API_BASE_URL = f"https://{repl_slug}.{repl_owner}.repl.co/quizmaster/api/quiz"
        SPRING_APP_URL = f"https://{repl_slug}.{repl_owner}.repl.co/quizmaster"
except Exception as e:
    st.sidebar.error(f"Error setting up URLs: {str(e)}")

# Add debug info in sidebar
st.sidebar.markdown("### Debug Information")
st.sidebar.write(f"API URL: {API_BASE_URL}")

# Add direct access button
st.sidebar.markdown("### Direct Access")
st.sidebar.markdown(
    f"You can access the Quiz Master application directly using the link below:"
)
st.sidebar.markdown(f"[Access Quiz Master Web Interface]({SPRING_APP_URL})")
st.sidebar.markdown(
    "*Note: If you encounter any issues with the Streamlit interface, try accessing the application directly.*"
)


# Function to check if API is available
def is_api_available():
    try:
        # Updated health check URL to include context path
        response = requests.get(f"{SPRING_APP_URL}/api/quiz/health", timeout=2)
        return response.status_code < 400
    except Exception as e:
        st.sidebar.error(f"Error connecting to API: {str(e)}")
        return False


# Show API status
if is_api_available():
    st.sidebar.success("✅ API is available")
else:
    st.sidebar.error("❌ API is not available")
    st.sidebar.info(
        "Make sure the Spring Boot application is running on port 5000")

# Add a custom CSS style
st.markdown("""
<style>
    .main-header {
        font-size: 2.5rem;
        color: #1E88E5;
        text-align: center;
    }
    .sub-header {
        font-size: 1.5rem;
        color: #424242;
        text-align: center;
        margin-bottom: 2rem;
    }
    .question-text {
        font-size: 1.2rem;
        font-weight: 500;
        background-color: #f0f2f6;
        padding: 1rem;
        border-radius: 0.5rem;
        margin-bottom: 1rem;
    }
    .correct-answer {
        color: #2e7d32;
        font-weight: bold;
    }
    .incorrect-answer {
        color: #d32f2f;
        font-weight: bold;
    }
    .score-display {
        font-size: 1.8rem;
        font-weight: bold;
        text-align: center;
        margin: 2rem 0;
    }
</style>
""",
            unsafe_allow_html=True)

# Initialize session state
if 'session_id' not in st.session_state:
    st.session_state.session_id = None
if 'current_question' not in st.session_state:
    st.session_state.current_question = None
if 'feedback' not in st.session_state:
    st.session_state.feedback = None
if 'quiz_ended' not in st.session_state:
    st.session_state.quiz_ended = False
if 'final_score' not in st.session_state:
    st.session_state.final_score = None

# Header
st.markdown("<h1 class='main-header'>Quiz Master</h1>", unsafe_allow_html=True)
st.markdown(
    "<p class='sub-header'>Test your knowledge with our interactive quiz!</p>",
    unsafe_allow_html=True)

# Direct access banner in the main section
if not is_api_available():
    st.warning(
        "⚠️ The Quiz Master API may not be accessible through Streamlit. Use the direct link below."
    )
    st.markdown(f"""
    ### Direct Access to Quiz Master

    For best results, access the Quiz Master application directly by clicking the button below:

    <a href='{SPRING_APP_URL}' target='_blank'>
        <button style='background-color: #1E88E5; color: white; padding: 10px 24px; border-radius: 5px; border: none; font-size: 16px; cursor: pointer;'>
            Access Quiz Master Web Interface
        </button>
    </a>
    """,
                unsafe_allow_html=True)
    st.markdown("""
    ---
    """)
else:
    st.info(
        f"🔗 Having trouble? You can also [access the Quiz Master interface directly]({SPRING_APP_URL})"
    )
    st.markdown("<hr>", unsafe_allow_html=True)


# Function to start a new quiz
def start_new_quiz(user_name, i_number):
    try:
        response = requests.post(f"{API_BASE_URL}/start",
                                 json={"userName": user_name, "iNumber": i_number})
        if response.status_code == 200:
            data = response.json()
            st.session_state.session_id = data.get('sessionId')
            st.session_state.current_question = None
            st.session_state.feedback = None
            st.session_state.quiz_ended = False
            st.session_state.final_score = None
            return True
        else:
            st.error(f"Error starting quiz: {response.text}")
            return False
    except Exception as e:
        st.error(f"Error connecting to the server: {str(e)}")
        return False


# Function to get the next question
def get_next_question():
    try:
        response = requests.get(
            f"{API_BASE_URL}/question",
            params={"sessionId": st.session_state.session_id})
        if response.status_code == 200:
            st.session_state.current_question = response.json()
            st.session_state.feedback = None
            return True
        else:
            st.error(f"Error getting question: {response.text}")
            return False
    except Exception as e:
        st.error(f"Error connecting to the server: {str(e)}")
        return False


# Function to validate an answer
def validate_answer(answer_id):
    try:
        response = requests.post(
            f"{API_BASE_URL}/validate",
            json={
                "sessionId": st.session_state.session_id,
                "questionId":
                st.session_state.current_question.get('questionId'),
                "answerId": answer_id
            })
        if response.status_code == 200:
            st.session_state.feedback = response.json()
            # If no more remaining attempts or the answer was correct
            if st.session_state.feedback.get(
                    'correct') or st.session_state.feedback.get(
                        'remainingAttempts', 0) == 0:
                # Get next question after a short delay (if there are remaining questions)
                if st.session_state.feedback.get('remainingQuestions', 0) > 0:
                    time.sleep(1)  # Short delay
                    get_next_question()
                else:
                    end_quiz()
            return True
        else:
            st.error(f"Error validating answer: {response.text}")
            return False
    except Exception as e:
        st.error(f"Error connecting to the server: {str(e)}")
        return False


# Function to get the current score
def get_score():
    try:
        response = requests.get(
            f"{API_BASE_URL}/score",
            params={"sessionId": st.session_state.session_id})
        if response.status_code == 200:
            return response.json()
        else:
            st.error(f"Error getting score: {response.text}")
            return None
    except Exception as e:
        st.error(f"Error connecting to the server: {str(e)}")
        return None


# Function to end the quiz
def end_quiz():
    try:
        response = requests.post(
            f"{API_BASE_URL}/end",
            params={"sessionId": st.session_state.session_id})
        if response.status_code == 200:
            st.session_state.final_score = response.json()
            st.session_state.quiz_ended = True
            return True
        else:
            st.error(f"Error ending quiz: {response.text}")
            return False
    except Exception as e:
        st.error(f"Error connecting to the server: {str(e)}")
        return False


# Main application logic
if st.session_state.session_id is None:
    # Welcome screen when no quiz is active
    with st.container():
        st.markdown("### Welcome to the Quiz!")
        st.markdown("Enter your name to start a new quiz session.")

        with st.form("start_quiz_form"):
            user_name = st.text_input("Your Name", max_chars=50)
            i_number = st.text_input("Your I-Number", max_chars=20, placeholder="e.g., I123456")
            st.caption("Each I-Number can only take the quiz once")
            start_button = st.form_submit_button("Start Quiz")

            if start_button and user_name and i_number:
                # Validate I-Number format (should start with 'I' followed by numbers)
                if not i_number.strip().upper().startswith('I') or not i_number.strip()[1:].isdigit():
                    st.warning("Please enter a valid I-Number (format: I followed by numbers)")
                else:
                    formatted_i_number = i_number.strip().upper()  # Ensure proper format
                    if start_new_quiz(user_name, formatted_i_number):
                        st.success(f"Welcome, {user_name}! Your quiz is starting...")
                        if get_next_question():
                            st.rerun()
            elif start_button:
                if not user_name:
                    st.warning("Please enter your name to start the quiz.")
                if not i_number:
                    st.warning("Please enter your I-Number to start the quiz.")

elif st.session_state.quiz_ended:
    # Quiz ended screen
    score_data = st.session_state.final_score

    st.markdown("<h2>Quiz Complete!</h2>", unsafe_allow_html=True)

    st.markdown(
        f"<div class='score-display'>Final Score: {score_data.get('percentageScore', 0):.1f}%</div>",
        unsafe_allow_html=True)

    col1, col2, col3 = st.columns(3)
    with col1:
        st.metric("Total Questions", score_data.get('totalQuestions', 0))
    with col2:
        st.metric("Correct Answers", score_data.get('correctAnswers', 0))
    with col3:
        st.metric("Time Taken", f"{score_data.get('duration', 0)} sec")

    st.markdown("### Thank you for playing!")

    if st.button("Start a New Quiz"):
        st.session_state.session_id = None
        st.session_state.current_question = None
        st.session_state.feedback = None
        st.session_state.quiz_ended = False
        st.session_state.final_score = None
        st.rerun()

else:
    # Active quiz screen - display current question and options
    question = st.session_state.current_question
    feedback = st.session_state.feedback

    # Display quiz progress
    progress = question.get('completedQuestions', 0) / question.get(
        'totalQuestions', 1)
    st.progress(progress)
    st.write(
        f"Question {question.get('completedQuestions', 0) + 1} of {question.get('totalQuestions', 0)}"
    )

    # Display question
    st.markdown(f"<div class='question-text'>{question.get('text', '')}</div>",
                unsafe_allow_html=True)

    # Display options
    options = question.get('options', [])

    # Create a form for answer submission
    with st.form(key=f"question_form_{question.get('questionId')}"):
        selected_option = st.radio("Select your answer:",
                                   options,
                                   format_func=lambda x: x.get('text', ''))
        submit_button = st.form_submit_button("Submit Answer")

        if submit_button:
            validate_answer(selected_option.get('id'))
            st.rerun()

    # Display feedback if available
    if feedback:
        if feedback.get('correct'):
            st.success(f"Correct! {feedback.get('message', '')}")
        else:
            st.error(f"Incorrect. {feedback.get('message', '')}")
            if 'correctAnswerId' in feedback:
                correct_option = next(
                    (opt for opt in options
                     if opt.get('id') == feedback.get('correctAnswerId')),
                    None)
                if correct_option:
                    st.info(
                        f"The correct answer was: {correct_option.get('text')}"
                    )

    # Display attempts information
    attempts = question.get('attempts', 0)
    max_attempts = question.get('maxAttempts', 3)
    st.write(f"Attempts: {attempts}/{max_attempts}")

    # Add a button to end the quiz early
    if st.button("End Quiz"):
        end_quiz()
        st.rerun()

# Add detailed instructions for accessing the Spring Boot application
st.markdown("""
## 🔍 Troubleshooting Guide

If you're having trouble accessing the Quiz Master application, try the following:

1. **Access via URL**: The Spring Boot application is running on port 5000, which is directly accessible via Replit.
   - You can access the Quiz Master application directly using Replit's URL (shown in your browser's address bar).
   - The Quiz Master application should be available at the root URL.

2. **Verify API Access**: Check if the API is available by clicking on the debug button below.

3. **Browser Issues**: Try opening the application in an incognito/private browsing window or a different browser.

4. **Refresh Spring Boot**: If needed, restart the Spring Boot application using Replit's workflow controls.
""")

# Add a debug button to test the API connection
if st.button("Test Spring Boot API Connection"):
    try:
        response = requests.get(f"{SPRING_APP_URL}/api/quiz/health", timeout=5)
        if response.status_code == 200:
            st.success(
                f"✅ Successfully connected to Spring Boot API: {response.text}"
            )
        else:
            st.error(
                f"⚠️ Connected but received error status: {response.status_code} - {response.text}"
            )
    except Exception as e:
        st.error(f"❌ Failed to connect to Spring Boot API: {str(e)}")
        st.info(
            "Make sure the Spring Boot application is running on port 5000")
