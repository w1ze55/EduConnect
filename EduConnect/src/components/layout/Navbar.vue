<template>
  <nav class="navbar navbar-expand-lg navbar-light bg-white shadow-sm">
    <div class="container-fluid">
      <router-link to="/dashboard" class="navbar-brand d-flex align-items-center">
        <i class="bi bi-mortarboard-fill text-primary fs-3 me-2"></i>
        <span class="fw-bold text-primary">EduConnect</span>
      </router-link>
      
      <button 
        class="navbar-toggler" 
        type="button" 
        data-bs-toggle="collapse" 
        data-bs-target="#navbarContent"
      >
        <span class="navbar-toggler-icon"></span>
      </button>
      
      <div class="collapse navbar-collapse" id="navbarContent">
        <ul class="navbar-nav ms-auto align-items-center">
          <!-- Notificações -->
          <li class="nav-item dropdown me-3">
            <a 
              class="nav-link position-relative" 
              href="#" 
              data-bs-toggle="dropdown"
            >
              <i class="bi bi-bell fs-5"></i>
              <span 
                v-if="unreadNotifications > 0" 
                class="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger"
              >
                {{ unreadNotifications }}
              </span>
            </a>
            <ul class="dropdown-menu dropdown-menu-end notification-dropdown">
              <li class="dropdown-header">Notificações</li>
              <li v-if="notifications.length === 0">
                <span class="dropdown-item-text text-muted">
                  Nenhuma notificação
                </span>
              </li>
              <li v-for="notif in notifications" :key="notif.id">
                <a class="dropdown-item" href="#">
                  <small class="text-muted">{{ notif.time }}</small>
                  <p class="mb-0">{{ notif.message }}</p>
                </a>
              </li>
            </ul>
          </li>
          
          <!-- Perfil do usuário -->
          <li class="nav-item dropdown">
            <a 
              class="nav-link dropdown-toggle d-flex align-items-center" 
              href="#" 
              data-bs-toggle="dropdown"
            >
              <div class="user-avatar me-2">
                {{ userInitials }}
              </div>
              <span>{{ userName }}</span>
            </a>
            <ul class="dropdown-menu dropdown-menu-end">
              <li>
                <router-link to="/perfil" class="dropdown-item">
                  <i class="bi bi-person me-2"></i>Meu Perfil
                </router-link>
              </li>
              <li><hr class="dropdown-divider"></li>
              <li>
                <a class="dropdown-item text-danger" href="#" @click.prevent="handleLogout">
                  <i class="bi bi-box-arrow-right me-2"></i>Sair
                </a>
              </li>
            </ul>
          </li>
        </ul>
      </div>
    </div>
  </nav>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const userName = computed(() => authStore.userName || 'Usuário')
const userInitials = computed(() => {
  const name = authStore.userName || 'U'
  return name.split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase()
})

// Mock de notificações - substituir por dados reais da API
const notifications = [
  { id: 1, message: 'Novo recado disponível', time: '5 min atrás' },
  { id: 2, message: 'Atividade entregue', time: '1 hora atrás' }
]
const unreadNotifications = computed(() => notifications.length)

const handleLogout = () => {
  authStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.user-avatar {
  width: 35px;
  height: 35px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 0.875rem;
}

.notification-dropdown {
  min-width: 300px;
  max-height: 400px;
  overflow-y: auto;
}

.nav-link {
  cursor: pointer;
}
</style>

