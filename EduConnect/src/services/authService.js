import api from './api'

export default {
  login(credentials) {
    return api.post('/auth/login', credentials)
  },
  
  register(userData) {
    return api.post('/auth/register', userData)
  },
  
  logout() {
    return api.post('/auth/logout')
  },
  
  getCurrentUser() {
    return api.get('/auth/me')
  },
  
  updateProfile(data) {
    return api.put('/auth/profile', data)
  }
}

