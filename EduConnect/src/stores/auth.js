import { defineStore } from 'pinia'
import { jwtDecode } from 'jwt-decode'
import api from '../services/api'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || null,
    user: null
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
        console.log('🔍 [AUTH] Tentando login com:', credentials.email)
        const response = await api.post('/auth/login', credentials)
        
        console.log('✅ [AUTH] Resposta do backend:', response.data)
        console.log('🔑 [AUTH] Token recebido:', response.data.token ? 'Sim' : 'Não')
        console.log('👤 [AUTH] Usuario recebido:', response.data.usuario)
        
        if (!response.data.token) {
          console.error('❌ [AUTH] Token não retornado pelo backend!')
          return { success: false, message: 'Token não recebido' }
        }
        
        if (!response.data.usuario) {
          console.error('❌ [AUTH] Usuario não retornado pelo backend!')
          return { success: false, message: 'Dados do usuário não recebidos' }
        }
        
        this.setToken(response.data.token)
        this.setUser(response.data.usuario)
        
        console.log('✅ [AUTH] Login bem-sucedido!')
        console.log('✅ [AUTH] User no store:', this.user)
        console.log('✅ [AUTH] UserRole:', this.userRole)
        
        return { success: true }
      } catch (error) {
        console.error('❌ [AUTH] Erro no login:', error)
        console.error('❌ [AUTH] Resposta de erro:', error.response?.data)
        return { 
          success: false, 
          message: error.response?.data?.message || 'Erro ao fazer login' 
        }
      }
    },
    
    async register(userData) {
      try {
        const response = await api.post('/auth/register', userData)
        // Backend retorna apenas UsuarioDTO, não retorna token automaticamente
        // Então após registrar, fazemos login
        if (response.data) {
          // Fazer login após registro bem-sucedido
          const loginResult = await this.login({
            email: userData.email,
            password: userData.password
          })
          return loginResult
        }
        return { success: true }
      } catch (error) {
        return { 
          success: false, 
          message: error.response?.data?.message || 'Erro ao fazer cadastro' 
        }
      }
    },
    
    logout() {
      this.token = null
      this.user = null
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    },
    
    setToken(token) {
      this.token = token
      localStorage.setItem('token', token)
      
      // Configurar token no api
      api.defaults.headers.common['Authorization'] = `Bearer ${token}`
    },
    
    setUser(user) {
      console.log('📝 [AUTH] Salvando user:', user)
      this.user = user
      localStorage.setItem('user', JSON.stringify(user))
      console.log('✅ [AUTH] User salvo no localStorage e store')
      console.log('✅ [AUTH] User.role:', user?.role)
    },
    
    initializeAuth() {
      console.log('🔄 [AUTH] Inicializando autenticação...')
      const token = localStorage.getItem('token')
      const user = localStorage.getItem('user')
      
      console.log('🔍 [AUTH] Token no localStorage:', token ? 'Existe' : 'Não existe')
      console.log('🔍 [AUTH] User no localStorage:', user ? 'Existe' : 'Não existe')
      
      if (token) {
        this.token = token
        api.defaults.headers.common['Authorization'] = `Bearer ${token}`
        
        if (user) {
          try {
            this.user = JSON.parse(user)
            console.log('✅ [AUTH] User carregado do localStorage:', this.user)
            console.log('✅ [AUTH] UserRole:', this.user?.role)
          } catch (error) {
            console.error('❌ [AUTH] Erro ao carregar usuário:', error)
            console.error('❌ [AUTH] User string:', user)
          }
        } else {
          console.warn('⚠️ [AUTH] Token existe mas user não!')
        }
      } else {
        console.log('ℹ️ [AUTH] Usuário não autenticado')
      }
    }
  }
})

