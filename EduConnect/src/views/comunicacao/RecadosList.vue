<template>
  <div class="container-fluid py-4">
    <div class="row mb-4">
      <div class="col-md-6">
        <h2 class="fw-bold">
          <i class="bi bi-envelope-fill me-2"></i>Recados
        </h2>
        <p class="text-muted">Confira os recados enviados pela escola</p>
      </div>
      <div class="col-md-6 text-md-end">
        <router-link 
          v-if="canSendRecados" 
          to="/recados/enviar" 
          class="btn btn-primary"
        >
          <i class="bi bi-plus-circle me-2"></i>Enviar Recado
        </router-link>
      </div>
    </div>
    
    <!-- Filtros -->
    <div class="card shadow-sm mb-4">
      <div class="card-body">
        <div class="row g-3">
          <div class="col-md-4">
            <input 
              type="text" 
              class="form-control" 
              placeholder="Buscar recados..."
              v-model="filters.search"
            />
          </div>
          <div class="col-md-3">
            <select class="form-select" v-model="filters.status">
              <option value="">Todos</option>
              <option value="lido">Lidos</option>
              <option value="nao_lido">Não Lidos</option>
            </select>
          </div>
          <div class="col-md-3">
            <select class="form-select" v-model="filters.categoria">
              <option value="">Todas as categorias</option>
              <option value="geral">Geral</option>
              <option value="academico">Acadêmico</option>
              <option value="financeiro">Financeiro</option>
              <option value="evento">Evento</option>
            </select>
          </div>
          <div class="col-md-2">
            <button class="btn btn-outline-secondary w-100" @click="limparFiltros">
              <i class="bi bi-x-circle me-1"></i>Limpar
            </button>
          </div>
        </div>
      </div>
    </div>
    
    <!-- Lista de Recados -->
    <div v-if="loading" class="text-center py-5">
      <loading-spinner message="Carregando recados..." />
    </div>
    
    <div v-else-if="recadosFiltrados.length === 0" class="text-center py-5">
      <i class="bi bi-inbox fs-1 text-muted"></i>
      <p class="mt-3 text-muted">Nenhum recado encontrado</p>
    </div>
    
    <div v-else class="row g-4">
      <div 
        v-for="recado in recadosFiltrados" 
        :key="recado.id"
        class="col-12"
      >
        <div 
          class="card shadow-sm recado-card"
          :class="{ 'recado-nao-lido': !recado.lido }"
          @click="verRecado(recado.id)"
        >
          <div class="card-body">
            <div class="row align-items-start">
              <div class="col-auto">
                <div class="recado-icon" :class="`bg-${getCategoriaColor(recado.categoria)}`">
                  <i :class="getCategoriaIcon(recado.categoria)"></i>
                </div>
              </div>
              <div class="col">
                <div class="d-flex justify-content-between align-items-start mb-2">
                  <div>
                    <h5 class="mb-1">
                      {{ recado.titulo }}
                      <span v-if="!recado.lido" class="badge bg-primary ms-2">Novo</span>
                    </h5>
                    <small class="text-muted">
                      <i class="bi bi-person me-1"></i>{{ recado.remetente }}
                      <span class="mx-2">•</span>
                      <i class="bi bi-calendar me-1"></i>{{ recado.dataEnvio }}
                    </small>
                  </div>
                  <span :class="`badge bg-${getCategoriaColor(recado.categoria)}`">
                    {{ recado.categoria }}
                  </span>
                </div>
                <p class="mb-2 text-muted">{{ recado.preview }}</p>
                <div class="d-flex gap-2">
                  <span v-if="recado.anexos > 0" class="badge bg-light text-dark">
                    <i class="bi bi-paperclip me-1"></i>{{ recado.anexos }} anexo(s)
                  </span>
                  <span v-if="recado.importante" class="badge bg-danger">
                    <i class="bi bi-exclamation-triangle me-1"></i>Importante
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import { useNotificationStore } from '../../stores/notifications'
import recadosService from '../../services/recadosService'
import LoadingSpinner from '../../components/common/LoadingSpinner.vue'

const router = useRouter()
const authStore = useAuthStore()
const notificationStore = useNotificationStore()

const loading = ref(false)
const recados = ref([])
const role = computed(() => authStore.userRole || authStore.user?.role || '')

const filters = ref({
  search: '',
  status: '',
  categoria: ''
})

const canSendRecados = computed(() => {
  return role.value === 'PROFESSOR' || role.value === 'DIRETORIA' || role.value === 'ADMINISTRADOR'
})

onMounted(async () => {
  loading.value = true
  try {
    const response = await recadosService.getRecados()
    recados.value = response.data.map(recado => ({
      id: recado.id,
      titulo: recado.titulo,
      remetente: recado.remetente,
      dataEnvio: formatarData(recado.dataEnvio),
      categoria: recado.categoria.toLowerCase(),
      preview: recado.conteudo.substring(0, 100) + '...',
      lido: recado.lido,
      anexos: recado.anexos ? recado.anexos.length : 0,
      importante: recado.importante
    }))
  } catch (error) {
    console.error('Erro ao carregar recados:', error)
    notificationStore.error('Erro ao carregar recados')
  } finally {
    loading.value = false
  }
})

const formatarData = (dataISO) => {
  const data = new Date(dataISO)
  return data.toLocaleDateString('pt-BR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric'
  })
}

const recadosFiltrados = computed(() => {
  return recados.value.filter(recado => {
    const matchSearch = !filters.value.search || 
      recado.titulo.toLowerCase().includes(filters.value.search.toLowerCase()) ||
      recado.remetente.toLowerCase().includes(filters.value.search.toLowerCase())
    
    const matchStatus = !filters.value.status ||
      (filters.value.status === 'lido' && recado.lido) ||
      (filters.value.status === 'nao_lido' && !recado.lido)
    
    const matchCategoria = !filters.value.categoria ||
      recado.categoria === filters.value.categoria
    
    return matchSearch && matchStatus && matchCategoria
  })
})

const limparFiltros = () => {
  filters.value = {
    search: '',
    status: '',
    categoria: ''
  }
}

const getCategoriaColor = (categoria) => {
  const colors = {
    geral: 'secondary',
    academico: 'primary',
    financeiro: 'success',
    evento: 'warning'
  }
  return colors[categoria] || 'secondary'
}

const getCategoriaIcon = (categoria) => {
  const icons = {
    geral: 'bi bi-info-circle-fill',
    academico: 'bi bi-book-fill',
    financeiro: 'bi bi-wallet2',
    evento: 'bi bi-calendar-event-fill'
  }
  return icons[categoria] || 'bi bi-envelope-fill'
}

const verRecado = (id) => {
  router.push(`/recados/${id}`)
}
</script>

<style scoped>
.recado-card {
  cursor: pointer;
  transition: all 0.2s;
  border-left: 4px solid transparent;
}

.recado-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15) !important;
  transform: translateY(-2px);
}

.recado-nao-lido {
  border-left-color: #0d6efd;
  background-color: #f8f9fa;
}

.recado-icon {
  width: 50px;
  height: 50px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 1.5rem;
}
</style>

