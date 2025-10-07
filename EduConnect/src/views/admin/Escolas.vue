<template>
  <div class="container-fluid py-4">
    <div class="row mb-4">
      <div class="col-md-6">
        <h2 class="fw-bold">
          <i class="bi bi-building me-2"></i>Gestão de Escolas
        </h2>
        <p class="text-muted">Cadastre escolas e seus diretores principais</p>
      </div>
      <div class="col-md-6 text-end">
        <button class="btn btn-primary" @click="abrirModalCriarEscola">
          <i class="bi bi-plus-circle me-2"></i>Nova Escola
        </button>
      </div>
    </div>
    
    <div class="card shadow-sm">
      <div class="card-body">
        <div class="row g-3 mb-3">
          <div class="col-md-6">
            <input 
              type="text" 
              class="form-control" 
              placeholder="Buscar escola..." 
              v-model="filtros.busca" 
            />
          </div>
          <div class="col-md-3">
            <select class="form-select" v-model="filtros.status">
              <option value="">Todos os status</option>
              <option value="ativo">Ativas</option>
              <option value="inativo">Inativas</option>
            </select>
          </div>
        </div>
        
        <div class="table-responsive">
          <table class="table table-hover">
            <thead>
              <tr>
                <th>Escola</th>
                <th>CNPJ</th>
                <th>Diretor Principal</th>
                <th>Usuários</th>
                <th>Status</th>
                <th>Ações</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="escola in escolasFiltradas" :key="escola.id">
                <td>
                  <div>
                    <strong>{{ escola.nome }}</strong>
                    <div class="text-muted small">{{ escola.email }}</div>
                    <div class="text-muted small">{{ escola.telefone }}</div>
                  </div>
                </td>
                <td>{{ escola.cnpj }}</td>
                <td>
                  <div v-if="escola.diretor">
                    <strong>{{ escola.diretor.nome }}</strong>
                    <div class="text-muted small">{{ escola.diretor.email }}</div>
                  </div>
                  <span v-else class="text-warning">
                    <i class="bi bi-exclamation-triangle me-1"></i>Sem diretor
                  </span>
                </td>
                <td>
                  <div class="d-flex gap-2">
                    <span class="badge bg-info">{{ escola.totalUsuarios || 0 }} usuários</span>
                    <span class="badge bg-success">{{ escola.totalAlunos || 0 }} alunos</span>
                  </div>
                </td>
                <td>
                  <span :class="`badge bg-${escola.ativo ? 'success' : 'secondary'}`">
                    {{ escola.ativo ? 'Ativa' : 'Inativa' }}
                  </span>
                </td>
                <td>
                  <div class="btn-group btn-group-sm">
                    <button 
                      class="btn btn-outline-primary"
                      @click="editarEscola(escola)"
                      title="Editar escola"
                    >
                      <i class="bi bi-pencil"></i>
                    </button>
                    <button 
                      class="btn btn-outline-info"
                      @click="gerenciarUsuarios(escola)"
                      title="Gerenciar usuários"
                    >
                      <i class="bi bi-people"></i>
                    </button>
                    <button 
                      class="btn btn-outline-warning"
                      @click="definirDiretor(escola)"
                      title="Definir/alterar diretor"
                    >
                      <i class="bi bi-person-badge"></i>
                    </button>
                    <button 
                      class="btn btn-outline-danger"
                      @click="confirmarExclusao(escola)"
                      title="Excluir escola"
                    >
                      <i class="bi bi-trash"></i>
                    </button>
                  </div>
                </td>
              </tr>
              <tr v-if="escolas.length === 0">
                <td colspan="6" class="text-center text-muted py-4">
                  <i class="bi bi-building-add fs-1 d-block mb-2"></i>
                  Nenhuma escola cadastrada. Clique em "Nova Escola" para começar.
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- Modal Criar/Editar Escola -->
    <div class="modal fade" :class="{ show: modalEscola }" :style="{ display: modalEscola ? 'block' : 'none' }" tabindex="-1">
      <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">
              <i class="bi bi-building me-2"></i>
              {{ escolaEditando ? 'Editar Escola' : 'Nova Escola' }}
            </h5>
            <button type="button" class="btn-close" @click="fecharModal"></button>
          </div>
          <div class="modal-body">
            <form @submit.prevent="salvarEscola">
              <div class="row">
                <div class="col-md-8">
                  <label class="form-label">Nome da Escola *</label>
                  <input 
                    type="text" 
                    class="form-control" 
                    v-model="formEscola.nome" 
                    required 
                    placeholder="Ex: Colégio São Paulo"
                  />
                </div>
                <div class="col-md-4">
                  <label class="form-label">CNPJ *</label>
                  <input 
                    type="text" 
                    class="form-control" 
                    v-model="formEscola.cnpj" 
                    required 
                    placeholder="00.000.000/0000-00"
                  />
                </div>
              </div>
              
              <div class="row mt-3">
                <div class="col-md-8">
                  <label class="form-label">E-mail Institucional *</label>
                  <input 
                    type="email" 
                    class="form-control" 
                    v-model="formEscola.email" 
                    required 
                    placeholder="contato@escola.edu.br"
                  />
                </div>
                <div class="col-md-4">
                  <label class="form-label">Telefone</label>
                  <input 
                    type="text" 
                    class="form-control" 
                    v-model="formEscola.telefone" 
                    placeholder="(11) 3456-7890"
                  />
                </div>
              </div>
              
              <div class="mt-3">
                <label class="form-label">Endereço Completo</label>
                <input 
                  type="text" 
                  class="form-control" 
                  v-model="formEscola.endereco" 
                  placeholder="Rua, número, bairro, cidade - UF"
                />
              </div>
              
              <div class="mt-3">
                <div class="form-check">
                  <input 
                    class="form-check-input" 
                    type="checkbox" 
                    v-model="formEscola.ativo" 
                    id="escolaAtiva"
                  />
                  <label class="form-check-label" for="escolaAtiva">
                    Escola Ativa
                  </label>
                </div>
              </div>

              <div class="mt-3 p-3 bg-light rounded">
                <h6 class="text-primary mb-2">
                  <i class="bi bi-info-circle me-1"></i>Próximos Passos
                </h6>
                <p class="mb-0 small text-muted">
                  Após criar a escola, você poderá cadastrar o diretor principal e outros usuários.
                </p>
              </div>
            </form>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="fecharModal">
              Cancelar
            </button>
            <button type="button" class="btn btn-primary" @click="salvarEscola" :disabled="salvando">
              <span v-if="salvando">
                <span class="spinner-border spinner-border-sm me-2"></span>
                Salvando...
              </span>
              <span v-else>
                <i class="bi bi-check-lg me-2"></i>
                {{ escolaEditando ? 'Atualizar' : 'Criar Escola' }}
              </span>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal Definir Diretor -->
    <div class="modal fade" :class="{ show: modalDiretor }" :style="{ display: modalDiretor ? 'block' : 'none' }" tabindex="-1">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">
              <i class="bi bi-person-badge me-2"></i>
              Definir Diretor Principal
            </h5>
            <button type="button" class="btn-close" @click="fecharModalDiretor"></button>
          </div>
          <div class="modal-body">
            <p class="text-muted mb-3">
              Escola: <strong>{{ escolaSelecionada?.nome }}</strong>
            </p>
            
            <div class="mb-3">
              <label class="form-label">Diretor Atual</label>
              <div v-if="escolaSelecionada?.diretor" class="p-2 bg-light rounded">
                <strong>{{ escolaSelecionada.diretor.nome }}</strong>
                <div class="text-muted small">{{ escolaSelecionada.diretor.email }}</div>
              </div>
              <div v-else class="p-2 bg-warning bg-opacity-10 rounded text-warning">
                <i class="bi bi-exclamation-triangle me-1"></i>
                Nenhum diretor definido
              </div>
            </div>

            <div class="mb-3">
              <label class="form-label">Selecionar Novo Diretor</label>
              <select class="form-select" v-model="novodiretorId">
                <option value="">Selecione um diretor...</option>
                <option v-for="diretor in diretoresDisponiveis" :key="diretor.id" :value="diretor.id">
                  {{ diretor.nome }} - {{ diretor.email }}
                </option>
              </select>
              <div class="form-text">
                Apenas usuários com perfil "Diretoria" aparecem nesta lista.
              </div>
            </div>

            <div class="alert alert-info">
              <i class="bi bi-info-circle me-2"></i>
              <strong>Importante:</strong> O diretor principal poderá cadastrar professores, alunos e responsáveis desta escola.
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="fecharModalDiretor">
              Cancelar
            </button>
            <button type="button" class="btn btn-primary" @click="definirNovodiretorr" :disabled="!novodiretorId">
              <i class="bi bi-check-lg me-2"></i>
              Definir Diretor
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal backdrop -->
    <div v-if="modalEscola || modalDiretor" class="modal-backdrop fade show"></div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useNotificationStore } from '../../stores/notifications'
import { useAuthStore } from '../../stores/auth'

const router = useRouter()
const notificationStore = useNotificationStore()
const authStore = useAuthStore()

// Estados
const escolas = ref([])
const diretoresDisponiveis = ref([])
const filtros = ref({ busca: '', status: '' })
const modalEscola = ref(false)
const modalDiretor = ref(false)
const escolaEditando = ref(null)
const escolaSelecionada = ref(null)
const salvando = ref(false)
const novodiretorId = ref('')

// Formulário
const formEscola = ref({
  nome: '',
  cnpj: '',
  email: '',
  telefone: '',
  endereco: '',
  ativo: true
})

// Computed
const escolasFiltradas = computed(() => {
  let resultado = escolas.value

  if (filtros.value.busca) {
    resultado = resultado.filter(escola => 
      escola.nome.toLowerCase().includes(filtros.value.busca.toLowerCase()) ||
      escola.cnpj.includes(filtros.value.busca) ||
      escola.email.toLowerCase().includes(filtros.value.busca.toLowerCase())
    )
  }

  if (filtros.value.status) {
    const ativo = filtros.value.status === 'ativo'
    resultado = resultado.filter(escola => escola.ativo === ativo)
  }

  return resultado
})

// Métodos
const carregarEscolas = async () => {
  try {
    // TODO: Implementar chamada para API
    console.log('📚 [ESCOLAS] Carregando escolas...')
    // Por enquanto, lista vazia até integração com backend
    escolas.value = []
  } catch (error) {
    console.error('Erro ao carregar escolas:', error)
    notificationStore.error('Erro ao carregar escolas')
  }
}

const carregarDiretores = async () => {
  try {
    // TODO: Implementar chamada para API para buscar usuários com role DIRETORIA
    console.log('👥 [ESCOLAS] Carregando diretores disponíveis...')
    diretoresDisponiveis.value = []
  } catch (error) {
    console.error('Erro ao carregar diretores:', error)
  }
}

const abrirModalCriarEscola = () => {
  escolaEditando.value = null
  formEscola.value = {
    nome: '',
    cnpj: '',
    email: '',
    telefone: '',
    endereco: '',
    ativo: true
  }
  modalEscola.value = true
}

const editarEscola = (escola) => {
  escolaEditando.value = escola
  formEscola.value = { ...escola }
  modalEscola.value = true
}

const fecharModal = () => {
  modalEscola.value = false
  escolaEditando.value = null
}

const salvarEscola = async () => {
  salvando.value = true
  
  try {
    if (escolaEditando.value) {
      // TODO: Implementar API de atualização
      console.log('✏️ [ESCOLAS] Atualizando escola:', formEscola.value)
      notificationStore.success('Escola atualizada com sucesso!')
    } else {
      // TODO: Implementar API de criação
      console.log('➕ [ESCOLAS] Criando escola:', formEscola.value)
      notificationStore.success('Escola criada com sucesso!')
    }
    
    fecharModal()
    await carregarEscolas()
  } catch (error) {
    console.error('Erro ao salvar escola:', error)
    notificationStore.error('Erro ao salvar escola')
  } finally {
    salvando.value = false
  }
}

const definirDiretor = async (escola) => {
  escolaSelecionada.value = escola
  novodiretorId.value = escola.diretor?.id || ''
  await carregarDiretores()
  modalDiretor.value = true
}

const fecharModalDiretor = () => {
  modalDiretor.value = false
  escolaSelecionada.value = null
  novodiretorId.value = ''
}

const definirNovodiretorr = async () => {
  try {
    // TODO: Implementar API para associar diretor à escola
    console.log('👤 [ESCOLAS] Definindo diretor:', novodiretorId.value, 'para escola:', escolaSelecionada.value.id)
    notificationStore.success('Diretor definido com sucesso!')
    fecharModalDiretor()
    await carregarEscolas()
  } catch (error) {
    console.error('Erro ao definir diretor:', error)
    notificationStore.error('Erro ao definir diretor')
  }
}

const gerenciarUsuarios = (escola) => {
  router.push({
    name: 'AdminUsuarios',
    query: { escolaId: escola.id, escolaNome: escola.nome }
  })
}

const confirmarExclusao = async (escola) => {
  if (confirm(`Tem certeza que deseja excluir a escola "${escola.nome}"?\n\nEsta ação também removerá todos os usuários associados.`)) {
    try {
      // TODO: Implementar API de exclusão
      console.log('🗑️ [ESCOLAS] Excluindo escola:', escola.id)
      notificationStore.success('Escola excluída com sucesso!')
      await carregarEscolas()
    } catch (error) {
      console.error('Erro ao excluir escola:', error)
      notificationStore.error('Erro ao excluir escola')
    }
  }
}

onMounted(async () => {
  console.log('🏫 [ESCOLAS] Componente carregado')
  await carregarEscolas()
})
</script>

<style scoped>
.modal.show {
  background-color: rgba(0, 0, 0, 0.5);
}

.table th {
  background-color: #f8f9fa;
  font-weight: 600;
}

.badge {
  font-size: 0.75rem;
}

.btn-group-sm .btn {
  padding: 0.25rem 0.5rem;
}
</style>


