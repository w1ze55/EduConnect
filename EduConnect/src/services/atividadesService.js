import api from './api'

export default {
  async listar() {
    const response = await api.get('/atividades')
    return response.data
  },
  
  async buscarPorId(id) {
    const response = await api.get(`/atividades/${id}`)
    return response.data
  },
  
  async criar(data) {
    const response = await api.post('/atividades', data)
    return response.data
  },
  
  async atualizar(id, data) {
    const response = await api.put(`/atividades/${id}`, data)
    return response.data
  },
  
  async deletar(id) {
    await api.delete(`/atividades/${id}`)
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

