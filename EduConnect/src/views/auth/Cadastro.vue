<template>
  <div class="auth-container">
    <div class="container">
      <div class="row justify-content-center">
        <div class="col-md-8 col-lg-6">
          <div class="card shadow-lg border-0">
            <div class="card-body p-5">
              <div class="text-center mb-4">
                <i class="bi bi-mortarboard-fill text-primary" style="font-size: 3rem;"></i>
                <h2 class="mt-3 fw-bold text-primary">Criar Conta</h2>
                <p class="text-muted">Preencha os dados para se cadastrar</p>
              </div>
              
              <form @submit.prevent="handleRegister">
                <div class="row">
                  <div class="col-md-6 mb-3">
                    <label for="nome" class="form-label">Nome Completo</label>
                    <input
                      type="text"
                      class="form-control"
                      id="nome"
                      v-model="form.nome"
                      placeholder="Seu nome completo"
                      required
                    />
                  </div>
                  
                  <div class="col-md-6 mb-3">
                    <label for="email" class="form-label">E-mail</label>
                    <input
                      type="email"
                      class="form-control"
                      id="email"
                      v-model="form.email"
                      placeholder="seu@email.com"
                      required
                    />
                  </div>
                </div>
                
                <div class="row">
                  <div class="col-md-6 mb-3">
                    <label for="cpf" class="form-label">CPF</label>
                    <input
                      type="text"
                      class="form-control"
                      id="cpf"
                      v-model="form.cpf"
                      placeholder="000.000.000-00"
                      v-maska="'###.###.###-##'"
                      required
                    />
                  </div>
                  
                  <div class="col-md-6 mb-3">
                    <label for="telefone" class="form-label">Telefone</label>
                    <input
                      type="text"
                      class="form-control"
                      id="telefone"
                      v-model="form.telefone"
                      placeholder="(00) 00000-0000"
                      required
                    />
                  </div>
                </div>
                
                <div class="mb-3">
                  <label for="role" class="form-label">Tipo de Perfil</label>
                  <select class="form-select" id="role" v-model="form.role" required>
                    <option value="">Selecione...</option>
                    <option value="ALUNO">Aluno</option>
                    <option value="RESPONSAVEL">Responsável</option>
                    <option value="PROFESSOR">Professor</option>
                  </select>
                </div>
                
                <div class="row">
                  <div class="col-md-6 mb-3">
                    <label for="password" class="form-label">Senha</label>
                    <input
                      :type="showPassword ? 'text' : 'password'"
                      class="form-control"
                      id="password"
                      v-model="form.password"
                      placeholder="Mínimo 6 caracteres"
                      required
                    />
                  </div>
                  
                  <div class="col-md-6 mb-3">
                    <label for="confirmPassword" class="form-label">Confirmar Senha</label>
                    <input
                      :type="showPassword ? 'text' : 'password'"
                      class="form-control"
                      id="confirmPassword"
                      v-model="form.confirmPassword"
                      placeholder="Repita a senha"
                      required
                    />
                  </div>
                </div>
                
                <div class="mb-3 form-check">
                  <input
                    type="checkbox"
                    class="form-check-input"
                    id="showPassword"
                    v-model="showPassword"
                  />
                  <label class="form-check-label" for="showPassword">
                    Mostrar senha
                  </label>
                </div>
                
                <div class="mb-3 form-check">
                  <input
                    type="checkbox"
                    class="form-check-input"
                    id="terms"
                    v-model="form.acceptTerms"
                    required
                  />
                  <label class="form-check-label" for="terms">
                    Aceito os <a href="#">termos de uso</a> e <a href="#">política de privacidade</a>
                  </label>
                </div>
                
                <button 
                  type="submit" 
                  class="btn btn-primary w-100 py-2"
                  :disabled="loading"
                >
                  <span v-if="loading">
                    <span class="spinner-border spinner-border-sm me-2"></span>
                    Cadastrando...
                  </span>
                  <span v-else>
                    <i class="bi bi-person-plus me-2"></i>
                    Criar Conta
                  </span>
                </button>
              </form>
              
              <hr class="my-4">
              
              <div class="text-center">
                <p class="mb-2 text-muted small">Já tem uma conta?</p>
                <router-link to="/login" class="btn btn-outline-primary w-100">
                  Fazer Login
                </router-link>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import { useNotificationStore } from '../../stores/notifications'

const router = useRouter()
const authStore = useAuthStore()
const notificationStore = useNotificationStore()

const form = ref({
  nome: '',
  email: '',
  cpf: '',
  telefone: '',
  role: '',
  password: '',
  confirmPassword: '',
  acceptTerms: false
})

const loading = ref(false)
const showPassword = ref(false)

const handleRegister = async () => {
  if (form.value.password !== form.value.confirmPassword) {
    notificationStore.error('As senhas não coincidem')
    return
  }
  
  if (form.value.password.length < 6) {
    notificationStore.error('A senha deve ter no mínimo 6 caracteres')
    return
  }
  
  loading.value = true
  
  try {
    // Preparar dados para envio ao backend
    const userData = {
      nome: form.value.nome,
      email: form.value.email,
      password: form.value.password,
      cpf: form.value.cpf,
      telefone: form.value.telefone,
      role: form.value.role
    }
    
    // Chamar API real do backend
    const result = await authStore.register(userData)
    
    if (result.success) {
      notificationStore.success('Cadastro realizado com sucesso!')
      router.push('/login')
    } else {
      notificationStore.error(result.message || 'Erro ao criar conta')
    }
  } catch (error) {
    console.error('Erro no cadastro:', error)
    
    // Tratamento específico de erros
    let errorMessage = 'Erro ao criar conta. Tente novamente.'
    
    if (error.response?.data?.message) {
      const backendMessage = error.response.data.message
      
      // Verificar se é erro de CPF duplicado
      if (backendMessage.includes('Duplicate entry') && backendMessage.includes('cpf')) {
        errorMessage = 'CPF já cadastrado. Use outro CPF ou faça login.'
      } 
      // Verificar se é erro de email duplicado
      else if (backendMessage.includes('E-mail já cadastrado') || 
               (backendMessage.includes('Duplicate entry') && backendMessage.includes('email'))) {
        errorMessage = 'E-mail já cadastrado. Use outro e-mail ou faça login.'
      } 
      // Outras mensagens do backend
      else {
        errorMessage = backendMessage
      }
    }
    
    notificationStore.error(errorMessage)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.card {
  border-radius: 15px;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}

.btn-primary:hover {
  background: linear-gradient(135deg, #5568d3 0%, #6a3f8f 100%);
}

.btn-outline-primary {
  color: #667eea;
  border-color: #667eea;
}

.btn-outline-primary:hover {
  background-color: #667eea;
  border-color: #667eea;
  color: white;
}
</style>

