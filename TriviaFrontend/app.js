// Lightweight Trivia Frontend
// The API base is fixed for this frontend (provided by user):
// GET  http://localhost:8080/api/questions
// POST http://localhost:8080/api/checkanswers
const DEFAULT_API_BASE = 'http://localhost:8080/api';

const els = {
  apiBase: document.getElementById('apiBase'),
  startBtn: document.getElementById('startBtn'),
  status: document.getElementById('status'),
  questionsForm: document.getElementById('questionsForm'),
  checkBtn: document.getElementById('checkBtn'),
  restartBtn: document.getElementById('restartBtn'),
  score: document.getElementById('score'),
  debug: document.getElementById('debug'),
};

let questions = [];
let selected = {}; // map questionId -> answer string
let checked = false;

// Decode HTML entities like &#039; returned inside JSON strings
function decodeHtml(html){
  if(!html) return '';
  const txt = document.createElement('textarea');
  txt.innerHTML = html;
  return txt.value;
}

function setStatus(msg, isError){
  els.status.textContent = msg || '';
  els.status.style.color = isError ? 'var(--danger)' : '';
  // also log brief info to debug panel
  if(els.debug){
    // keep status short in debug panel as header
    els.debug.textContent = `${new Date().toLocaleTimeString()} | ${msg || ''}`;
  }
}

function setDebug(full){
  if(!els.debug) return;
  // append with timestamp so multiple errors are visible
  const ts = new Date().toLocaleTimeString();
  els.debug.textContent = `${ts} | ${full}\n\n` + els.debug.textContent;
  console.debug(full);
}

function clearUI(){
  els.questionsForm.innerHTML = '';
  els.score.textContent = '';
  selected = {};
  questions = [];
  checked = false;
  els.checkBtn.disabled = true;
  els.restartBtn.style.display = 'none';
}

async function fetchQuestions(){
  const base = (els.apiBase.value || DEFAULT_API_BASE).replace(/\/$/, '');
  const url = base + '/questions';
  setStatus('Loading questions...');
  try{
    const res = await fetch(url);

    // If the API rate-limits or returns an error, read the body and headers for a clearer message
    if(!res.ok){
      let bodyText = '';
      try{
        const j = await res.json();
        bodyText = j && (j.message || j.error) ? (j.message || j.error) : JSON.stringify(j);
      }catch(e){
        bodyText = await res.text().catch(()=> '');
      }

      // include useful headers like Retry-After if present
      const retryAfter = res.headers.get('Retry-After');
      const hdrs = [];
      if(retryAfter) hdrs.push(`Retry-After: ${retryAfter}`);
      const hdrText = hdrs.length ? hdrs.join('; ') : '';

      const statusLine = `HTTP ${res.status}${res.statusText ? ' - ' + res.statusText : ''}`;
      const message = bodyText ? `${statusLine}: ${bodyText}` : statusLine;
      setStatus(message, res.status === 429);
      setDebug(`GET ${url} returned ${res.status}\nHeaders: ${hdrText}\nBody: ${bodyText}`);
      return;
    }

  const data = await res.json();
  // support API that returns either an array or an object wrapper { questionResponse: [...] }
  const list = Array.isArray(data) ? data : (data && Array.isArray(data.questionResponse) ? data.questionResponse : []);
  if(!Array.isArray(list)) throw new Error('Expected an array of questions');
  questions = list;
    renderQuestions();
    setStatus(`Loaded ${questions.length} questions.`);
  }catch(err){
    setStatus('Failed to load questions: ' + err.message, true);
    // show more detail in debug panel to help diagnose NetworkError/CORS
    const details = `Failed fetching GET ${url}\nError name: ${err.name}\nMessage: ${err.message}\nCheck that the API server is running and that CORS allows requests from this page.`;
    setDebug(details);
  }
}

function renderQuestions(){
  els.questionsForm.innerHTML = '';
  if(questions.length === 0){
    els.questionsForm.innerHTML = '<p class="hint">No questions to show.</p>';
    return;
  }

  questions.forEach((q, idx) => {
    const card = document.createElement('fieldset');
    card.className = 'card';
  card.dataset.qid = q.id;

    const legend = document.createElement('legend');
    legend.className = 'question-text';
    legend.textContent = `${idx + 1}. ${decodeHtml(q.question)}`;
    card.appendChild(legend);

    const opts = document.createElement('div');
    opts.className = 'options';

    // support API that uses either `answers` or `answerOptions`
    const answers = Array.isArray(q.answers) ? q.answers : (Array.isArray(q.answerOptions) ? q.answerOptions : []);
    if(answers.length === 0){
      const p = document.createElement('p');
      p.className = 'hint';
      p.textContent = 'No answers available for this question.';
      opts.appendChild(p);
    }

    answers.forEach((ans, i) => {
      const label = document.createElement('label');
      label.className = 'option';
      label.tabIndex = 0; // make label focusable for keyboard

      const input = document.createElement('input');
      input.type = 'radio';
      input.name = `q_${q.id}`;
      input.value = ans;
      input.addEventListener('change', ()=>{
        selected[q.id] = input.value;
        // enable check button when every question has an answer selected
        const allAnswered = questions.length && questions.every(x => selected[x.id]);
        els.checkBtn.disabled = !allAnswered;
      });

      const span = document.createElement('span');
  span.className = 'option-text';
  span.textContent = decodeHtml(ans);

      label.appendChild(input);
      label.appendChild(span);
      opts.appendChild(label);
    });

    card.appendChild(opts);
    els.questionsForm.appendChild(card);
  });
}

async function checkAnswers(){
  if(checked) return;
  const base = (els.apiBase.value || DEFAULT_API_BASE).replace(/\/$/, '');
  // build payload matching backend shape:
  // { "answerRequests": [ { questionId: 1, answer: "A" }, ... ] }
  const answerRequests = questions.map(q => ({ questionId: q.id, answer: selected[q.id] || null }));
  const payload = { answerRequests };
  setStatus('Checking answers...');
  try{
    const res = await fetch(base + '/checkanswers', {
      method: 'POST',
      headers: {'Content-Type':'application/json'},
      body: JSON.stringify(payload)
    });
    // If non-ok, read the body & headers to show helpful error info (avoid hiding 429 as generic error)
    if(!res.ok){
      let bodyText = '';
      try{ const j = await res.json(); bodyText = j && (j.message || j.error) ? (j.message || j.error) : JSON.stringify(j); }catch(e){ bodyText = await res.text().catch(()=> ''); }
      const retryAfter = res.headers.get('Retry-After');
      const hdrText = retryAfter ? `Retry-After: ${retryAfter}` : '';
      const statusLine = `HTTP ${res.status}${res.statusText ? ' - ' + res.statusText : ''}`;
      const message = bodyText ? `${statusLine}: ${bodyText}` : statusLine;
      setStatus(message, res.status === 429);
      setDebug(`POST ${base + '/checkanswers'} returned ${res.status}\nHeaders: ${hdrText}\nBody: ${bodyText}`);
      return;
    }
    const results = await res.json();
    // results expected: [{id, correct: true|false}, ...] or { answerResponse: [...] }
    showResults(results);
  }catch(err){
    setStatus('Failed to check answers: ' + err.message, true);
    const url = base + '/checkanswers';
    const details = `Failed POST ${url}\nPayload: ${JSON.stringify(payload, null, 2)}\nError name: ${err.name}\nMessage: ${err.message}`;
    setDebug(details);
  }
}

function showResults(results){
  checked = true;
  // Normalize results: backend may return either an array or an object wrapper
  // Example shapes supported:
  // 1) [{ questionId: 11, result: true }, ...]
  // 2) { answerResponse: [{ questionId: 11, result: true }, ...] }
  const resultsList = Array.isArray(results)
    ? results
    : (results && Array.isArray(results.answerResponse) ? results.answerResponse : []);

  // map id -> correct bool (string keys to match dataset/qid)
  const map = new Map(resultsList.map(r => [String(r.questionId ?? r.id), !!(r.result ?? r.correct ?? false)]));
  let correctCount = 0;

  // for each question card, mark options
  const cards = [...els.questionsForm.querySelectorAll('.card')];
  cards.forEach(card => {
    const qid = card.dataset.qid;
    const isCorrect = map.get(qid);
    const inputs = [...card.querySelectorAll('input')];

    // find which answer is correct by asking the API result: the API doesn't return the correct answer string,
    // so we assume the API responds with correct=true for the user's chosen answer id only. To show the correct answer visually,
    // we mark the user's chosen option as correct or incorrect and also highlight the correct one when provided.
    
    const chosen = selected[qid];
    if(isCorrect) correctCount += 1;

    inputs.forEach(inp => {
      const label = inp.closest('.option');
      // disable inputs
      inp.disabled = true;
      // reset classes
      label.classList.remove('correct','incorrect','dimmed');
      if(inp.value === chosen){
        // this was user's selected answer
        if(isCorrect){
          label.classList.add('correct');
        }else{
          label.classList.add('incorrect');
        }
      }else{
        // dim other options
        label.classList.add('dimmed');
      }
    });
  });

  els.score.textContent = `Score: ${correctCount} / ${questions.length}`;
  els.checkBtn.disabled = true;
  els.restartBtn.style.display = 'inline-block';
  setStatus('Results shown.');
}

// Restart: enable re-playing
function restart(){
  clearUI();
  setStatus('Ready. Enter API base and click Start Game.');
}

// wire buttons
els.startBtn.addEventListener('click', (e)=>{ e.preventDefault(); clearUI(); fetchQuestions(); });
els.checkBtn.addEventListener('click', (e)=>{ e.preventDefault(); checkAnswers(); });
els.restartBtn.addEventListener('click', (e)=>{ e.preventDefault(); restart(); });

// on load set default base and a helpful hint
window.addEventListener('load', ()=>{
  // lock the API base to the known server
  els.apiBase.value = DEFAULT_API_BASE;
  els.apiBase.disabled = true;
  setStatus('Ready. Click Start Game to load questions from the API.');
});
