import api from './api'

export default {
  async listar() {
    const response = await api.get('/usuarios')
    return response.data
  },

  async getAllUsuarios() {
    const response = await api.get('/usuarios')
    return response.data
  },
  
  async getUsuarioById(id) {
    const response = await api.get(`/usuarios/${id}`)
    return response.data
  },
  
  async getAlunos() {
    const response = await api.get('/usuarios/alunos')
    return response.data
  },

  async criar(usuarioData) {
    const response = await api.post('/usuarios', usuarioData)
    return response.data
  },
  
  async atualizar(id, data) {
    const response = await api.put(`/usuarios/${id}`, data)
    return response.data
  },

  async atualizarUsuario(id, data) {
    const response = await api.put(`/usuarios/${id}`, data)
    return response.data
  },
  
  async deletar(id) {
    const response = await api.delete(`/usuarios/${id}`)
    return response.data
  },

  async deletarUsuario(id) {
    const response = await api.delete(`/usuarios/${id}`)
    return response.data
  },
  
  async atualizarPerfil(data) {
    const response = await api.put('/usuarios/perfil', data)
    return response.data
  },

  async listarPorRole(role) {
    const response = await api.get(`/usuarios?role=${role}`)
    return response.data
  },

  async listarPorEscola(escolaId) {
    const response = await api.get(`/usuarios?escolaId=${escolaId}`)
    return response.data
  }
}
