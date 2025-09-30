import api from './api'

export default {
  getEventos(params = {}) {
    return api.get('/calendario/eventos', { params })
  },
  
  getEventoById(id) {
    return api.get(`/calendario/eventos/${id}`)
  },
  
  criarEvento(data) {
    return api.post('/calendario/eventos', data)
  },
  
  atualizarEvento(id, data) {
    return api.put(`/calendario/eventos/${id}`, data)
  },
  
  deletarEvento(id) {
    return api.delete(`/calendario/eventos/${id}`)
  }
}

