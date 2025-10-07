<template>
  <div class="sidebar bg-white shadow-sm">
    <div class="sidebar-header p-3 border-bottom">
      <h6 class="mb-0 text-muted">Menu</h6>
    </div>
    
    <nav class="sidebar-nav">
      <router-link 
        v-for="item in filteredMenuItems" 
        :key="item.path"
        :to="item.path"
        class="sidebar-item"
        :class="{ 'active': isActive(item.path) }"
      >
        <i :class="item.icon"></i>
        <span>{{ item.label }}</span>
      </router-link>
    </nav>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '../../stores/auth'

const route = useRoute()
const authStore = useAuthStore()

const menuItems = [
  { 
    path: '/dashboard', 
    label: 'Dashboard', 
    icon: 'bi bi-house-door-fill',
    roles: ['ALUNO', 'RESPONSAVEL', 'PROFESSOR', 'DIRETORIA', 'ADMINISTRADOR']
  },
  { 
    path: '/recados', 
    label: 'Recados', 
    icon: 'bi bi-envelope-fill',
    roles: ['ALUNO', 'RESPONSAVEL', 'PROFESSOR', 'DIRETORIA', 'ADMINISTRADOR']
  },
  { 
    path: '/atividades', 
    label: 'Atividades', 
    icon: 'bi bi-journal-text',
    roles: ['ALUNO', 'PROFESSOR', 'DIRETORIA', 'ADMINISTRADOR']
  },
  { 
    path: '/calendario', 
    label: 'Calendário', 
    icon: 'bi bi-calendar-event-fill',
    roles: ['ALUNO', 'RESPONSAVEL', 'PROFESSOR', 'DIRETORIA', 'ADMINISTRADOR']
  },
  { 
    path: '/documentos', 
    label: 'Documentos', 
    icon: 'bi bi-file-earmark-text-fill',
    roles: ['ALUNO', 'RESPONSAVEL', 'PROFESSOR', 'DIRETORIA', 'ADMINISTRADOR']
  },
  { 
    path: '/financeiro', 
    label: 'Financeiro', 
    icon: 'bi bi-wallet2',
    roles: ['RESPONSAVEL', 'DIRETORIA', 'ADMINISTRADOR']
  },
  { 
    path: '/admin/usuarios', 
    label: 'Usuários', 
    icon: 'bi bi-people-fill',
    roles: ['DIRETORIA', 'ADMINISTRADOR']
  },
  { 
    path: '/admin/turmas', 
    label: 'Turmas', 
    icon: 'bi bi-diagram-3-fill',
    roles: ['DIRETORIA', 'ADMINISTRADOR']
  },
  { 
    path: '/admin/estatisticas', 
    label: 'Estatísticas', 
    icon: 'bi bi-graph-up',
    roles: ['DIRETORIA', 'ADMINISTRADOR']
  }
]

const filteredMenuItems = computed(() => {
  const userRole = authStore.userRole
  console.log('🔍 Debug Sidebar - User:', authStore.user)
  console.log('🔍 Debug Sidebar - UserRole:', userRole)
  console.log('🔍 Debug Sidebar - Token:', authStore.token ? 'Existe' : 'Não existe')
  
  if (!userRole) {
    console.warn('⚠️ UserRole está nulo! Usuário:', authStore.user)
    return []
  }
  
  const filtered = menuItems.filter(item => item.roles.includes(userRole))
  console.log('✅ Itens de menu filtrados:', filtered.length)
  return filtered
})

const isActive = (path) => {
  return route.path.startsWith(path)
}
</script>

<style scoped>
.sidebar {
  width: 250px;
  min-height: calc(100vh - 60px);
  position: sticky;
  top: 60px;
}

.sidebar-item {
  display: flex;
  align-items: center;
  padding: 12px 20px;
  color: #6c757d;
  text-decoration: none;
  transition: all 0.3s;
  border-left: 3px solid transparent;
}

.sidebar-item i {
  font-size: 1.2rem;
  margin-right: 12px;
  width: 24px;
}

.sidebar-item:hover {
  background-color: #f8f9fa;
  color: #0d6efd;
}

.sidebar-item.active {
  background-color: #e7f1ff;
  color: #0d6efd;
  border-left-color: #0d6efd;
  font-weight: 500;
}

@media (max-width: 768px) {
  .sidebar {
    width: 100%;
    min-height: auto;
    position: relative;
  }
}
</style>

