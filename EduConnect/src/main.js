import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'

// Bootstrap CSS
import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap-icons/font/bootstrap-icons.css'

// Bootstrap JS
import 'bootstrap/dist/js/bootstrap.bundle.min.js'

// Estilos globais
import './style.css'

// Criar aplicação
const app = createApp(App)

// Plugins
app.use(createPinia())
app.use(router)

// Inicializar autenticação
import { useAuthStore } from './stores/auth'
const authStore = useAuthStore()
authStore.initializeAuth()

// Montar aplicação
app.mount('#app')
