import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/auth/Login.vue'),
    meta: { requiresGuest: true }
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('../views/Dashboard.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/recados',
    name: 'Recados',
    component: () => import('../views/comunicacao/RecadosList.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/recados/:id',
    name: 'RecadoDetalhes',
    component: () => import('../views/comunicacao/RecadoDetalhes.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/recados/enviar',
    name: 'EnviarRecado',
    component: () => import('../views/comunicacao/EnviarRecado.vue'),
    meta: { requiresAuth: true, roles: ['PROFESSOR', 'DIRETORIA', 'ADMINISTRADOR'] }
  },
  {
    path: '/recados/editar/:id',
    name: 'EditarRecado',
    component: () => import('../views/comunicacao/EditarRecado.vue'),
    meta: { requiresAuth: true, roles: ['PROFESSOR', 'DIRETORIA', 'ADMINISTRADOR'] }
  },
  {
    path: '/atividades',
    name: 'Atividades',
    component: () => import('../views/atividades/AtividadesList.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/atividades/:id',
    name: 'AtividadeDetalhes',
    component: () => import('../views/atividades/AtividadeDetalhes.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/calendario',
    name: 'Calendario',
    component: () => import('../views/Calendario.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/documentos',
    name: 'Documentos',
    component: () => import('../views/documentos/DocumentosList.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/admin/usuarios',
    name: 'AdminUsuarios',
    component: () => import('../views/admin/Usuarios.vue'),
    meta: { requiresAuth: true, roles: ['DIRETORIA', 'ADMINISTRADOR'] }
  },
  {
    path: '/admin/turmas',
    name: 'AdminTurmas',
    component: () => import('../views/admin/Turmas.vue'),
    meta: { requiresAuth: true, roles: ['DIRETORIA', 'ADMINISTRADOR'] }
  },
  {
    path: '/admin/estatisticas',
    name: 'AdminEstatisticas',
    component: () => import('../views/admin/Estatisticas.vue'),
    meta: { requiresAuth: true, roles: ['DIRETORIA', 'ADMINISTRADOR'] }
  },
  {
    path: '/perfil',
    name: 'Perfil',
    component: () => import('../views/Perfil.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('../views/NotFound.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// Navigation guards
router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore()
  
  try {
    // Se rota exige auth mas n�o h� token
    if (to.meta.requiresAuth && !authStore.isAuthenticated) {
      return next('/login')
    }

    // Se temos token mas ainda n�o carregamos o usu�rio, buscar antes de aplicar regras de role
    if (authStore.isAuthenticated && !authStore.user) {
      try {
        await authStore.fetchCurrentUser()
      } catch (e) {
        authStore.logout()
        return next('/login')
      }
    }

    // Evitar acessar rotas de guest logado
    if (to.meta.requiresGuest && authStore.isAuthenticated) {
      return next('/dashboard')
    }

    // Checagem de roles
    if (to.meta.roles) {
      const role = authStore.userRole || authStore.user?.role || ''
      if (!role || !to.meta.roles.includes(role)) {
        return next('/dashboard')
      }
    }

    return next()
  } catch (e) {
    console.error('Erro no guard de rotas:', e)
    return next('/login')
  }
})

export default router

