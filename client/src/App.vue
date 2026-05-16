<script setup lang="ts">
import { onMounted } from 'vue'
import { useAppStore } from './stores/app'
import LoginView from './views/LoginView.vue'
import SelectCharacterView from './views/SelectCharacterView.vue'
import CreateCharacterView from './views/CreateCharacterView.vue'
import CreateAgentView from './views/CreateAgentView.vue'
import ChatView from './views/ChatView.vue'

const store = useAppStore()

onMounted(() => {
  store.start()
})
</script>

<template>
  <main>
    <section v-if="store.initializing" class="auth-shell">
      <div class="auth-panel">
        <h1>Auto Login</h1>
        <p>Connecting to the server and restoring your last account.</p>
      </div>
    </section>
    <LoginView v-else-if="store.view === 'login'" />
    <SelectCharacterView v-else-if="store.view === 'players'" />
    <CreateCharacterView v-else-if="store.view === 'create-player'" />
    <CreateAgentView v-else-if="store.view === 'create-agent'" />
    <ChatView v-else />
  </main>
</template>
