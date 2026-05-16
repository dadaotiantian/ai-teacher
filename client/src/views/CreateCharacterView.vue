<script setup lang="ts">
import { ref } from 'vue'
import { useAppStore } from '../stores/app'

const store = useAppStore()
const name = ref('')
</script>

<template>
  <section class="page narrow">
    <header class="topbar">
      <h1>Create Character</h1>
      <button v-if="store.players.length" class="secondary" @click="store.view = 'players'">Back</button>
    </header>
    <div class="form-panel">
      <p>Characters created: {{ store.players.length }} / 6</p>
      <div class="field">
        <label>Character Name</label>
        <input v-model="name" maxlength="40" placeholder="Supports Chinese, up to 40 characters" />
      </div>
      <p v-if="store.error" class="error">{{ store.error }}</p>
      <button @click="store.createPlayer(name)">Create Character</button>
    </div>
  </section>
</template>
