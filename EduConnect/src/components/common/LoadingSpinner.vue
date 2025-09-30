<template>
  <div class="text-center py-5">
    <div class="spinner-border text-primary" role="status" :style="{ width: size, height: size }">
      <span class="visually-hidden">Carregando...</span>
    </div>
    <p v-if="message" class="mt-3 text-muted">{{ message }}</p>
  </div>
</template>

<script setup>
import { defineProps } from 'vue'

const props = defineProps({
  size: {
    type: String,
    default: '3rem'
  },
  message: {
    type: String,
    default: ''
  }
})
</script>

