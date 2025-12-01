<template>
  <div class="dashboard">
    <div class="container-fluid py-4">
      <div class="row align-items-start mb-4">
        <div class="col">
          <h2 class="fw-bold">
            <i class="bi bi-house-door-fill me-2"></i>
            Bem-vindo, {{ userName }}!
          </h2>
          <p class="text-muted mb-0">Confira as últimas atualizações e notificações</p>
        </div>
        <div class="col-auto" v-if="exibeFiltros">
          <button class="btn btn-outline-primary d-flex align-items-center" @click="abrirFiltros">
            <i class="bi bi-sliders me-2"></i> Filtros
          </button>
        </div>
      </div>
      
      <!-- Cards de Estatísticas -->
      <div class="row g-4 mb-4">
        <div class="col-md-4">
          <div class="stat-card bg-primary text-white">
            <div class="d-flex justify-content-between align-items-center">
              <div>
                <h6 class="text-uppercase mb-1 opacity-75">Recados</h6>
                <h2 class="mb-0">{{ stats.recados }}</h2>
              </div>
              <i class="bi bi-envelope-fill fs-1 opacity-50"></i>
            </div>
          </div>
        </div>
        
        <div class="col-md-4">
          <div class="stat-card bg-success text-white">
            <div class="d-flex justify-content-between align-items-center">
              <div>
                <h6 class="text-uppercase mb-1 opacity-75">Atividades</h6>
                <h2 class="mb-0">{{ stats.atividades }}</h2>
              </div>
              <i class="bi bi-journal-text fs-1 opacity-50"></i>
            </div>
          </div>
        </div>
        
        <div class="col-md-4">
          <div class="stat-card bg-warning text-white">
            <div class="d-flex justify-content-between align-items-center">
              <div>
                <h6 class="text-uppercase mb-1 opacity-75">Eventos</h6>
                <h2 class="mb-0">{{ stats.eventos }}</h2>
              </div>
              <i class="bi bi-calendar-event-fill fs-1 opacity-50"></i>
            </div>
          </div>
        </div>
      </div>

      <div v-if="carregando" class="text-center py-5 text-muted">
        <div class="spinner-border text-primary mb-3" role="status"></div>
        <div>Carregando dados do dashboard...</div>
      </div>

      <div v-else>
      <div 
        class="filtros-sidebar" 
        :class="{ 'filtros-sidebar-open': sidebarFiltrosAberto }"
        v-if="exibeFiltros"
      >
        <div class="filtros-header">
          <h6 class="mb-0">Filtros</h6>
          <button class="btn btn-sm btn-outline-secondary" @click="fecharFiltros">
            <i class="bi bi-x-lg"></i>
          </button>
        </div>
        <div class="filtros-body">
          <div class="mb-3">
            <label class="form-label">Data</label>
            <select class="form-select" v-model="filtros.datePreset" @change="selecionarDataPreset($event.target.value)">
              <option value="">Todas</option>
              <option v-for="op in opcoesData" :key="op.value" :value="op.value">{{ op.label }}</option>
            </select>
          </div>

          <div class="mb-3">
            <label class="form-label">Intervalo personalizado</label>
            <div class="row g-2">
              <div class="col-6">
                <input type="date" class="form-control" placeholder="dd/mm/aaaa" v-model="filtros.startDate" @change="onCustomDateChange">
              </div>
              <div class="col-6">
                <input type="date" class="form-control" placeholder="dd/mm/aaaa" v-model="filtros.endDate" @change="onCustomDateChange">
              </div>
            </div>
          </div>

          <div class="mb-3" v-if="podeFiltrarEscola">
            <label class="form-label">Escola</label>
            <select class="form-select" v-model="filtros.escolaId" @change="onEscolaChange">
              <option value="">Todas</option>
              <option v-for="escola in opcoesFiltros.escolas" :key="escola.id" :value="escola.id">
                {{ escola.nome }}
              </option>
            </select>
          </div>

          <div class="mb-3" v-if="podeFiltrarTurma">
            <label class="form-label">Turma</label>
            <select class="form-select" v-model="filtros.turmaId">
              <option value="">Todas</option>
              <option 
                v-for="turma in turmasFiltradas" 
                :key="turma.id" 
                :value="turma.id">
                {{ turma.nome }} - {{ turma.anoLetivo }}
              </option>
            </select>
          </div>

          <div class="mb-3" v-if="podeFiltrarUsuario">
            <label class="form-label">Usuário</label>
            <select class="form-select" v-model="filtros.usuarioId">
              <option value="">Todos</option>
              <option 
                v-for="usuario in usuariosFiltrados" 
                :key="usuario.id" 
                :value="usuario.id">
                {{ usuario.nome }} ({{ usuario.role }})
              </option>
            </select>
          </div>

          <div class="d-flex gap-2">
            <button class="btn btn-primary w-100" @click="aplicarFiltros">Aplicar</button>
            <button class="btn btn-outline-secondary w-100" @click="limparFiltros">Limpar</button>
          </div>
        </div>
      </div>

      <div class="filtros-overlay" v-if="sidebarFiltrosAberto" @click="fecharFiltros"></div>

      <div class="row g-4">
        <!-- Recados Recentes -->
        <div class="col-lg-6">
          <div class="card shadow-sm">
            <div class="card-header bg-white d-flex justify-content-between align-items-center">
              <h5 class="mb-0">
                <i class="bi bi-envelope me-2"></i>Recados Recentes
              </h5>
              <router-link to="/recados" class="btn btn-sm btn-outline-primary">
                Ver todos
              </router-link>
            </div>
            <div class="card-body">
              <div v-if="recadosRecentes.length === 0" class="text-center py-4 text-muted">
                <i class="bi bi-inbox fs-1"></i>
                <p class="mt-2">Nenhum recado disponível</p>
              </div>
              <div v-else>
                <div 
                  v-for="recado in recadosRecentes" 
                  :key="recado.id"
                  class="recado-item"
                  @click="router.push(`/recados/${recado.id}`)"
                >
                  <div class="d-flex justify-content-between align-items-start">
                    <div>
                      <h6 class="mb-1">{{ recado.titulo }}</h6>
                      <small class="text-muted">
                        <i class="bi bi-person me-1"></i>{{ recado.remetente }}
                      </small>
                    </div>
                    <small class="text-muted">{{ recado.data }}</small>
                  </div>
                  <p class="mb-0 mt-2 text-muted small">{{ recado.preview }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- Atividades Pendentes -->
        <div class="col-lg-6">
          <div class="card shadow-sm">
            <div class="card-header bg-white d-flex justify-content-between align-items-center">
              <h5 class="mb-0">
                <i class="bi bi-clipboard-check me-2"></i>Atividades Pendentes
              </h5>
              <router-link to="/atividades" class="btn btn-sm btn-outline-primary">
                Ver todas
              </router-link>
            </div>
            <div class="card-body">
              <div v-if="atividadesPendentes.length === 0" class="text-center py-4 text-muted">
                <i class="bi bi-check-circle fs-1"></i>
                <p class="mt-2">Nenhuma atividade pendente</p>
              </div>
              <div v-else>
                <div 
                  v-for="atividade in atividadesPendentes" 
                  :key="atividade.id"
                  class="atividade-item"
                  @click="router.push(`/atividades/${atividade.id}`)"
                >
                  <div class="d-flex justify-content-between align-items-start">
                    <div>
                      <h6 class="mb-1">{{ atividade.titulo }}</h6>
                      <small class="text-muted">
                        <i class="bi bi-book me-1"></i>{{ atividade.disciplina }}
                      </small>
                    </div>
                    <span :class="`badge ${getPrazoClass(atividade.prazo)}`">
                      {{ atividade.prazo }}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- Próximos Eventos -->
      <div class="row g-4 mt-0">
        <div class="col-12">
          <div class="card shadow-sm">
            <div class="card-header bg-white d-flex justify-content-between align-items-center">
              <h5 class="mb-0">
                <i class="bi bi-calendar-event me-2"></i>Próximos Eventos
              </h5>
              <router-link to="/calendario" class="btn btn-sm btn-outline-primary">
                Ver calendário
              </router-link>
            </div>
            <div class="card-body">
              <div v-if="proximosEventos.length === 0" class="text-center py-4 text-muted">
                <i class="bi bi-calendar-x fs-1"></i>
                <p class="mt-2">Nenhum evento próximo</p>
              </div>
              <div v-else class="row g-3">
                <div 
                  v-for="evento in proximosEventos" 
                  :key="evento.id"
                  class="col-md-4"
                >
                  <div class="evento-card">
                    <div class="evento-date">
                      <div class="day">{{ evento.dia }}</div>
                      <div class="month">{{ evento.mes }}</div>
                    </div>
                    <div class="evento-info">
                      <h6 class="mb-1">{{ evento.titulo }}</h6>
                      <small class="text-muted">
                        <i class="bi bi-clock me-1"></i>{{ evento.horario }}
                      </small>
                    </div>
                  </div>
                </div>
              </div>
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
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { useNotificationStore } from '../stores/notifications'
import dashboardService from '../services/dashboardService'
import escolasService from '../services/escolasService'
import turmasService from '../services/turmasService'
import usuariosService from '../services/usuariosService'

const router = useRouter()
const authStore = useAuthStore()
const notificationStore = useNotificationStore()

const userName = computed(() => authStore.userName)
const userRole = computed(() => authStore.userRole)

const stats = ref({
  recados: 0,
  atividades: 0,
  eventos: 0
})

const filtros = ref({
  escolaId: '',
  turmaId: '',
  usuarioId: '',
  datePreset: 'essa_semana',
  startDate: '',
  endDate: ''
})

const opcoesFiltros = ref({
  escolas: [],
  turmas: [],
  usuarios: []
})

const recadosRecentes = ref([])
const atividadesPendentes = ref([])
const proximosEventos = ref([])
const carregando = ref(false)
const sidebarFiltrosAberto = ref(false)

const podeFiltrarEscola = computed(() => userRole.value === 'ADMINISTRADOR')
const podeFiltrarTurma = computed(() => ['ADMINISTRADOR', 'DIRETORIA', 'PROFESSOR'].includes(userRole.value))
const podeFiltrarUsuario = computed(() => ['ADMINISTRADOR', 'DIRETORIA', 'PROFESSOR'].includes(userRole.value))
const exibeFiltros = computed(() => podeFiltrarEscola.value || podeFiltrarTurma.value || podeFiltrarUsuario.value)

const opcoesData = [
  { label: 'Hoje', value: 'hoje' },
  { label: 'Ontem', value: 'ontem' },
  { label: 'Essa semana', value: 'essa_semana' },
  { label: 'Esse mês', value: 'esse_mes' },
  { label: 'Último mês', value: 'ultimo_mes' }
]

const turmasFiltradas = computed(() => {
  const escolaSelecionada = filtros.value.escolaId ? Number(filtros.value.escolaId) : null
  if (!escolaSelecionada) return opcoesFiltros.value.turmas
  return opcoesFiltros.value.turmas.filter(turma => turma.escolaId === escolaSelecionada)
})

const usuariosFiltrados = computed(() => {
  const escolaSelecionada = filtros.value.escolaId ? Number(filtros.value.escolaId) : null
  if (!escolaSelecionada) return opcoesFiltros.value.usuarios
  return opcoesFiltros.value.usuarios.filter(usuario => !usuario.escolaId || usuario.escolaId === escolaSelecionada)
})

const getPrazoClass = (prazo) => {
  if (!prazo) return 'bg-secondary'
  if (prazo.toLowerCase().includes('hoje')) return 'bg-danger'
  if (prazo.toLowerCase().includes('amanhã')) return 'bg-warning'
  return 'bg-info'
}

const formatarDataCurta = (dataISO) => {
  if (!dataISO) return ''
  const data = new Date(dataISO)
  return data.toLocaleDateString('pt-BR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric'
  })
}

const formatarPrazo = (dataISO) => {
  if (!dataISO) return 'Sem data'
  const agora = new Date()
  const data = new Date(dataISO)
  const diffDias = Math.floor((data - agora) / (1000 * 60 * 60 * 24))

  if (diffDias < 0) return 'Vencida'
  if (diffDias === 0) return 'Hoje'
  if (diffDias === 1) return 'Amanhã'
  return data.toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit' })
}

const formatarEvento = (dataISO) => {
  if (!dataISO) {
    return {
      dia: '--',
      mes: '--',
      horario: '--:--'
    }
  }
  const data = new Date(dataISO)
  const meses = ['JAN', 'FEV', 'MAR', 'ABR', 'MAI', 'JUN', 'JUL', 'AGO', 'SET', 'OUT', 'NOV', 'DEZ']
  return {
    dia: String(data.getDate()).padStart(2, '0'),
    mes: meses[data.getMonth()],
    horario: data.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })
  }
}

const mapearRespostaDashboard = (data) => {
  stats.value = data?.stats || { recados: 0, atividades: 0, eventos: 0 }

  recadosRecentes.value = (data?.recadosRecentes || []).map(recado => ({
    id: recado.id,
    titulo: recado.titulo,
    remetente: recado.remetente || '—',
    data: formatarDataCurta(recado.dataEnvio),
    preview: recado.conteudo ? `${recado.conteudo.substring(0, 100)}${recado.conteudo.length > 100 ? '...' : ''}` : ''
  }))

  atividadesPendentes.value = (data?.atividadesPendentes || []).map(atividade => ({
    id: atividade.id,
    titulo: atividade.titulo,
    disciplina: atividade.disciplina,
    prazo: formatarPrazo(atividade.dataEntrega)
  }))

  proximosEventos.value = (data?.proximosEventos || []).map(evento => ({
    id: evento.id,
    titulo: evento.titulo,
    ...formatarEvento(evento.dataInicio)
  }))
}

const carregarOpcoesFiltros = async () => {
  try {
    if (podeFiltrarEscola.value) {
      opcoesFiltros.value.escolas = await escolasService.listarTodas()
    }

    if (podeFiltrarTurma.value) {
      const response = await turmasService.listarTurmas()
      opcoesFiltros.value.turmas = response.data || []
    }

    if (podeFiltrarUsuario.value) {
      if (userRole.value === 'ADMINISTRADOR' || userRole.value === 'DIRETORIA') {
        opcoesFiltros.value.usuarios = await usuariosService.listar()
      } else if (userRole.value === 'PROFESSOR') {
        const [alunos, responsaveis, professores] = await Promise.all([
          usuariosService.listarPorRole('ALUNO'),
          usuariosService.listarPorRole('RESPONSAVEL'),
          usuariosService.listarPorRole('PROFESSOR')
        ])
        opcoesFiltros.value.usuarios = [...alunos, ...responsaveis, ...professores]
      }
    }
  } catch (error) {
    console.error('Erro ao carregar filtros do dashboard:', error)
    notificationStore.error('Não foi possível carregar os filtros do dashboard')
  }
}

const aplicarFiltros = async () => {
  carregando.value = true
  try {
    const params = {}
    if (filtros.value.escolaId) params.escolaId = filtros.value.escolaId
    if (filtros.value.turmaId) params.turmaId = filtros.value.turmaId
    if (filtros.value.usuarioId) params.usuarioId = filtros.value.usuarioId
    if (filtros.value.datePreset) params.datePreset = filtros.value.datePreset
    if (filtros.value.startDate) params.startDate = filtros.value.startDate
    if (filtros.value.endDate) params.endDate = filtros.value.endDate

    const data = await dashboardService.getResumo(params)
    mapearRespostaDashboard(data)
    sidebarFiltrosAberto.value = false
  } catch (error) {
    console.error('Erro ao carregar dashboard:', error)
    notificationStore.error('Não foi possível carregar os dados do dashboard')
  } finally {
    carregando.value = false
  }
}

const onEscolaChange = () => {
  filtros.value.turmaId = ''
  filtros.value.usuarioId = ''
}

const selecionarDataPreset = (preset) => {
  filtros.value.datePreset = preset
  filtros.value.startDate = ''
  filtros.value.endDate = ''
}

const onCustomDateChange = () => {
  filtros.value.datePreset = ''
}

const limparFiltros = () => {
  filtros.value = {
    escolaId: '',
    turmaId: '',
    usuarioId: '',
    datePreset: 'essa_semana',
    startDate: '',
    endDate: ''
  }
  aplicarFiltros()
}

const abrirFiltros = () => {
  sidebarFiltrosAberto.value = true
}

const fecharFiltros = () => {
  sidebarFiltrosAberto.value = false
}

onMounted(async () => {
  await carregarOpcoesFiltros()
  await aplicarFiltros()
})
</script>

<style scoped>
.stat-card {
  padding: 1.5rem;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.recado-item,
.atividade-item {
  padding: 1rem;
  border-bottom: 1px solid #e9ecef;
  cursor: pointer;
  transition: background-color 0.2s;
}

.recado-item:last-child,
.atividade-item:last-child {
  border-bottom: none;
}

.recado-item:hover,
.atividade-item:hover {
  background-color: #f8f9fa;
}

.evento-card {
  display: flex;
  gap: 1rem;
  padding: 1rem;
  border: 1px solid #dee2e6;
  border-radius: 8px;
  transition: all 0.2s;
}

.evento-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.evento-date {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 0.5rem 1rem;
  border-radius: 8px;
  text-align: center;
  min-width: 60px;
}

.evento-date .day {
  font-size: 1.5rem;
  font-weight: bold;
  line-height: 1;
}

.evento-date .month {
  font-size: 0.75rem;
  text-transform: uppercase;
}

.evento-info {
  flex: 1;
}

.filtros-sidebar {
  position: fixed;
  top: 0;
  right: -360px;
  height: 100vh;
  width: 360px;
  background: #fff;
  box-shadow: -2px 0 12px rgba(0, 0, 0, 0.1);
  padding: 1.5rem;
  z-index: 1050;
  transition: right 0.3s ease;
  overflow-y: auto;
}

.filtros-sidebar-open {
  right: 0;
}

.filtros-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.25);
  z-index: 1040;
}

.filtros-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1rem;
  position: sticky;
  top: 0;
  background: #fff;
  padding-bottom: 0.5rem;
  z-index: 1;
}

.filtros-body {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.btn-group .btn.active {
  background-color: #0d6efd;
  color: #fff;
}
</style>

