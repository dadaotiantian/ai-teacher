<script setup lang="ts">
import { ref } from 'vue'
import { useAppStore } from '../stores/app'
import ChatBubble from '../components/ChatBubble.vue'
import WordTestCard from '../components/WordTestCard.vue'

const store = useAppStore()
const input = ref('')
const mode = ref('spelling')

function send() {
  const text = input.value
  input.value = ''
  store.sendChat(text)
}
</script>

<template>
  <section class="workspace">
    <aside>
      <div class="side-head">
        <h1>Agents</h1>
        <button title="Create agent" @click="store.view = 'create-agent'">+</button>
      </div>
      <button
        v-for="agent in store.agents"
        :key="agent.agent_id"
        class="agent-item"
        :class="{ active: store.activeAgentId === agent.agent_id }"
        @click="store.activeAgentId = agent.agent_id"
      >
        {{ agent.agent_name }}
      </button>
      <button class="secondary full" @click="store.view = 'players'">Switch Character</button>
      <button class="secondary" @click="store.settingsOpen = true">Settings</button>
    </aside>

    <section class="chat">
      <header class="chat-head">
        <div>
          <h2>{{ store.agents.find((item) => item.agent_id === store.activeAgentId)?.agent_name || 'Agent' }}</h2>
          <p>UID {{ store.uid }}</p>
        </div>
        <div class="tabs">
          <button :class="{ active: mode === 'pronunciation' }" @click="mode = 'pronunciation'">Pronunciation</button>
          <button :class="{ active: mode === 'spelling' }" @click="mode = 'spelling'">Spelling</button>
          <button :class="{ active: mode === 'usage' }" @click="mode = 'usage'">Usage</button>
          <button @click="store.newWord(mode)">New Word</button>
        </div>
      </header>

      <WordTestCard
        v-if="store.currentWord"
        :word="store.currentWord"
        :mode="mode"
        @submit="(answer) => store.reviewWord(answer, mode)"
      />

      <div class="messages">
        <ChatBubble v-for="(message, index) in store.messages" :key="index" :message="message" />
      </div>

      <footer>
        <input v-model="input" placeholder="Type a message" @keydown.enter="send" />
        <button @click="send">Send</button>
      </footer>
    </section>

    <div v-if="store.settingsOpen" class="modal-mask" @click.self="store.settingsOpen = false">
      <section class="modal">
        <header class="topbar">
          <h2>Settings</h2>
          <button class="secondary" @click="store.settingsOpen = false">Close</button>
        </header>
        <p>Current account: {{ store.username }}</p>
        <button class="danger" @click="store.logoutAccount()">Sign Out</button>
      </section>
    </div>
  </section>
</template>
