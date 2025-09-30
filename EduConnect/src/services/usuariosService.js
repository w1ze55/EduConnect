import api from './api'

export default {
  getUsuarios(params = {}) {
    return api.get('/usuarios', { params })
  },
  
  getUsuarioById(id) {
    return api.get(`/usuarios/${id}`)
  },
  
  criarUsuario(data) {
    return api.post('/usuarios', data)
  },
  
  atualizarUsuario(id, data) {
    return api.put(`/usuarios/${id}`, data)
  },
  
  deletarUsuario(id) {
    return api.delete(`/usuarios/${id}`)
  },
  
  ativarDesativarUsuario(id) {
    return api.patch(`/usuarios/${id}/toggle-status`)
  }
}

