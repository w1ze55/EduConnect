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
              
              <!-- Multi-select de Usuários quando "Específico" for selecionado -->
              <div v-if="form.destinatarios === 'especifico'" class="mb-3">
                <div class="row mb-3">
                  <div class="col-md-6">
                    <label class="form-label">Tipo de Usuário *</label>
                    <select class="form-select" v-model="form.tipoUsuarioEspecifico" @change="carregarUsuariosPorTipo" required>
                      <option value="">Selecione o tipo...</option>
                      <option value="ALUNO">Alunos</option>
                      <option value="PROFESSOR">Professores</option>
                      <option value="RESPONSAVEL">Responsáveis</option>
                      <option value="DIRETORIA">Diretoria</option>
                    </select>
                  </div>
                </div>
                
                <label class="form-label">Selecionar Usuários *</label>
                <div v-if="carregandoUsuarios" class="text-center py-3">
                  <div class="spinner-border spinner-border-sm text-primary" role="status">
                    <span class="visually-hidden">Carregando...</span>
                  </div>
                  <p class="text-muted mt-2 mb-0">Carregando usuários...</p>
                </div>
                <div v-else-if="!form.tipoUsuarioEspecifico" class="alert alert-info">
                  <i class="bi bi-info-circle me-2"></i>Selecione um tipo de usuário para ver a lista.
                </div>
                <div v-else-if="usuariosDisponiveis.length === 0" class="alert alert-info">
                  <i class="bi bi-info-circle me-2"></i>Nenhum usuário encontrado.
                </div>
                <div v-else class="usuarios-list border rounded p-3" style="max-height: 300px; overflow-y: auto;">
                  <div v-for="usuario in usuariosDisponiveis" :key="usuario.id" class="form-check mb-2">
                    <input
                      class="form-check-input"
                      type="checkbox"
                      :id="`usuario-${usuario.id}`"
                      :value="usuario.id"
                      v-model="form.usuariosSelecionados"
                    />
                    <label class="form-check-label d-flex justify-content-between w-100" :for="`usuario-${usuario.id}`">
                      <span>
                        <strong>{{ usuario.nome }}</strong>
                        <span v-if="usuario.turma" class="text-muted ms-2">({{ usuario.turma }})</span>
                        <span v-if="usuario.escolaNome" class="text-muted ms-2">- {{ usuario.escolaNome }}</span>
                      </span>
                      <span v-if="usuario.responsavelNome" class="text-muted small">
                        <i class="bi bi-person me-1"></i>{{ usuario.responsavelNome }}
                      </span>
                    </label>
                  </div>
                </div>
                <small class="form-text text-muted">
                  {{ form.usuariosSelecionados.length }} usuário(s) selecionado(s).
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
const carregandoUsuarios = ref(false)
const usuariosDisponiveis = ref([])

const form = ref({
  titulo: '',
  categoria: '',
  destinatarios: '',
  tipoUsuarioEspecifico: '',
  usuariosSelecionados: [],
  conteudo: '',
  importante: false,
  exigirConfirmacao: false
})

// Limpar seleção quando mudar o tipo de destinatário
watch(() => form.value.destinatarios, (novoValor) => {
  if (novoValor !== 'especifico') {
    form.value.tipoUsuarioEspecifico = ''
    form.value.usuariosSelecionados = []
    usuariosDisponiveis.value = []
  }
})

// Carregar usuários quando o tipo mudar
watch(() => form.value.tipoUsuarioEspecifico, () => {
  if (form.value.tipoUsuarioEspecifico) {
    carregarUsuariosPorTipo()
  }
})

const carregarUsuariosPorTipo = async () => {
  if (!form.value.tipoUsuarioEspecifico) {
    usuariosDisponiveis.value = []
    return
  }
  
  carregandoUsuarios.value = true
  
  // Preservar IDs selecionados que ainda são válidos
  const idsSelecionadosAnteriores = [...form.value.usuariosSelecionados]
  
  try {
    console.log('🔄 Carregando usuários do tipo:', form.value.tipoUsuarioEspecifico)
    
    let usuarios = []
    
    switch (form.value.tipoUsuarioEspecifico) {
      case 'ALUNO':
        usuarios = await usuariosService.getAlunos()
        break
      case 'PROFESSOR':
        usuarios = await usuariosService.listarPorRole('PROFESSOR')
        break
      case 'RESPONSAVEL':
        usuarios = await usuariosService.listarPorRole('RESPONSAVEL')
        break
      case 'DIRETORIA':
        usuarios = await usuariosService.listarPorRole('DIRETORIA')
        break
      default:
        usuarios = []
    }
    
    console.log('✅ Usuários carregados:', usuarios)
    usuariosDisponiveis.value = Array.isArray(usuarios) ? usuarios : []
    
    // Manter apenas IDs que ainda estão na lista de usuários disponíveis
    const idsDisponiveis = usuariosDisponiveis.value.map(u => u.id)
    form.value.usuariosSelecionados = idsSelecionadosAnteriores.filter(id => idsDisponiveis.includes(id))
  } catch (error) {
    console.error('❌ Erro ao carregar usuários:', error)
    console.error('❌ Resposta do erro:', error.response)
    notificationStore.error(error.response?.data?.message || 'Erro ao carregar lista de usuários')
    usuariosDisponiveis.value = []
  } finally {
    carregandoUsuarios.value = false
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
    let usuariosIds = []
    let tipoUsuarioEspecifico = ''
    
    // Verificar se é destinatário específico
    if (recado.destinatariosEspecificos && recado.destinatariosEspecificos.length > 0) {
      destinatariosValue = 'especifico'
      usuariosIds = recado.destinatariosEspecificos
      
      // Por padrão, assumir ALUNO para compatibilidade com recados antigos
      // O usuário pode mudar o tipo depois se necessário
      tipoUsuarioEspecifico = 'ALUNO'
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
      tipoUsuarioEspecifico: tipoUsuarioEspecifico,
      usuariosSelecionados: usuariosIds,
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
  // Validar se selecionou usuários quando "específico"
  if (form.value.destinatarios === 'especifico') {
    if (!form.value.tipoUsuarioEspecifico) {
      notificationStore.error('Selecione o tipo de usuário para destinatários específicos.')
      return
    }
    if (form.value.usuariosSelecionados.length === 0) {
      notificationStore.error('Selecione pelo menos um usuário para enviar o recado.')
      return
    }
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
    
    // Se for específico, enviar IDs dos usuários selecionados
    if (form.value.destinatarios === 'especifico') {
      recadoData.destinatariosEspecificos = form.value.usuariosSelecionados
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

