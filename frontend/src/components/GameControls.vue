<template>
  <div class="controls">
    <div class="controls__status">
      <div class="status-row">
        <span class="label">回合：</span>
        <span class="value">第 {{ moveNumber }} 步</span>
      </div>
      <div class="status-row">
        <span class="label">当前：</span>
        <span class="turn-indicator" :class="turn === 0 ? 'turn--white' : 'turn--black'">
          {{ turn === 0 ? '白方' : '黑方' }}
        </span>
      </div>
      <div v-if="isEnd" class="status-row status--end">
        🏁 游戏结束
      </div>
    </div>

    <div class="controls__clocks">
      <div class="clock">
        <span class="clock__label">⬜ 白方</span>
        <span class="clock__time">{{ formatTime(whiteTime) }}</span>
      </div>
      <div class="clock">
        <span class="clock__label">⬛ 黑方</span>
        <span class="clock__time">{{ formatTime(blackTime) }}</span>
      </div>
    </div>

    <div class="controls__buttons">
      <button class="btn" @click="emit('newGame')">新游戏</button>
      <button class="btn" @click="emit('undo')">悔棋</button>
      <button class="btn" @click="emit('redo')">重做</button>
    </div>

    <div class="controls__fen">
      <input
        v-model="fenInput"
        type="text"
        class="fen-input"
        placeholder="输入 FEN 字符串..."
      />
      <button class="btn btn--small" @click="onLoadFen">加载</button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

defineProps({
  turn: { type: Number, default: 0 },
  moveNumber: { type: Number, default: 1 },
  whiteTime: { type: Number, default: 300 },
  blackTime: { type: Number, default: 300 },
  isEnd: { type: Boolean, default: false }
})

const emit = defineEmits(['newGame', 'undo', 'redo', 'loadPosition'])

const fenInput = ref('')

function formatTime(seconds) {
  const s = Math.max(0, Math.floor(seconds))
  const mm = String(Math.floor(s / 60)).padStart(2, '0')
  const ss = String(s % 60).padStart(2, '0')
  return `${mm}:${ss}`
}

function onLoadFen() {
  if (fenInput.value.trim()) {
    emit('loadPosition', fenInput.value.trim())
  }
}
</script>

<style scoped>
.controls {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 16px;
  background: #f8f8f8;
  border-radius: 8px;
  border: 1px solid #ddd;
  min-width: 220px;
}

.controls__status {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.status-row {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.95rem;
}

.label {
  color: #666;
}

.value {
  font-weight: 600;
}

.turn-indicator {
  font-weight: 700;
  padding: 2px 10px;
  border-radius: 4px;
}

.turn--white {
  background: #fff;
  border: 1px solid #aaa;
  color: #333;
}

.turn--black {
  background: #333;
  color: #fff;
}

.status--end {
  font-weight: 700;
  color: #e74c3c;
  font-size: 1.1rem;
}

.controls__clocks {
  display: flex;
  gap: 12px;
}

.clock {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 8px;
  background: #fff;
  border-radius: 6px;
  border: 1px solid #ddd;
}

.clock__label {
  font-size: 0.8rem;
  color: #666;
}

.clock__time {
  font-size: 1.4rem;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
}

.controls__buttons {
  display: flex;
  gap: 8px;
}

.btn {
  flex: 1;
  padding: 8px 12px;
  border: none;
  border-radius: 6px;
  background: #3498db;
  color: #fff;
  font-size: 0.9rem;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
}

.btn:hover {
  background: #2980b9;
}

.btn:active {
  background: #1f6da0;
}

.btn--small {
  flex: none;
  padding: 8px 16px;
}

.controls__fen {
  display: flex;
  gap: 6px;
}

.fen-input {
  flex: 1;
  padding: 8px;
  border: 1px solid #ccc;
  border-radius: 6px;
  font-size: 0.85rem;
}

.fen-input:focus {
  outline: none;
  border-color: #3498db;
}
</style>
