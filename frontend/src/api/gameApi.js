const BASE_URL = '/api/game'

async function handleResponse(response) {
  if (!response.ok) {
    throw new Error(`HTTP error: ${response.status}`)
  }
  return response.json()
}

export function fetchGameState() {
  return fetch(`${BASE_URL}/state`).then(handleResponse)
}

export function makeMove(from, to) {
  return fetch(`${BASE_URL}/move`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ from, to })
  }).then(handleResponse)
}

export function newGame() {
  return fetch(`${BASE_URL}/new`, { method: 'POST' }).then(handleResponse)
}

export function undoMove() {
  return fetch(`${BASE_URL}/undo`, { method: 'POST' }).then(handleResponse)
}

export function redoMove() {
  return fetch(`${BASE_URL}/redo`, { method: 'POST' }).then(handleResponse)
}

export function loadPosition(fen) {
  return fetch(`${BASE_URL}/position`, {
    method: 'POST',
    headers: { 'Content-Type': 'text/plain' },
    body: fen
  }).then(handleResponse)
}

export function fetchLegalMoves() {
  return fetch(`${BASE_URL}/moves`).then(handleResponse)
}
