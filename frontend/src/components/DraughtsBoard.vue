<template>
  <div class="board-container">
    <div class="board">
      <div
        v-for="(cell, index) in cells"
        :key="index"
        class="cell"
        :class="{
          'cell--light': cell.light,
          'cell--dark': !cell.light,
          'cell--selected': cell.squareNum === selectedSquare,
          'cell--highlighted': highlightedSet.has(cell.squareNum),
          'cell--legal-target': cell.squareNum !== null && isLegalTarget(cell.squareNum)
        }"
        @click="onCellClick(cell)"
      >
        <span v-if="!cell.light" class="cell__number">{{ cell.squareNum }}</span>
        <div v-if="cell.piece" class="piece" :class="pieceClass(cell.piece)">
          <div v-if="cell.piece === 3 || cell.piece === 4" class="piece__crown"></div>
        </div>
        <div v-if="cell.squareNum !== null && isLegalTarget(cell.squareNum) && !cell.piece" class="legal-dot"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'

const props = defineProps({
  board: { type: Array, default: () => [] },
  turn: { type: Number, default: 0 },
  legalMoves: { type: Array, default: () => [] },
  highlightedSquares: { type: Array, default: () => [] }
})

const emit = defineEmits(['move'])

const selectedSquare = ref(null)

const highlightedSet = computed(() => new Set(props.highlightedSquares))

const cells = computed(() => {
  const result = []
  let squareNum = 0
  for (let row = 0; row < 10; row++) {
    for (let col = 0; col < 10; col++) {
      const val = props.board[row]?.[col] ?? -1
      const light = val === -1
      let sn = null
      if (!light) {
        squareNum++
        sn = squareNum
      }
      result.push({
        row,
        col,
        light,
        squareNum: sn,
        piece: val > 0 ? val : null
      })
    }
  }
  return result
})

const movesFromSelected = computed(() => {
  if (selectedSquare.value === null) return []
  return props.legalMoves.filter(m => m.from === selectedSquare.value)
})

function isLegalTarget(sq) {
  return movesFromSelected.value.some(m => m.to === sq)
}

function pieceClass(piece) {
  return {
    'piece--white': piece === 1 || piece === 3,
    'piece--black': piece === 2 || piece === 4,
    'piece--king': piece === 3 || piece === 4
  }
}

function onCellClick(cell) {
  if (cell.light) return

  if (selectedSquare.value !== null) {
    if (isLegalTarget(cell.squareNum)) {
      emit('move', { from: selectedSquare.value, to: cell.squareNum })
      selectedSquare.value = null
      return
    }
  }

  if (cell.piece && hasMovesFrom(cell.squareNum)) {
    selectedSquare.value = cell.squareNum
  } else {
    selectedSquare.value = null
  }
}

function hasMovesFrom(sq) {
  return props.legalMoves.some(m => m.from === sq)
}
</script>

<style scoped>
.board-container {
  display: flex;
  justify-content: center;
  align-items: flex-start;
}

.board {
  display: grid;
  grid-template-columns: repeat(10, 1fr);
  grid-template-rows: repeat(10, 1fr);
  width: min(80vw, 560px);
  height: min(80vw, 560px);
  border: 3px solid #333;
  border-radius: 4px;
  overflow: hidden;
}

.cell {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cell--light {
  background-color: #dcc06e;
}

.cell--dark {
  background-color: #b3834b;
}

.cell--selected {
  background-color: #e74c3c !important;
}

.cell--highlighted {
  background-color: #e74c3c !important;
}

.cell--dark:hover {
  cursor: pointer;
  filter: brightness(1.1);
}

.cell__number {
  position: absolute;
  top: 1px;
  left: 2px;
  font-size: 0.6em;
  color: rgba(255, 255, 255, 0.5);
  pointer-events: none;
  user-select: none;
}

.piece {
  width: 75%;
  height: 75%;
  border-radius: 50%;
  position: relative;
  z-index: 1;
  box-sizing: border-box;
}

.piece--white {
  background: radial-gradient(circle at 35% 35%, #fff, #ddd);
  border: 2px solid #555;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.piece--black {
  background: radial-gradient(circle at 35% 35%, #555, #222);
  border: 2px solid #ccc;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.4);
}

.piece__crown {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 45%;
  height: 45%;
  transform: translate(-50%, -50%);
  border-radius: 50%;
  border: 2px solid;
}

.piece--white .piece__crown {
  border-color: #555;
  background: radial-gradient(circle, #f0e68c, #daa520);
}

.piece--black .piece__crown {
  border-color: #ccc;
  background: radial-gradient(circle, #c0c0c0, #808080);
}

.legal-dot {
  width: 28%;
  height: 28%;
  border-radius: 50%;
  background-color: rgba(46, 204, 113, 0.7);
  z-index: 1;
}

.cell--legal-target .piece {
  box-shadow: 0 0 0 3px rgba(46, 204, 113, 0.7);
}
</style>
