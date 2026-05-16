<script setup lang="ts">
import { ref } from 'vue'
import { useAppStore } from '../stores/app'

const store = useAppStore()
const name = ref('')
</script>

<template>
  <section class="page narrow">
    <header class="topbar">
      <h1>创建角色</h1>
      <button v-if="store.players.length" class="secondary" @click="store.view = 'players'">返回</button>
    </header>
    <div class="form-panel">
      <p>已创建 {{ store.players.length }} / 6</p>
      <div class="field">
        <label>角色名称</label>
        <input v-model="name" maxlength="40" placeholder="支持中文，最多 40 个字符" />
      </div>
      <p v-if="store.error" class="error">{{ store.error }}</p>
      <button @click="store.createPlayer(name)">创建角色</button>
    </div>
  </section>
</template>
