import api from './api'

export default {
  getMensalidades(params = {}) {
    return api.get('/financeiro/mensalidades', { params })
  },
  
  getMensalidadeById(id) {
    return api.get(`/financeiro/mensalidades/${id}`)
  },
  
  gerarBoleto(id) {
    return api.post(`/financeiro/mensalidades/${id}/boleto`)
  },
  
  registrarPagamento(id, data) {
    return api.post(`/financeiro/mensalidades/${id}/pagamento`, data)
  },
  
  getHistoricoPagamentos(params = {}) {
    return api.get('/financeiro/historico', { params })
  }
}

