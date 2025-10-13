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
                <a v-for="(anexo, index) in atividade.anexos" :key="index" :href="anexo" target="_blank" class="list-group-item list-group-item-action">
                  <i class="bi bi-paperclip me-2"></i>Anexo {{ index + 1 }}
                </a>
              </div>
            </div>
            
            <!-- Estatísticas para professores/diretores/admins -->
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
        
        <!-- Formulário de resposta para alunos -->
        <div v-if="podeResponder" class="card shadow-sm">
          <div class="card-header bg-white">
            <h5 class="mb-0">Enviar Resposta</h5>
          </div>
          <div class="card-body">
            <form @submit.prevent="enviarResposta">
              <div class="mb-3">
                <label class="form-label">Comentário/Resposta</label>
                <textarea class="form-control" rows="6" v-model="resposta.comentario" placeholder="Escreva sua resposta aqui..." required></textarea>
              </div>
              <button type="submit" class="btn btn-primary" :disabled="enviando">
                <span v-if="enviando"><span class="spinner-border spinner-border-sm me-2"></span>Enviando...</span>
                <span v-else><i class="bi bi-send me-2"></i>Enviar Atividade</span>
              </button>
            </form>
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
    
    <!-- Modal de Editar -->
    <AtividadeModal
      v-if="mostrandoModal"
      :atividade="atividade"
      mode="edit"
      @close="mostrandoModal = false"
      @save="salvarEdicao"
    />
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

const resposta = ref({
  comentario: ''
})

// Computed
const podeGerenciar = computed(() => {
  const role = authStore.userRole
  return role === 'PROFESSOR' || role === 'DIRETORIA' || role === 'ADMINISTRADOR'
})

const podeEditar = computed(() => {
  if (!atividade.value) return false
  const role = authStore.userRole
  const userId = authStore.user?.id
  
  // Admin pode editar tudo
  if (role === 'ADMINISTRADOR') return true
  
  // Diretoria pode editar atividades da sua escola
  if (role === 'DIRETORIA') return true
  
  // Professor pode editar apenas suas atividades
  if (role === 'PROFESSOR' && atividade.value.professorId === userId) return true
  
  return false
})

const podeResponder = computed(() => {
  if (!atividade.value) return false
  const role = authStore.userRole
  
  // Apenas alunos podem responder
  if (role !== 'ALUNO') return false
  
  // Verificar se não está atrasada (opcional - pode permitir respostas atrasadas)
  const agora = new Date()
  const dataEntrega = new Date(atividade.value.dataEntrega)
  
  // Por enquanto, permite resposta mesmo atrasada
  return true
})

// Métodos
const carregarAtividade = async () => {
  try {
    loading.value = true
    atividade.value = await atividadesService.buscarPorId(route.params.id)
  } catch (error) {
    console.error('Erro ao carregar atividade:', error)
    notifications.error(error.response?.data?.message || 'Erro ao carregar atividade')
    router.push('/atividades')
  } finally {
    loading.value = false
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
    // TODO: Implementar envio de resposta quando o endpoint estiver pronto
    await new Promise(resolve => setTimeout(resolve, 1000))
    notifications.success('Resposta enviada com sucesso!')
    router.push('/atividades')
  } catch (error) {
    console.error('Erro ao enviar resposta:', error)
    notifications.error(error.response?.data?.message || 'Erro ao enviar resposta')
  } finally {
    enviando.value = false
  }
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

// Inicialização
onMounted(() => {
  carregarAtividade()
})
</script>

<style scoped>
.card-header {
  border-bottom: 2px solid #f0f0f0;
}
</style>
