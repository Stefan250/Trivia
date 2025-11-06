# Trivia Backend and Frontend
This repository contians a Trivia backend which filters the results of the Open Trivia API so the answer is not visible in the network tab of a browser.

## Running the backend
1. Make sure Java 25 is installed and JAVA_HOME is set
1. Make sure maven is installed
1. Navigate to the `\TriviaBackend` folder and run `mvn spring-boot:run`

## Running the frontend
1. Install serve: `npm install -g serve`
1. Run `npx serve -l 8000 TriviaFrontend` from this folder


# Seeing the result
1. Navigate to `http://localhost:8000/` in your browser