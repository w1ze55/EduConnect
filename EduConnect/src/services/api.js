import axios from 'axios'
import { useNotificationStore } from '../stores/notifications'

const DEFAULT_API_TIMEOUT = 60000

const normalizeApiBaseUrl = (value) => {
  const rawUrl = (value || '/api').trim().replace(/\/+$/, '')

  if (rawUrl.endsWith('/api')) {
    return rawUrl
  }

  return `${rawUrl}/api`
}

const resolveApiTimeout = (value) => {
  const parsedTimeout = Number.parseInt(value, 10)

  if (Number.isFinite(parsedTimeout) && parsedTimeout > 0) {
    return parsedTimeout
  }

  return DEFAULT_API_TIMEOUT
}

const api = axios.create({
  baseURL: normalizeApiBaseUrl(import.meta.env.VITE_API_URL),
  timeout: resolveApiTimeout(import.meta.env.VITE_API_TIMEOUT),
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
    const isLoginRequest = url.includes('/auth/login')

    if (error.response) {
      if (url.includes('/documentos')) {
        return Promise.reject(error)
      }

      // A própria tela de login exibe a mensagem final para evitar duplicidade.
      if (isLoginRequest) {
        return Promise.reject(error)
      }

      if (!token && (error.response.status === 401 || error.response.status === 403)) {
        return Promise.reject(error)
      }

      if (url.includes('/dashboard') && (error.response.status === 401 || error.response.status === 403)) {
        return Promise.reject(error)
      }

      switch (error.response.status) {
        case 401:
          notificationStore.error('Sessão expirada. Faça login novamente.')
          sessionStorage.removeItem('token')
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
      if (isLoginRequest) {
        return Promise.reject(error)
      }

      notificationStore.error('Erro de conexão. Verifique sua internet.')
    }

    return Promise.reject(error)
  }
)

export default api
