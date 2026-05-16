<script setup lang="ts">
defineProps<{ name: string }>()
</script>

<template>
  <div class="avatar">{{ name.slice(0, 1) }}</div>
</template>
