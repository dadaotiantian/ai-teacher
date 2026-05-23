<script setup lang="ts">
import { useAppStore } from '../stores/app'

const store = useAppStore()
</script>

<template>
  <section class="page">
    <header class="topbar">
      <div>
        <h1>Student Study</h1>
        <p class="muted">{{ store.username }}</p>
      </div>
      <button class="secondary" @click="store.logoutAccount()">Sign Out</button>
    </header>

    <div class="dashboard-grid">
      <article class="card">
        <h2>Characters</h2>
        <p>Choose a character and continue the word study flow.</p>
        <button @click="store.enterStudentLearning(true)">Enter Study</button>
      </article>
      <article class="card">
        <h2>Review</h2>
        <p>Practice words based on the memory schedule.</p>
      </article>
      <article class="card">
        <h2>Chat</h2>
        <p>Study with the selected AI teaching agent.</p>
      </article>
    </div>
  </section>
</template>
