import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { useAuthStore } from './auth'
import api from '../services/api'
import notificacoesService from '../services/notificacoesService'

const normalizeNotification = (notification) => {
  const date = notification.dataCriacao || notification.date || new Date().toISOString()
  return {
    id: notification.id,
    type: notification.tipo || notification.type,
    title: notification.titulo || notification.title,
    message: notification.mensagem || notification.message,
    date,
    timestamp: new Date(date).getTime() || 0,
    link: notification.link,
    read: Boolean(notification.lida ?? notification.read),
    referenceType: notification.referenciaTipo,
    referenceId: notification.referenciaId
  }
}

const streamUrl = () => {
  const baseUrl = api.defaults.baseURL || '/api'
  return `${baseUrl.replace(/\/$/, '')}/notificacoes/stream`
}

export const useUserNotificationsStore = defineStore('userNotifications', () => {
  const authStore = useAuthStore()

  const notifications = ref([])
  const filterType = ref('ALL')
  const loading = ref(false)
  const streamController = ref(null)

  let reconnectTimer = null
  let reconnectAttempts = 0

  const filteredNotifications = computed(() => {
    return notifications.value
      .filter((n) => filterType.value === 'ALL' || n.type === filterType.value)
      .sort((a, b) => (b.timestamp || 0) - (a.timestamp || 0))
  })

  const unreadCount = computed(() => notifications.value.filter((n) => !n.read).length)

  const sortNotifications = () => {
    notifications.value.sort((a, b) => (b.timestamp || 0) - (a.timestamp || 0))
  }

  const upsertNotification = (rawNotification) => {
    const notification = normalizeNotification(rawNotification)
    const index = notifications.value.findIndex((n) => n.id === notification.id)

    if (index === -1) {
      notifications.value.unshift(notification)
    } else {
      notifications.value[index] = notification
    }

    sortNotifications()
  }

  const loadNotifications = async () => {
    if (!authStore.isAuthenticated) {
      notifications.value = []
      return
    }

    loading.value = true
    try {
      const response = await notificacoesService.listar()
      notifications.value = (response.data || []).map(normalizeNotification)
      sortNotifications()
    } finally {
      loading.value = false
    }
  }

  const setRead = async (id, value) => {
    const index = notifications.value.findIndex((n) => n.id === id)
    if (index === -1) return

    const previous = notifications.value[index].read
    notifications.value[index].read = value

    try {
      const response = await notificacoesService.alterarLeitura(id, value)
      notifications.value[index] = normalizeNotification(response.data)
      sortNotifications()
    } catch (error) {
      notifications.value[index].read = previous
      throw error
    }
  }

  const toggleRead = async (id) => {
    const notification = notifications.value.find((n) => n.id === id)
    if (!notification) return
    await setRead(id, !notification.read)
  }

  const markAllAsRead = async () => {
    const response = await notificacoesService.marcarTodasComoLidas()
    notifications.value = (response.data || []).map(normalizeNotification)
    sortNotifications()
  }

  const clearNotifications = async () => {
    await notificacoesService.limpar()
    notifications.value = []
  }

  const handleSseMessage = (rawEvent) => {
    const lines = rawEvent.split(/\r?\n/)
    let eventName = 'message'
    const dataLines = []

    for (const line of lines) {
      if (line.startsWith('event:')) {
        eventName = line.slice(6).trim()
      } else if (line.startsWith('data:')) {
        dataLines.push(line.slice(5).trimStart())
      }
    }

    if (eventName !== 'notificacao' || dataLines.length === 0) {
      return
    }

    try {
      upsertNotification(JSON.parse(dataLines.join('\n')))
    } catch (error) {
      console.warn('Nao foi possivel processar notificacao em tempo real', error)
    }
  }

  const consumeStream = async (response) => {
    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''

    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      buffer += decoder.decode(value, { stream: true })
      const events = buffer.split(/\r?\n\r?\n/)
      buffer = events.pop() || ''
      events.forEach(handleSseMessage)
    }
  }

  const scheduleReconnect = () => {
    if (reconnectTimer || !authStore.isAuthenticated) {
      return
    }

    const delay = Math.min(30000, 1000 * (2 ** reconnectAttempts))
    reconnectAttempts += 1
    reconnectTimer = window.setTimeout(() => {
      reconnectTimer = null
      startStream()
    }, delay)
  }

  const startStream = async () => {
    if (streamController.value || !authStore.token) {
      return
    }

    const controller = new AbortController()
    streamController.value = controller

    try {
      const response = await fetch(streamUrl(), {
        method: 'GET',
        headers: {
          Authorization: `Bearer ${authStore.token}`,
          Accept: 'text/event-stream'
        },
        cache: 'no-store',
        signal: controller.signal
      })

      if (!response.ok || !response.body) {
        throw new Error(`SSE indisponivel: ${response.status}`)
      }

      reconnectAttempts = 0
      await consumeStream(response)
    } catch (error) {
      if (!controller.signal.aborted) {
        console.warn('Stream de notificacoes desconectado', error)
      }
    } finally {
      if (streamController.value === controller) {
        streamController.value = null
      }
      if (!controller.signal.aborted) {
        scheduleReconnect()
      }
    }
  }

  const stopStream = () => {
    if (reconnectTimer) {
      window.clearTimeout(reconnectTimer)
      reconnectTimer = null
    }
    reconnectAttempts = 0

    if (streamController.value) {
      streamController.value.abort()
      streamController.value = null
    }
  }

  return {
    notifications,
    filteredNotifications,
    filterType,
    loading,
    unreadCount,
    loadNotifications,
    markAllAsRead,
    clearNotifications,
    toggleRead,
    setRead,
    startStream,
    stopStream
  }
})
