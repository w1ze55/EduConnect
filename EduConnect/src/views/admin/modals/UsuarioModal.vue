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
                :disabled="formData.role === 'ADMINISTRADOR'"
              >
                <option value="">Selecione a escola</option>
                <option v-for="escola in escolas" :key="escola.id" :value="escola.id">
                  {{ escola.nome }}
                </option>
              </select>
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
                <label class="form-label">Formação Acadêmica</label>
                <input
                  type="text"
                  class="form-control"
                  v-model="formData.formacaoAcademica"
                  required
                  placeholder="Digite a formação acadêmica"
                />
              </div>

              <div class="mb-3">
                <label class="form-label">Disciplinas</label>
                <input
                  type="text"
                  class="form-control"
                  v-model="formData.disciplinas"
                  required
                  placeholder="Digite as disciplinas (separadas por vírgula)"
                />
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
  // Campos específicos
  dataNascimento: '',
  turma: '',
  responsavelId: '',
  formacaoAcademica: '',
  disciplinas: ''
})

const responsaveisDisponiveis = ref([])

onMounted(async () => {
  if (props.usuario) {
    formData.value = { ...props.usuario }
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
  // TODO: Implementar lógica de permissão baseada no usuário atual
  return true
}

const handleRoleChange = () => {
  // Limpar campos específicos quando mudar o tipo de usuário
  formData.value = {
    ...formData.value,
    dataNascimento: '',
    turma: '',
    responsavelId: '',
    formacaoAcademica: '',
    disciplinas: ''
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
