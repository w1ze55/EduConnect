<template>
  <div class="container-fluid py-4">
    <div class="row mb-4">
      <div class="col">
        <button class="btn btn-link text-decoration-none ps-0" @click="router.back()">
          <i class="bi bi-arrow-left me-2"></i>Voltar
        </button>
      </div>
    </div>
    
    <div v-if="carregando" class="text-center py-5">
      <div class="spinner-border text-primary" role="status">
        <span class="visually-hidden">Carregando...</span>
      </div>
      <p class="mt-3 text-muted">Carregando recado...</p>
    </div>
    
    <div v-else class="row justify-content-center">
      <div class="col-lg-8">
        <div class="card shadow-sm">
          <div class="card-header bg-white">
            <h4 class="mb-0">
              <i class="bi bi-pencil me-2"></i>Editar Recado
            </h4>
          </div>
          
          <div class="card-body">
            <form @submit.prevent="salvarRecado">
              <div class="mb-3">
                <label for="titulo" class="form-label">Título *</label>
                <input
                  type="text"
                  class="form-control"
                  id="titulo"
                  v-model="form.titulo"
                  placeholder="Digite o título do recado"
                  required
                />
              </div>
              
              <div class="row">
                <div class="col-md-6 mb-3">
                  <label for="categoria" class="form-label">Categoria *</label>
                  <select class="form-select" id="categoria" v-model="form.categoria" required>
                    <option value="">Selecione...</option>
                    <option value="geral">Geral</option>
                    <option value="academico">Acadêmico</option>
                    <option value="financeiro">Financeiro</option>
                    <option value="evento">Evento</option>
                  </select>
                </div>
                
                <div class="col-md-6 mb-3">
                  <label for="destinatarios" class="form-label">Destinatários *</label>
                  <select class="form-select" id="destinatarios" v-model="form.destinatarios" required>
                    <option value="">Selecione...</option>
                    <option value="todos">Todos</option>
                    <option value="alunos">Alunos</option>
                    <option value="responsaveis">Responsáveis</option>
                    <option value="professores">Professores</option>
                    <option value="especifico">Específico</option>
                  </select>
                </div>
              </div>
              
              <!-- Multi-select de Alunos quando "Específico" for selecionado -->
              <div v-if="form.destinatarios === 'especifico'" class="mb-3">
                <label class="form-label">Selecionar Alunos *</label>
                <div v-if="carregandoAlunos" class="text-center py-3">
                  <div class="spinner-border spinner-border-sm text-primary" role="status">
                    <span class="visually-hidden">Carregando...</span>
                  </div>
                  <p class="text-muted mt-2 mb-0">Carregando alunos...</p>
                </div>
                <div v-else-if="alunosDisponiveis.length === 0" class="alert alert-info">
                  <i class="bi bi-info-circle me-2"></i>Nenhum aluno encontrado.
                </div>
                <div v-else class="alunos-list border rounded p-3" style="max-height: 300px; overflow-y: auto;">
                  <div v-for="aluno in alunosDisponiveis" :key="aluno.id" class="form-check mb-2">
                    <input
                      class="form-check-input"
                      type="checkbox"
                      :id="`aluno-${aluno.id}`"
                      :value="aluno.id"
                      v-model="form.alunosSelecionados"
                    />
                    <label class="form-check-label d-flex justify-content-between w-100" :for="`aluno-${aluno.id}`">
                      <span>
                        <strong>{{ aluno.nome }}</strong>
                        <span v-if="aluno.turma" class="text-muted ms-2">({{ aluno.turma }})</span>
                      </span>
                      <span v-if="aluno.responsavelNome" class="text-muted small">
                        <i class="bi bi-person me-1"></i>{{ aluno.responsavelNome }}
                      </span>
                    </label>
                  </div>
                </div>
                <small class="form-text text-muted">
                  {{ form.alunosSelecionados.length }} aluno(s) selecionado(s). O recado será enviado para o aluno e seu responsável.
                </small>
              </div>
              
              <div class="mb-3">
                <label for="conteudo" class="form-label">Mensagem *</label>
                <textarea
                  class="form-control"
                  id="conteudo"
                  rows="10"
                  v-model="form.conteudo"
                  placeholder="Digite a mensagem do recado..."
                  required
                ></textarea>
              </div>
              
              <div class="mb-3 form-check">
                <input
                  type="checkbox"
                  class="form-check-input"
                  id="importante"
                  v-model="form.importante"
                />
                <label class="form-check-label" for="importante">
                  Marcar como importante
                </label>
              </div>
              
              <div class="mb-3 form-check">
                <input
                  type="checkbox"
                  class="form-check-input"
                  id="exigirConfirmacao"
                  v-model="form.exigirConfirmacao"
                />
                <label class="form-check-label" for="exigirConfirmacao">
                  Exigir confirmação de leitura
                </label>
              </div>
              
              <hr>
              
              <div class="d-flex gap-2">
                <button 
                  type="submit" 
                  class="btn btn-primary"
                  :disabled="salvando"
                >
                  <span v-if="salvando">
                    <span class="spinner-border spinner-border-sm me-2"></span>
                    Salvando...
                  </span>
                  <span v-else>
                    <i class="bi bi-check2 me-2"></i>
                    Salvar Alterações
                  </span>
                </button>
                <button 
                  type="button" 
                  class="btn btn-outline-secondary"
                  @click="router.back()"
                >
                  Cancelar
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
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useNotificationStore } from '../../stores/notifications'
import recadosService from '../../services/recadosService'
import usuariosService from '../../services/usuariosService'

const router = useRouter()
const route = useRoute()
const notificationStore = useNotificationStore()

const carregando = ref(false)
const salvando = ref(false)
const carregandoAlunos = ref(false)
const alunosDisponiveis = ref([])

const form = ref({
  titulo: '',
  categoria: '',
  destinatarios: '',
  alunosSelecionados: [],
  conteudo: '',
  importante: false,
  exigirConfirmacao: false
})

// Carregar alunos quando "específico" for selecionado
watch(() => form.value.destinatarios, async (novoValor) => {
  if (novoValor === 'especifico' && alunosDisponiveis.value.length === 0) {
    await carregarAlunos()
  }
})

const carregarAlunos = async () => {
  carregandoAlunos.value = true
  try {
    const response = await usuariosService.getAlunos()
    alunosDisponiveis.value = response.data
  } catch (error) {
    console.error('Erro ao carregar alunos:', error)
    notificationStore.error('Erro ao carregar lista de alunos')
  } finally {
    carregandoAlunos.value = false
  }
}

// Carregar dados do recado
onMounted(async () => {
  carregando.value = true
  try {
    const response = await recadosService.getRecadoById(route.params.id)
    const recado = response.data
    
    // Mapear destinatários do backend para o frontend
    let destinatariosValue = 'todos'
    let alunosIds = []
    
    // Verificar se é destinatário específico
    if (recado.destinatariosEspecificos && recado.destinatariosEspecificos.length > 0) {
      destinatariosValue = 'especifico'
      alunosIds = recado.destinatariosEspecificos
      // Carregar lista de alunos
      await carregarAlunos()
    } else if (recado.destinatarios && recado.destinatarios.length > 0) {
      const dest = recado.destinatarios[0]
      if (dest === 'ALUNO') destinatariosValue = 'alunos'
      else if (dest === 'RESPONSAVEL') destinatariosValue = 'responsaveis'
      else if (dest === 'PROFESSOR') destinatariosValue = 'professores'
      else if (dest === 'TODOS') destinatariosValue = 'todos'
    }
    
    form.value = {
      titulo: recado.titulo,
      categoria: recado.categoria.toLowerCase(),
      destinatarios: destinatariosValue,
      alunosSelecionados: alunosIds,
      conteudo: recado.conteudo,
      importante: recado.importante,
      exigirConfirmacao: recado.exigirConfirmacao
    }
  } catch (error) {
    console.error('Erro ao carregar recado:', error)
    notificationStore.error('Erro ao carregar recado')
    router.push('/recados')
  } finally {
    carregando.value = false
  }
})

const salvarRecado = async () => {
  // Validar se selecionou alunos quando "específico"
  if (form.value.destinatarios === 'especifico' && form.value.alunosSelecionados.length === 0) {
    notificationStore.error('Selecione pelo menos um aluno para enviar o recado.')
    return
  }
  
  salvando.value = true
  
  try {
    // Mapear categoria para o formato esperado pelo backend
    const categoriaMap = {
      'geral': 'GERAL',
      'academico': 'ACADEMICO',
      'financeiro': 'FINANCEIRO',
      'evento': 'EVENTO'
    }
    
    // Mapear destinatários para formato esperado pelo backend
    const destinatariosMap = {
      'todos': ['TODOS'],
      'alunos': ['ALUNO'],
      'responsaveis': ['RESPONSAVEL'],
      'professores': ['PROFESSOR'],
      'especifico': []
    }
    
    const recadoData = {
      titulo: form.value.titulo,
      conteudo: form.value.conteudo,
      categoria: categoriaMap[form.value.categoria],
      importante: form.value.importante,
      exigirConfirmacao: form.value.exigirConfirmacao,
      anexos: []
    }
    
    // Se for específico, enviar IDs dos alunos
    if (form.value.destinatarios === 'especifico') {
      recadoData.destinatariosEspecificos = form.value.alunosSelecionados
      recadoData.destinatarios = [] // Limpar destinatários gerais
    } else {
      recadoData.destinatarios = destinatariosMap[form.value.destinatarios] || ['TODOS']
      recadoData.destinatariosEspecificos = []
    }
    
    await recadosService.atualizarRecado(route.params.id, recadoData)
    notificationStore.success('Recado atualizado com sucesso!')
    router.push(`/recados/${route.params.id}`)
  } catch (error) {
    console.error('Erro ao atualizar recado:', error)
    notificationStore.error(error.response?.data?.message || 'Erro ao atualizar recado')
  } finally {
    salvando.value = false
  }
}
</script>

<style scoped>
/* Estilos podem ser reutilizados do EnviarRecado */
</style>

