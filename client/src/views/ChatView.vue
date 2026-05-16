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
        <h1>智能体</h1>
        <button title="新增" @click="store.view = 'create-agent'">+</button>
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
      <button class="secondary full" @click="store.view = 'players'">切换角色</button>
      <button class="secondary" @click="store.settingsOpen = true">设置</button>
    </aside>
    <section class="chat">
      <header class="chat-head">
        <div>
          <h2>{{ store.agents.find((item) => item.agent_id === store.activeAgentId)?.agent_name || '智能体' }}</h2>
          <p>UID {{ store.uid }}</p>
        </div>
        <div class="tabs">
          <button :class="{ active: mode === 'pronunciation' }" @click="mode = 'pronunciation'">音标</button>
          <button :class="{ active: mode === 'spelling' }" @click="mode = 'spelling'">拼写</button>
          <button :class="{ active: mode === 'usage' }" @click="mode = 'usage'">使用</button>
          <button @click="store.newWord(mode)">出题</button>
        </div>
      </header>
      <WordTestCard v-if="store.currentWord" :word="store.currentWord" :mode="mode" @submit="(answer) => store.reviewWord(answer, mode)" />
      <div class="messages">
        <ChatBubble v-for="(message, index) in store.messages" :key="index" :message="message" />
      </div>
      <footer>
        <input v-model="input" placeholder="输入消息" @keydown.enter="send" />
        <button @click="send">发送</button>
      </footer>
    </section>

    <div v-if="store.settingsOpen" class="modal-mask" @click.self="store.settingsOpen = false">
      <section class="modal">
        <header class="topbar">
          <h2>设置</h2>
          <button class="secondary" @click="store.settingsOpen = false">关闭</button>
        </header>
        <p>当前账号：{{ store.username }}</p>
        <button class="danger" @click="store.logoutAccount()">退出账号</button>
      </section>
    </div>
  </section>
</template>
