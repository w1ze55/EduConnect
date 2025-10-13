<template>
  <div class="auth-container">
    <div class="container">
      <div class="row justify-content-center">
        <div class="col-md-6 col-lg-5">
          <div class="card shadow-lg border-0">
            <div class="card-body p-5">
              <div class="text-center mb-4">
                <i class="bi bi-mortarboard-fill text-primary" style="font-size: 3rem;"></i>
                <h2 class="mt-3 fw-bold text-primary">EduConnect</h2>
                <p class="text-muted">Faça login para continuar</p>
              </div>
              
              <form @submit.prevent="handleLogin">
                <div class="mb-3">
                  <label for="email" class="form-label">E-mail</label>
                  <div class="input-group">
                    <span class="input-group-text">
                      <i class="bi bi-envelope"></i>
                    </span>
                    <input
                      type="email"
                      class="form-control"
                      id="email"
                      v-model="form.email"
                      placeholder="seu@email.com"
                      required
                    />
                  </div>
                  <div v-if="errors.email" class="text-danger small mt-1">
                    {{ errors.email }}
                  </div>
                </div>
                
                <div class="mb-3">
                  <label for="password" class="form-label">Senha</label>
                  <div class="input-group">
                    <span class="input-group-text">
                      <i class="bi bi-lock"></i>
                    </span>
                    <input
                      :type="showPassword ? 'text' : 'password'"
                      class="form-control"
                      id="password"
                      v-model="form.password"
                      placeholder="Digite sua senha"
                      required
                    />
                    <button 
                      class="btn btn-outline-secondary" 
                      type="button"
                      @click="showPassword = !showPassword"
                    >
                      <i :class="showPassword ? 'bi bi-eye-slash' : 'bi bi-eye'"></i>
                    </button>
                  </div>
                  <div v-if="errors.password" class="text-danger small mt-1">
                    {{ errors.password }}
                  </div>
                </div>
                
                <div class="mb-3 form-check">
                  <input
                    type="checkbox"
                    class="form-check-input"
                    id="rememberMe"
                    v-model="form.rememberMe"
                  />
                  <label class="form-check-label" for="rememberMe">
                    Lembrar-me
                  </label>
                </div>
                
                <button 
                  type="submit" 
                  class="btn btn-primary w-100 py-2"
                  :disabled="loading"
                >
                  <span v-if="loading">
                    <span class="spinner-border spinner-border-sm me-2"></span>
                    Entrando...
                  </span>
                  <span v-else>
                    <i class="bi bi-box-arrow-in-right me-2"></i>
                    Entrar
                  </span>
                </button>
              </form>
              
              <div class="text-center mt-4">
                <a href="#" class="text-decoration-none small">
                  Esqueceu sua senha?
                </a>
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
  email: '',
  password: '',
  rememberMe: false
})

const errors = ref({})
const loading = ref(false)
const showPassword = ref(false)

const validateForm = () => {
  errors.value = {}
  
  if (!form.value.email) {
    errors.value.email = 'E-mail é obrigatório'
  } else if (!/\S+@\S+\.\S+/.test(form.value.email)) {
    errors.value.email = 'E-mail inválido'
  }
  
  if (!form.value.password) {
    errors.value.password = 'Senha é obrigatória'
  } else if (form.value.password.length < 6) {
    errors.value.password = 'Senha deve ter no mínimo 6 caracteres'
  }
  
  return Object.keys(errors.value).length === 0
}

const handleLogin = async () => {
  if (!validateForm()) return
  
  loading.value = true
  
  try {
    console.log('📝 [LOGIN] Fazendo login...')
    const result = await authStore.login({
      email: form.value.email,
      password: form.value.password
    })
    
    if (result.success) {
      console.log('✅ [LOGIN] Login bem-sucedido!')
      notificationStore.success(`Bem-vindo(a), ${authStore.userName}!`)
      router.push('/dashboard')
    } else {
      console.error('❌ [LOGIN] Login falhou:', result.message)
      notificationStore.error(result.message || 'E-mail ou senha inválidos')
    }
  } catch (error) {
    console.error('❌ [LOGIN] Erro no login:', error)
    notificationStore.error('Erro ao fazer login. Tente novamente.')
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

.input-group-text {
  background-color: #f8f9fa;
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

.credential-box:hover {
  background-color: #f8f9fa;
  border-color: #667eea;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(102, 126, 234, 0.2);
}

.credential-box strong {
  display: block;
  margin-bottom: 0.25rem;
  color: #667eea;
}

.credential-box .text-muted {
  font-size: 0.75rem;
  line-height: 1.3;
}
</style>

