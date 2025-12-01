import api from './api'

export default {
  async getResumo(filtros = {}) {
    const response = await api.get('/dashboard', { params: filtros })
    return response.data
  }
}
