import api from './api'

export default {
  getRecados(params = {}) {
    return api.get('/recados', { params })
  },
  
  getRecadoById(id) {
    return api.get(`/recados/${id}`)
  },
  
  enviarRecado(data) {
    return api.post('/recados', data)
  },
  
  atualizarRecado(id, data) {
    return api.put(`/recados/${id}`, data)
  },
  
  deletarRecado(id) {
    return api.delete(`/recados/${id}`)
  },
  
  confirmarLeitura(id) {
    return api.post(`/recados/${id}/confirmar-leitura`)
  },
  
  uploadAnexo(formData) {
    return api.post('/recados/anexo', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  }
}

