import axios from 'axios'
import { useNotificationStore } from '../stores/notifications'

// Configuração base do Axios
const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// Interceptor de request
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Interceptor de response
api.interceptors.response.use(
  (response) => {
    return response
  },
  (error) => {
    const notificationStore = useNotificationStore()
    
    if (error.response) {
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

