import api from './api'

export default {
  getDocumentos(params = {}) {
    return api.get('/documentos', { params })
  },
  
  getDocumentoById(id) {
    return api.get(`/documentos/${id}`)
  },
  
  uploadDocumento(formData) {
    return api.post('/documentos', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },
  
  assinarDocumento(id, assinatura) {
    return api.post(`/documentos/${id}/assinar`, { assinatura })
  },
  
  downloadDocumento(id) {
    return api.get(`/documentos/${id}/download`, {
      responseType: 'blob'
    })
  }
}

