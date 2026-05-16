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
      <h1>创建账号</h1>
      <div class="field">
        <label>账号名称</label>
        <input v-model="username" autocomplete="username" maxlength="40" placeholder="支持中文，最多 40 个字符" />
      </div>
      <div class="field">
        <label>密码</label>
        <input v-model="password" autocomplete="current-password" type="password" />
      </div>
      <p v-if="store.error" class="error">{{ store.error }}</p>
      <div class="actions">
        <button :disabled="store.loading" @click="store.register(username, password)">创建账号</button>
        <button class="secondary" :disabled="store.loading" @click="store.login(username, password, true)">已有账号登录</button>
      </div>
    </div>
  </section>
</template>
