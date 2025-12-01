<template>
  <div class="container-fluid py-4">
    <h2 class="fw-bold mb-4">
      <i class="bi bi-person-circle me-2"></i>Meu Perfil
    </h2>
    
    <div class="row">
      <div class="col-lg-8">
        <div class="card shadow-sm">
          <div class="card-body">
            <div v-if="!authStore.user" class="text-center py-5">
              <div class="spinner-border text-primary" role="status">
                <span class="visually-hidden">Carregando...</span>
              </div>
              <p class="mt-3 text-muted">Carregando dados do perfil...</p>
            </div>
            
            <form v-else @submit.prevent="salvarPerfil">
              <div class="row">
                <div class="col-md-6 mb-3">
                  <label class="form-label">Nome Completo</label>
                  <input 
                    type="text" 
                    class="form-control" 
                    v-model="perfil.nome"
                    required
                  />
                </div>
                <div class="col-md-6 mb-3">
                  <label class="form-label">E-mail</label>
                  <input 
                    type="email" 
                    class="form-control"
                    :class="{ 'is-invalid': perfil.email && !validarEmail(perfil.email) }"
                    v-model="perfil.email"
                    required
                    placeholder="exemplo@email.com"
                    @blur="validarEmailEmTempoReal"
                  />
                  <div v-if="perfil.email && !validarEmail(perfil.email)" class="invalid-feedback d-block">
                    <small class="text-danger">Digite um email válido (ex: exemplo@email.com)</small>
                  </div>
                </div>
              </div>
              <div class="row">
                <div class="col-md-6 mb-3">
                  <label class="form-label">Telefone</label>
                  <input 
                    type="text" 
                    class="form-control"
                    :class="{ 'is-invalid': perfil.telefone && !validarTelefone(perfil.telefone) }"
                    v-model="perfil.telefone"
                    placeholder="(00) 00000-0000"
                    maxlength="15"
                    @input="aplicarMascaraTelefone"
                  />
                  <div v-if="perfil.telefone && !validarTelefone(perfil.telefone)" class="invalid-feedback d-block">
                    <small class="text-danger">Digite um telefone válido (00) 00000-0000 ou (00) 0000-0000</small>
                  </div>
                </div>
                <div class="col-md-6 mb-3">
                  <label class="form-label">CPF</label>
                  <input type="text" class="form-control" v-model="perfil.cpf" disabled />
                </div>
              </div>
              <div class="row">
                <div class="col-md-6 mb-3">
                  <label class="form-label">Perfil</label>
                  <input type="text" class="form-control" :value="roleLabel" disabled />
                </div>
                <div class="col-md-6 mb-3" v-if="perfil.turma">
                  <label class="form-label">Turma</label>
                  <input type="text" class="form-control" v-model="perfil.turma" disabled />
                </div>
                <div class="col-md-6 mb-3" v-if="perfil.matricula">
                  <label class="form-label">Matrícula</label>
                  <input type="text" class="form-control" v-model="perfil.matricula" disabled />
                </div>
              </div>
              
              <hr class="my-4">
              
              <h5 class="mb-3">Alterar Senha</h5>
              <div class="row">
                <div class="col-md-6 mb-3">
                  <label class="form-label">Nova Senha</label>
                  <input 
                    type="password" 
                    class="form-control"
                    :class="{ 'is-invalid': perfil.novaSenha && !validarSenha(perfil.novaSenha).valida }"
                    v-model="perfil.novaSenha"
                    placeholder="Deixe em branco para não alterar"
                    @input="validarSenhaEmTempoReal"
                  />
                  <div v-if="perfil.novaSenha && !validarSenha(perfil.novaSenha).valida" class="invalid-feedback d-block">
                    <small class="text-danger">A senha deve atender aos seguintes critérios:</small>
                    <ul class="mb-0 mt-1 small">
                      <li :class="{ 'text-success': validarSenha(perfil.novaSenha).temMinimo }">Mínimo 8 caracteres</li>
                      <li :class="{ 'text-success': validarSenha(perfil.novaSenha).temMaiuscula }">1 letra maiúscula</li>
                      <li :class="{ 'text-success': validarSenha(perfil.novaSenha).temMinuscula }">1 letra minúscula</li>
                      <li :class="{ 'text-success': validarSenha(perfil.novaSenha).temNumero }">1 número</li>
                      <li :class="{ 'text-success': validarSenha(perfil.novaSenha).temEspecial }">1 caractere especial (!@#$%^&*)</li>
                    </ul>
                  </div>
                  <small v-else-if="perfil.novaSenha && validarSenha(perfil.novaSenha).valida" class="text-success">
                    <i class="bi bi-check-circle-fill me-1"></i>Senha válida
                  </small>
                </div>
                <div class="col-md-6 mb-3">
                  <label class="form-label">Confirmar Nova Senha</label>
                  <input 
                    type="password" 
                    class="form-control"
                    :class="{ 'is-invalid': perfil.confirmarSenha && perfil.novaSenha !== perfil.confirmarSenha }"
                    v-model="perfil.confirmarSenha"
                    placeholder="Confirme a nova senha"
                  />
                  <div v-if="perfil.confirmarSenha && perfil.novaSenha !== perfil.confirmarSenha" class="invalid-feedback d-block">
                    <small class="text-danger">As senhas não coincidem</small>
                  </div>
                  <small v-else-if="perfil.confirmarSenha && perfil.novaSenha === perfil.confirmarSenha" class="text-success">
                    <i class="bi bi-check-circle-fill me-1"></i>Senhas coincidem
                  </small>
                </div>
              </div>
              
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
                  <i class="bi bi-check2 me-2"></i>Salvar Alterações
                </span>
              </button>
            </form>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useAuthStore } from '../stores/auth'
import { useNotificationStore } from '../stores/notifications'
import api from '../services/api'

const authStore = useAuthStore()
const notificationStore = useNotificationStore()

const salvando = ref(false)
const carregando = ref(false)

const perfil = ref({
  nome: '',
  email: '',
  telefone: '',
  cpf: '',
  role: '',
  turma: '',
  matricula: '',
  novaSenha: '',
  confirmarSenha: ''
})

// Função para carregar dados do perfil
const carregarPerfil = () => {
  if (authStore.user) {
    perfil.value.nome = authStore.user.nome || ''
    perfil.value.email = authStore.user.email || ''
    perfil.value.telefone = authStore.user.telefone || ''
    perfil.value.cpf = authStore.user.cpf || ''
    perfil.value.role = authStore.user.role || ''
    perfil.value.turma = authStore.user.turma || ''
    perfil.value.matricula = authStore.user.matricula || ''
  }
}

// Carregar dados quando o componente montar
onMounted(async () => {
  // Se não há usuário no store, tentar buscar do backend
  if (!authStore.user && authStore.token) {
    carregando.value = true
    try {
      await authStore.fetchCurrentUser()
    } catch (error) {
      console.error('Erro ao carregar usuário:', error)
    } finally {
      carregando.value = false
    }
  }
  
  carregarPerfil()
})

// Observar mudanças no usuário (quando o store atualizar)
watch(() => authStore.user, (newUser) => {
  if (newUser) {
    carregarPerfil()
  }
}, { deep: true })

// Tradução do role para exibição
const roleLabel = computed(() => {
  const roles = {
    'ALUNO': 'Aluno',
    'PROFESSOR': 'Professor',
    'RESPONSAVEL': 'Responsável',
    'DIRETORIA': 'Diretoria',
    'ADMINISTRADOR': 'Administrador'
  }
  return roles[perfil.value.role] || perfil.value.role
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
    perfil.value.telefone = valor
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

const salvarPerfil = async () => {
  // Validar formato do email
  if (!validarEmail(perfil.value.email)) {
    notificationStore.error('Por favor, digite um email válido')
    return
  }
  
  // Validar formato do telefone
  if (perfil.value.telefone && !validarTelefone(perfil.value.telefone)) {
    notificationStore.error('Por favor, digite um telefone válido')
    return
  }
  
  // Validar senhas se estiver alterando
  if (perfil.value.novaSenha) {
    const validacao = validarSenha(perfil.value.novaSenha)
    if (!validacao.valida) {
      notificationStore.error('A senha não atende aos critérios de segurança:\n' +
            '- Mínimo 8 caracteres\n' +
            '- 1 letra maiúscula\n' +
            '- 1 letra minúscula\n' +
            '- 1 número\n' +
            '- 1 caractere especial (!@#$%^&*)')
      return
    }
    
    if (perfil.value.novaSenha !== perfil.value.confirmarSenha) {
      notificationStore.error('As senhas não coincidem')
      return
    }
  }
  
  salvando.value = true
  
  try {
    const dados = {
      nome: perfil.value.nome,
      email: perfil.value.email,
      telefone: perfil.value.telefone
    }
    
    // Adicionar senha se estiver alterando
    if (perfil.value.novaSenha) {
      dados.password = perfil.value.novaSenha
    }
    
    const response = await api.put('/usuarios/perfil', dados)
    
    // Atualizar dados no store
    authStore.user = response.data
    
    // Limpar campos de senha
    perfil.value.novaSenha = ''
    perfil.value.confirmarSenha = ''
    
    notificationStore.success('Perfil atualizado com sucesso!')
  } catch (error) {
    console.error('Erro ao atualizar perfil:', error)
    notificationStore.error(error.response?.data?.message || 'Erro ao atualizar perfil')
  } finally {
    salvando.value = false
  }
}
</script>

