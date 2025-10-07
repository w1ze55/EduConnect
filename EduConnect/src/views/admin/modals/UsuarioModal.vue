<template>
  <div class="modal fade show" style="display: block; background: rgba(0,0,0,0.5)">
    <div class="modal-dialog">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">{{ mode === 'create' ? 'Novo Usuário' : 'Editar Usuário' }}</h5>
          <button type="button" class="btn-close" @click="$emit('close')"></button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="handleSubmit">
            <div class="mb-3">
              <label class="form-label">Tipo de Usuário</label>
              <select class="form-select" v-model="formData.role" required @change="handleRoleChange">
                <option value="">Selecione o tipo</option>
                <option v-if="canCreateRole('ADMINISTRADOR')" value="ADMINISTRADOR">Administrador</option>
                <option v-if="canCreateRole('DIRETORIA')" value="DIRETORIA">Diretor</option>
                <option value="PROFESSOR">Professor</option>
                <option value="ALUNO">Aluno</option>
                <option value="RESPONSAVEL">Responsável</option>
              </select>
            </div>

            <div class="mb-3">
              <label class="form-label">Escola</label>
              <select 
                class="form-select" 
                v-model="formData.escolaId" 
                required
                :disabled="formData.role === 'ADMINISTRADOR' || authStore.userRole === 'DIRETORIA'"
              >
                <option value="">Selecione a escola</option>
                <option v-for="escola in escolas" :key="escola.id" :value="escola.id">
                  {{ escola.nome }}
                </option>
              </select>
              <small v-if="authStore.userRole === 'DIRETORIA'" class="text-muted">
                Como diretor, você só pode criar usuários para sua escola
              </small>
            </div>

            <div class="mb-3">
              <label class="form-label">Nome Completo</label>
              <input
                type="text"
                class="form-control"
                v-model="formData.nome"
                required
                placeholder="Digite o nome completo"
              />
            </div>

            <div class="mb-3">
              <label class="form-label">Email</label>
              <input
                type="email"
                class="form-control"
                v-model="formData.email"
                required
                placeholder="Digite o email"
              />
            </div>

            <div class="mb-3">
              <label class="form-label">CPF</label>
              <input
                type="text"
                class="form-control"
                v-model="formData.cpf"
                required
                placeholder="Digite o CPF"
              />
            </div>

            <div class="mb-3">
              <label class="form-label">Telefone</label>
              <input
                type="tel"
                class="form-control"
                v-model="formData.telefone"
                required
                placeholder="Digite o telefone"
              />
            </div>

            <div class="mb-3">
              <label class="form-label">Senha {{ mode === 'edit' ? '(deixe em branco para não alterar)' : '' }}</label>
              <input
                type="password"
                class="form-control"
                v-model="formData.senha"
                :required="mode === 'create'"
                :placeholder="mode === 'create' ? 'Digite a senha' : 'Digite a nova senha (opcional)'"
              />
            </div>

            <!-- Campos específicos para Aluno -->
            <div v-if="formData.role === 'ALUNO'" class="border rounded p-3 mb-3">
              <h6 class="mb-3">Dados do Aluno</h6>
              
              <div class="mb-3">
                <label class="form-label">Data de Nascimento</label>
                <input
                  type="date"
                  class="form-control"
                  v-model="formData.dataNascimento"
                  required
                />
              </div>

              <div class="mb-3">
                <label class="form-label">Série/Turma</label>
                <input
                  type="text"
                  class="form-control"
                  v-model="formData.turma"
                  required
                  placeholder="Digite a série/turma"
                />
              </div>

              <div class="mb-3">
                <label class="form-label">Responsável</label>
                <select class="form-select" v-model="formData.responsavelId" required>
                  <option value="">Selecione o responsável</option>
                  <option v-for="resp in responsaveisDisponiveis" :key="resp.id" :value="resp.id">
                    {{ resp.nome }}
                  </option>
                </select>
                <small class="text-muted">
                  Se o responsável ainda não está cadastrado, cadastre-o primeiro.
                </small>
              </div>
            </div>

            <!-- Campos específicos para Professor -->
            <div v-if="formData.role === 'PROFESSOR'" class="border rounded p-3 mb-3">
              <h6 class="mb-3">Dados do Professor</h6>
              
              <div class="mb-3">
                <label class="form-label">Disciplinas</label>
                <input
                  type="text"
                  class="form-control"
                  v-model="formData.disciplinas"
                  required
                  placeholder="Ex: Matemática, Física, Química"
                />
                <small class="text-muted">Separe múltiplas disciplinas por vírgula</small>
              </div>

              <div class="mb-3">
                <label class="form-label">Turmas (Opcional)</label>
                <input
                  type="text"
                  class="form-control"
                  v-model="formData.turmas"
                  placeholder="Ex: 1A, 2B, 3C"
                />
                <small class="text-muted">Separe múltiplas turmas por vírgula</small>
              </div>
            </div>

            <div class="mb-3">
              <div class="form-check">
                <input
                  type="checkbox"
                  class="form-check-input"
                  v-model="formData.ativo"
                  id="usuarioAtivo"
                />
                <label class="form-check-label" for="usuarioAtivo">Usuário Ativo</label>
              </div>
            </div>
          </form>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" @click="$emit('close')">Cancelar</button>
          <button type="button" class="btn btn-primary" @click="handleSubmit">Salvar</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import api from '@/services/api'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()

const props = defineProps({
  usuario: {
    type: Object,
    default: null
  },
  escolas: {
    type: Array,
    required: true
  },
  mode: {
    type: String,
    required: true,
    validator: value => ['create', 'edit'].includes(value)
  }
})

const emit = defineEmits(['close', 'save'])

const formData = ref({
  nome: '',
  email: '',
  cpf: '',
  telefone: '',
  senha: '',
  role: '',
  escolaId: '',
  ativo: true,
  // Campos específicos para ALUNO
  dataNascimento: '',
  turma: '',
  responsavelId: '',
  // Campos específicos para PROFESSOR
  disciplinas: '',
  turmas: ''
})

const responsaveisDisponiveis = ref([])

onMounted(async () => {
  if (props.usuario) {
    formData.value = { ...props.usuario }
    
    // Converter arrays para strings para exibição no formulário
    if (formData.value.role === 'PROFESSOR') {
      if (Array.isArray(formData.value.disciplinas)) {
        formData.value.disciplinas = formData.value.disciplinas.join(', ')
      }
      if (Array.isArray(formData.value.turmas)) {
        formData.value.turmas = formData.value.turmas.join(', ')
      }
    }
  } else if (authStore.userRole === 'DIRETORIA' && authStore.user?.escolaId) {
    // Se for diretor, pré-selecionar sua escola
    formData.value.escolaId = authStore.user.escolaId
  }
  
  if (formData.value.role === 'ALUNO') {
    await loadResponsaveis()
  }
})

watch(() => formData.value.role, async (newRole) => {
  if (newRole === 'ALUNO') {
    await loadResponsaveis()
  }
})

const loadResponsaveis = async () => {
  try {
    const params = formData.value.escolaId ? { escolaId: formData.value.escolaId } : {}
    const response = await api.get('/usuarios/responsaveis-disponiveis', { params })
    responsaveisDisponiveis.value = response.data
    console.log('Responsáveis disponíveis carregados:', responsaveisDisponiveis.value)
  } catch (error) {
    console.error('Erro ao carregar responsáveis:', error)
  }
}

const canCreateRole = (role) => {
  // Apenas administradores podem criar ADMINISTRADOR e DIRETORIA
  if (role === 'ADMINISTRADOR' || role === 'DIRETORIA') {
    return authStore.userRole === 'ADMINISTRADOR'
  }
  return true
}

const handleRoleChange = () => {
  // Limpar campos específicos quando mudar o tipo de usuário
  formData.value = {
    ...formData.value,
    dataNascimento: '',
    turma: '',
    responsavelId: '',
    disciplinas: '',
    turmas: ''
  }
  
  // Limpar escola se for administrador
  if (formData.value.role === 'ADMINISTRADOR') {
    formData.value.escolaId = ''
  }
}

const handleSubmit = () => {
  // Criar uma cópia dos dados
  const dadosParaEnviar = { ...formData.value }
  
  // Se estiver editando e a senha estiver vazia, remover do objeto
  if (props.mode === 'edit' && (!dadosParaEnviar.senha || dadosParaEnviar.senha.trim() === '')) {
    delete dadosParaEnviar.senha
  }
  
  // Converter disciplinas de string para array (para PROFESSOR)
  if (dadosParaEnviar.role === 'PROFESSOR' && dadosParaEnviar.disciplinas) {
    if (typeof dadosParaEnviar.disciplinas === 'string') {
      dadosParaEnviar.disciplinas = dadosParaEnviar.disciplinas
        .split(',')
        .map(d => d.trim())
        .filter(d => d.length > 0)
    }
  }
  
  // Converter turmas de string para array (para PROFESSOR) se existir
  if (dadosParaEnviar.role === 'PROFESSOR' && dadosParaEnviar.turmas) {
    if (typeof dadosParaEnviar.turmas === 'string') {
      dadosParaEnviar.turmas = dadosParaEnviar.turmas
        .split(',')
        .map(t => t.trim())
        .filter(t => t.length > 0)
    }
  }
  
  // Validar campos obrigatórios
  if (!dadosParaEnviar.nome || !dadosParaEnviar.email || !dadosParaEnviar.cpf || 
      !dadosParaEnviar.telefone || !dadosParaEnviar.role) {
    alert('Por favor, preencha todos os campos obrigatórios')
    return
  }
  
  // Validar senha na criação
  if (props.mode === 'create' && (!dadosParaEnviar.senha || dadosParaEnviar.senha.trim() === '')) {
    alert('Senha é obrigatória')
    return
  }
  
  // Validar escola (exceto para administrador)
  if (dadosParaEnviar.role !== 'ADMINISTRADOR' && !dadosParaEnviar.escolaId) {
    alert('Escola é obrigatória para este tipo de usuário')
    return
  }
  
  // Validar responsável para aluno
  if (dadosParaEnviar.role === 'ALUNO' && !dadosParaEnviar.responsavelId) {
    alert('Responsável é obrigatório para alunos')
    return
  }
  
  emit('save', dadosParaEnviar)
}
</script>
