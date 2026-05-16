<script setup lang="ts">
defineProps<{ message: { from: 'user' | 'agent' | 'system'; text: string } }>()
</script>

<template>
  <div class="bubble" :class="message.from">{{ message.text }}</div>
</template>
