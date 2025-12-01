<template>
  <div class="container-fluid py-4">
    <h2 class="fw-bold mb-4">
      <i class="bi bi-graph-up me-2"></i>Estatísticas
    </h2>
    
    <div class="row g-3 mb-4">
      <div class="col-md-4">
        <div class="stat-card bg-primary text-white">
          <h6 class="opacity-75">Total de Usuários</h6>
          <h2>{{ stats.totalUsuarios }}</h2>
        </div>
      </div>
      <div class="col-md-4">
        <div class="stat-card bg-success text-white">
          <h6 class="opacity-75">Usuários Ativos</h6>
          <h2>{{ stats.usuariosAtivos }}</h2>
        </div>
      </div>
      <div class="col-md-4">
        <div class="stat-card bg-warning text-white">
          <h6 class="opacity-75">Recados Enviados</h6>
          <h2>{{ stats.recadosEnviados }}</h2>
        </div>
      </div>
    </div>
    
    <div class="row g-4">
      <div class="col-lg-6">
        <div class="card shadow-sm">
          <div class="card-header bg-white">
            <h5 class="mb-0">Usuários por Perfil</h5>
          </div>
          <div class="card-body">
            <div 
              v-for="(item, index) in usuariosPorPerfil" 
              :key="item.label" 
              class="mb-3"
            >
              <div class="d-flex justify-content-between mb-1">
                <span>{{ item.label }}</span>
                <strong>{{ item.total }}</strong>
              </div>
              <div class="progress">
                <div 
                  class="progress-bar" 
                  :class="item.barClass"
                  :style="`width: ${item.percent}%`"
                ></div>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <div class="col-lg-6">
        <div class="card shadow-sm">
          <div class="card-header bg-white">
            <h5 class="mb-0">Atividades Recentes</h5>
          </div>
            <div class="card-body">
              <div v-if="atividadesRecentes.length === 0" class="text-muted text-center py-4">
                Nenhuma atividade encontrada
              </div>
              <div 
                v-for="(atv, idx) in atividadesRecentes" 
                :key="idx" 
                class="activity-item mb-3"
              >
                <i :class="atv.icon" :style="`color: ${atv.color}`"></i>
                <div>
                  <strong>{{ atv.titulo }}</strong>
                  <small class="text-muted d-block">{{ atv.descricao }}</small>
                  <small class="text-muted">{{ atv.data }}</small>
                </div>
              </div>
            </div>
          </div>
        </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import usuariosService from '../../services/usuariosService'
import recadosService from '../../services/recadosService'
import atividadesService from '../../services/atividadesService'
import dashboardService from '../../services/dashboardService'
import { useNotificationStore } from '../../stores/notifications'

const notificationStore = useNotificationStore()

const stats = ref({
  totalUsuarios: 0,
  usuariosAtivos: 0,
  recadosEnviados: 0
})

const usuariosPorPerfil = computed(() => {
  const total = statsDetalhados.value.total || 1
  return [
    { label: 'Alunos', total: statsDetalhados.value.alunos, percent: (statsDetalhados.value.alunos / total) * 100, barClass: 'bg-primary' },
    { label: 'Responsáveis', total: statsDetalhados.value.responsaveis, percent: (statsDetalhados.value.responsaveis / total) * 100, barClass: 'bg-success' },
    { label: 'Professores', total: statsDetalhados.value.professores, percent: (statsDetalhados.value.professores / total) * 100, barClass: 'bg-warning' },
    { label: 'Administradores', total: statsDetalhados.value.administradores, percent: (statsDetalhados.value.administradores / total) * 100, barClass: 'bg-danger' }
  ]
})

const statsDetalhados = ref({
  total: 0,
  alunos: 0,
  responsaveis: 0,
  professores: 0,
  administradores: 0
})

const atividadesRecentes = ref([])

const carregarDados = async () => {
  try {
    const [usuarios, recados, atividades, dashboard] = await Promise.all([
      usuariosService.getAllUsuarios(),
      recadosService.getRecados(),
      atividadesService.listar(),
      dashboardService.getResumo()
    ])

    const totalUsuarios = usuarios.length
    const usuariosAtivos = usuarios.filter(u => u.ativo !== false).length
    const recadosEnviados = (recados?.data || recados || []).length

    stats.value = {
      totalUsuarios,
      usuariosAtivos,
      recadosEnviados
    }

    statsDetalhados.value = {
      total: totalUsuarios || 1,
      alunos: usuarios.filter(u => u.role === 'ALUNO').length,
      responsaveis: usuarios.filter(u => u.role === 'RESPONSAVEL').length,
      professores: usuarios.filter(u => u.role === 'PROFESSOR').length,
      administradores: usuarios.filter(u => u.role === 'ADMINISTRADOR' || u.role === 'DIRETORIA').length
    }

    const eventos = dashboard?.proximosEventos || []
    const recadosData = recados?.data || recados || []
    const atividadesData = atividades || []

    const linhaTempo = [
      ...recadosData.map(r => ({
        data: r.dataEnvio,
        titulo: 'Recado enviado',
        descricao: r.titulo,
        icon: 'bi bi-envelope-fill',
        color: '#0d6efd'
      })),
      ...atividadesData.map(a => ({
        data: a.dataCriacao || a.dataEntrega,
        titulo: 'Atividade publicada',
        descricao: a.titulo,
        icon: 'bi bi-clipboard-check',
        color: '#198754'
      })),
      ...eventos.map(e => ({
        data: e.dataInicio,
        titulo: 'Evento no calendário',
        descricao: e.titulo,
        icon: 'bi bi-calendar-event',
        color: '#fd7e14'
      }))
    ]

    atividadesRecentes.value = linhaTempo
      .filter(item => item.data)
      .sort((a, b) => new Date(b.data) - new Date(a.data))
      .slice(0, 8)
      .map(item => ({
        ...item,
        data: formatarData(item.data)
      }))
  } catch (error) {
    console.error('Erro ao carregar estatísticas:', error)
    notificationStore.error('Não foi possível carregar estatísticas')
  }
}

const formatarData = (dataISO) => {
  if (!dataISO) return ''
  const data = new Date(dataISO)
  return data.toLocaleDateString('pt-BR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric'
  })
}

onMounted(() => {
  carregarDados()
})
</script>

<style scoped>
.stat-card { padding: 1.5rem; border-radius: 10px; }
.activity-item { display: flex; gap: 1rem; align-items: start; }
.activity-item i { font-size: 1.5rem; }
</style>

