import { defineStore } from 'pinia'

let notificationId = 0

export const useNotificationStore = defineStore('notifications', {
  state: () => ({
    notifications: []
  }),
  
  actions: {
    addNotification(notification, type = 'info') {
      const normalized = typeof notification === 'string'
        ? { type, message: notification }
        : { ...notification, type: notification?.type || type, message: notification?.message || '' }
      const message = String(normalized.message || '').trim()

      if (!message) return null

      const duplicated = this.notifications.some((item) =>
        item.type === normalized.type && item.message === message
      )

      if (duplicated) return null

      const id = `${Date.now()}-${notificationId++}`
      this.notifications.push({
        id,
        ...normalized,
        message,
        timestamp: new Date()
      })
      
      // Auto remover após 5 segundos
      setTimeout(() => {
        this.removeNotification(id)
      }, 5000)

      return id
    },
    
    removeNotification(id) {
      const index = this.notifications.findIndex(n => n.id === id)
      if (index > -1) {
        this.notifications.splice(index, 1)
      }
    },
    
    success(message) {
      this.addNotification({ type: 'success', message })
    },
    
    error(message) {
      this.addNotification({ type: 'error', message })
    },
    
    info(message) {
      this.addNotification({ type: 'info', message })
    },
    
    warning(message) {
      this.addNotification({ type: 'warning', message })
    }
  }
})

