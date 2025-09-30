import { defineStore } from 'pinia'

export const useNotificationStore = defineStore('notifications', {
  state: () => ({
    notifications: []
  }),
  
  actions: {
    addNotification(notification) {
      const id = Date.now()
      this.notifications.push({
        id,
        ...notification,
        timestamp: new Date()
      })
      
      // Auto remover após 5 segundos
      setTimeout(() => {
        this.removeNotification(id)
      }, 5000)
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

