<script setup lang="ts">
import { useAppStore } from '../stores/app'

const store = useAppStore()

function remove(uid: number) {
  if (confirm('确认删除这个角色？')) {
    store.deletePlayer(uid)
  }
}
</script>

<template>
  <section class="page">
    <header class="topbar">
      <h1>选择角色</h1>
      <div class="row">
        <button :disabled="store.players.length >= 6" @click="store.view = 'create-player'">创建角色</button>
        <button class="secondary" @click="store.settingsOpen = true">设置</button>
      </div>
    </header>
    <div class="grid">
      <article v-for="player in store.players" :key="player.uid" class="card">
        <div>
          <h2>{{ player.player_name }}</h2>
          <p>Lv.{{ player.level }} / EXP {{ player.experience }}</p>
        </div>
        <div class="row">
          <button @click="store.selectPlayer(player.uid)">选择</button>
          <button class="danger" @click="remove(player.uid)">删除</button>
        </div>
      </article>
      <article v-if="store.players.length === 0" class="empty">
        <p>当前账号还没有角色。</p>
        <button @click="store.view = 'create-player'">创建第一个角色</button>
      </article>
    </div>

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
