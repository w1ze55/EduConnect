<template>
  <div class="modal fade show" style="display: block; background: rgba(0,0,0,0.5)">
    <div class="modal-dialog modal-lg">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">{{ mode === 'create' ? 'Novo Usuário' : 'Editar Usuário' }}</h5>
          <button type="button" class="btn-close" @click="$emit('close')"></button>
        </div>
        
        <div class="modal-body">
          <!-- Indicador de Progresso -->
          <div class="progress mb-4" style="height: 8px;">
            <div 
              class="progress-bar" 
              role="progressbar" 
              :style="`width: ${progressoPercentual}%`"
              :aria-valuenow="progressoPercentual" 
              aria-valuemin="0" 
              aria-valuemax="100"
            ></div>
          </div>

          <!-- Steps Navigation -->
          <ul class="nav nav-pills nav-fill mb-4">
            <li class="nav-item">
              <button
                type="button"
                class="nav-link"
                :class="{ active: etapaAtual === 1, 'text-success': etapaAtual > 1 }"
                @click="irParaEtapa(1)"
              >
                <i class="bi" :class="etapaAtual > 1 ? 'bi-check-circle-fill' : 'bi-1-circle'"></i>
                <span class="d-none d-md-inline ms-1">Tipo e Escola</span>
              </button>
            </li>
            <li class="nav-item">
              <button
                type="button"
                class="nav-link"
                :class="{ active: etapaAtual === 2, 'text-success': etapaAtual > 2 }"
                @click="irParaEtapa(2)"
                :disabled="!podeAvancar(1)"
              >
                <i class="bi" :class="etapaAtual > 2 ? 'bi-check-circle-fill' : 'bi-2-circle'"></i>
                <span class="d-none d-md-inline ms-1">Dados Pessoais</span>
              </button>
            </li>
            <li class="nav-item">
              <button
                type="button"
                class="nav-link"
                :class="{ active: etapaAtual === 3, 'text-success': etapaAtual > 3 }"
                @click="irParaEtapa(3)"
                :disabled="!podeAvancar(2)"
              >
                <i class="bi" :class="etapaAtual > 3 ? 'bi-check-circle-fill' : 'bi-3-circle'"></i>
                <span class="d-none d-md-inline ms-1">Dados Específicos</span>
              </button>
            </li>
          </ul>

          <form @submit.prevent="handleSubmit">
            <!-- Etapa 1: Tipo e Escola -->
            <div v-show="etapaAtual === 1">
              <h6 class="mb-3"><i class="bi bi-person-badge me-2"></i>Tipo de Usuário e Escola</h6>
              
              <div class="mb-3">
                <label class="form-label">Tipo de Usuário *</label>
                <select class="form-select" v-model="formData.role" required @change="handleRoleChange">
                  <option value="">Selecione o tipo</option>
                  <option v-if="canCreateRole('ADMINISTRADOR')" value="ADMINISTRADOR">Administrador</option>
                  <option v-if="canCreateRole('DIRETORIA')" value="DIRETORIA">Diretor</option>
                  <option value="PROFESSOR">Professor</option>
                  <option value="ALUNO">Aluno</option>
                  <option value="RESPONSAVEL">Responsável</option>
                </select>
              </div>

              <div class="mb-3" v-if="formData.role">
                <label class="form-label">Escola {{ formData.role !== 'ADMINISTRADOR' ? '*' : '' }}</label>
                <select 
                  class="form-select" 
                  v-model="formData.escolaId" 
                  :required="formData.role !== 'ADMINISTRADOR'"
                  :disabled="formData.role === 'ADMINISTRADOR' || authStore.userRole === 'DIRETORIA'"
                >
                  <option value="">Selecione a escola</option>
                  <option v-for="escola in escolas" :key="escola.id" :value="String(escola.id)">
                    {{ escola.nome }}
                  </option>
                </select>
                <div v-if="authStore.userRole === 'DIRETORIA' && formData.escolaId" class="alert alert-success mt-2 mb-0 py-2">
                  <i class="bi bi-check-circle-fill me-2"></i>
                  <strong>Escola automaticamente vinculada: </strong> 
                  <span v-if="escolaNomeDoUsuario">{{ escolaNomeDoUsuario }}</span>
                </div>
                <small v-if="formData.role === 'ADMINISTRADOR'" class="text-muted">
                  Administradores não são vinculados a uma escola específica
                </small>
                <small v-if="authStore.userRole === 'ADMINISTRADOR' && formData.role === 'DIRETORIA'" class="text-muted">
                  Selecione a escola que este diretor irá gerenciar
                </small>
              </div>

              <!-- Alerta para aluno -->
              <div v-if="formData.role === 'ALUNO'" class="alert alert-info" role="alert">
                <i class="bi bi-info-circle-fill me-2"></i>
                <strong>Lembre-se:</strong> Para cadastrar um aluno você deve vincular a um responsável. Cadastre o responsável primeiro se necessário!
              </div>
            </div>

            <!-- Etapa 2: Dados Pessoais -->
            <div v-show="etapaAtual === 2">
              <h6 class="mb-3"><i class="bi bi-person-fill me-2"></i>Dados Pessoais</h6>
              
              <div class="mb-3">
                <label class="form-label">Nome Completo *</label>
                <input
                  type="text"
                  class="form-control"
                  v-model="formData.nome"
                  required
                  placeholder="Digite o nome completo"
                />
              </div>

              <div class="mb-3">
                <label class="form-label">Email *</label>
                <input
                  type="email"
                  class="form-control"
                  :class="{ 'is-invalid': formData.email && !validarEmail(formData.email) }"
                  v-model="formData.email"
                  required
                  placeholder="exemplo@email.com"
                  @blur="validarEmailEmTempoReal"
                />
                <div v-if="formData.email && !validarEmail(formData.email)" class="invalid-feedback d-block">
                  <small class="text-danger">Digite um email válido (ex: exemplo@email.com)</small>
                </div>
              </div>

              <div class="row">
                <div class="col-md-6 mb-3">
                  <label class="form-label">CPF *</label>
                  <input
                    type="text"
                    class="form-control"
                    :class="{ 'is-invalid': formData.cpf && !validarCPF(formData.cpf) }"
                    v-model="formData.cpf"
                    required
                    placeholder="000.000.000-00"
                    maxlength="14"
                    @input="aplicarMascaraCPF"
                  />
                  <div v-if="formData.cpf && !validarCPF(formData.cpf)" class="invalid-feedback d-block">
                    <small class="text-danger">Digite um CPF válido (000.000.000-00)</small>
                  </div>
                </div>

                <div class="col-md-6 mb-3">
                  <label class="form-label">Telefone *</label>
                  <input
                    type="tel"
                    class="form-control"
                    :class="{ 'is-invalid': formData.telefone && !validarTelefone(formData.telefone) }"
                    v-model="formData.telefone"
                    required
                    placeholder="(00) 00000-0000"
                    maxlength="15"
                    @input="aplicarMascaraTelefone"
                  />
                  <div v-if="formData.telefone && !validarTelefone(formData.telefone)" class="invalid-feedback d-block">
                    <small class="text-danger">Digite um telefone válido (00) 00000-0000 ou (00) 0000-0000</small>
                  </div>
                </div>
              </div>

              <div class="mb-3">
                <label class="form-label">Senha {{ mode === 'edit' ? '(deixe em branco para não alterar)' : '*' }}</label>
                <input
                  type="password"
                  class="form-control"
                  :class="{ 'is-invalid': formData.senha && !validarSenha(formData.senha).valida }"
                  v-model="formData.senha"
                  :required="mode === 'create'"
                  :placeholder="mode === 'create' ? 'Digite a senha' : 'Digite a nova senha (opcional)'"
                  @input="validarSenhaEmTempoReal"
                />
                <div v-if="formData.senha && !validarSenha(formData.senha).valida" class="invalid-feedback d-block">
                  <small class="text-danger">A senha deve atender aos seguintes critérios:</small>
                  <ul class="mb-0 mt-1 small">
                    <li :class="{ 'text-success': validarSenha(formData.senha).temMinimo }">Mínimo 8 caracteres</li>
                    <li :class="{ 'text-success': validarSenha(formData.senha).temMaiuscula }">1 letra maiúscula</li>
                    <li :class="{ 'text-success': validarSenha(formData.senha).temMinuscula }">1 letra minúscula</li>
                    <li :class="{ 'text-success': validarSenha(formData.senha).temNumero }">1 número</li>
                    <li :class="{ 'text-success': validarSenha(formData.senha).temEspecial }">1 caractere especial (!@#$%^&*)</li>
                  </ul>
                </div>
                <small v-else-if="formData.senha && validarSenha(formData.senha).valida" class="text-success">
                  <i class="bi bi-check-circle-fill me-1"></i>Senha válida
                </small>
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
            </div>

            <!-- Etapa 3: Dados Específicos -->
            <div v-show="etapaAtual === 3">
              <!-- Dados do Aluno -->
              <div v-if="formData.role === 'ALUNO'">
                <h6 class="mb-3"><i class="bi bi-mortarboard-fill me-2"></i>Dados do Aluno</h6>
                
                <div class="row">
                  <div class="col-md-6 mb-3">
                    <label class="form-label">Data de Nascimento *</label>
                    <input
                      type="date"
                      class="form-control"
                      v-model="formData.dataNascimento"
                      required
                    />
                  </div>

                  <div class="col-md-6 mb-3">
                    <label class="form-label">Matrícula</label>
                    <input
                      type="text"
                      class="form-control"
                      v-model="formData.matricula"
                      placeholder="Ex: 2024001"
                    />
                  </div>
                </div>

                <div class="mb-3">
                  <label class="form-label">Turma *</label>
                  <select class="form-select" v-model="formData.turma" required>
                    <option value="">Selecione a turma</option>
                    <option v-for="turma in turmasDisponiveis" :key="turma.id" :value="turma.nome">
                      {{ turma.nome }} - {{ turma.anoLetivo || turma.ano }}
                    </option>
                  </select>
                  <small class="text-muted d-block">
                    Se a turma ainda não está cadastrada, cadastre-a primeiro.
                  </small>
                  <small class="text-info d-block mt-1" v-if="formData.turma">
                    <strong>Turma selecionada:</strong> {{ formData.turma }}
                  </small>
                  <small class="text-warning d-block mt-1" v-if="turmasDisponiveis.length === 0">
                    ⚠️ Nenhuma turma disponível. Cadastre uma turma primeiro.
                  </small>
                </div>

                <div class="mb-3">
                  <label class="form-label">Responsável *</label>
                  <select class="form-select" v-model="formData.responsavelId" required>
                    <option value="">Selecione o responsável</option>
                    <option v-for="resp in responsaveisDisponiveis" :key="resp.id" :value="String(resp.id)">
                      {{ resp.nome }}
                    </option>
                  </select>
                  <small class="text-muted">
                    Se o responsável ainda não está cadastrado, cadastre-o primeiro.
                  </small>
                </div>
              </div>

              <!-- Dados do Professor -->
              <div v-if="formData.role === 'PROFESSOR'">
                <h6 class="mb-3"><i class="bi bi-person-workspace me-2"></i>Dados do Professor</h6>
                
                <div class="mb-3">
                  <label class="form-label">Disciplinas *</label>
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
                  <label class="form-label">Turmas</label>
                  <div class="border rounded p-3 bg-light" style="max-height: 300px; overflow-y: auto;">
                    <div v-if="turmasDisponiveis.length === 0" class="text-center text-muted py-3">
                      <i class="bi bi-inbox" style="font-size: 2rem; opacity: 0.3;"></i>
                      <p class="mt-2 mb-0">Nenhuma turma cadastrada</p>
                    </div>
                    
                    <div v-for="turma in turmasDisponiveis" :key="turma.id" class="form-check mb-2">
                      <input
                        type="checkbox"
                        class="form-check-input"
                        :id="`turma-prof-${turma.id}`"
                        :value="turma.nome"
                        v-model="formData.turmasSelecionadas"
                      />
                      <label class="form-check-label" :for="`turma-prof-${turma.id}`">
                        <strong>{{ turma.nome }}</strong>
                        <small class="text-muted ms-2">
                          {{ turma.serie }} - {{ turma.turno }} - {{ turma.anoLetivo }}
                        </small>
                      </label>
                    </div>
                  </div>
                  <small class="text-muted d-block mt-2" v-if="formData.turmasSelecionadas && formData.turmasSelecionadas.length > 0">
                    <strong>Selecionadas:</strong> {{ formData.turmasSelecionadas.join(', ') }}
                  </small>
                </div>
              </div>

              <!-- Dados do Responsável, Diretoria ou Administrador -->
              <div v-if="['RESPONSAVEL', 'DIRETORIA', 'ADMINISTRADOR'].includes(formData.role)">
                <div class="alert alert-success">
                  <i class="bi bi-check-circle-fill me-2"></i>
                  <strong>Pronto!</strong> Todos os dados necessários foram preenchidos.
                  Clique em "Salvar" para finalizar o cadastro.
                </div>
              </div>
            </div>
          </form>
        </div>

        <div class="modal-footer d-flex justify-content-between">
          <button 
            type="button" 
            class="btn btn-outline-secondary" 
            @click="etapaAnterior"
            v-if="etapaAtual > 1"
          >
            <i class="bi bi-arrow-left me-2"></i>Anterior
          </button>
          
          <button type="button" class="btn btn-secondary" @click="$emit('close')">
            Cancelar
          </button>
          
          <button 
            v-if="etapaAtual < 3" 
            type="button" 
            class="btn btn-primary" 
            @click="proximaEtapa"
            :disabled="!podeAvancar(etapaAtual)"
          >
            Próximo<i class="bi bi-arrow-right ms-2"></i>
          </button>
          
          <button 
            v-if="etapaAtual === 3" 
            type="button" 
            class="btn btn-success" 
            @click="handleSubmit"
          >
            <i class="bi bi-check-circle me-2"></i>Salvar
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import api from '@/services/api'
import turmasService from '@/services/turmasService'
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

const etapaAtual = ref(1)

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
  matricula: '',
  responsavelId: '',
  // Campos específicos para PROFESSOR
  disciplinas: '',
  turmas: '',
  turmasSelecionadas: []
})

const responsaveisDisponiveis = ref([])
const turmasDisponiveis = ref([])

const progressoPercentual = computed(() => {
  return Math.round((etapaAtual.value / 3) * 100)
})

const escolaNomeDoUsuario = computed(() => {
  if (authStore.userRole === 'DIRETORIA' && formData.value.escolaId) {
    const escolaIdNum = Number(formData.value.escolaId)
    const escola = props.escolas.find(e => Number(e.id) === escolaIdNum)
    return escola?.nome || 'Carregando...'
  }
  return ''
})

const podeAvancar = (etapa) => {
  if (etapa === 1) {
    return formData.value.role !== ''
  }
  if (etapa === 2) {
    return formData.value.nome && formData.value.email && formData.value.cpf && formData.value.telefone
  }
  return true
}

const proximaEtapa = () => {
  if (podeAvancar(etapaAtual.value) && etapaAtual.value < 3) {
    etapaAtual.value++
  }
}

const etapaAnterior = () => {
  if (etapaAtual.value > 1) {
    etapaAtual.value--
  }
}

const irParaEtapa = (etapa) => {
  // Só permite ir para etapas anteriores ou se pode avançar
  if (etapa < etapaAtual.value || (etapa === etapaAtual.value + 1 && podeAvancar(etapaAtual.value))) {
    etapaAtual.value = etapa
  }
}

onMounted(async () => {
  console.log('🔄 Modal montado - usuário:', props.usuario)
  console.log('👤 AuthStore user:', authStore.user)
  console.log('👤 AuthStore user (JSON):', JSON.stringify(authStore.user, null, 2))
  console.log('🏫 AuthStore user escolaId:', authStore.user?.escolaId)
  console.log('🏫 AuthStore user escola:', authStore.user?.escola)
  console.log('🔑 AuthStore userRole:', authStore.userRole)
  
  if (props.usuario) {
    formData.value = { ...props.usuario }
    
    console.log('📋 Dados do usuário carregados:', formData.value)
    
    // Garantir que escolaId e responsavelId sejam strings para os selects
    if (formData.value.escolaId) {
      formData.value.escolaId = String(formData.value.escolaId)
    }
    if (formData.value.responsavelId) {
      formData.value.responsavelId = String(formData.value.responsavelId)
    }
    
    console.log('🎯 Turma do usuário:', formData.value.turma)
    
    // Converter arrays para strings para exibição no formulário
    if (formData.value.role === 'PROFESSOR') {
      if (Array.isArray(formData.value.disciplinas)) {
        formData.value.disciplinas = formData.value.disciplinas.join(', ')
      }
      if (Array.isArray(formData.value.turmas)) {
        // turmas vem como array de strings do backend
        formData.value.turmasSelecionadas = [...formData.value.turmas]
      }
    }
  } else if (authStore.userRole === 'DIRETORIA' && authStore.user?.escolaId) {
    // Preencher automaticamente a escola do diretor logado
    formData.value.escolaId = String(authStore.user.escolaId)
    console.log('🏫 Escola do diretor automaticamente vinculada:', formData.value.escolaId)
  }
  
  console.log('✅ formData.escolaId final no onMounted:', formData.value.escolaId)
  
  if (formData.value.role === 'ALUNO') {
    await loadResponsaveis()
    await loadTurmas()
  }
  
  if (formData.value.role === 'PROFESSOR') {
    await loadTurmas()
  }
})

watch(() => formData.value.role, async (newRole) => {
  if (newRole === 'ALUNO') {
    await loadResponsaveis()
    await loadTurmas()
  }
  if (newRole === 'PROFESSOR') {
    await loadTurmas()
  }
})

const loadResponsaveis = async () => {
  try {
    const params = formData.value.escolaId ? { escolaId: formData.value.escolaId } : {}
    const response = await api.get('/usuarios/responsaveis-disponiveis', { params })
    responsaveisDisponiveis.value = response.data
  } catch (error) {
    console.error('Erro ao carregar responsáveis:', error)
  }
}

const loadTurmas = async () => {
  try {
    console.log('🔄 Carregando turmas disponíveis...')
    const response = await turmasService.listarTurmas()
    console.log('📋 Turmas recebidas do backend:', response.data)
    turmasDisponiveis.value = response.data
    // Se for diretor, filtrar apenas turmas da sua escola
    if (authStore.userRole === 'DIRETORIA' && authStore.user?.escolaId) {
      turmasDisponiveis.value = turmasDisponiveis.value.filter(
        turma => turma.escolaId === authStore.user.escolaId
      )
    }
    console.log('✅ Turmas disponíveis após filtro:', turmasDisponiveis.value)
  } catch (error) {
    console.error('❌ Erro ao carregar turmas:', error)
  }
}

const canCreateRole = (role) => {
  if (role === 'ADMINISTRADOR' || role === 'DIRETORIA') {
    return authStore.userRole === 'ADMINISTRADOR'
  }
  return true
}

const validarSenha = (senha) => {
  if (!senha || senha.trim() === '') {
    return {
      valida: false,
      temMinimo: false,
      temMaiuscula: false,
      temMinuscula: false,
      temNumero: false,
      temEspecial: false
    }
  }
  
  const temMinimo = senha.length >= 8
  const temMaiuscula = /[A-Z]/.test(senha)
  const temMinuscula = /[a-z]/.test(senha)
  const temNumero = /[0-9]/.test(senha)
  const temEspecial = /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(senha)
  
  return {
    valida: temMinimo && temMaiuscula && temMinuscula && temNumero && temEspecial,
    temMinimo,
    temMaiuscula,
    temMinuscula,
    temNumero,
    temEspecial
  }
}

const validarSenhaEmTempoReal = () => {
  // Função chamada quando o usuário digita a senha
  // A validação visual já é feita pelo computed no template
}

const aplicarMascaraCPF = (event) => {
  let valor = event.target.value.replace(/\D/g, '') // Remove tudo que não é dígito
  
  if (valor.length <= 11) {
    // Aplica a máscara: 000.000.000-00
    valor = valor.replace(/(\d{3})(\d)/, '$1.$2')
    valor = valor.replace(/(\d{3})(\d)/, '$1.$2')
    valor = valor.replace(/(\d{3})(\d{1,2})$/, '$1-$2')
    formData.value.cpf = valor
  }
}

const validarCPF = (cpf) => {
  if (!cpf) return false
  
  // Remove caracteres não numéricos
  const cpfLimpo = cpf.replace(/\D/g, '')
  
  // Verifica se tem 11 dígitos
  if (cpfLimpo.length !== 11) return false
  
  // Verifica se todos os dígitos são iguais (CPF inválido)
  if (/^(\d)\1{10}$/.test(cpfLimpo)) return false
  
  // Validação do dígito verificador
  let soma = 0
  let resto
  
  for (let i = 1; i <= 9; i++) {
    soma += parseInt(cpfLimpo.substring(i - 1, i)) * (11 - i)
  }
  
  resto = (soma * 10) % 11
  if (resto === 10 || resto === 11) resto = 0
  if (resto !== parseInt(cpfLimpo.substring(9, 10))) return false
  
  soma = 0
  for (let i = 1; i <= 10; i++) {
    soma += parseInt(cpfLimpo.substring(i - 1, i)) * (12 - i)
  }
  
  resto = (soma * 10) % 11
  if (resto === 10 || resto === 11) resto = 0
  if (resto !== parseInt(cpfLimpo.substring(10, 11))) return false
  
  return true
}

const aplicarMascaraTelefone = (event) => {
  let valor = event.target.value.replace(/\D/g, '') // Remove tudo que não é dígito
  
  if (valor.length <= 11) {
    // Aplica a máscara: (00) 00000-0000 ou (00) 0000-0000
    if (valor.length <= 10) {
      // Telefone fixo: (00) 0000-0000
      valor = valor.replace(/(\d{2})(\d)/, '($1) $2')
      valor = valor.replace(/(\d{4})(\d)/, '$1-$2')
    } else {
      // Celular: (00) 00000-0000
      valor = valor.replace(/(\d{2})(\d)/, '($1) $2')
      valor = valor.replace(/(\d{5})(\d)/, '$1-$2')
    }
    formData.value.telefone = valor
  }
}

const validarTelefone = (telefone) => {
  if (!telefone) return false
  
  // Remove caracteres não numéricos
  const telefoneLimpo = telefone.replace(/\D/g, '')
  
  // Telefone deve ter 10 ou 11 dígitos (fixo ou celular)
  return telefoneLimpo.length === 10 || telefoneLimpo.length === 11
}

const validarEmail = (email) => {
  if (!email) return false
  
  // Regex para validar email
  const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return regex.test(email)
}

const validarEmailEmTempoReal = () => {
  // Função chamada quando o usuário sai do campo de email
}

const handleRoleChange = () => {
  console.log('🔄 handleRoleChange chamado - role:', formData.value.role)
  console.log('🔑 authStore.userRole:', authStore.userRole)
  console.log('🏫 authStore.user?.escolaId:', authStore.user?.escolaId)
  
  // Limpar campos específicos quando mudar o tipo de usuário
  formData.value = {
    ...formData.value,
    dataNascimento: '',
    turma: '',
    matricula: '',
    responsavelId: '',
    disciplinas: '',
    turmas: '',
    turmasSelecionadas: []
  }
  
  // Limpar escola se for administrador
  if (formData.value.role === 'ADMINISTRADOR') {
    formData.value.escolaId = ''
    console.log('❌ Escola limpa (ADMINISTRADOR)')
  }
  // Se for diretor criando qualquer tipo de usuário, vincular automaticamente à sua escola
  else if (authStore.userRole === 'DIRETORIA' && authStore.user?.escolaId) {
    formData.value.escolaId = String(authStore.user.escolaId)
    console.log('✅ Escola vinculada automaticamente:', formData.value.escolaId)
  }
  
  console.log('📋 formData.escolaId após handleRoleChange:', formData.value.escolaId)
}

const handleSubmit = () => {
  // Criar uma cópia dos dados
  const dadosParaEnviar = { ...formData.value }
  
  console.log('📤 Dados antes da conversão:', dadosParaEnviar)
  console.log('🔍 DEBUG - escolaId em dadosParaEnviar:', dadosParaEnviar.escolaId)
  console.log('🔍 DEBUG - authStore.user:', authStore.user)
  console.log('🔍 DEBUG - authStore.userRole:', authStore.userRole)
  
  // Converter IDs de string para número (apenas se não forem vazios)
  if (dadosParaEnviar.escolaId && dadosParaEnviar.escolaId !== '' && dadosParaEnviar.escolaId !== '0') {
    dadosParaEnviar.escolaId = Number(dadosParaEnviar.escolaId)
  } else if (dadosParaEnviar.escolaId === '' || dadosParaEnviar.escolaId === '0') {
    dadosParaEnviar.escolaId = null
  }
  
  if (dadosParaEnviar.responsavelId && dadosParaEnviar.responsavelId !== '' && dadosParaEnviar.responsavelId !== '0') {
    dadosParaEnviar.responsavelId = Number(dadosParaEnviar.responsavelId)
  } else if (dadosParaEnviar.responsavelId === '' || dadosParaEnviar.responsavelId === '0') {
    dadosParaEnviar.responsavelId = null
  }
  
  console.log('✅ Turma sendo enviada:', dadosParaEnviar.turma)
  
  // Se estiver editando e a senha estiver vazia, remover do objeto
  if (props.mode === 'edit' && (!dadosParaEnviar.senha || dadosParaEnviar.senha.trim() === '')) {
    delete dadosParaEnviar.senha
  }
  
  // Para ALUNO: remover campos de professor
  if (dadosParaEnviar.role === 'ALUNO') {
    delete dadosParaEnviar.disciplinas
    delete dadosParaEnviar.turmas
  }
  
  // Limpar campos vazios para evitar erro 400
  Object.keys(dadosParaEnviar).forEach(key => {
    if (dadosParaEnviar[key] === '' || dadosParaEnviar[key] === null) {
      delete dadosParaEnviar[key]
    }
  })
  
  // Para PROFESSOR: remover campos de aluno
  if (dadosParaEnviar.role === 'PROFESSOR') {
    delete dadosParaEnviar.dataNascimento
    delete dadosParaEnviar.turma
    delete dadosParaEnviar.matricula
    delete dadosParaEnviar.responsavelId
    
    // Converter disciplinas de string para array
    if (dadosParaEnviar.disciplinas && typeof dadosParaEnviar.disciplinas === 'string') {
      dadosParaEnviar.disciplinas = dadosParaEnviar.disciplinas
        .split(',')
        .map(d => d.trim())
        .filter(d => d.length > 0)
    }
    
    // Usar turmasSelecionadas (já é um array)
    if (dadosParaEnviar.turmasSelecionadas && Array.isArray(dadosParaEnviar.turmasSelecionadas)) {
      dadosParaEnviar.turmas = dadosParaEnviar.turmasSelecionadas
    } else {
      dadosParaEnviar.turmas = []
    }
    delete dadosParaEnviar.turmasSelecionadas
  }
  
  // Para RESPONSAVEL, DIRETORIA, ADMINISTRADOR: remover campos específicos
  if (['RESPONSAVEL', 'DIRETORIA', 'ADMINISTRADOR'].includes(dadosParaEnviar.role)) {
    delete dadosParaEnviar.dataNascimento
    delete dadosParaEnviar.turma
    delete dadosParaEnviar.matricula
    delete dadosParaEnviar.responsavelId
    delete dadosParaEnviar.disciplinas
    delete dadosParaEnviar.turmas
  }
  
  // Validar campos obrigatórios
  if (!dadosParaEnviar.nome || !dadosParaEnviar.email || !dadosParaEnviar.cpf || 
      !dadosParaEnviar.telefone || !dadosParaEnviar.role) {
    alert('Por favor, preencha todos os campos obrigatórios')
    return
  }
  
  // Validar formato do email
  if (!validarEmail(dadosParaEnviar.email)) {
    alert('Por favor, digite um email válido')
    return
  }
  
  // Validar formato do CPF
  if (!validarCPF(dadosParaEnviar.cpf)) {
    alert('Por favor, digite um CPF válido')
    return
  }
  
  // Validar formato do telefone
  if (!validarTelefone(dadosParaEnviar.telefone)) {
    alert('Por favor, digite um telefone válido')
    return
  }
  
  // Validar senha na criação
  if (props.mode === 'create' && (!dadosParaEnviar.senha || dadosParaEnviar.senha.trim() === '')) {
    alert('Senha é obrigatória')
    return
  }
  
  // Validar senha se fornecida (criação ou edição)
  if (dadosParaEnviar.senha && dadosParaEnviar.senha.trim() !== '') {
    const validacao = validarSenha(dadosParaEnviar.senha)
    if (!validacao.valida) {
      alert('A senha não atende aos critérios de segurança:\n' +
            '- Mínimo 8 caracteres\n' +
            '- 1 letra maiúscula\n' +
            '- 1 letra minúscula\n' +
            '- 1 número\n' +
            '- 1 caractere especial (!@#$%^&*)')
      return
    }
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
  
  // Validar turma para aluno
  if (dadosParaEnviar.role === 'ALUNO' && !dadosParaEnviar.turma) {
    alert('Turma é obrigatória para alunos')
    return
  }
  
  console.log('📤 Enviando dados:', dadosParaEnviar)
  emit('save', dadosParaEnviar)
}
</script>

<style scoped>
.nav-link {
  cursor: pointer;
  transition: all 0.3s ease;
}

.nav-link:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.progress-bar {
  transition: width 0.4s ease;
}

.form-check-label {
  cursor: pointer;
  user-select: none;
}

.modal-dialog {
  max-width: 700px;
}
</style>
