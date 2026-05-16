<script setup lang="ts">
import { useAppStore } from '../stores/app'

const store = useAppStore()

function remove(uid: number) {
  if (confirm('Delete this character?')) {
    store.deletePlayer(uid)
  }
}
</script>

<template>
  <section class="page">
    <header class="topbar">
      <h1>Select Character</h1>
      <div class="row">
        <button :disabled="store.players.length >= 6" @click="store.view = 'create-player'">Create Character</button>
        <button class="secondary" @click="store.settingsOpen = true">Settings</button>
      </div>
    </header>
    <div class="grid">
      <article v-for="player in store.players" :key="player.uid" class="card">
        <div>
          <h2>{{ player.player_name }}</h2>
          <p>Lv.{{ player.level }} / EXP {{ player.experience }}</p>
        </div>
        <div class="row">
          <button @click="store.selectPlayer(player.uid)">Select</button>
          <button class="danger" @click="remove(player.uid)">Delete</button>
        </div>
      </article>
      <article v-if="store.players.length === 0" class="empty">
        <p>No characters exist for this account yet.</p>
        <button @click="store.view = 'create-player'">Create First Character</button>
      </article>
    </div>

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
