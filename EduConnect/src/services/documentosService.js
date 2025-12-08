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
  
  assinarDocumento(id, dadosAssinatura) {
    return api.post(`/documentos/${id}/assinar`, dadosAssinatura)
  },
  
  downloadDocumento(id) {
    return api.get(`/documentos/${id}/download`, {
      responseType: 'blob'
    })
  },
  
  downloadAnexo(documentoId, indice) {
    return api.get(`/documentos/${documentoId}/anexos/${indice}/download`, {
      responseType: 'blob'
    })
  },
  
  deletarDocumento(id) {
    return api.delete(`/documentos/${id}`)
  }
}

