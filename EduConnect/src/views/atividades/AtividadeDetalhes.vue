<template>
  <div class="container-fluid py-4">
    <button class="btn btn-link ps-0 mb-4" @click="router.back()">
      <i class="bi bi-arrow-left me-2"></i>Voltar
    </button>
    
    <div v-if="loading" class="text-center py-5">
      <div class="spinner-border text-primary" role="status">
        <span class="visually-hidden">Carregando...</span>
      </div>
    </div>
    
    <div v-else-if="atividade" class="row">
      <div class="col-lg-8">
        <div class="card shadow-sm mb-4">
          <div class="card-header bg-white">
            <div class="d-flex justify-content-between align-items-start">
              <div>
                <h3>{{ atividade.titulo }}</h3>
                <span class="badge bg-primary me-2">{{ atividade.disciplina }}</span>
                <span class="badge bg-secondary">Turma {{ atividade.turma }}</span>
              </div>
              <div>
                <span :class="`badge bg-${getStatusBadge()}`">
                  {{ getStatusTexto() }}
                </span>
                <div class="mt-2" v-if="podeEditar">
                  <button class="btn btn-sm btn-outline-primary me-1" @click="editarAtividade">
                    <i class="bi bi-pencil"></i>
                  </button>
                  <button class="btn btn-sm btn-outline-danger" @click="excluirAtividade">
                    <i class="bi bi-trash"></i>
                  </button>
                </div>
              </div>
            </div>
          </div>
          <div class="card-body">
            <div class="mb-4">
              <h6>Descrição</h6>
              <p>{{ atividade.descricao || 'Sem descrição' }}</p>
            </div>
            
            <div class="row mb-4">
              <div class="col-md-6">
                <small class="text-muted d-block">Data de entrega</small>
                <strong><i class="bi bi-calendar me-2"></i>{{ formatarData(atividade.dataEntrega) }}</strong>
              </div>
              <div class="col-md-6">
                <small class="text-muted d-block">Professor</small>
                <strong><i class="bi bi-person me-2"></i>{{ atividade.professorNome }}</strong>
              </div>
            </div>
            
            <div v-if="atividade.anexos && atividade.anexos.length > 0">
              <h6>Anexos do Professor</h6>
              <div class="list-group mb-4">
                <a
                  v-for="(anexo, index) in atividade.anexos"
                  :key="index"
                  :href="anexo"
                  target="_blank"
                  class="list-group-item list-group-item-action"
                >
                  <i class="bi bi-paperclip me-2"></i>Anexo {{ index + 1 }}
                </a>
              </div>
            </div>
            
            <div v-if="podeGerenciar" class="alert alert-info">
              <h6 class="mb-2">Estatísticas</h6>
              <div class="row">
                <div class="col-md-6">
                  <small class="text-muted d-block">Total de Respostas</small>
                  <strong>{{ atividade.totalRespostas || 0 }}</strong>
                </div>
                <div class="col-md-6">
                  <small class="text-muted d-block">Respostas Corrigidas</small>
                  <strong>{{ atividade.respostasCorrigidas || 0 }}</strong>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <div v-if="podeResponder" class="card shadow-sm mb-4" v-show="prazoAberto || !minhaResposta">
          <div class="card-header bg-white d-flex justify-content-between align-items-center">
            <h5 class="mb-0">Enviar Resposta</h5>
            <span v-if="!prazoAberto" class="badge bg-warning text-dark">Prazo encerrado</span>
          </div>
          <div class="card-body">
            <form @submit.prevent="enviarResposta">
              <div class="mb-3">
                <label class="form-label">Comentário/Resposta</label>
                <textarea
                  class="form-control"
                  rows="6"
                  v-model="resposta.comentario"
                  placeholder="Escreva sua resposta aqui..."
                  required
                ></textarea>
              </div>
              <div class="mb-3">
                <label class="form-label">Links (opcional)</label>
                <div class="d-flex gap-2">
                  <input
                    type="url"
                    class="form-control"
                    v-model="novoLink"
                    placeholder="https://drive.google.com/..."
                  />
                  <button type="button" class="btn btn-outline-primary" @click="adicionarLink" :disabled="!novoLink">
                    Adicionar
                  </button>
                </div>
                <div v-if="resposta.links.length" class="mt-2 d-flex flex-wrap gap-2">
                  <span
                    v-for="(link, index) in resposta.links"
                    :key="`${link}-${index}`"
                    class="badge bg-light text-dark border"
                  >
                    <a :href="link" target="_blank" rel="noopener" class="text-decoration-none">
                      Link {{ index + 1 }}
                    </a>
                    <button type="button" class="btn-close ms-2" aria-label="Remover" @click="removerLink(index)"></button>
                  </span>
                </div>
              </div>
              <div class="mb-3">
                <label class="form-label">Anexos (opcional)</label>
                <input
                  type="file"
                  class="form-control"
                  multiple
                  @change="onArquivosChange"
                />
                <small class="text-muted">São aceitos vários formatos (PDF, DOCX, imagens, etc.).</small>
                <ul v-if="resposta.arquivos.length" class="small mt-2 mb-0 ps-3">
                  <li v-for="(file, index) in resposta.arquivos" :key="index">{{ file.name }}</li>
                </ul>
              </div>
              <button type="submit" class="btn btn-primary" :disabled="enviando || !prazoAberto">
                <span v-if="enviando"><span class="spinner-border spinner-border-sm me-2"></span>Enviando...</span>
                <span v-else><i class="bi bi-send me-2"></i>{{ minhaResposta ? 'Atualizar entrega' : 'Enviar Atividade' }}</span>
              </button>
            </form>
          </div>
        </div>

        <div v-if="minhaResposta && podeResponder" class="card shadow-sm">
          <div class="card-header bg-white d-flex justify-content-between align-items-center">
            <h5 class="mb-0">Sua entrega</h5>
            <div class="d-flex align-items-center gap-2">
              <span class="badge" :class="statusBadgeClasse(minhaResposta.status)">{{ minhaResposta.status }}</span>
              <button
                v-if="prazoAberto"
                class="btn btn-sm btn-outline-primary"
                @click="prepararEdicaoEntrega"
              >
                <i class="bi bi-pencil me-1"></i>Editar entrega
              </button>
            </div>
          </div>
          <div class="card-body">
            <p class="mb-2"><strong>Comentário:</strong> {{ minhaResposta.comentario || 'Sem comentário' }}</p>
            <div class="mb-2">
              <strong>Anexos/Links:</strong>
              <div v-if="minhaResposta.anexos && minhaResposta.anexos.length">
                <a
                  v-for="(anexo, idx) in minhaResposta.anexos"
                  :key="`${anexo}-${idx}`"
                  :href="anexo"
                  target="_blank"
                  class="d-block text-decoration-none"
                >
                  <i class="bi bi-paperclip me-1"></i>Anexo {{ idx + 1 }}
                </a>
              </div>
              <span v-else class="text-muted">Nenhum anexo ou link</span>
            </div>
            <small class="text-muted">Enviado em: {{ formatarData(minhaResposta.dataEnvio) }}</small>
          </div>
        </div>

        <div v-if="podeGerenciar" class="card shadow-sm mt-4">
          <div class="card-header bg-white d-flex justify-content-between align-items-center">
            <h5 class="mb-0">Entregas dos Alunos</h5>
            <span class="badge bg-secondary" v-if="respostas.length">{{ respostas.length }} entrega(s)</span>
          </div>
          <div class="card-body">
            <div v-if="carregandoRespostas" class="text-center py-3">
              <div class="spinner-border text-primary" role="status">
                <span class="visually-hidden">Carregando...</span>
              </div>
            </div>
            <div v-else-if="!respostas.length" class="text-muted">Nenhuma entrega registrada ainda.</div>
            <div v-else class="table-responsive">
              <table class="table align-middle">
                <thead>
                  <tr>
                    <th>Aluno</th>
                    <th>Data de envio</th>
                    <th>Status</th>
                    <th>Nota</th>
                    <th>Anexos / Links</th>
                    <th>Ações</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="respostaItem in respostas" :key="respostaItem.id">
                    <td>
                      <div class="fw-semibold">{{ respostaItem.alunoNome || 'Aluno' }}</div>
                      <small class="text-muted">{{ respostaItem.alunoEmail }}</small>
                    </td>
                    <td>{{ formatarData(respostaItem.dataEnvio) }}</td>
                    <td><span class="badge" :class="statusBadgeClasse(respostaItem.status)">{{ respostaItem.status }}</span></td>
                    <td>{{ respostaItem.nota ?? '-' }}</td>
                    <td>
                      <div v-if="respostaItem.anexos && respostaItem.anexos.length">
                        <a
                          v-for="(anexo, idx) in respostaItem.anexos"
                          :key="`${anexo}-${idx}`"
                          :href="anexo"
                          target="_blank"
                          class="d-block text-decoration-none"
                        >
                          <i class="bi bi-paperclip me-1"></i>Anexo {{ idx + 1 }}
                        </a>
                      </div>
                      <span v-else class="text-muted">Sem anexos</span>
                    </td>
                    <td class="d-flex gap-2">
                      <button class="btn btn-sm btn-outline-secondary" @click="verDetalhesEntrega(respostaItem)">
                        <i class="bi bi-eye"></i>
                      </button>
                      <a
                        v-if="respostaItem.anexos && respostaItem.anexos.length"
                        class="btn btn-sm btn-outline-primary"
                        :href="respostaItem.anexos[0]"
                        target="_blank"
                        download
                        title="Baixar primeiro anexo"
                      >
                        <i class="bi bi-download"></i>
                      </a>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
      
      <div class="col-lg-4">
        <div class="card shadow-sm">
          <div class="card-header bg-white">
            <h6 class="mb-0">Informações</h6>
          </div>
          <div class="card-body">
            <div class="mb-3">
              <small class="text-muted">Valor</small>
              <h5>{{ atividade.valor }} pontos</h5>
            </div>
            <div class="mb-3">
              <small class="text-muted">Peso</small>
              <h5>{{ atividade.peso }}</h5>
            </div>
            <div>
              <small class="text-muted">Tentativas permitidas</small>
              <h5>{{ atividade.tentativasPermitidas }}</h5>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <AtividadeModal
      v-if="mostrandoModal"
      :atividade="atividade"
      mode="edit"
      @close="mostrandoModal = false"
      @save="salvarEdicao"
    />

    <div class="modal fade show" tabindex="-1" style="display: block; background: rgba(0,0,0,0.5);" v-if="mostrandoResposta">
      <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">Detalhes da entrega</h5>
            <button type="button" class="btn-close" @click="mostrandoResposta = false"></button>
          </div>
          <div class="modal-body" v-if="respostaSelecionada">
            <p><strong>Aluno:</strong> {{ respostaSelecionada.alunoNome }} ({{ respostaSelecionada.alunoEmail }})</p>
            <p><strong>Status:</strong> <span class="badge" :class="statusBadgeClasse(respostaSelecionada.status)">{{ respostaSelecionada.status }}</span></p>
            <p><strong>Comentário:</strong> {{ respostaSelecionada.comentario || 'Sem comentário' }}</p>
            <div class="mb-3">
              <strong>Anexos / Links:</strong>
              <div v-if="respostaSelecionada.anexos && respostaSelecionada.anexos.length">
                <a
                  v-for="(anexo, idx) in respostaSelecionada.anexos"
                  :key="`${anexo}-${idx}`"
                  :href="anexo"
                  target="_blank"
                  class="d-block text-decoration-none"
                >
                  <i class="bi bi-paperclip me-1"></i>Anexo {{ idx + 1 }}
                </a>
              </div>
              <span v-else class="text-muted">Sem anexos</span>
            </div>
            <p><strong>Enviado em:</strong> {{ formatarData(respostaSelecionada.dataEnvio) }}</p>
            <p><strong>Nota:</strong> {{ respostaSelecionada.nota ?? '-' }}</p>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary" @click="mostrandoResposta = false">Fechar</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useNotificationStore } from '@/stores/notifications'
import atividadesService from '@/services/atividadesService'
import AtividadeModal from './modals/AtividadeModal.vue'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const notifications = useNotificationStore()

const loading = ref(false)
const atividade = ref(null)
const enviando = ref(false)
const mostrandoModal = ref(false)
const respostas = ref([])
const carregandoRespostas = ref(false)
const novoLink = ref('')
const minhaResposta = ref(null)
const mostrandoResposta = ref(false)
const respostaSelecionada = ref(null)

const resposta = ref({
  comentario: '',
  links: [],
  arquivos: []
})

const podeGerenciar = computed(() => {
  const role = authStore.userRole
  return role === 'PROFESSOR' || role === 'DIRETORIA' || role === 'ADMINISTRADOR'
})

const podeEditar = computed(() => {
  if (!atividade.value) return false
  const role = authStore.userRole
  const userId = authStore.user?.id
  
  if (role === 'ADMINISTRADOR') return true
  if (role === 'DIRETORIA') return true
  if (role === 'PROFESSOR' && atividade.value.professorId === userId) return true
  
  return false
})

const podeResponder = computed(() => {
  if (!atividade.value) return false
  const role = authStore.userRole
  if (role !== 'ALUNO') return false
  return true
})

const prazoAberto = computed(() => {
  if (!atividade.value) return false
  const agora = new Date()
  const dataEntrega = new Date(atividade.value.dataEntrega)
  return agora <= dataEntrega
})

const carregarAtividade = async () => {
  try {
    loading.value = true
    atividade.value = await atividadesService.buscarPorId(route.params.id)
    if (authStore.userRole === 'ALUNO') {
      await carregarMinhaResposta()
    }
    if (podeGerenciar.value) {
      await carregarRespostas()
    }
  } catch (error) {
    console.error('Erro ao carregar atividade:', error)
    notifications.error(error.response?.data?.message || 'Erro ao carregar atividade')
    router.push('/atividades')
  } finally {
    loading.value = false
  }
}

const formatarData = (data) => {
  if (!data) return '-'
  const date = new Date(data)
  return date.toLocaleDateString('pt-BR', { 
    day: '2-digit', 
    month: '2-digit', 
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const getStatusBadge = () => {
  const dataEntrega = new Date(atividade.value.dataEntrega)
  const agora = new Date()
  
  if (dataEntrega < agora) return 'danger'
  
  const diasRestantes = Math.ceil((dataEntrega - agora) / (1000 * 60 * 60 * 24))
  if (diasRestantes <= 2) return 'warning'
  
  return 'success'
}

const getStatusTexto = () => {
  const dataEntrega = new Date(atividade.value.dataEntrega)
  const agora = new Date()
  
  if (dataEntrega < agora) return 'Atrasada'
  
  const diasRestantes = Math.ceil((dataEntrega - agora) / (1000 * 60 * 60 * 24))
  if (diasRestantes === 0) return 'Entrega Hoje'
  if (diasRestantes === 1) return 'Entrega Amanhã'
  if (diasRestantes <= 2) return 'Urgente'
  
  return 'No Prazo'
}

const enviarResposta = async () => {
  enviando.value = true
  try {
    if (!resposta.value.comentario && resposta.value.links.length === 0 && resposta.value.arquivos.length === 0) {
      notifications.error('Adicione um comentário, link ou arquivo para enviar a resposta')
      return
    }

    const formData = new FormData()
    formData.append('comentario', resposta.value.comentario || '')
    resposta.value.links.forEach(link => formData.append('links', link))
    resposta.value.arquivos.forEach(arquivo => {
      formData.append('arquivos', arquivo)
    })

    const resp = await atividadesService.enviarResposta(atividade.value.id, formData)
    const respostaSalva = resp.data
    notifications.success(minhaResposta.value ? 'Entrega atualizada com sucesso!' : 'Resposta enviada com sucesso!')
    resposta.value = { comentario: '', links: [], arquivos: [] }
    novoLink.value = ''
    minhaResposta.value = respostaSalva
    if (podeGerenciar.value) {
      await carregarRespostas()
    }
  } catch (error) {
    console.error('Erro ao enviar resposta:', error)
    notifications.error(error.response?.data?.message || 'Erro ao enviar resposta')
  } finally {
    enviando.value = false
  }
}

const onArquivosChange = (event) => {
  const files = Array.from(event.target.files || [])
  resposta.value.arquivos = files
}

const adicionarLink = () => {
  if (!novoLink.value) return
  resposta.value.links.push(novoLink.value.trim())
  novoLink.value = ''
}

const removerLink = (index) => {
  resposta.value.links.splice(index, 1)
}

const carregarRespostas = async () => {
  try {
    carregandoRespostas.value = true
    const response = await atividadesService.listarRespostas(atividade.value.id)
    respostas.value = response.data || []
  } catch (error) {
    console.error('Erro ao carregar entregas:', error)
    notifications.error(error.response?.data?.message || 'Erro ao carregar entregas')
  } finally {
    carregandoRespostas.value = false
  }
}

const carregarMinhaResposta = async () => {
  try {
    const response = await atividadesService.minhaResposta(atividade.value.id)
    minhaResposta.value = response.data
  } catch (error) {
    minhaResposta.value = null
  }
}

const statusBadgeClasse = (status) => {
  if (status === 'ENTREGUE') return 'bg-success'
  if (status === 'ATRASADA') return 'bg-danger'
  if (status === 'AVALIADA') return 'bg-primary'
  return 'bg-secondary'
}

const prepararEdicaoEntrega = () => {
  if (!minhaResposta.value) return
  resposta.value.comentario = minhaResposta.value.comentario || ''
  const linksExistentes = (minhaResposta.value.anexos || []).filter(a => a && !a.includes('/uploads/respostas/'))
  resposta.value.links = [...linksExistentes]
  resposta.value.arquivos = []
}

const verDetalhesEntrega = (respostaItem) => {
  respostaSelecionada.value = respostaItem
  mostrandoResposta.value = true
}

const editarAtividade = () => {
  mostrandoModal.value = true
}

const salvarEdicao = async (dados) => {
  try {
    await atividadesService.atualizar(atividade.value.id, dados)
    notifications.success('Atividade atualizada com sucesso!')
    mostrandoModal.value = false
    await carregarAtividade()
  } catch (error) {
    console.error('Erro ao atualizar atividade:', error)
    notifications.error(error.response?.data?.message || 'Erro ao atualizar atividade')
  }
}

const excluirAtividade = async () => {
  if (!confirm('Tem certeza que deseja excluir esta atividade?')) return
  
  try {
    await atividadesService.deletar(atividade.value.id)
    notifications.success('Atividade excluída com sucesso!')
    router.push('/atividades')
  } catch (error) {
    console.error('Erro ao excluir atividade:', error)
    notifications.error(error.response?.data?.message || 'Erro ao excluir atividade')
  }
}

onMounted(() => {
  carregarAtividade()
})
</script>

<style scoped>
.card-header {
  border-bottom: 2px solid #f0f0f0;
}
</style>
