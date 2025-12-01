import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { useAuthStore } from './auth'
import recadosService from '../services/recadosService'
import atividadesService from '../services/atividadesService'
import dashboardService from '../services/dashboardService'
import documentosService from '../services/documentosService'

const storageKeyFor = (userId) => `educonnect-notifs-${userId || 'anon'}`

export const useUserNotificationsStore = defineStore('userNotifications', () => {
  const authStore = useAuthStore()

  const notifications = ref([])
  const filterType = ref('ALL')
  const loading = ref(false)
  const readState = ref({})

  const filteredNotifications = computed(() => {
    return notifications.value
      .filter((n) => filterType.value === 'ALL' || n.type === filterType.value)
      .sort((a, b) => new Date(b.date || 0) - new Date(a.date || 0))
  })

  const unreadCount = computed(() => notifications.value.filter((n) => !n.read).length)

  const persistRead = () => {
    try {
      localStorage.setItem(storageKeyFor(authStore.user?.id), JSON.stringify(readState.value))
    } catch (e) {
      console.warn('Não foi possível salvar estado de leitura', e)
    }
  }

  const loadRead = () => {
    try {
      const raw = localStorage.getItem(storageKeyFor(authStore.user?.id))
      readState.value = raw ? JSON.parse(raw) : {}
    } catch (e) {
      readState.value = {}
    }
  }

  const setRead = (id, value) => {
    readState.value[id] = value
    const idx = notifications.value.findIndex((n) => n.id === id)
    if (idx !== -1) notifications.value[idx].read = value
    persistRead()
  }

  const toggleRead = (id) => setRead(id, !(readState.value[id] || false))

  const markAllAsRead = () => {
    notifications.value.forEach((n) => setRead(n.id, true))
  }

  const clearNotifications = () => {
    notifications.value = []
    readState.value = {}
    persistRead()
  }

  const asDate = (value) => (value ? new Date(value) : new Date())

  const mapRecados = (recados) => recados.map((r) => {
    const id = `recado-${r.id}`
    return {
      id,
      type: 'RECADO',
      title: 'Novo recado',
      message: r.titulo,
      date: asDate(r.dataEnvio),
      link: `/recados/${r.id}`,
      read: readState.value[id] || false
    }
  })

  const mapAtividades = (atividades) => atividades.map((a) => {
    const id = `atividade-${a.id}`
    return {
      id,
      type: 'ATIVIDADE',
      title: 'Entrega de atividade',
      message: a.titulo,
      date: asDate(a.dataEntrega || a.dataCriacao),
      link: `/atividades/${a.id}`,
      read: readState.value[id] || false
    }
  })

  const mapEventos = (eventos) => eventos.map((e) => {
    const id = `evento-${e.id}`
    return {
      id,
      type: 'EVENTO',
      title: e.tipo === 'PROVA' ? 'Nova prova no calendário' : 'Evento no calendário',
      message: e.titulo,
      date: asDate(e.dataInicio),
      link: '/calendario',
      read: readState.value[id] || false
    }
  })

  const mapDocumentos = (docs) => docs.map((d) => {
    const id = `documento-${d.id}`
    return {
      id,
      type: 'DOCUMENTO',
      title: 'Documento recebido',
      message: d.nome || d.titulo || 'Novo documento',
      date: asDate(d.data || d.dataEnvio || d.createdAt),
      link: '/documentos',
      read: readState.value[id] || false
    }
  })

  const loadNotifications = async () => {
    loading.value = true
    loadRead()
    const aggregated = []

    try {
      const recadosResp = await recadosService.getRecados()
      if (recadosResp?.data) aggregated.push(...mapRecados(recadosResp.data))
    } catch (e) {
      console.warn('Erro ao carregar recados para notificações', e)
    }

    try {
      const atividades = await atividadesService.listar()
      if (atividades) aggregated.push(...mapAtividades(atividades))
    } catch (e) {
      console.warn('Erro ao carregar atividades para notificações', e)
    }

    try {
      const dashboard = await dashboardService.getResumo()
      if (dashboard?.proximosEventos) aggregated.push(...mapEventos(dashboard.proximosEventos))
    } catch (e) {
      console.warn('Erro ao carregar eventos para notificações', e)
    }

    try {
      const docs = await documentosService.getDocumentos()
      const lista = docs?.data || docs
      if (Array.isArray(lista)) aggregated.push(...mapDocumentos(lista))
    } catch (e) {
      console.warn('Erro ao carregar documentos para notificações', e)
    }

    notifications.value = aggregated
      .sort((a, b) => new Date(b.date || 0) - new Date(a.date || 0))
      .map((n) => ({ ...n, read: readState.value[n.id] || false }))

    loading.value = false
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
    setRead
  }
})
