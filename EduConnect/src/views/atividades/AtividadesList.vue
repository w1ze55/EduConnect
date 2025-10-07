<template>
  <div class="container-fluid py-4">
    <div class="row mb-4">
      <div class="col-md-6">
        <h2 class="fw-bold">
          <i class="bi bi-journal-text me-2"></i>Atividades
        </h2>
        <p class="text-muted">Confira suas atividades escolares</p>
      </div>
      <div class="col-md-6 text-end" v-if="podeGerenciar">
        <button class="btn btn-primary" @click="mostrarModalCriar">
          <i class="bi bi-plus-circle me-2"></i>Nova Atividade
        </button>
      </div>
    </div>
    
    <div class="row g-3 mb-4">
      <div class="col-md-4">
        <div class="stat-card bg-primary text-white">
          <h6 class="text-uppercase mb-1 opacity-75">Total</h6>
          <h2 class="mb-0">{{ atividades.length }}</h2>
        </div>
      </div>
      <div class="col-md-4">
        <div class="stat-card bg-warning text-white">
          <h6 class="text-uppercase mb-1 opacity-75">Pendentes</h6>
          <h2 class="mb-0">{{ atividadesPendentes }}</h2>
        </div>
      </div>
      <div class="col-md-4">
        <div class="stat-card bg-danger text-white">
          <h6 class="text-uppercase mb-1 opacity-75">Atrasadas</h6>
          <h2 class="mb-0">{{ atividadesAtrasadas }}</h2>
        </div>
      </div>
    </div>
    
    <div class="card shadow-sm mb-4">
      <div class="card-body">
        <div class="row g-3">
          <div class="col-md-4">
            <input type="text" class="form-control" placeholder="Buscar atividades..." v-model="filtros.busca" />
          </div>
          <div class="col-md-4">
            <select class="form-select" v-model="filtros.disciplina">
              <option value="">Todas as disciplinas</option>
              <option v-for="disciplina in disciplinasUnicas" :key="disciplina" :value="disciplina">
                {{ disciplina }}
              </option>
            </select>
          </div>
          <div class="col-md-4">
            <select class="form-select" v-model="filtros.turma">
              <option value="">Todas as turmas</option>
              <option v-for="turma in turmasUnicas" :key="turma" :value="turma">
                {{ turma }}
              </option>
            </select>
          </div>
        </div>
      </div>
    </div>
    
    <div v-if="loading" class="text-center py-5">
      <div class="spinner-border text-primary" role="status">
        <span class="visually-hidden">Carregando...</span>
      </div>
    </div>
    
    <div v-else-if="atividadesFiltradas.length === 0" class="text-center py-5">
      <i class="bi bi-journal-x" style="font-size: 4rem; color: #ccc;"></i>
      <p class="text-muted mt-3">Nenhuma atividade encontrada</p>
    </div>
    
    <div v-else class="row g-4">
      <div v-for="atividade in atividadesFiltradas" :key="atividade.id" class="col-md-6 col-lg-4">
        <div class="card shadow-sm h-100 atividade-card" @click="router.push(`/atividades/${atividade.id}`)">
          <div class="card-body">
            <div class="d-flex justify-content-between align-items-start mb-3">
              <span :class="`badge bg-${getStatusBadge(atividade)}`">
                {{ getStatusTexto(atividade) }}
              </span>
              <span class="text-muted small">{{ atividade.disciplina }}</span>
            </div>
            <h5 class="card-title">{{ atividade.titulo }}</h5>
            <p class="card-text text-muted small">{{ atividade.descricao || 'Sem descrição' }}</p>
            <div class="mt-3">
              <small class="text-muted d-block mb-2">
                <i class="bi bi-person me-1"></i>{{ atividade.professorNome }}
              </small>
              <small class="text-muted d-block mb-2">
                <i class="bi bi-calendar me-1"></i>Entrega: {{ formatarData(atividade.dataEntrega) }}
              </small>
              <small class="text-muted d-block">
                <i class="bi bi-diagram-3 me-1"></i>Turma {{ atividade.turma }} | Valor: {{ atividade.valor }} pts
              </small>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- Modal de Criar/Editar Atividade -->
    <AtividadeModal
      v-if="mostrandoModal"
      :atividade="atividadeSelecionada"
      :mode="modoModal"
      @close="fecharModal"
      @save="salvarAtividade"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useNotificationStore } from '@/stores/notifications'
import atividadesService from '@/services/atividadesService'
import AtividadeModal from './modals/AtividadeModal.vue'

const router = useRouter()
const authStore = useAuthStore()
const notifications = useNotificationStore()

const loading = ref(false)
const atividades = ref([])
const mostrandoModal = ref(false)
const modoModal = ref('create')
const atividadeSelecionada = ref(null)

const filtros = ref({
  busca: '',
  disciplina: '',
  turma: ''
})

// Computed
const podeGerenciar = computed(() => {
  const role = authStore.userRole
  return role === 'PROFESSOR' || role === 'DIRETORIA' || role === 'ADMINISTRADOR'
})

const atividadesFiltradas = computed(() => {
  return atividades.value.filter(atividade => {
    const matchBusca = !filtros.value.busca || 
      atividade.titulo.toLowerCase().includes(filtros.value.busca.toLowerCase()) ||
      atividade.descricao?.toLowerCase().includes(filtros.value.busca.toLowerCase())
    
    const matchDisciplina = !filtros.value.disciplina || atividade.disciplina === filtros.value.disciplina
    const matchTurma = !filtros.value.turma || atividade.turma === filtros.value.turma
    
    return matchBusca && matchDisciplina && matchTurma
  })
})

const disciplinasUnicas = computed(() => {
  return [...new Set(atividades.value.map(a => a.disciplina))].sort()
})

const turmasUnicas = computed(() => {
  return [...new Set(atividades.value.map(a => a.turma))].sort()
})

const atividadesPendentes = computed(() => {
  const agora = new Date()
  return atividades.value.filter(a => new Date(a.dataEntrega) > agora).length
})

const atividadesAtrasadas = computed(() => {
  const agora = new Date()
  return atividades.value.filter(a => new Date(a.dataEntrega) < agora).length
})

// Métodos
const carregarAtividades = async () => {
  try {
    loading.value = true
    atividades.value = await atividadesService.listar()
  } catch (error) {
    console.error('Erro ao carregar atividades:', error)
    notifications.error(error.response?.data?.message || 'Erro ao carregar atividades')
  } finally {
    loading.value = false
  }
}

const mostrarModalCriar = () => {
  atividadeSelecionada.value = null
  modoModal.value = 'create'
  mostrandoModal.value = true
}

const fecharModal = () => {
  mostrandoModal.value = false
  atividadeSelecionada.value = null
}

const salvarAtividade = async (dados) => {
  try {
    if (modoModal.value === 'create') {
      await atividadesService.criar(dados)
      notifications.success('Atividade criada com sucesso!')
    } else {
      await atividadesService.atualizar(atividadeSelecionada.value.id, dados)
      notifications.success('Atividade atualizada com sucesso!')
    }
    fecharModal()
    await carregarAtividades()
  } catch (error) {
    console.error('Erro ao salvar atividade:', error)
    notifications.error(error.response?.data?.message || 'Erro ao salvar atividade')
  }
}

const formatarData = (data) => {
  const date = new Date(data)
  return date.toLocaleDateString('pt-BR', { 
    day: '2-digit', 
    month: '2-digit', 
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const getStatusBadge = (atividade) => {
  const dataEntrega = new Date(atividade.dataEntrega)
  const agora = new Date()
  
  if (dataEntrega < agora) return 'danger'
  
  const diasRestantes = Math.ceil((dataEntrega - agora) / (1000 * 60 * 60 * 24))
  if (diasRestantes <= 2) return 'warning'
  
  return 'primary'
}

const getStatusTexto = (atividade) => {
  const dataEntrega = new Date(atividade.dataEntrega)
  const agora = new Date()
  
  if (dataEntrega < agora) return 'Atrasada'
  
  const diasRestantes = Math.ceil((dataEntrega - agora) / (1000 * 60 * 60 * 24))
  if (diasRestantes === 0) return 'Hoje'
  if (diasRestantes === 1) return 'Amanhã'
  if (diasRestantes <= 2) return 'Urgente'
  
  return 'Pendente'
}

// Inicialização
onMounted(() => {
  carregarAtividades()
})
</script>

<style scoped>
.stat-card { padding: 1.5rem; border-radius: 10px; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1); }
.atividade-card { cursor: pointer; transition: all 0.2s; }
.atividade-card:hover { transform: translateY(-4px); box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15) !important; }
</style>

