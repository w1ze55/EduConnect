<template>
  <div class="modal fade show" style="display: block; background: rgba(0,0,0,0.5)">
    <div class="modal-dialog">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">Atribuir Diretor Principal</h5>
          <button type="button" class="btn-close" @click="$emit('close')"></button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="handleSubmit">
            <div class="mb-3">
              <p class="fw-bold mb-2">Escola: {{ escola.nome }}</p>
            </div>
            
            <div v-if="!criarNovo" class="mb-3">
              <label class="form-label">Selecionar Diretor Existente</label>
              <select class="form-select" v-model.number="selectedDiretorId">
                <option value="">Selecione um diretor</option>
                <option v-for="diretor in diretoresDisponiveis" :key="diretor.id" :value="diretor.id">
                  {{ diretor.nome }} ({{ diretor.email }})
                </option>
              </select>
              <div class="mt-2">
                <button type="button" class="btn btn-link ps-0" @click="criarNovo = true">
                  Criar novo diretor
                </button>
              </div>
            </div>

            <div v-else>
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
              <div class="mb-3">
                <label class="form-label">CPF</label>
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
              <div class="mb-3">
                <label class="form-label">Telefone</label>
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
              <div class="mb-3">
                <label class="form-label">Senha</label>
                <input
                  type="password"
                  class="form-control"
                  :class="{ 'is-invalid': formData.senha && !validarSenha(formData.senha).valida }"
                  v-model="formData.senha"
                  required
                  placeholder="Digite a senha"
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
                <button type="button" class="btn btn-link ps-0" @click="criarNovo = false">
                  Voltar para seleção de diretor existente
                </button>
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
import { ref, onMounted } from 'vue'
import api from '@/services/api'

const props = defineProps({
  escola: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['close', 'save'])

const criarNovo = ref(false)
const selectedDiretorId = ref('')
const diretoresDisponiveis = ref([])

const formData = ref({
  nome: '',
  email: '',
  cpf: '',
  telefone: '',
  senha: ''
})

onMounted(async () => {
  try {
    const response = await api.get('/usuarios/diretores-disponiveis')
    diretoresDisponiveis.value = response.data
    console.log('Diretores disponíveis carregados:', diretoresDisponiveis.value)
  } catch (error) {
    console.error('Erro ao carregar diretores:', error)
    console.error('Detalhes do erro:', error.response)
  }
})

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

const handleSubmit = () => {
  if (criarNovo.value) {
    // Validar campos do formulário
    if (!formData.value.nome || !formData.value.email || !formData.value.cpf || 
        !formData.value.telefone || !formData.value.senha) {
      alert('Por favor, preencha todos os campos')
      return
    }
    
    // Validar formato do email
    if (!validarEmail(formData.value.email)) {
      alert('Por favor, digite um email válido')
      return
    }
    
    // Validar formato do CPF
    if (!validarCPF(formData.value.cpf)) {
      alert('Por favor, digite um CPF válido')
      return
    }
    
    // Validar formato do telefone
    if (!validarTelefone(formData.value.telefone)) {
      alert('Por favor, digite um telefone válido')
      return
    }
    
    // Validar senha
    const validacao = validarSenha(formData.value.senha)
    if (!validacao.valida) {
      alert('A senha não atende aos critérios de segurança:\n' +
            '- Mínimo 8 caracteres\n' +
            '- 1 letra maiúscula\n' +
            '- 1 letra minúscula\n' +
            '- 1 número\n' +
            '- 1 caractere especial (!@#$%^&*)')
      return
    }
    
    emit('save', {
      tipo: 'novo',
      dados: {
        ...formData.value,
        escolaId: props.escola.id
      }
    })
  } else {
    // Validar se um diretor foi selecionado
    if (!selectedDiretorId.value) {
      alert('Por favor, selecione um diretor')
      return
    }
    
    emit('save', {
      tipo: 'existente',
      dados: {
        diretorId: selectedDiretorId.value,
        escolaId: props.escola.id
      }
    })
  }
}
</script>
