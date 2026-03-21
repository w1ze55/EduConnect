import api from './api'

export default {
  getStatus() {
    return api.get('/google-calendar/status')
  },

  connect() {
    return api.post('/google-calendar/connect')
  },

  disconnect() {
    return api.post('/google-calendar/disconnect')
  },

  sync() {
    return api.post('/google-calendar/sync')
  }
}
