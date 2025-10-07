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
                    v-model="perfil.email"
                    required
                  />
                </div>
              </div>
              <div class="row">
                <div class="col-md-6 mb-3">
                  <label class="form-label">Telefone</label>
                  <input 
                    type="text" 
                    class="form-control" 
                    v-model="perfil.telefone"
                    placeholder="(00) 00000-0000"
                  />
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
                    v-model="perfil.novaSenha"
                    placeholder="Deixe em branco para não alterar"
                    minlength="6"
                  />
                  <small class="text-muted">Mínimo 6 caracteres</small>
                </div>
                <div class="col-md-6 mb-3">
                  <label class="form-label">Confirmar Nova Senha</label>
                  <input 
                    type="password" 
                    class="form-control" 
                    v-model="perfil.confirmarSenha"
                    placeholder="Confirme a nova senha"
                  />
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

const salvarPerfil = async () => {
  // Validar senhas se estiver alterando
  if (perfil.value.novaSenha) {
    if (perfil.value.novaSenha.length < 6) {
      notificationStore.error('A senha deve ter no mínimo 6 caracteres')
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

