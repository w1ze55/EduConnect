<template>
  <div class="container-fluid py-4">
    <!-- Escolas Section - Apenas ADMIN -->
    <template v-if="isAdmin">
      <div class="row mb-4">
        <div class="col-md-6">
          <h2 class="fw-bold">
            <i class="bi bi-building me-2"></i>Gestão de Escolas
          </h2>
        </div>
        <div class="col-md-6 text-end">
          <button class="btn btn-primary" @click="showEscolaModal('create')">
            <i class="bi bi-plus-circle me-2"></i>Nova Escola
          </button>
        </div>
      </div>

      <div class="card shadow-sm mb-4">
        <div class="card-body">
          <div class="table-responsive">
            <table class="table table-hover">
              <thead>
                <tr>
                  <th>Nome da Escola</th>
                  <th>Diretor Principal</th>
                  <th>Status</th>
                  <th>Ações</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="escola in escolas" :key="escola.id">
                  <td>{{ escola.nome }}</td>
                  <td>
                    <div v-if="escola.diretor">
                      <strong>{{ escola.diretor.nome }}</strong>
                      <br>
                      <small class="text-muted">{{ escola.diretor.email }}</small>
                    </div>
                    <span v-else class="text-muted">Não atribuído</span>
                  </td>
                  <td>
                    <span :class="`badge bg-${escola.ativo ? 'success' : 'secondary'}`">
                      {{ escola.ativo ? 'Ativa' : 'Inativa' }}
                    </span>
                  </td>
                  <td>
                    <button class="btn btn-sm btn-outline-primary me-1" @click="showEscolaModal('edit', escola)">
                      <i class="bi bi-pencil"></i>
                    </button>
                    <button class="btn btn-sm btn-outline-info me-1" @click="showDiretorModal(escola)" :title="escola.diretor ? 'Alterar diretor' : 'Atribuir diretor'">
                      <i class="bi bi-person-badge"></i>
                    </button>
                    <button class="btn btn-sm btn-outline-danger" @click="deleteEscola(escola.id)">
                      <i class="bi bi-trash"></i>
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </template>

    <!-- Usuários Section -->
    <div class="row mb-4">
      <div class="col-md-6">
        <h2 class="fw-bold">
          <i class="bi bi-people-fill me-2"></i>Gestão de Usuários
        </h2>
      </div>
      <div class="col-md-6 text-end">
        <button class="btn btn-primary" @click="showUsuarioModal('create')">
          <i class="bi bi-plus-circle me-2"></i>Novo Usuário
        </button>
      </div>
    </div>
    
    <div class="card shadow-sm">
      <div class="card-body">
        <div class="row g-3 mb-3">
          <div class="col-md-3">
            <input type="text" class="form-control" placeholder="Buscar usuário..." v-model="filtros.busca" />
          </div>
          <div class="col-md-3">
            <select class="form-select" v-model="filtros.role">
              <option value="">Todos os perfis</option>
              <option value="ALUNO">Aluno</option>
              <option value="RESPONSAVEL">Responsável</option>
              <option value="PROFESSOR">Professor</option>
              <option value="DIRETORIA" v-if="isAdmin">Diretoria</option>
              <option value="ADMINISTRADOR" v-if="isAdmin">Administrador</option>
            </select>
          </div>
          <div class="col-md-3" v-if="isAdmin">
            <select class="form-select" v-model="filtros.escolaId">
              <option value="">Todas as escolas</option>
              <option v-for="escola in escolas" :key="escola.id" :value="escola.id">
                {{ escola.nome }}
              </option>
            </select>
          </div>
        </div>
        
        <div class="table-responsive">
          <table class="table table-hover">
            <thead>
              <tr>
                <th>Nome</th>
                <th>E-mail</th>
                <th>Perfil</th>
                <th>Escola</th>
                <th>Status</th>
                <th>Ações</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="usuario in usuariosFiltrados" :key="usuario.id">
                <td>
                  <strong>{{ usuario.nome }}</strong>
                  <div v-if="usuario.role === 'ALUNO' && usuario.responsavelNome" class="small text-muted">
                    Responsável: {{ usuario.responsavelNome }}
                  </div>
                </td>
                <td>{{ usuario.email }}</td>
                <td><span class="badge bg-info">{{ usuario.role }}</span></td>
                <td>
                  <span v-if="usuario.escolaNome">{{ usuario.escolaNome }}</span>
                  <span v-else class="text-muted">-</span>
                </td>
                <td>
                  <span :class="`badge bg-${usuario.ativo ? 'success' : 'secondary'}`">
                    {{ usuario.ativo ? 'Ativo' : 'Inativo' }}
                  </span>
                </td>
                <td>
                  <button class="btn btn-sm btn-outline-primary me-1" @click="showUsuarioModal('edit', usuario)">
                    <i class="bi bi-pencil"></i>
                  </button>
                  <button class="btn btn-sm btn-outline-danger" @click="deleteUsuario(usuario.id)">
                    <i class="bi bi-trash"></i>
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- Modais -->
    <EscolaModal
      v-if="showingEscolaModal"
      :escola="selectedEscola"
      :mode="modalMode"
      @close="closeEscolaModal"
      @save="saveEscola"
    />

    <DiretorModal
      v-if="showingDiretorModal"
      :escola="selectedEscola"
      @close="closeDiretorModal"
      @save="saveDiretor"
    />

    <UsuarioModal
      v-if="showingUsuarioModal"
      :usuario="selectedUsuario"
      :escolas="escolas"
      :mode="modalMode"
      @close="closeUsuarioModal"
      @save="saveUsuario"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import EscolaModal from './modals/EscolaModal.vue'
import DiretorModal from './modals/DiretorModal.vue'
import UsuarioModal from './modals/UsuarioModal.vue'
import escolasService from '@/services/escolasService'
import usuariosService from '@/services/usuariosService'
import { useNotificationStore } from '@/stores/notifications'
import { useAuthStore } from '@/stores/auth'

const notifications = useNotificationStore()
const authStore = useAuthStore()

// Estado
const escolas = ref([])
const usuarios = ref([])
const filtros = ref({ 
  busca: '', 
  role: '',
  escolaId: '' 
})

// Modais
const showingEscolaModal = ref(false)
const showingDiretorModal = ref(false)
const showingUsuarioModal = ref(false)
const modalMode = ref('create')
const selectedEscola = ref(null)
const selectedUsuario = ref(null)

// Computed
const isAdmin = computed(() => authStore.userRole === 'ADMINISTRADOR')
const isDiretoria = computed(() => authStore.userRole === 'DIRETORIA')

const usuariosFiltrados = computed(() => {
  return usuarios.value.filter(usuario => {
    const matchBusca = usuario.nome.toLowerCase().includes(filtros.value.busca.toLowerCase()) ||
                      usuario.email.toLowerCase().includes(filtros.value.busca.toLowerCase())
    const matchRole = !filtros.value.role || usuario.role === filtros.value.role
    const matchEscola = !filtros.value.escolaId || usuario.escolaId === parseInt(filtros.value.escolaId)
    return matchBusca && matchRole && matchEscola
  })
})

// Métodos - Escola
const showEscolaModal = (mode, escola = null) => {
  modalMode.value = mode
  selectedEscola.value = escola ? { ...escola } : null
  showingEscolaModal.value = true
}

const closeEscolaModal = () => {
  showingEscolaModal.value = false
  selectedEscola.value = null
}

const saveEscola = async (escolaData) => {
  try {
    if (modalMode.value === 'create') {
      await escolasService.criar(escolaData)
      notifications.success('Escola criada com sucesso!')
    } else {
      await escolasService.atualizar(escolaData.id, escolaData)
      notifications.success('Escola atualizada com sucesso!')
    }
    closeEscolaModal()
    await loadEscolas()
  } catch (error) {
    console.error('Erro ao salvar escola:', error)
    notifications.error(error.response?.data?.message || 'Erro ao salvar escola')
  }
}

const deleteEscola = async (escolaId) => {
  if (!confirm('Tem certeza que deseja excluir esta escola?')) return
  try {
    await escolasService.deletar(escolaId)
    notifications.success('Escola excluída com sucesso!')
    await loadEscolas()
  } catch (error) {
    console.error('Erro ao excluir escola:', error)
    notifications.error(error.response?.data?.message || 'Erro ao excluir escola')
  }
}

// Métodos - Diretor
const showDiretorModal = (escola) => {
  selectedEscola.value = { ...escola }
  showingDiretorModal.value = true
}

const closeDiretorModal = () => {
  showingDiretorModal.value = false
  selectedEscola.value = null
}

const saveDiretor = async (diretorData) => {
  try {
    if (diretorData.tipo === 'novo') {
      // Criar novo diretor
      const novoUsuario = {
        nome: diretorData.dados.nome,
        email: diretorData.dados.email,
        senha: diretorData.dados.senha,
        cpf: diretorData.dados.cpf,
        telefone: diretorData.dados.telefone,
        role: 'DIRETORIA',
        escolaId: diretorData.dados.escolaId,
        ativo: true
      }
      
      console.log('Criando diretor:', novoUsuario)
      
      // Criar o diretor
      const diretorCriado = await usuariosService.criar(novoUsuario)
      
      console.log('Diretor criado:', diretorCriado)
      
      // Atribuir o diretor à escola
      console.log('Atribuindo diretor à escola:', diretorData.dados.escolaId, diretorCriado.id)
      await escolasService.atribuirDiretor(diretorData.dados.escolaId, diretorCriado.id)
      
      notifications.success('Diretor criado e atribuído com sucesso!')
    } else {
      // Atribuir diretor existente
      console.log('Atribuindo diretor existente:', {
        escolaId: diretorData.dados.escolaId,
        diretorId: diretorData.dados.diretorId
      })
      
      await escolasService.atribuirDiretor(diretorData.dados.escolaId, diretorData.dados.diretorId)
      notifications.success('Diretor atribuído com sucesso!')
    }
    closeDiretorModal()
    await loadEscolas()
    await loadUsuarios()
  } catch (error) {
    console.error('Erro completo ao salvar diretor:', error)
    console.error('Resposta do servidor:', error.response)
    notifications.error(error.response?.data?.message || error.message || 'Erro ao atribuir diretor')
  }
}

// Métodos - Usuário
const showUsuarioModal = (mode, usuario = null) => {
  modalMode.value = mode
  selectedUsuario.value = usuario ? { ...usuario } : null
  showingUsuarioModal.value = true
}

const closeUsuarioModal = () => {
  showingUsuarioModal.value = false
  selectedUsuario.value = null
}

const saveUsuario = async (usuarioData) => {
  try {
    console.log('Salvando usuário:', usuarioData)
    console.log('Modo:', modalMode.value)
    
    if (modalMode.value === 'create') {
      const resultado = await usuariosService.criar(usuarioData)
      console.log('Usuário criado:', resultado)
      notifications.success('Usuário criado com sucesso!')
    } else {
      console.log('Atualizando usuário ID:', usuarioData.id)
      const resultado = await usuariosService.atualizar(usuarioData.id, usuarioData)
      console.log('Usuário atualizado:', resultado)
      notifications.success('Usuário atualizado com sucesso!')
    }
    closeUsuarioModal()
    await loadUsuarios()
  } catch (error) {
    console.error('Erro completo ao salvar usuário:', error)
    console.error('Resposta do servidor:', error.response?.data)
    notifications.error(error.response?.data?.message || error.message || 'Erro ao salvar usuário')
  }
}

const deleteUsuario = async (usuarioId) => {
  if (!confirm('Tem certeza que deseja excluir este usuário?')) return
  try {
    await usuariosService.deletar(usuarioId)
    notifications.success('Usuário excluído com sucesso!')
    await loadUsuarios()
  } catch (error) {
    console.error('Erro ao excluir usuário:', error)
    notifications.error(error.response?.data?.message || 'Erro ao excluir usuário')
  }
}

// Carregamento de dados
const loadEscolas = async () => {
  try {
    escolas.value = await escolasService.listarTodas()
  } catch (error) {
    console.error('Erro ao carregar escolas:', error)
    notifications.error('Erro ao carregar escolas')
  }
}

const loadUsuarios = async () => {
  try {
    usuarios.value = await usuariosService.listar()
  } catch (error) {
    console.error('Erro ao carregar usuários:', error)
    notifications.error('Erro ao carregar usuários')
  }
}

// Inicialização
onMounted(() => {
  // Carregar escolas apenas para admin (diretoria carrega automaticamente a sua escola através da API)
  loadEscolas()
  loadUsuarios()
})
</script>

