import axios from 'axios'
import { useNotificationStore } from '../stores/notifications'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api',
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' }
})

api.interceptors.request.use(
  (config) => {
    const token = sessionStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const notificationStore = useNotificationStore()
    const token = sessionStorage.getItem('token')
    const url = error.config?.url || ''

    if (error.response) {
      // Silenciar toasts para chamadas opcionais de documentos
      if (url.includes('/documentos')) {
        return Promise.reject(error)
      }

      // Se não há token (usuário deslogado), ignore toasts de auth
      if (!token && (error.response.status === 401 || error.response.status === 403)) {
        return Promise.reject(error)
      }

       // Evitar toast para dashboard ao sair/logar
      if (url.includes('/dashboard') && (error.response.status === 401 || error.response.status === 403)) {
        return Promise.reject(error)
      }

      switch (error.response.status) {
        case 401:
          notificationStore.error('Sessão expirada. Faça login novamente.')
          localStorage.removeItem('token')
          localStorage.removeItem('user')
          window.location.href = '/login'
          break
        case 403:
          notificationStore.error('Você não tem permissão para acessar este recurso.')
          break
        case 404:
          notificationStore.error('Recurso não encontrado.')
          break
        case 500:
          notificationStore.error('Erro interno do servidor.')
          break
        default:
          notificationStore.error(error.response.data?.message || 'Erro na requisição.')
      }
    } else if (error.request) {
      notificationStore.error('Erro de conexão. Verifique sua internet.')
    }

    return Promise.reject(error)
  }
)

export default api
