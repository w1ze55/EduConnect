import api from './api'

export default {
  listar() {
    return api.get('/notificacoes')
  },

  alterarLeitura(id, lida) {
    return api.patch(`/notificacoes/${id}/leitura`, { lida })
  },

  marcarTodasComoLidas() {
    return api.patch('/notificacoes/marcar-todas-lidas')
  },

  limpar() {
    return api.delete('/notificacoes')
  }
}
