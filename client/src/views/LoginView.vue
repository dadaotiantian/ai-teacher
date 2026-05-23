<script setup lang="ts">
import { ref } from 'vue'
import { useAppStore } from '../stores/app'

const store = useAppStore()
const username = ref('')
const password = ref('')
</script>

<template>
  <section class="auth-shell">
    <div class="auth-panel">
      <h1>Create Account</h1>
      <div class="field">
        <label>Mode</label>
        <div class="mode-selector" role="tablist" aria-label="Login mode">
          <button
            class="mode-option"
            :class="{ active: store.selectedMode === 'admin' }"
            type="button"
            @click="store.selectMode('admin')"
          >
            Admin
          </button>
          <button
            class="mode-option"
            :class="{ active: store.selectedMode === 'teacher' }"
            type="button"
            @click="store.selectMode('teacher')"
          >
            Teacher
          </button>
          <button
            class="mode-option"
            :class="{ active: store.selectedMode === 'student' }"
            type="button"
            @click="store.selectMode('student')"
          >
            Student
          </button>
        </div>
      </div>
      <div class="field">
        <label>Account Name</label>
        <input v-model="username" autocomplete="username" maxlength="40" placeholder="Supports Chinese, up to 40 characters" />
      </div>
      <div class="field">
        <label>Password</label>
        <input v-model="password" autocomplete="current-password" type="password" />
      </div>
      <p v-if="store.error" class="error">{{ store.error }}</p>
      <div class="actions">
        <button :disabled="store.loading" @click="store.register(username, password)">Create Account</button>
        <button class="secondary" :disabled="store.loading" @click="store.login(username, password)">Sign In</button>
      </div>
    </div>
  </section>
</template>
