import api from './api'

export default {
  getAtividades(params = {}) {
    return api.get('/atividades', { params })
  },
  
  getAtividadeById(id) {
    return api.get(`/atividades/${id}`)
  },
  
  criarAtividade(data) {
    return api.post('/atividades', data)
  },
  
  enviarResposta(id, formData) {
    return api.post(`/atividades/${id}/resposta`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },
  
  avaliarResposta(id, data) {
    return api.put(`/atividades/${id}/avaliar`, data)
  }
}

