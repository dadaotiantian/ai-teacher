<script setup lang="ts">
import { useAppStore } from '../stores/app'

const store = useAppStore()
</script>

<template>
  <section class="page">
    <header class="topbar">
      <div>
        <h1>Teacher Workspace</h1>
        <p class="muted">{{ store.username }}</p>
      </div>
      <button class="secondary" @click="store.logoutAccount()">Sign Out</button>
    </header>

    <div class="dashboard-grid">
      <article class="card">
        <h2>Classes</h2>
        <p>Track class progress and learning activity.</p>
      </article>
      <article class="card">
        <h2>Assignments</h2>
        <p>Create word review tasks and practice plans.</p>
      </article>
      <article class="card">
        <h2>Reports</h2>
        <p>Review pronunciation, spelling, and usage results.</p>
      </article>
    </div>
  </section>
</template>
