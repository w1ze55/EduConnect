import api from './api'

export default {
  getAllUsuarios() {
    return api.get('/usuarios')
  },
  
  getUsuarioById(id) {
    return api.get(`/usuarios/${id}`)
  },
  
  getAlunos() {
    return api.get('/usuarios/alunos')
  },
  
  atualizarUsuario(id, data) {
    return api.put(`/usuarios/${id}`, data)
  },
  
  deletarUsuario(id) {
    return api.delete(`/usuarios/${id}`)
  },
  
  atualizarPerfil(data) {
    return api.put('/usuarios/perfil', data)
  }
}
