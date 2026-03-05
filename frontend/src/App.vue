<template>
  <div class="app">
    <header class="app-header">
      <h1>Hub - 国际跳棋</h1>
    </header>
    <main class="app-main">
      <DraughtsBoard
        :board="board"
        :turn="turn"
        :legal-moves="legalMoves"
        @move="onMove"
      />
      <GameControls
        :turn="turn"
        :move-number="moveNumber"
        :white-time="whiteTime"
        :black-time="blackTime"
        :is-end="isEnd"
        @new-game="onNewGame"
        @undo="onUndo"
        @redo="onRedo"
        @load-position="onLoadPosition"
      />
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { fetchGameState, makeMove, newGame, undoMove, redoMove, loadPosition } from './api/gameApi.js'
import DraughtsBoard from './components/DraughtsBoard.vue'
import GameControls from './components/GameControls.vue'

const board = ref([])
const turn = ref(0)
const legalMoves = ref([])
const highlightedSquares = ref([])
const moveNumber = ref(1)
const whiteTime = ref(300)
const blackTime = ref(300)
const isEnd = ref(false)

const emit = defineEmits(['newGame', 'undo', 'redo', 'loadPosition'])


function applyState(state) {
  if (state.board) {
    board.value = state.board.board
    turn.value = state.board.turn
    moveNumber.value = state.board.moveNumber
    isEnd.value = state.board.isEnd
    highlightedSquares.value = state.board.highlightedSquares || []
  }
  legalMoves.value = state.legalMoves || []
  whiteTime.value = state.whiteTime ?? 300
  blackTime.value = state.blackTime ?? 300
}

async function loadState() {
  try {
    const state = await fetchGameState()
    applyState(state)
  } catch (e) {
    console.error('Failed to load game state:', e)
  }
}

async function onMove({ from, to }) {
  try {
    const resp = await makeMove(from, to)
    if (resp.board) {
      applyState(resp)
      // 移动后清空高亮
      highlightedSquares.value = []
    } else {
      await loadState()
    }
  } catch (e) {
    console.error('Move failed:', e)
    await loadState()
  }
}


async function onNewGame() {
  try {
    const state = await newGame()
    applyState(state)
  } catch (e) {
    console.error('New game failed:', e)
  }
}

async function onUndo() {
  try {
    const state = await undoMove()
    applyState(state)
  } catch (e) {
    console.error('Undo failed:', e)
  }
}

async function onRedo() {
  try {
    const state = await redoMove()
    applyState(state)
  } catch (e) {
    console.error('Redo failed:', e)
  }
}

async function onLoadPosition(fen) {
  try {
    const state = await loadPosition(fen)
    applyState(state)
  } catch (e) {
    console.error('Load position failed:', e)
  }
}

onMounted(loadState)
</script>
