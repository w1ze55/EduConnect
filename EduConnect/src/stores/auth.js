import { defineStore } from 'pinia'
import api from '../services/api'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: sessionStorage.getItem('token') || null,
    user: null,
    loading: false
  }),

  getters: {
    isAuthenticated: (state) => !!state.token,
    userRole: (state) => state.user?.role || null,
    userName: (state) => state.user?.nome || '',
    userEmail: (state) => state.user?.email || ''
  },

  actions: {
    async login(credentials) {
      try {
        console.log('[AUTH] Tentando login com:', credentials.email)
        const response = await api.post('/auth/login', credentials)

        console.log('[AUTH] Resposta do login:', response.data)

        if (!response.data.token) {
          console.error('[AUTH] Token não retornado!')
          return { success: false, message: 'Token não recebido' }
        }

        this.setToken(response.data.token)

        console.log('[AUTH] Buscando dados do usuário do backend...')
        await this.fetchCurrentUser()

        console.log('[AUTH] Login completo!')
        console.log('[AUTH] User:', this.user)
        console.log('[AUTH] UserRole:', this.userRole)

        return { success: true }
      } catch (error) {
        console.error('[AUTH] Erro no login:', error)
        console.error('[AUTH] Resposta:', error.response?.data)
        this.token = null
        this.user = null

        const message = error.response?.data?.message
          || (error.request ? 'Não foi possível conectar ao servidor.' : null)
          || 'Erro ao fazer login'

        return {
          success: false,
          message
        }
      }
    },

    async fetchCurrentUser() {
      try {
        console.log('[AUTH] GET /auth/me')
        console.log('[AUTH] Token atual:', this.token ? 'Existe' : 'Não existe')
        console.log('[AUTH] Header Authorization:', api.defaults.headers.common.Authorization)

        const response = await api.get('/auth/me')

        console.log('[AUTH] Dados do usuário recebidos:', response.data)
        this.user = response.data

        return response.data
      } catch (error) {
        console.error('[AUTH] Erro ao buscar usuário:', error)
        console.error('[AUTH] Status:', error.response?.status)
        console.error('[AUTH] Dados:', error.response?.data)
        console.error('[AUTH] Fazendo logout devido ao erro')
        this.logout()
        throw error
      }
    },

    logout() {
      console.log('[AUTH] Fazendo logout')
      this.token = null
      this.user = null
      sessionStorage.removeItem('token')
      delete api.defaults.headers.common.Authorization
    },

    setToken(token) {
      console.log('[AUTH] Salvando token')
      this.token = token
      sessionStorage.setItem('token', token)
      api.defaults.headers.common.Authorization = `Bearer ${token}`
    },

    async initializeAuth() {
      console.log('[AUTH] Inicializando autenticação...')
      const token = sessionStorage.getItem('token')

      if (token) {
        console.log('[AUTH] Token encontrado, configurando...')
        this.token = token
        api.defaults.headers.common.Authorization = `Bearer ${token}`

        try {
          console.log('[AUTH] Buscando usuário do backend...')
          await this.fetchCurrentUser()
          console.log('[AUTH] Usuário carregado:', this.user)
        } catch (error) {
          console.error('[AUTH] Erro ao carregar usuário, fazendo logout')
          this.logout()
        }
      } else {
        console.log('[AUTH] Nenhum token encontrado')
      }
    }
  }
})
