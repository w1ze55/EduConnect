<template>
  <div class="calendario-page">
    <div class="container-fluid py-4 py-xl-5">
      <section class="calendar-hero mb-4">
        <div class="calendar-hero__content">
          <div class="calendar-hero__copy">
            <span class="calendar-hero__eyebrow">Planejamento escolar</span>
            <h2 class="calendar-hero__title">
              <i class="bi bi-calendar-event-fill me-2"></i>Calendario Escolar
            </h2>
            <p class="calendar-hero__subtitle">
              Provas, reunioes, atividades e eventos organizados em uma agenda mais clara para toda a comunidade.
            </p>
          </div>

          <div class="calendar-hero__cta">
            <div class="calendar-mode-pill">
              <i class="bi bi-person-badge me-2"></i>{{ modoCalendarioLabel }}
            </div>
            <button v-if="podeGerenciar" class="btn btn-light calendar-hero__button" @click="abrirFormulario()">
              <i class="bi bi-plus-circle me-2"></i>Novo evento
            </button>
          </div>
        </div>

        <div class="row g-3 calendar-stats">
          <div class="col-sm-6 col-xl-3">
            <div class="calendar-stat-card">
              <span class="calendar-stat-card__label">Eventos cadastrados</span>
              <strong class="calendar-stat-card__value">{{ eventos.length }}</strong>
              <small class="calendar-stat-card__meta">{{ eventosNoMesAtual }} neste mes</small>
            </div>
          </div>
          <div class="col-sm-6 col-xl-3">
            <div class="calendar-stat-card">
              <span class="calendar-stat-card__label">Proximos 30 dias</span>
              <strong class="calendar-stat-card__value">{{ eventosProximos30Dias }}</strong>
              <small class="calendar-stat-card__meta">{{ proximosEventos.length }} em destaque</small>
            </div>
          </div>
          <div class="col-sm-6 col-xl-3">
            <div class="calendar-stat-card">
              <span class="calendar-stat-card__label">Proximo compromisso</span>
              <strong class="calendar-stat-card__value calendar-stat-card__value--small">{{ proximoEventoDestaqueData }}</strong>
              <small class="calendar-stat-card__meta">{{ proximoEventoDestaqueTitulo }}</small>
            </div>
          </div>
          <div class="col-sm-6 col-xl-3">
            <div class="calendar-stat-card">
              <span class="calendar-stat-card__label">{{ resumoAcessoLabel }}</span>
              <strong class="calendar-stat-card__value calendar-stat-card__value--small">{{ resumoAcessoValue }}</strong>
              <small class="calendar-stat-card__meta">{{ resumoAcessoDescricao }}</small>
            </div>
          </div>
        </div>
      </section>
    
      <div class="row g-4 align-items-start">
        <div class="col-xl-8">
          <div class="calendar-panel card shadow-sm border-0">
            <div class="card-body p-0">
              <div class="calendar-panel__toolbar">
                <div>
                  <span class="calendar-panel__eyebrow">Visao mensal</span>
                  <h5 class="calendar-panel__title">Agenda da comunidade escolar</h5>
                  <p class="calendar-panel__hint mb-0">
                    Clique em um evento para abrir os detalhes
                    <span v-if="podeGerenciar">ou em qualquer dia para criar um novo item.</span>
                  </p>
                </div>
                <div class="calendar-panel__badge-group">
                  <span class="calendar-outline-pill">
                    <i class="bi bi-stars me-2"></i>{{ legenda.length }} categorias
                  </span>
                </div>
              </div>

              <div class="calendar-panel__body position-relative calendario-wrapper">
                <vue-cal
                  class="calendar-ui"
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

        <div class="col-xl-4">
          <div class="sidebar-stack">
            <div v-if="podeGerenciar" class="calendar-info-card google-calendar-card card shadow-sm border-0">
              <div class="card-body">
                <div class="calendar-info-card__header">
                  <div class="calendar-info-card__icon">
                    <i class="bi bi-google"></i>
                  </div>
                  <span :class="['status-pill', googleCalendarBadgeClass]">{{ googleCalendarBadgeLabel }}</span>
                </div>

                <h5 class="calendar-info-card__title">Google Calendar em sincronia</h5>
                <p class="calendar-info-card__text">{{ googleCalendarResumo }}</p>

                <div class="sync-metrics" v-if="googleCalendarStatus.connected">
                  <div class="sync-metric">
                    <span class="sync-metric__label">Calendario</span>
                    <strong class="sync-metric__value">{{ googleCalendarStatus.calendarId || 'primary' }}</strong>
                  </div>
                  <div class="sync-metric">
                    <span class="sync-metric__label">Eventos</span>
                    <strong class="sync-metric__value">
                      {{ googleCalendarStatus.eventosSincronizados || 0 }}/{{ googleCalendarStatus.eventosCriados || 0 }}
                    </strong>
                  </div>
                </div>
                <p v-else-if="googleCalendarStatus.configured" class="calendar-info-card__support">
                  Ao conectar sua conta, eventos novos e alterados serao sincronizados automaticamente.
                </p>
                <p v-else class="calendar-info-card__support">
                  Configure as envs do Google Calendar no backend para habilitar a integracao.
                </p>

                <div class="calendar-info-card__actions">
                  <button
                    v-if="!googleCalendarStatus.connected"
                    class="btn btn-outline-primary"
                    @click="conectarGoogleCalendar"
                    :disabled="conectandoGoogleCalendar || !googleCalendarStatus.configured"
                  >
                    <span v-if="conectandoGoogleCalendar" class="spinner-border spinner-border-sm me-2" role="status"></span>
                    <i v-else class="bi bi-link-45deg me-2"></i>Conectar conta
                  </button>

                  <button
                    v-if="googleCalendarStatus.connected"
                    class="btn btn-primary"
                    @click="sincronizarGoogleCalendar"
                    :disabled="sincronizandoGoogleCalendar"
                  >
                    <span v-if="sincronizandoGoogleCalendar" class="spinner-border spinner-border-sm me-2" role="status"></span>
                    <i v-else class="bi bi-arrow-repeat me-2"></i>Sincronizar eventos
                  </button>

                  <button
                    v-if="googleCalendarStatus.connected"
                    class="btn btn-outline-danger"
                    @click="desconectarGoogleCalendar"
                  >
                    <i class="bi bi-x-circle me-2"></i>Desconectar
                  </button>
                </div>
              </div>
            </div>

            <div class="calendar-info-card card shadow-sm border-0">
              <div class="card-body">
                <div class="section-heading">
                  <div>
                    <span class="section-heading__eyebrow">Legenda</span>
                    <h6 class="section-heading__title mb-0">Tipos de evento</h6>
                  </div>
                </div>

                <div class="legend-grid">
                  <div v-for="item in legenda" :key="item.value" class="legend-chip">
                    <span :class="['legend-chip__icon', `legend-chip__icon--${item.value}`]">
                      <i :class="item.icon"></i>
                    </span>
                    <div>
                      <strong>{{ item.label }}</strong>
                      <small>{{ item.short }}</small>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div class="calendar-info-card card shadow-sm border-0">
              <div class="card-body">
                <div class="section-heading">
                  <div>
                    <span class="section-heading__eyebrow">Radar</span>
                    <h6 class="section-heading__title mb-0">Proximos eventos</h6>
                  </div>
                  <span class="calendar-outline-pill">{{ proximosEventos.length }}</span>
                </div>

                <div v-if="proximosEventos.length === 0" class="empty-state">
                  <i class="bi bi-calendar2-week"></i>
                  <p class="mb-0">Nenhum evento previsto no momento.</p>
                </div>

                <div v-else class="upcoming-list">
                  <article v-for="evento in proximosEventos" :key="evento.id" class="upcoming-event">
                    <div class="evento-date" :class="`evento-${evento.tipo}`">
                      <div class="day">{{ evento.dia }}</div>
                      <div class="month">{{ evento.mes }}</div>
                    </div>

                    <div class="upcoming-event__content">
                      <div class="upcoming-event__topline">
                        <span :class="['event-type-tag', `event-type-tag--${evento.tipo}`]">{{ evento.tipoLabel }}</span>
                        <small>{{ evento.horario }}</small>
                      </div>

                      <h6 class="upcoming-event__title">{{ evento.titulo }}</h6>
                      <p class="upcoming-event__context">{{ evento.contexto }}</p>
                    </div>
                  </article>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Modal de detalhes -->
      <div v-if="modalDetalhesAberto" class="modal-overlay" role="dialog" aria-modal="true" @click.self="fecharModalDetalhes">
        <div class="modal-dialog">
          <div class="modal-content card shadow-lg modal-sheet details-modal">
            <div class="modal-header modal-sheet__header">
              <div>
                <div class="details-title-row">
                  <span :class="['type-pill me-2', `type-pill--${eventoSelecionado.tipo}`]">
                    <i :class="getTipoConfig(eventoSelecionado.tipo).icon"></i>
                  </span>
                  <h5 class="modal-title mb-0">{{ eventoSelecionado.titulo }}</h5>
                </div>
                <small class="modal-subtitle">Criado por {{ eventoSelecionado.criadoPorNome || 'N/I' }}</small>
              </div>
              <button type="button" class="btn-close" @click="fecharModalDetalhes"></button>
            </div>
            <div class="modal-body modal-sheet__body">
              <div class="detail-summary">
                {{ eventoSelecionado.descricao || 'Sem descricao cadastrada para este evento.' }}
              </div>
              <div class="row g-3">
                <div class="col-md-6">
                  <div class="detail-card">
                    <small>Data</small>
                    <strong>{{ formatarDataApresentacao(eventoSelecionado.data) }}</strong>
                  </div>
                </div>
                <div class="col-md-6">
                  <div class="detail-card">
                    <small>Tipo</small>
                    <strong>{{ getTipoConfig(eventoSelecionado.tipo).label }}</strong>
                  </div>
                </div>
                <div class="col-md-6">
                  <div class="detail-card">
                    <small>Escola</small>
                    <strong>{{ eventoSelecionado.escolaNome || 'Todas' }}</strong>
                  </div>
                </div>
                <div class="col-md-6">
                  <div class="detail-card">
                    <small>Turma</small>
                    <strong>{{ eventoSelecionado.turmaNome || 'Todas' }}</strong>
                  </div>
                </div>
                <div class="col-md-6">
                  <div class="detail-card">
                    <small>Aluno</small>
                    <strong>{{ eventoSelecionado.alunoNome || 'Todos' }}</strong>
                  </div>
                </div>
                <div class="col-md-6" v-if="podeEditar(eventoSelecionado)">
                  <div class="detail-card">
                    <small>Google Calendar</small>
                    <strong :class="{ 'text-warning': eventoSelecionado.googleCalendarSyncError }">
                      {{ getGoogleSyncStatusLabel(eventoSelecionado) }}
                    </strong>
                    <span v-if="eventoSelecionado.googleCalendarLastSyncedAt" class="detail-card__support">
                      Ultima sync: {{ formatarDataApresentacao(eventoSelecionado.googleCalendarLastSyncedAt) }}
                    </span>
                    <span v-else-if="eventoSelecionado.googleCalendarSyncError" class="detail-card__support">
                      {{ eventoSelecionado.googleCalendarSyncError }}
                    </span>
                  </div>
                </div>
              </div>
            </div>
            <div class="modal-footer modal-sheet__footer">
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
      <div v-if="modalFormularioAberto" class="modal-overlay" role="dialog" aria-modal="true" @click.self="fecharFormulario">
        <div class="modal-dialog modal-form">
          <div class="modal-content card shadow-lg modal-sheet form-modal">
            <form @submit.prevent="salvarEvento">
              <div class="modal-header modal-sheet__header">
                <div>
                  <span class="modal-kicker">{{ eventoEditando ? 'Atualizacao' : 'Criacao de evento' }}</span>
                  <h5 class="modal-title mb-0">
                    {{ eventoEditando ? 'Editar evento' : 'Novo evento' }}
                  </h5>
                </div>
                <button type="button" class="btn-close" @click="fecharFormulario"></button>
              </div>
              <div class="modal-body modal-sheet__body">
                <div class="form-section">
                  <div class="form-section__header">
                    <h6 class="mb-1">Informacoes principais</h6>
                    <p class="mb-0">Defina nome, data e categoria do compromisso.</p>
                  </div>

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
                </div>

                <div class="form-section">
                  <div class="form-section__header">
                    <h6 class="mb-1">Publico do evento</h6>
                    <p class="mb-0">Use os filtros abaixo para limitar o alcance do aviso.</p>
                  </div>

                  <div class="row g-3">
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
                </div>

                <small class="form-help d-block mt-3">
                  Se nenhuma turma ou aluno for selecionado, o evento sera exibido para toda a escola.
                </small>
              </div>
              <div class="modal-footer modal-sheet__footer">
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
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import VueCal from 'vue-cal'
import 'vue-cal/dist/vuecal.css'
import { useAuthStore } from '@/stores/auth'
import { useNotificationStore } from '@/stores/notifications'
import calendarioService from '@/services/calendarioService'
import googleCalendarService from '@/services/googleCalendarService'
import turmasService from '@/services/turmasService'
import usuariosService from '@/services/usuariosService'
import escolasService from '@/services/escolasService'

const authStore = useAuthStore()
const notifications = useNotificationStore()
const route = useRoute()
const router = useRouter()

const carregando = ref(false)
const salvando = ref(false)
const eventos = ref([])
const turmas = ref([])
const alunos = ref([])
const escolas = ref([])
const googleCalendarStatus = ref({
  configured: false,
  connected: false,
  eventosCriados: 0,
  eventosSincronizados: 0
})
const conectandoGoogleCalendar = ref(false)
const sincronizandoGoogleCalendar = ref(false)

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

const googleCalendarBadgeLabel = computed(() => {
  if (!googleCalendarStatus.value.configured) return 'Nao configurado'
  if (googleCalendarStatus.value.connected) return 'Conectado'
  return 'Disponivel'
})

const googleCalendarBadgeClass = computed(() => {
  if (!googleCalendarStatus.value.configured) return 'status-pill--secondary'
  if (googleCalendarStatus.value.connected) return 'status-pill--success'
  return 'status-pill--warning'
})

const googleCalendarResumo = computed(() => {
  if (!googleCalendarStatus.value.configured) {
    return 'A integracao depende das credenciais OAuth do Google configuradas no backend.'
  }

  if (googleCalendarStatus.value.connected) {
    return 'Sua conta Google esta conectada e seus eventos do EduConnect podem ser mantidos em sincronia.'
  }

  return 'Conecte sua conta para espelhar no Google Calendar os eventos que voce cria no EduConnect.'
})

const eventosComData = computed(() => {
  return eventos.value
    .map(evento => ({ ...evento, dataObjeto: normalizarData(getDataEvento(evento)) }))
    .filter(evento => evento.dataObjeto)
})

const eventosNoMesAtual = computed(() => {
  const hoje = new Date()
  return eventosComData.value.filter(evento => {
    return (
      evento.dataObjeto.getMonth() === hoje.getMonth() &&
      evento.dataObjeto.getFullYear() === hoje.getFullYear()
    )
  }).length
})

const eventosProximos30Dias = computed(() => {
  const inicio = new Date()
  const fim = new Date(inicio)
  fim.setDate(fim.getDate() + 30)

  return eventosComData.value.filter(evento => evento.dataObjeto >= inicio && evento.dataObjeto <= fim).length
})

const proximoEventoDestaque = computed(() => {
  const agora = new Date()
  return [...eventosComData.value]
    .filter(evento => evento.dataObjeto >= agora)
    .sort((a, b) => a.dataObjeto - b.dataObjeto)[0] || null
})

const proximoEventoDestaqueData = computed(() => {
  if (!proximoEventoDestaque.value) return 'Agenda livre'

  return proximoEventoDestaque.value.dataObjeto.toLocaleString('pt-BR', {
    day: '2-digit',
    month: 'short',
    hour: '2-digit',
    minute: '2-digit'
  })
})

const proximoEventoDestaqueTitulo = computed(() => {
  return proximoEventoDestaque.value?.titulo || 'Sem novos compromissos'
})

const modoCalendarioLabel = computed(() => {
  switch (authStore.userRole) {
    case 'ADMINISTRADOR':
      return 'Gestao total'
    case 'DIRETORIA':
      return 'Gestao escolar'
    case 'PROFESSOR':
      return 'Edicao propria'
    default:
      return 'Consulta geral'
  }
})

const resumoAcessoLabel = computed(() => {
  return podeGerenciar.value ? 'Integracao Google' : 'Modo de acesso'
})

const resumoAcessoValue = computed(() => {
  return podeGerenciar.value ? googleCalendarBadgeLabel.value : modoCalendarioLabel.value
})

const resumoAcessoDescricao = computed(() => {
  return podeGerenciar.value
    ? googleCalendarResumo.value
    : 'Os eventos sao exibidos de acordo com o seu perfil e permissoes no sistema.'
})

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
        tipoLabel: getTipoConfig(e.tipo).label,
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

const getGoogleSyncStatusLabel = (evento) => {
  if (!evento?.googleCalendarEventId) return 'Nao sincronizado'
  if (evento.googleCalendarSyncError) return 'Falha na sincronizacao'
  return 'Sincronizado'
}

const limparQueryGoogleCalendar = async () => {
  const query = { ...route.query }
  delete query.googleCalendar
  delete query.googleCalendarMessage
  delete query.googleCalendarSynced
  delete query.googleCalendarFailed
  await router.replace({ query })
}

const tratarRetornoGoogleCalendar = async () => {
  const status = Array.isArray(route.query.googleCalendar) ? route.query.googleCalendar[0] : route.query.googleCalendar
  if (!status) return

  const message = Array.isArray(route.query.googleCalendarMessage)
    ? route.query.googleCalendarMessage[0]
    : route.query.googleCalendarMessage
  const failed = Number(Array.isArray(route.query.googleCalendarFailed)
    ? route.query.googleCalendarFailed[0]
    : route.query.googleCalendarFailed || 0)

  if (status === 'connected') {
    if (failed > 0) {
      notifications.warning(message || 'Conta conectada, mas alguns eventos nao puderam ser sincronizados.')
    } else {
      notifications.success(message || 'Conta Google Calendar conectada com sucesso!')
    }
  } else {
    notifications.error(message || 'Nao foi possivel conectar com Google Calendar.')
  }

  await limparQueryGoogleCalendar()
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

    let response
    if (eventoEditando.value?.id) {
      response = await calendarioService.atualizarEvento(eventoEditando.value.id, payload)
      notifications.success('Evento atualizado com sucesso!')
    } else {
      response = await calendarioService.criarEvento(payload)
      notifications.success('Evento criado com sucesso e visivel para todos!')
    }

    const eventoSalvo = response?.data ?? response
    if (eventoSalvo?.googleCalendarSyncError) {
      notifications.warning(`Evento salvo, mas a sincronizacao com Google Calendar falhou: ${eventoSalvo.googleCalendarSyncError}`)
    } else if (eventoSalvo?.googleCalendarSynced) {
      notifications.info('Evento sincronizado com Google Calendar.')
    }

    fecharFormulario()
    await carregarEventos()
    await carregarStatusGoogleCalendar()
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
    await carregarStatusGoogleCalendar()
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

const carregarStatusGoogleCalendar = async () => {
  if (!podeGerenciar.value) return

  try {
    const response = await googleCalendarService.getStatus()
    googleCalendarStatus.value = response?.data ?? response ?? googleCalendarStatus.value
  } catch (error) {
    console.error('Erro ao carregar status do Google Calendar:', error)
  }
}

const conectarGoogleCalendar = async () => {
  try {
    conectandoGoogleCalendar.value = true
    const response = await googleCalendarService.connect()
    const url = response?.data?.url || response?.url

    if (!url) {
      throw new Error('URL de autorizacao nao retornada pelo backend.')
    }

    window.location.href = url
  } catch (error) {
    console.error('Erro ao iniciar conexao com Google Calendar:', error)
    notifications.error(error.response?.data?.message || error.message || 'Nao foi possivel iniciar a conexao com Google Calendar.')
  } finally {
    conectandoGoogleCalendar.value = false
  }
}

const sincronizarGoogleCalendar = async () => {
  try {
    sincronizandoGoogleCalendar.value = true
    const response = await googleCalendarService.sync()
    const payload = response?.data ?? response

    if ((payload?.falhas || 0) > 0) {
      notifications.warning(payload?.mensagem || 'Sincronizacao concluida com falhas.')
    } else {
      notifications.success(payload?.mensagem || 'Eventos sincronizados com Google Calendar.')
    }

    await carregarEventos()
    await carregarStatusGoogleCalendar()
  } catch (error) {
    console.error('Erro ao sincronizar eventos com Google Calendar:', error)
    notifications.error(error.response?.data?.message || 'Nao foi possivel sincronizar os eventos com Google Calendar.')
  } finally {
    sincronizandoGoogleCalendar.value = false
  }
}

const desconectarGoogleCalendar = async () => {
  try {
    await googleCalendarService.disconnect()
    notifications.success('Conta Google Calendar desconectada.')
    await carregarEventos()
    await carregarStatusGoogleCalendar()
  } catch (error) {
    console.error('Erro ao desconectar Google Calendar:', error)
    notifications.error(error.response?.data?.message || 'Nao foi possivel desconectar o Google Calendar.')
  }
}

onMounted(async () => {
  await carregarEventos()
  await carregarListasAuxiliares()
  await carregarStatusGoogleCalendar()
  await tratarRetornoGoogleCalendar()
})
</script>

<style scoped>
.calendario-page {
  min-height: 100%;
  background: #f4f7fb;
  color: #17212b;
}

.calendar-hero {
  border: 1px solid #dbe4ee;
  border-radius: 8px;
  padding: 2rem;
  background: #ffffff;
  box-shadow: 0 12px 30px rgba(21, 32, 43, 0.08);
}

.calendar-hero__content {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 1.5rem;
  margin-bottom: 1.5rem;
}

.calendar-hero__copy {
  min-width: 0;
}

.calendar-hero__eyebrow,
.calendar-panel__eyebrow,
.section-heading__eyebrow,
.modal-kicker {
  display: inline-block;
  margin-bottom: 0.55rem;
  color: #0f766e;
  font-size: 0.76rem;
  font-weight: 800;
  letter-spacing: 0;
  text-transform: uppercase;
}

.calendar-hero__title {
  margin: 0;
  color: #17212b;
  font-size: 2.25rem;
  font-weight: 800;
  line-height: 1.12;
}

.calendar-hero__subtitle {
  margin: 0.75rem 0 0;
  max-width: 44rem;
  color: #5f6d7a;
  font-size: 1rem;
}

.calendar-hero__cta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: flex-end;
  gap: 0.75rem;
}

.calendar-hero__button {
  min-width: 11rem;
  border: 1px solid #0f766e;
  border-radius: 8px;
  padding: 0.8rem 1.15rem;
  background: #0f766e;
  color: #ffffff;
  font-weight: 700;
}

.calendar-hero__button:hover {
  background: #0b5f59;
  border-color: #0b5f59;
  color: #ffffff;
}

.calendar-mode-pill,
.calendar-outline-pill,
.status-pill,
.event-type-tag,
.type-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 0.35rem;
  border-radius: 8px;
  font-size: 0.78rem;
  font-weight: 700;
  letter-spacing: 0;
}

.calendar-mode-pill {
  padding: 0.65rem 0.9rem;
  background: #e8f5f3;
  border: 1px solid #b8ddd8;
  color: #0f766e;
}

.calendar-stat-card {
  height: 100%;
  padding: 1rem;
  border: 1px solid #dbe4ee;
  border-left: 4px solid #0f766e;
  border-radius: 8px;
  background: #fbfcfe;
}

.calendar-stat-card__label {
  display: block;
  margin-bottom: 0.5rem;
  color: #5f6d7a;
  font-size: 0.82rem;
}

.calendar-stat-card__value {
  display: block;
  margin-bottom: 0.4rem;
  color: #17212b;
  font-size: 1.9rem;
  line-height: 1;
  font-weight: 800;
}

.calendar-stat-card__value--small {
  font-size: 1.15rem;
  line-height: 1.25;
}

.calendar-stat-card__meta {
  display: block;
  color: #6f7c88;
  font-size: 0.84rem;
}

.calendar-panel,
.calendar-info-card {
  border: 1px solid #dbe4ee;
  border-radius: 8px;
  overflow: hidden;
  background: #ffffff;
  box-shadow: 0 10px 24px rgba(21, 32, 43, 0.07);
}

.calendar-panel__toolbar {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 1rem;
  padding: 1.4rem 1.5rem 1.1rem;
  border-bottom: 1px solid #e2e8f0;
  background: #ffffff;
}

.calendar-panel__title,
.calendar-info-card__title,
.section-heading__title,
.modal-title {
  color: #17212b;
  font-weight: 800;
}

.calendar-panel__hint {
  max-width: 36rem;
  color: #5f6d7a;
}

.calendar-outline-pill {
  min-width: 2.5rem;
  padding: 0.5rem 0.8rem;
  border: 1px solid #cfd9e5;
  color: #334155;
  background: #f8fafc;
}

.calendar-panel__body {
  padding: 1rem;
}

.calendario-wrapper {
  min-height: 640px;
}

.calendar-ui {
  border: 1px solid #dbe4ee;
  border-radius: 8px;
  overflow: hidden;
  background: #ffffff;
}

.calendar-ui :deep(.vuecal) {
  --vuecal-primary: #0f766e;
  font-size: 0.95rem;
  border: none;
  background: #ffffff;
}

.calendar-ui :deep(.vuecal__title-bar) {
  padding: 1rem 1.1rem;
  background: #f8fafc;
  border-bottom: 1px solid #e2e8f0;
  color: #17212b;
  font-weight: 800;
}

.calendar-ui :deep(.vuecal__arrow),
.calendar-ui :deep(.vuecal__menu) {
  color: #0f766e;
}

.calendar-ui :deep(.vuecal__weekdays-headings) {
  background: #eef6f5;
  color: #0f766e;
  text-transform: uppercase;
  font-size: 0.72rem;
  letter-spacing: 0;
  font-weight: 800;
}

.calendar-ui :deep(.vuecal__cell) {
  border-color: #e5edf5;
  background: #ffffff;
  transition: background-color 0.2s ease;
}

.calendar-ui :deep(.vuecal__cell-content) {
  padding: 0.55rem;
}

.calendar-ui :deep(.vuecal__cell-date) {
  color: #17212b;
  font-weight: 700;
}

.calendar-ui :deep(.vuecal__cell:hover) {
  background: #f2f8f7;
}

.calendar-ui :deep(.vuecal__cell--today) {
  background: #fff7df;
}

.calendar-ui :deep(.vuecal__event) {
  border: none;
  border-radius: 8px;
  color: #ffffff;
  font-weight: 700;
  font-size: 0.82rem;
  padding: 0.38rem 0.55rem;
  box-shadow: none;
}

.calendar-ui :deep(.vuecal__event.cal-primary) {
  background: #2563eb;
}

.calendar-ui :deep(.vuecal__event.cal-success) {
  background: #198754;
}

.calendar-ui :deep(.vuecal__event.cal-warning) {
  background: #ffc107;
  color: #2d2300;
}

.calendar-ui :deep(.vuecal__event.cal-info) {
  background: #0ea5b8;
  color: #ffffff;
}

.sidebar-stack {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.calendar-info-card .card-body {
  padding: 1.35rem;
}

.calendar-info-card__header,
.section-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  margin-bottom: 1rem;
}

.calendar-info-card__icon {
  width: 3rem;
  height: 3rem;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #c7d2fe;
  border-radius: 8px;
  background: #eef2ff;
  color: #2563eb;
  font-size: 1.25rem;
}

.google-calendar-card {
  border-top: 4px solid #2563eb;
}

.calendar-info-card__text,
.calendar-info-card__support,
.form-section__header p,
.modal-subtitle {
  color: #5f6d7a;
}

.calendar-info-card__text {
  margin-bottom: 1rem;
}

.sync-metrics {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0.75rem;
  margin-bottom: 1rem;
}

.sync-metric {
  padding: 0.9rem 1rem;
  border: 1px solid #dbe4ee;
  border-radius: 8px;
  background: #f8fafc;
}

.sync-metric__label,
.detail-card small,
.legend-chip small {
  display: block;
  margin-bottom: 0.35rem;
  color: #6f7c88;
  font-size: 0.78rem;
}

.sync-metric__value {
  color: #17212b;
  font-size: 0.95rem;
  font-weight: 700;
  word-break: break-word;
}

.calendar-info-card__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;
}

.calendar-info-card__actions .btn,
.modal-sheet__footer .btn {
  border-radius: 8px;
}

.status-pill {
  padding: 0.45rem 0.75rem;
}

.status-pill--secondary {
  background: #eef2f7;
  color: #52606d;
}

.status-pill--success {
  background: #e7f5ee;
  color: #146c43;
}

.status-pill--warning {
  background: #fff4ce;
  color: #7a5900;
}

.legend-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0.8rem;
}

.legend-chip {
  display: flex;
  align-items: center;
  gap: 0.8rem;
  padding: 0.9rem;
  border: 1px solid #dbe4ee;
  border-radius: 8px;
  background: #f8fafc;
}

.legend-chip__icon {
  width: 2.4rem;
  height: 2.4rem;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  font-size: 1rem;
}

.legend-chip__icon--EVENTO {
  background: #e7f5ee;
  color: #198754;
}

.legend-chip__icon--PROVA {
  background: #eaf1ff;
  color: #2563eb;
}

.legend-chip__icon--REUNIAO {
  background: #fff4ce;
  color: #7a5900;
}

.legend-chip__icon--ATIVIDADE {
  background: #e8f7fa;
  color: #0b7285;
}

.empty-state {
  min-height: 12rem;
  display: grid;
  place-items: center;
  gap: 0.65rem;
  border: 1px dashed #b9c6d3;
  border-radius: 8px;
  background: #f8fafc;
  color: #5f6d7a;
  text-align: center;
  padding: 1.5rem;
}

.empty-state i {
  font-size: 1.8rem;
  color: #0f766e;
}

.upcoming-list {
  display: flex;
  flex-direction: column;
  gap: 0.9rem;
}

.upcoming-event {
  display: flex;
  align-items: flex-start;
  gap: 0.95rem;
  padding: 1rem;
  border: 1px solid #dbe4ee;
  border-radius: 8px;
  background: #ffffff;
}

.evento-date {
  min-width: 4rem;
  padding: 0.65rem;
  border-radius: 8px;
  color: #17212b;
  text-align: center;
}

.evento-date .day {
  font-size: 1.45rem;
  font-weight: 800;
  line-height: 1;
}

.evento-date .month {
  margin-top: 0.2rem;
  font-size: 0.7rem;
  text-transform: uppercase;
  letter-spacing: 0;
}

.evento-PROVA {
  background: #eaf1ff;
  color: #1d4ed8;
}

.evento-EVENTO {
  background: #e7f5ee;
  color: #146c43;
}

.evento-REUNIAO {
  background: #fff4ce;
  color: #7a5900;
}

.evento-ATIVIDADE {
  background: #e8f7fa;
  color: #0b7285;
}

.upcoming-event__content {
  min-width: 0;
  flex: 1;
}

.upcoming-event__topline {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.75rem;
  margin-bottom: 0.55rem;
  color: #5f6d7a;
}

.upcoming-event__title {
  margin: 0 0 0.25rem;
  color: #17212b;
  font-weight: 800;
  overflow-wrap: anywhere;
}

.upcoming-event__context {
  margin: 0;
  color: #5f6d7a;
  font-size: 0.9rem;
  overflow-wrap: anywhere;
}

.event-type-tag,
.type-pill {
  padding: 0.3rem 0.65rem;
}

.event-type-tag--EVENTO,
.type-pill--EVENTO {
  background: #e7f5ee;
  color: #146c43;
}

.event-type-tag--PROVA,
.type-pill--PROVA {
  background: #eaf1ff;
  color: #1d4ed8;
}

.event-type-tag--REUNIAO,
.type-pill--REUNIAO {
  background: #fff4ce;
  color: #7a5900;
}

.event-type-tag--ATIVIDADE,
.type-pill--ATIVIDADE {
  background: #e8f7fa;
  color: #0b7285;
}

.modal-overlay {
  position: fixed;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  padding: 1rem;
  background: rgba(15, 23, 42, 0.55);
}

.modal-dialog {
  width: min(680px, calc(100vw - 2rem));
  max-width: none;
  max-height: calc(100vh - 2rem);
  margin: 0;
  display: flex;
}

.modal-dialog.modal-form {
  width: min(780px, calc(100vw - 2rem));
}

.modal-sheet {
  width: 100%;
  max-height: calc(100vh - 2rem);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border: 1px solid #dbe4ee;
  border-radius: 8px;
  background: #ffffff;
  padding: 0;
}

.form-modal form {
  min-height: 0;
  display: flex;
  flex: 1;
  flex-direction: column;
}

.modal-sheet__header,
.modal-sheet__footer {
  flex: 0 0 auto;
  padding: 1.2rem 1.35rem;
  border-color: #e2e8f0;
  background: #ffffff;
}

.modal-sheet__body {
  min-height: 0;
  overflow-y: auto;
  padding: 1.25rem 1.35rem;
  background: #f8fafc;
}

.details-title-row {
  display: flex;
  align-items: center;
  gap: 0.7rem;
  margin-bottom: 0.35rem;
}

.detail-summary {
  margin-bottom: 1rem;
  padding: 1rem;
  border: 1px solid #dbe4ee;
  border-radius: 8px;
  background: #ffffff;
  color: #425466;
  overflow-wrap: anywhere;
}

.detail-card {
  height: 100%;
  padding: 1rem;
  border: 1px solid #dbe4ee;
  border-radius: 8px;
  background: #ffffff;
}

.detail-card strong {
  display: block;
  color: #17212b;
  overflow-wrap: anywhere;
}

.detail-card__support {
  display: block;
  margin-top: 0.4rem;
  color: #5f6d7a;
  font-size: 0.82rem;
}

.form-section + .form-section {
  margin-top: 1rem;
}

.form-section {
  padding: 1rem;
  border: 1px solid #dbe4ee;
  border-radius: 8px;
  background: #ffffff;
}

.form-section__header {
  margin-bottom: 1rem;
}

.form-section__header h6 {
  color: #17212b;
  font-weight: 800;
}

.form-help {
  color: #5f6d7a;
}

.form-control,
.form-select {
  border-radius: 8px;
  border-color: #cfd9e5;
  padding: 0.78rem 0.9rem;
  background: #ffffff;
  box-shadow: none;
}

.form-control:focus,
.form-select:focus {
  border-color: #0f766e;
  box-shadow: 0 0 0 0.2rem rgba(15, 118, 110, 0.14);
}

.overlay-loading {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;
  background: rgba(255, 255, 255, 0.72);
}

@media (max-width: 1199.98px) {
  .calendario-wrapper {
    min-height: 580px;
  }
}

@media (max-width: 991.98px) {
  .calendar-hero__content,
  .calendar-panel__toolbar,
  .section-heading,
  .calendar-info-card__header {
    flex-direction: column;
    align-items: flex-start;
  }

  .calendar-hero__cta {
    justify-content: flex-start;
  }
}

@media (max-width: 767.98px) {
  .calendar-hero {
    padding: 1.25rem;
  }

  .calendar-hero__title {
    font-size: 1.75rem;
  }

  .calendar-hero__button,
  .calendar-mode-pill {
    width: 100%;
  }

  .calendar-stat-card__value {
    font-size: 1.55rem;
  }

  .legend-grid,
  .sync-metrics {
    grid-template-columns: 1fr;
  }

  .calendar-panel__toolbar,
  .calendar-info-card .card-body,
  .modal-sheet__header,
  .modal-sheet__body,
  .modal-sheet__footer {
    padding-left: 1rem;
    padding-right: 1rem;
  }

  .calendar-panel__body {
    padding: 0.75rem;
  }

  .calendario-wrapper {
    min-height: 520px;
  }

  .upcoming-event {
    padding: 0.9rem;
  }

  .modal-overlay {
    align-items: stretch;
    padding: 0.75rem;
  }

  .modal-dialog,
  .modal-dialog.modal-form {
    width: 100%;
    max-height: calc(100vh - 1.5rem);
  }

  .modal-sheet {
    max-height: calc(100vh - 1.5rem);
  }

  .modal-sheet__header,
  .modal-sheet__footer {
    gap: 0.75rem;
  }

  .modal-sheet__footer {
    flex-direction: column-reverse;
    align-items: stretch;
  }

  .modal-sheet__footer .btn {
    width: 100%;
  }
}

@media (max-width: 575.98px) {
  .calendar-hero {
    padding: 1rem;
  }

  .calendar-hero__title {
    font-size: 1.45rem;
  }

  .calendar-hero__subtitle,
  .calendar-panel__hint,
  .calendar-info-card__text,
  .calendar-info-card__support {
    font-size: 0.92rem;
  }

  .calendar-panel__toolbar,
  .calendar-info-card .card-body,
  .modal-sheet__header,
  .modal-sheet__body,
  .modal-sheet__footer {
    padding: 0.9rem;
  }

  .form-section {
    padding: 0.85rem;
  }

  .upcoming-event {
    flex-direction: column;
  }

  .evento-date {
    width: 100%;
  }
}
</style>
