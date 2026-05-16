<script setup lang="ts">
import { ref } from 'vue'
import type { WordInfo } from '../types/models'

const props = defineProps<{ word: WordInfo; mode: string }>()
const emit = defineEmits<{ submit: [answer: string] }>()
const answer = ref('')
</script>

<template>
  <article class="word-card">
    <div>
      <span>{{ props.mode }}</span>
      <h3 v-if="props.mode === 'pronunciation'">{{ props.word.pronunciation_uk || props.word.pronunciation_us }}</h3>
      <h3 v-else-if="props.mode === 'usage'">根据释义造词：{{ props.word.meaning_zh }}</h3>
      <h3 v-else>{{ props.word.meaning_zh }}</h3>
    </div>
    <div class="row">
      <input v-model="answer" placeholder="填写单词" @keydown.enter="emit('submit', answer)" />
      <button @click="emit('submit', answer)">提交</button>
    </div>
  </article>
</template>
