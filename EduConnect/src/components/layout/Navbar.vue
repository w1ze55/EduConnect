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
              @click="carregarSeNecessario"
            >
              <i class="bi bi-bell fs-5"></i>
              <span 
                v-if="unreadCount > 0" 
                class="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger"
              >
                {{ unreadCount }}
              </span>
            </a>
            <div class="dropdown-menu dropdown-menu-end notification-dropdown p-0">
              <div class="notification-header px-4 py-3 border-bottom d-flex justify-content-between align-items-center">
                <div>
                  <div class="fw-semibold">Notificações</div>
                  <small class="text-muted">{{ unreadCount }} não lidas</small>
                </div>
                <div class="d-flex gap-2 notification-actions">
                  <button class="btn btn-sm btn-outline-secondary" @click.stop="marcarTodas">
                    Marcar todas como lidas
                  </button>
                  <button class="btn btn-sm btn-outline-danger" @click.stop="limpar">
                    Limpar
                  </button>
                </div>
              </div>

              <div class="px-4 py-3 border-bottom bg-light">
                <label class="form-label small text-muted mb-1">Filtrar por tipo</label>
                <select class="form-select form-select-sm" v-model="filtroTipo">
                  <option value="ALL">Todos</option>
                  <option value="RECADO">Recados</option>
                  <option value="EVENTO">Eventos</option>
                  <option value="ATIVIDADE">Atividades</option>
                  <option value="DOCUMENTO">Documentos</option>
                </select>
              </div>

              <div class="px-4 py-4" v-if="loadingNotifications">
                <div class="text-center text-muted">
                  <div class="spinner-border spinner-border-sm text-primary mb-2" role="status"></div>
                  <div>Carregando notificações...</div>
                </div>
              </div>

              <div v-else class="notification-list px-4 py-3">
                <div v-if="notifications.length === 0" class="text-center text-muted py-3">
                  Nenhuma notificação
                </div>

                <div 
                  v-for="notif in notifications" 
                  :key="notif.id" 
                  class="notification-card d-flex gap-3"
                  :class="{ 'notification-unread': !notif.read }"
                >
                  <div class="notif-icon" :class="iconClass(notif.type)">
                    <i :class="iconName(notif.type)"></i>
                  </div>
                  <div class="flex-grow-1">
                    <div class="d-flex justify-content-between align-items-start flex-wrap gap-2">
                      <div class="d-flex align-items-center gap-2 flex-wrap">
                        <span class="badge bg-light text-dark">{{ mapTipoLabel(notif.type) }}</span>
                        <button 
                          class="btn btn-sm btn-icon"
                          :class="notif.read ? 'btn-outline-secondary' : 'btn-outline-primary'"
                          @click.stop="toggleRead(notif.id)"
                          :title="notif.read ? 'Marcar como não lida' : 'Marcar como lida'"
                        >
                          <i :class="notif.read ? 'bi bi-envelope-open' : 'bi bi-envelope'"></i>
                        </button>
                        <router-link 
                          v-if="notif.link" 
                          class="btn btn-sm btn-outline-dark btn-icon" 
                          :to="notif.link"
                          title="Abrir"
                        >
                          <i class="bi bi-box-arrow-up-right"></i>
                        </router-link>
                      </div>
                      <small class="text-muted">{{ formatarData(notif.date) }}</small>
                    </div>
                    <div class="fw-semibold mt-2 mb-1">{{ notif.title }}</div>
                    <div class="text-muted small">{{ notif.message }}</div>
                  </div>
                </div>
              </div>
            </div>
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
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import { useUserNotificationsStore } from '../../stores/userNotifications'

const router = useRouter()
const authStore = useAuthStore()
const notifStore = useUserNotificationsStore()

const userName = computed(() => authStore.userName || 'Usuário')
const userInitials = computed(() => {
  const name = authStore.userName || 'U'
  return name.split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase()
})

const notifications = computed(() => [...notifStore.filteredNotifications].sort((a, b) => (b.timestamp || 0) - (a.timestamp || 0)))
const unreadCount = computed(() => notifStore.unreadCount)
const loadingNotifications = computed(() => notifStore.loading)
const filtroTipo = computed({
  get: () => notifStore.filterType,
  set: (v) => { notifStore.filterType = v }
})

const handleLogout = () => {
  authStore.logout()
  router.push('/login')
}

const carregarSeNecessario = () => {
  if (!notifStore.notifications.length && !notifStore.loading) {
    notifStore.loadNotifications()
  }
}

const marcarTodas = () => notifStore.markAllAsRead()
const limpar = () => notifStore.clearNotifications()
const toggleRead = (id) => notifStore.toggleRead(id)

const formatarData = (data) => {
  if (!data) return ''
  const d = new Date(data)
  return d.toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit', year: 'numeric' })
}

const mapTipoLabel = (tipo) => {
  const mapa = {
    RECADO: 'Recado',
    EVENTO: 'Evento',
    ATIVIDADE: 'Atividade',
    DOCUMENTO: 'Documento'
  }
  return mapa[tipo] || 'Outro'
}

const iconName = (tipo) => {
  const mapa = {
    RECADO: 'bi bi-envelope-open-fill',
    EVENTO: 'bi bi-calendar-event-fill',
    ATIVIDADE: 'bi bi-clipboard-check',
    DOCUMENTO: 'bi bi-file-earmark-text-fill'
  }
  return mapa[tipo] || 'bi bi-bell'
}

const iconClass = (tipo) => {
  const mapa = {
    RECADO: 'notif-icon-blue',
    EVENTO: 'notif-icon-orange',
    ATIVIDADE: 'notif-icon-green',
    DOCUMENTO: 'notif-icon-purple'
  }
  return mapa[tipo] || 'notif-icon-gray'
}

onMounted(() => {
  notifStore.loadNotifications()
})
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
  min-width: 420px;
  max-width: 520px;
  width: 480px;
  max-height: 620px;
  overflow-y: auto;
}

.nav-link {
  cursor: pointer;
}

.notification-list {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.notification-card {
  padding: 1.25rem;
  border: 1px solid #e9ecef;
  border-radius: 10px;
  background: #fff;
  box-shadow: 0 6px 18px rgba(0,0,0,0.05);
}

.notification-unread {
  border-color: #0d6efd;
  background: #f3f8ff;
}

.notif-icon {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 1.15rem;
  flex-shrink: 0;
  margin-top: 2px;
}

.btn-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 0;
  padding: 0.4rem 0.5rem;
  width: 36px;
  height: 36px;
}

.notification-actions button {
  padding-left: 0.8rem;
  padding-right: 0.8rem;
}

.notif-icon-blue { background: linear-gradient(135deg, #4dabf7, #1c7ed6); }
.notif-icon-orange { background: linear-gradient(135deg, #f59f00, #f76707); }
.notif-icon-green { background: linear-gradient(135deg, #51cf66, #2b8a3e); }
.notif-icon-purple { background: linear-gradient(135deg, #9775fa, #7048e8); }
.notif-icon-gray { background: linear-gradient(135deg, #adb5bd, #868e96); }
</style>

