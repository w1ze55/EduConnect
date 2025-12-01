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
    // O backend retorna uma lista diretamente, não precisa de .data
    return Array.isArray(response.data) ? response.data : []
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
    // Usar endpoints específicos quando disponíveis
    switch (role) {
      case 'ALUNO':
        return await this.getAlunos()
      case 'PROFESSOR':
        const responseProf = await api.get('/usuarios/professores')
        return Array.isArray(responseProf.data) ? responseProf.data : []
      case 'RESPONSAVEL':
        const responseResp = await api.get('/usuarios/responsaveis-disponiveis')
        return Array.isArray(responseResp.data) ? responseResp.data : []
      case 'DIRETORIA':
        const responseDir = await api.get('/usuarios/diretores-disponiveis')
        return Array.isArray(responseDir.data) ? responseDir.data : []
      default:
        const response = await api.get(`/usuarios`)
        return Array.isArray(response.data) ? response.data.filter(u => u.role === role) : []
    }
  },

  async listarPorEscola(escolaId) {
    const response = await api.get(`/usuarios?escolaId=${escolaId}`)
    return response.data
  }
}
