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
    path: '/cadastro',
    name: 'Cadastro',
    component: () => import('../views/auth/Cadastro.vue'),
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
    meta: { requiresAuth: true, roles: ['PROFESSOR', 'ADMINISTRADOR'] }
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
    path: '/financeiro',
    name: 'Financeiro',
    component: () => import('../views/financeiro/Financeiro.vue'),
    meta: { requiresAuth: true, roles: ['RESPONSAVEL', 'ADMINISTRADOR'] }
  },
  {
    path: '/admin/usuarios',
    name: 'AdminUsuarios',
    component: () => import('../views/admin/Usuarios.vue'),
    meta: { requiresAuth: true, roles: ['ADMINISTRADOR'] }
  },
  {
    path: '/admin/estatisticas',
    name: 'AdminEstatisticas',
    component: () => import('../views/admin/Estatisticas.vue'),
    meta: { requiresAuth: true, roles: ['ADMINISTRADOR'] }
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
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  
  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    next('/login')
  } else if (to.meta.requiresGuest && authStore.isAuthenticated) {
    next('/dashboard')
  } else if (to.meta.roles && !to.meta.roles.includes(authStore.user?.role)) {
    next('/dashboard')
  } else {
    next()
  }
})

export default router

