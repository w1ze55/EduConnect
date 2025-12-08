<template>
  <div class="container-fluid py-4">
    <div class="row mb-4 align-items-center">
      <div class="col">
        <h2 class="fw-bold">
          <i class="bi bi-calendar-event-fill me-2"></i>Calendario Escolar
        </h2>
        <p class="text-muted mb-0">Provas, eventos, reunioes e atividades visiveis para todos</p>
      </div>
      <div class="col text-end" v-if="podeGerenciar">
        <button class="btn btn-primary" @click="abrirFormulario()">
          <i class="bi bi-plus-circle me-2"></i>Novo evento
        </button>
      </div>
    </div>
    
    <div class="row g-4">
      <div class="col-12">
        <div class="card shadow-sm">
          <div class="card-body position-relative calendario-wrapper">
            <vue-cal 
              :events="eventosCalendario"
              :time="false"
              locale="pt-br"
              :disable-views="['years', 'year']"
              default-view="month"
              @cell-click="handleCellClick"
              @event-click="handleEventClick"
            />
            <div v-if="carregando" class="overlay-loading">
              <div class="spinner-border text-primary" role="status">
                <span class="visually-hidden">Carregando...</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="row g-4 mt-2">
      <div class="col-lg-6">
        <div class="card shadow-sm h-100">
          <div class="card-header bg-white d-flex align-items-center">
            <h6 class="mb-0 flex-grow-1">Legenda</h6>
          </div>
          <div class="card-body">
            <div class="row row-cols-2 row-cols-md-3 g-3">
              <div v-for="item in legenda" :key="item.value" class="d-flex align-items-center">
                <span :class="['badge me-2', `badge-${item.color}`]">
                  <i :class="item.icon"></i>
                </span>
                <span>{{ item.label }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <div class="col-lg-6">
        <div class="card shadow-sm h-100">
          <div class="card-header bg-white">
            <h6 class="mb-0">Proximos eventos</h6>
          </div>
          <div class="card-body">
            <div v-if="proximosEventos.length === 0" class="text-center text-muted py-3">
              Nenhum evento previsto
            </div>
            <div v-else class="row g-3">
              <div v-for="evento in proximosEventos" :key="evento.id" class="col-md-6">
                <div class="evento-item p-3 rounded border h-100 d-flex">
                  <div class="evento-date me-3" :class="`evento-${evento.tipo}`">
                    <div class="day">{{ evento.dia }}</div>
                    <div class="month">{{ evento.mes }}</div>
                  </div>
                  <div class="flex-grow-1">
                    <div class="d-flex align-items-center mb-1">
                      <i :class="[getTipoConfig(evento.tipo).icon, 'me-2 text-muted']"></i>
                      <h6 class="mb-0">{{ evento.titulo }}</h6>
                    </div>
                    <small class="text-muted d-block">{{ evento.horario }}</small>
                    <small class="text-muted">{{ evento.contexto }}</small>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal de detalhes -->
    <div v-if="modalDetalhesAberto" class="modal-overlay" role="dialog" aria-modal="true">
      <div class="modal-dialog">
        <div class="modal-content card shadow-lg">
          <div class="modal-header">
            <div>
              <div class="d-flex align-items-center">
                <span :class="['badge me-2', `badge-${getTipoConfig(eventoSelecionado.tipo).color}`]">
                  <i :class="getTipoConfig(eventoSelecionado.tipo).icon"></i>
                </span>
                <h5 class="modal-title mb-0">{{ eventoSelecionado.titulo }}</h5>
              </div>
              <small class="text-muted">Criado por {{ eventoSelecionado.criadoPorNome || 'N/I' }}</small>
            </div>
            <button type="button" class="btn-close" @click="fecharModalDetalhes"></button>
          </div>
          <div class="modal-body">
            <p class="text-muted">{{ eventoSelecionado.descricao || 'Sem descricao' }}</p>
            <div class="row g-3">
              <div class="col-md-6">
                <small class="text-muted d-block">Data</small>
                <strong>{{ formatarDataApresentacao(eventoSelecionado.data) }}</strong>
              </div>
              <div class="col-md-6">
                <small class="text-muted d-block">Tipo</small>
                <strong>{{ getTipoConfig(eventoSelecionado.tipo).label }}</strong>
              </div>
              <div class="col-md-6">
                <small class="text-muted d-block">Escola</small>
                <strong>{{ eventoSelecionado.escolaNome || 'Todas' }}</strong>
              </div>
              <div class="col-md-6">
                <small class="text-muted d-block">Turma</small>
                <strong>{{ eventoSelecionado.turmaNome || 'Todas' }}</strong>
              </div>
              <div class="col-md-6">
                <small class="text-muted d-block">Aluno</small>
                <strong>{{ eventoSelecionado.alunoNome || 'Todos' }}</strong>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-outline-secondary" @click="fecharModalDetalhes">Fechar</button>
            <button 
              v-if="podeEditar(eventoSelecionado)"
              type="button" 
              class="btn btn-outline-primary" 
              @click="editarEvento(eventoSelecionado)"
            >
              <i class="bi bi-pencil me-2"></i>Editar
            </button>
            <button 
              v-if="podeEditar(eventoSelecionado)"
              type="button" 
              class="btn btn-danger" 
              @click="removerEvento(eventoSelecionado)"
            >
              <i class="bi bi-trash me-2"></i>Excluir
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal de formulario -->
    <div v-if="modalFormularioAberto" class="modal-overlay" role="dialog" aria-modal="true">
      <div class="modal-dialog modal-form">
        <div class="modal-content card shadow-lg">
          <form @submit.prevent="salvarEvento">
            <div class="modal-header">
              <h5 class="modal-title mb-0">
                {{ eventoEditando ? 'Editar evento' : 'Novo evento' }}
              </h5>
              <button type="button" class="btn-close" @click="fecharFormulario"></button>
            </div>
            <div class="modal-body">
              <div class="mb-3">
                <label class="form-label">Nome do evento</label>
                <input type="text" class="form-control" v-model="formulario.titulo" required />
              </div>
              <div class="mb-3">
                <label class="form-label">Descricao</label>
                <textarea class="form-control" rows="3" v-model="formulario.descricao"></textarea>
              </div>
              <div class="row g-3">
                <div class="col-md-6">
                  <label class="form-label">Data</label>
                  <input type="datetime-local" class="form-control" v-model="formulario.data" required />
                </div>
                <div class="col-md-6">
                  <label class="form-label">Tipo</label>
                  <select class="form-select" v-model="formulario.tipo" required>
                    <option v-for="item in legenda" :key="item.value" :value="item.value">
                      {{ item.label }}
                    </option>
                  </select>
                </div>
              </div>
              <div class="row g-3 mt-1">
                <div class="col-md-12" v-if="isAdmin">
                  <label class="form-label">Escola (opcional)</label>
                  <select class="form-select" v-model="formulario.escolaId">
                    <option value="">Todas</option>
                    <option v-for="escola in escolas" :key="escola.id" :value="escola.id">
                      {{ escola.nome || escola.name || `Escola ${escola.id}` }}
                    </option>
                  </select>
                </div>
                <div class="col-md-6">
                  <label class="form-label">Turma (opcional)</label>
                  <select class="form-select" v-model="formulario.turmaId">
                    <option value="">Todas</option>
                    <option v-for="turma in turmas" :key="turma.id" :value="turma.id">
                      {{ turma.nome || turma.titulo || `Turma ${turma.id}` }}
                    </option>
                  </select>
                </div>
                <div class="col-md-6">
                  <label class="form-label">Aluno (opcional)</label>
                  <select class="form-select" v-model="formulario.alunoId">
                    <option value="">Todos</option>
                    <option v-for="aluno in alunos" :key="aluno.id" :value="aluno.id">
                      {{ aluno.nome || aluno.name || `Aluno ${aluno.id}` }}
                    </option>
                  </select>
                </div>
              </div>
              <small class="text-muted d-block mt-2">
                Se nenhuma turma ou aluno for selecionado, o evento sera exibido para toda a escola.
              </small>
            </div>
            <div class="modal-footer">
              <button type="button" class="btn btn-outline-secondary" @click="fecharFormulario">Cancelar</button>
              <button type="submit" class="btn btn-primary" :disabled="salvando">
                <span v-if="salvando" class="spinner-border spinner-border-sm me-2" role="status"></span>
                {{ eventoEditando ? 'Salvar alteracoes' : 'Criar evento' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import VueCal from 'vue-cal'
import 'vue-cal/dist/vuecal.css'
import { useAuthStore } from '@/stores/auth'
import { useNotificationStore } from '@/stores/notifications'
import calendarioService from '@/services/calendarioService'
import turmasService from '@/services/turmasService'
import usuariosService from '@/services/usuariosService'
import escolasService from '@/services/escolasService'

const authStore = useAuthStore()
const notifications = useNotificationStore()

const carregando = ref(false)
const salvando = ref(false)
const eventos = ref([])
const turmas = ref([])
const alunos = ref([])
const escolas = ref([])

const modalDetalhesAberto = ref(false)
const modalFormularioAberto = ref(false)
const eventoSelecionado = ref({})
const eventoEditando = ref(null)

const formulario = ref({
  titulo: '',
  descricao: '',
  data: '',
  tipo: 'EVENTO',
  turmaId: '',
  alunoId: '',
  escolaId: ''
})

const legenda = [
  { value: 'EVENTO', label: 'Evento', color: 'success', icon: 'bi bi-calendar2-event', short: '[E]', emoji: '🗓️' },
  { value: 'PROVA', label: 'Prova', color: 'primary', icon: 'bi bi-journal-check', short: '[P]', emoji: '📝' },
  { value: 'REUNIAO', label: 'Reuniao', color: 'warning', icon: 'bi bi-people', short: '[R]', emoji: '👥' },
  { value: 'ATIVIDADE', label: 'Atividade', color: 'info', icon: 'bi bi-clipboard-check', short: '[A]', emoji: '✅' }
]

const podeGerenciar = computed(() => {
  const role = authStore.userRole
  return role === 'PROFESSOR' || role === 'DIRETORIA' || role === 'ADMINISTRADOR'
})

const isAdmin = computed(() => authStore.userRole === 'ADMINISTRADOR')

const eventosCalendario = computed(() => {
  return eventos.value
    .map(evento => {
      const config = getTipoConfig(evento.tipo)
      const inicio = normalizarData(getDataEvento(evento))
      return {
        ...evento,
        start: inicio,
        end: inicio,
        title: `${config.short} ${evento.titulo}`,
        content: `${config.emoji || ''} ${evento.titulo}`,
        class: `cal-${config.color}`,
        original: evento
      }
    })
    .filter(evento => evento.start)
})

const proximosEventos = computed(() => {
  const agora = new Date()
  return eventos.value
    .map(e => ({ ...e, dataBase: getDataEvento(e) }))
    .map(e => ({ ...e, dataObjeto: normalizarData(e.dataBase) }))
    .filter(e => e.dataObjeto && e.dataObjeto >= agora)
    .sort((a, b) => a.dataObjeto - b.dataObjeto)
    .slice(0, 6)
    .map(e => {
      const data = e.dataObjeto
      return {
        id: e.id,
        titulo: e.titulo,
        tipo: e.tipo,
        dia: String(data.getDate()).padStart(2, '0'),
        mes: data.toLocaleString('pt-BR', { month: 'short' }).toUpperCase(),
        horario: data.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' }),
        contexto: e.escolaNome || e.turmaNome || e.alunoNome || 'Toda a escola'
      }
    })
})

const getTipoConfig = (tipo) => {
  return legenda.find(l => l.value === tipo) || legenda[0]
}

const getDataEvento = (evento) => {
  return evento.data || evento.dataEvento || evento.dataInicio || evento.inicio || evento.start
}

const normalizarData = (data) => {
  if (!data) return null
  const parsed = new Date(data)
  return isNaN(parsed) ? null : parsed
}

const formatarDataApresentacao = (data) => {
  if (!data) return 'N/I'
  return new Date(data).toLocaleString('pt-BR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const dataParaInput = (data) => {
  const d = data ? new Date(data) : new Date()
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`
}

const abrirFormulario = (dataBase) => {
  if (!podeGerenciar.value) return
  eventoEditando.value = null
  formulario.value = {
    titulo: '',
    descricao: '',
    data: dataParaInput(dataBase),
    tipo: 'EVENTO',
    turmaId: '',
    alunoId: '',
    escolaId: ''
  }
  modalFormularioAberto.value = true
}

const fecharFormulario = () => {
  modalFormularioAberto.value = false
  eventoEditando.value = null
}

const handleCellClick = (payload) => {
  if (!podeGerenciar.value) return
  const data = payload.date || payload
  abrirFormulario(data)
}

const handleEventClick = (evento) => {
  eventoSelecionado.value = evento.original || evento
  modalDetalhesAberto.value = true
}

const fecharModalDetalhes = () => {
  modalDetalhesAberto.value = false
  eventoSelecionado.value = {}
}

const podeEditar = (evento) => {
  const role = authStore.userRole
  if (role === 'ADMINISTRADOR' || role === 'DIRETORIA') return true
  if (role === 'PROFESSOR') {
    const criadorId = evento.criadoPorId || evento.criadorId || evento.userId || evento.professorId
    return criadorId && authStore.user?.id && criadorId === authStore.user.id
  }
  return false
}

const editarEvento = (evento) => {
  if (!podeEditar(evento)) return
  eventoEditando.value = evento
  formulario.value = {
    titulo: evento.titulo,
    descricao: evento.descricao,
    data: dataParaInput(getDataEvento(evento)),
    tipo: evento.tipo,
    turmaId: evento.turmaId || '',
    alunoId: evento.alunoId || '',
    escolaId: evento.escolaId || ''
  }
  modalDetalhesAberto.value = false
  modalFormularioAberto.value = true
}

const salvarEvento = async () => {
  try {
    salvando.value = true
    const payload = {
      titulo: formulario.value.titulo,
      descricao: formulario.value.descricao,
      data: formulario.value.data,
      tipo: formulario.value.tipo,
      turmaId: formulario.value.turmaId || null,
      alunoId: formulario.value.alunoId || null,
      escolaId: formulario.value.escolaId || null
    }

    if (eventoEditando.value?.id) {
      await calendarioService.atualizarEvento(eventoEditando.value.id, payload)
      notifications.success('Evento atualizado com sucesso!')
    } else {
      await calendarioService.criarEvento(payload)
      notifications.success('Evento criado com sucesso e visivel para todos!')
    }

    fecharFormulario()
    await carregarEventos()
  } catch (error) {
    console.error('Erro ao salvar evento:', error)
    notifications.error(error.response?.data?.message || 'Erro ao salvar evento')
  } finally {
    salvando.value = false
  }
}

const removerEvento = async (evento) => {
  try {
    if (!podeEditar(evento)) return
    await calendarioService.deletarEvento(evento.id)
    notifications.success('Evento removido')
    fecharModalDetalhes()
    await carregarEventos()
  } catch (error) {
    console.error('Erro ao remover evento:', error)
    notifications.error(error.response?.data?.message || 'Erro ao remover evento')
  }
}

const carregarEventos = async () => {
  try {
    carregando.value = true
    const response = await calendarioService.getEventos()
    const payload = response?.data ?? response ?? []
    eventos.value = Array.isArray(payload) ? payload : payload.eventos || []
  } catch (error) {
    console.error('Erro ao carregar eventos:', error)
    notifications.error(error.response?.data?.message || 'Erro ao carregar calendario')
  } finally {
    carregando.value = false
  }
}

const carregarListasAuxiliares = async () => {
  if (!podeGerenciar.value) return
  try {
    const [turmasResponse, alunosResponse, escolasResponse] = await Promise.allSettled([
      turmasService.listarTurmas(),
      usuariosService.listarPorRole ? usuariosService.listarPorRole('ALUNO') : usuariosService.getAlunos(),
      isAdmin.value ? escolasService.listarTodas() : Promise.resolve([])
    ])

    turmas.value = turmasResponse.status === 'fulfilled'
      ? (turmasResponse.value.data || turmasResponse.value || [])
      : []

    const alunosData = alunosResponse.status === 'fulfilled'
      ? (alunosResponse.value.data || alunosResponse.value || [])
      : []
    alunos.value = Array.isArray(alunosData) ? alunosData : []

    const escolasData = escolasResponse.status === 'fulfilled'
      ? (escolasResponse.value.data || escolasResponse.value || [])
      : []
    escolas.value = Array.isArray(escolasData) ? escolasData : []
  } catch (error) {
    console.error('Erro ao carregar listas auxiliares:', error)
  }
}

onMounted(async () => {
  await carregarEventos()
  await carregarListasAuxiliares()
})
</script>

<style scoped>
.calendario-wrapper {
  min-height: 540px;
}

.vuecal {
  --vuecal-primary: #0d6efd;
  font-size: 0.95rem;
}

.vuecal__weekdays-headings,
.vuecal__title-bar {
  font-weight: 600;
}

.vuecal__event {
  border: none;
  color: #fff;
  font-weight: 700;
  font-size: 0.9rem;
  padding: 4px 6px;
  display: flex;
  align-items: center;
  gap: 6px;
}
.vuecal__event i {
  font-size: 1rem;
}
.vuecal__event.cal-primary { background: #0d6efd; }
.vuecal__event.cal-success { background: #198754; }
.vuecal__event.cal-warning { background: #ffc107; color: #212529; }
.vuecal__event.cal-info { background: #0dcaf0; color: #212529; }

.evento-date {
  background: #e9ecef;
  color: #212529;
  padding: 0.5rem;
  border-radius: 8px;
  text-align: center;
  min-width: 52px;
}
.evento-date .day { font-size: 1.25rem; font-weight: bold; line-height: 1; }
.evento-date .month { font-size: 0.7rem; text-transform: uppercase; }
.evento-PROVA { background: #0d6efd; color: #fff; }
.evento-EVENTO { background: #198754; color: #fff; }
.evento-REUNIAO { background: #ffc107; color: #212529; }
.evento-ATIVIDADE { background: #0dcaf0; color: #212529; }

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(2px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  padding: 1rem;
}

.modal-dialog {
  max-width: 680px;
  width: 100%;
  margin: 2.5rem auto;
}
.modal-dialog.modal-form {
  max-width: 760px;
  width: 100%;
}

.modal-content {
  background: #fff;
  border: none;
  border-radius: 12px;
  overflow: hidden;
  padding: 1.5rem;
}

.modal-header {
  border-bottom: 1px solid #e9ecef;
}

.modal-footer {
  border-top: 1px solid #e9ecef;
  gap: 0.75rem;
}

.badge-primary { background-color: #0d6efd; }
.badge-success { background-color: #198754; }
.badge-warning { background-color: #ffc107; color: #212529; }
.badge-info { background-color: #0dcaf0; color: #212529; }

.overlay-loading {
  position: absolute;
  inset: 0;
  background: rgba(255, 255, 255, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;
}
</style>
