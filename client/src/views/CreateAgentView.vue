<script setup lang="ts">
import { ref } from 'vue'
import { useAppStore } from '../stores/app'

const store = useAppStore()
const name = ref('单词老师')
const wordTest = ref(true)
const studyPlan = ref(true)

function submit() {
  const abilities = ['dialogue']
  if (wordTest.value) abilities.push('word_test')
  if (studyPlan.value) abilities.push('study_plan')
  store.createAgent(name.value, abilities)
}
</script>

<template>
  <section class="page narrow">
    <header class="topbar">
      <h1>创建智能体</h1>
      <button v-if="store.agents.length" class="secondary" @click="store.view = 'chat'">返回</button>
    </header>
    <div class="form-panel">
      <div class="field">
        <label>智能体名称</label>
        <input v-model="name" />
      </div>
      <label class="check"><input type="checkbox" checked disabled /> 对话能力</label>
      <label class="check"><input v-model="wordTest" type="checkbox" /> 单词考核能力</label>
      <label class="check"><input v-model="studyPlan" type="checkbox" /> 学习计划能力</label>
      <p v-if="store.error" class="error">{{ store.error }}</p>
      <button @click="submit">创建智能体</button>
    </div>
  </section>
</template>
