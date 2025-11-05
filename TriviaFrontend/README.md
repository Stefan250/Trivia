# TriviaFrontend

Lightweight static frontend for a Trivia API.

## Overview

This is a minimal static UI that calls a backend Trivia API to:
- GET /api/getQuestions — retrieve a list of questions
- POST /api/checkAnswers — send answers for checking and receive results

The frontend expects the backend to be served at http://localhost:8080 and exposes the static UI and API under the same origin when possible. By default the frontend is configured to use the fixed base URL:

http://localhost:8080/api

You can open the UI in a browser or serve it from the backend so both are same-origin.

## Files

- `index.html` — main page and controls
- `style.css` — styles for layout and result highlighting
- `app.js` — UI logic, fetches questions and posts answers
- `README.md` — this file

## How to run (quick)

Option A — Serve static files only (good for testing the UI, watch for CORS):

From PowerShell inside this folder:

```powershell
# start a simple HTTP server (Python)
python -m http.server 8000
```

Then open: http://localhost:8000

Note: If you run the UI from a different origin than your API (e.g. UI on port 8000 and API on port 8080) the browser requires CORS headers from the API. See "CORS" below.

Option B — Serve frontend from your API server (recommended: same origin)

Copy the contents of this folder into your backend's static/public directory or configure your backend to serve `index.html` and static assets. Example (Express):

```js
// serve static files in Express
app.use(express.static(path.join(__dirname, 'public')));
// API routes live under /api
```

Then open: http://localhost:8080 (or your server origin)

This is the preferred setup: the frontend and API share the same origin and no CORS configuration is needed.

## API contract

The frontend expects the following endpoints and data shapes.

GET /api/questions
- Response: JSON array of question objects
- Each question object shape (supported variations):
  - { id: <number|string>, question: <string>, answers: ["A","B","C","D"] }
  - or { id: <...>, question: <string>, answerOptions: [..] }
- Notes: HTML entities in strings (like `&#039;`) are decoded by the frontend.

POST /api/checkanswers
- Request body (JSON):

```json
{
  "answerRequests": [
    { "questionId": 1, "answer": "A" },
    { "questionId": 2, "answer": "C" }
  ]
}
```

- Response: the frontend accepts either of these shapes:
  1) An array: `[ { "questionId": 1, "result": true }, ... ]`
  2) An object wrapper (matches Java records used on the backend):

```json
{ "answerResponse": [ { "questionId": 1, "result": true }, ... ] }
```

The frontend normalizes both shapes and displays per-question feedback (correct/incorrect) and score.

If your backend uses slightly different field names (e.g., `id` / `correct`), the frontend attempts to tolerate common variants — but if it still fails, paste the actual request/response and the frontend will be adjusted.

## Debugging & CORS

If the UI reports "Failed to load questions: NetworkError" or similar:

1. Verify the backend is reachable from your machine (PowerShell):
```powershell
Invoke-WebRequest -Uri http://localhost:8080/api/questions -UseBasicParsing
```
If this fails, make sure your API server is running and listening on port 8080.

2. If the backend responds locally but the browser still fails, it's likely CORS. The browser console will show messages like:
```
Access to fetch at 'http://localhost:8080/api/getQuestions' from origin 'http://localhost:8000' has been blocked by CORS policy
```

How to fix CORS (quick): the API must include these headers in responses (or use framework CORS middleware):

```
Access-Control-Allow-Origin: http://localhost:8000
Access-Control-Allow-Methods: GET, POST, OPTIONS
Access-Control-Allow-Headers: Content-Type
```

For local development you can use `Access-Control-Allow-Origin: *` but do not do this in production if you need authentication/cookies.

Browser debug panel & debug panel in the UI
- The UI includes a debug panel at the bottom that prints detailed fetch errors (URL, error name, payload). Open DevTools → Network to inspect request/response headers and bodies.

## Troubleshooting checklist

- Server down? Start your API and verify the GET request in PowerShell or with curl.
- Wrong port/path? Ensure API is reachable at http://localhost:8080/api/questions and POST at /api/checkanswers.
- CORS blocked? Add CORS headers or serve frontend from same origin.
- Request/response shapes differ? Copy the browser Network response JSON to confirm fields. The frontend tolerates common variations but can be adapted.

## Optional adjustments you might want

- Highlight the actual correct answer if the backend returns it (e.g., `correctAnswer` field in response). The frontend can be updated to show that.
- Omit unanswered questions from the payload (the frontend currently sends null for unanswered answers); this can be changed to send only answered items.
- Add authentication headers if your API requires them.

## Contact / Next steps

If anything doesn't work, paste:
- The exact GET and POST request/response JSON from the browser DevTools Network tab, and
- Any console errors (CORS-related messages)

I can then modify the frontend to match the exact server schema or provide the exact CORS/server changes for your backend.

---

Enjoy the trivia UI — open `index.html` (or serve it via your backend) and click "Start Game".