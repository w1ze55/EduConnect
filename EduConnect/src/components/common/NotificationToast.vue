<template>
  <div class="notification-container">
    <div 
      v-for="notification in notifications" 
      :key="notification.id"
      class="alert alert-dismissible fade show"
      :class="`alert-${getAlertClass(notification.type)}`"
      role="alert"
    >
      <i :class="getIcon(notification.type)" class="me-2"></i>
      {{ notification.message }}
      <button 
        type="button" 
        class="btn-close" 
        @click="removeNotification(notification.id)"
      ></button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useNotificationStore } from '../../stores/notifications'

const notificationStore = useNotificationStore()
const notifications = computed(() => notificationStore.notifications)

const getAlertClass = (type) => {
  const classes = {
    success: 'success',
    error: 'danger',
    warning: 'warning',
    info: 'info'
  }
  return classes[type] || 'info'
}

const getIcon = (type) => {
  const icons = {
    success: 'bi bi-check-circle-fill',
    error: 'bi bi-exclamation-circle-fill',
    warning: 'bi bi-exclamation-triangle-fill',
    info: 'bi bi-info-circle-fill'
  }
  return icons[type] || 'bi bi-info-circle-fill'
}

const removeNotification = (id) => {
  notificationStore.removeNotification(id)
}
</script>

<style scoped>
.notification-container {
  position: fixed;
  top: 80px;
  right: 20px;
  z-index: 9999;
  max-width: 400px;
}

.alert {
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  animation: slideIn 0.3s ease-out;
}

@keyframes slideIn {
  from {
    transform: translateX(100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}
</style>

