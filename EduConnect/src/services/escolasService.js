import api from './api'

export default {
  // Listar todas as escolas
  async listarTodas() {
    const response = await api.get('/escolas')
    return response.data
  },

  // Listar apenas escolas ativas
  async listarAtivas() {
    const response = await api.get('/escolas/ativas')
    return response.data
  },

  // Buscar escola por ID
  async buscarPorId(id) {
    const response = await api.get(`/escolas/${id}`)
    return response.data
  },

  // Criar nova escola
  async criar(escolaData) {
    const response = await api.post('/escolas', escolaData)
    return response.data
  },

  // Atualizar escola
  async atualizar(id, escolaData) {
    const response = await api.put(`/escolas/${id}`, escolaData)
    return response.data
  },

  // Deletar escola
  async deletar(id) {
    const response = await api.delete(`/escolas/${id}`)
    return response.data
  },

  // Atribuir diretor à escola
  async atribuirDiretor(escolaId, diretorId) {
    const response = await api.post('/escolas/atribuir-diretor', {
      escolaId,
      diretorId
    })
    return response.data
  },

  // Remover diretor da escola
  async removerDiretor(escolaId) {
    const response = await api.delete(`/escolas/${escolaId}/remover-diretor`)
    return response.data
  }
}
