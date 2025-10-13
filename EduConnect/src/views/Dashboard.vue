<template>
  <div class="dashboard">
    <div class="container-fluid py-4">
      <div class="row mb-4">
        <div class="col">
          <h2 class="fw-bold">
            <i class="bi bi-house-door-fill me-2"></i>
            Bem-vindo, {{ userName }}!
          </h2>
          <p class="text-muted">Confira as últimas atualizações e notificações</p>
        </div>
      </div>
      
      <!-- Cards de Estatísticas -->
      <div class="row g-4 mb-4">
        <div class="col-md-3">
          <div class="stat-card bg-primary text-white">
            <div class="d-flex justify-content-between align-items-center">
              <div>
                <h6 class="text-uppercase mb-1 opacity-75">Recados</h6>
                <h2 class="mb-0">{{ stats.recados }}</h2>
              </div>
              <i class="bi bi-envelope-fill fs-1 opacity-50"></i>
            </div>
          </div>
        </div>
        
        <div class="col-md-3">
          <div class="stat-card bg-success text-white">
            <div class="d-flex justify-content-between align-items-center">
              <div>
                <h6 class="text-uppercase mb-1 opacity-75">Atividades</h6>
                <h2 class="mb-0">{{ stats.atividades }}</h2>
              </div>
              <i class="bi bi-journal-text fs-1 opacity-50"></i>
            </div>
          </div>
        </div>
        
        <div class="col-md-3">
          <div class="stat-card bg-warning text-white">
            <div class="d-flex justify-content-between align-items-center">
              <div>
                <h6 class="text-uppercase mb-1 opacity-75">Eventos</h6>
                <h2 class="mb-0">{{ stats.eventos }}</h2>
              </div>
              <i class="bi bi-calendar-event-fill fs-1 opacity-50"></i>
            </div>
          </div>
        </div>
        
        <div class="col-md-3">
          <div class="stat-card bg-info text-white">
            <div class="d-flex justify-content-between align-items-center">
              <div>
                <h6 class="text-uppercase mb-1 opacity-75">Documentos</h6>
                <h2 class="mb-0">{{ stats.documentos }}</h2>
              </div>
              <i class="bi bi-file-earmark-text-fill fs-1 opacity-50"></i>
            </div>
          </div>
        </div>
      </div>
      
      <div class="row g-4">
        <!-- Recados Recentes -->
        <div class="col-lg-6">
          <div class="card shadow-sm">
            <div class="card-header bg-white d-flex justify-content-between align-items-center">
              <h5 class="mb-0">
                <i class="bi bi-envelope me-2"></i>Recados Recentes
              </h5>
              <router-link to="/recados" class="btn btn-sm btn-outline-primary">
                Ver todos
              </router-link>
            </div>
            <div class="card-body">
              <div v-if="recadosRecentes.length === 0" class="text-center py-4 text-muted">
                <i class="bi bi-inbox fs-1"></i>
                <p class="mt-2">Nenhum recado disponível</p>
              </div>
              <div v-else>
                <div 
                  v-for="recado in recadosRecentes" 
                  :key="recado.id"
                  class="recado-item"
                  @click="router.push(`/recados/${recado.id}`)"
                >
                  <div class="d-flex justify-content-between align-items-start">
                    <div>
                      <h6 class="mb-1">{{ recado.titulo }}</h6>
                      <small class="text-muted">
                        <i class="bi bi-person me-1"></i>{{ recado.remetente }}
                      </small>
                    </div>
                    <small class="text-muted">{{ recado.data }}</small>
                  </div>
                  <p class="mb-0 mt-2 text-muted small">{{ recado.preview }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- Atividades Pendentes -->
        <div class="col-lg-6">
          <div class="card shadow-sm">
            <div class="card-header bg-white d-flex justify-content-between align-items-center">
              <h5 class="mb-0">
                <i class="bi bi-clipboard-check me-2"></i>Atividades Pendentes
              </h5>
              <router-link to="/atividades" class="btn btn-sm btn-outline-primary">
                Ver todas
              </router-link>
            </div>
            <div class="card-body">
              <div v-if="atividadesPendentes.length === 0" class="text-center py-4 text-muted">
                <i class="bi bi-check-circle fs-1"></i>
                <p class="mt-2">Nenhuma atividade pendente</p>
              </div>
              <div v-else>
                <div 
                  v-for="atividade in atividadesPendentes" 
                  :key="atividade.id"
                  class="atividade-item"
                  @click="router.push(`/atividades/${atividade.id}`)"
                >
                  <div class="d-flex justify-content-between align-items-start">
                    <div>
                      <h6 class="mb-1">{{ atividade.titulo }}</h6>
                      <small class="text-muted">
                        <i class="bi bi-book me-1"></i>{{ atividade.disciplina }}
                      </small>
                    </div>
                    <span :class="`badge ${getPrazoClass(atividade.prazo)}`">
                      {{ atividade.prazo }}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- Próximos Eventos -->
      <div class="row g-4 mt-0">
        <div class="col-12">
          <div class="card shadow-sm">
            <div class="card-header bg-white d-flex justify-content-between align-items-center">
              <h5 class="mb-0">
                <i class="bi bi-calendar-event me-2"></i>Próximos Eventos
              </h5>
              <router-link to="/calendario" class="btn btn-sm btn-outline-primary">
                Ver calendário
              </router-link>
            </div>
            <div class="card-body">
              <div v-if="proximosEventos.length === 0" class="text-center py-4 text-muted">
                <i class="bi bi-calendar-x fs-1"></i>
                <p class="mt-2">Nenhum evento próximo</p>
              </div>
              <div v-else class="row g-3">
                <div 
                  v-for="evento in proximosEventos" 
                  :key="evento.id"
                  class="col-md-4"
                >
                  <div class="evento-card">
                    <div class="evento-date">
                      <div class="day">{{ evento.dia }}</div>
                      <div class="month">{{ evento.mes }}</div>
                    </div>
                    <div class="evento-info">
                      <h6 class="mb-1">{{ evento.titulo }}</h6>
                      <small class="text-muted">
                        <i class="bi bi-clock me-1"></i>{{ evento.horario }}
                      </small>
                    </div>
                  </div>
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
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const userName = computed(() => authStore.userName)
const userRole = computed(() => authStore.userRole)

// Dados dinâmicos baseados no perfil
const stats = ref({
  recados: 0,
  atividades: 0,
  eventos: 0,
  documentos: 0
})

const recadosRecentes = ref([])
const atividadesPendentes = ref([])

const proximosEventos = ref([])

// TODO: Carregar eventos do backend

const getPrazoClass = (prazo) => {
  if (prazo.includes('Hoje')) return 'bg-danger'
  if (prazo.includes('Amanhã')) return 'bg-warning'
  return 'bg-info'
}

onMounted(async () => {
  // Carregar dados do dashboard do backend
  try {
    // TODO: Implementar chamadas para API real
    console.log('📊 [DASHBOARD] Carregando dados do usuário:', authStore.user?.role)
    
    // Por enquanto, deixar dados vazios até integração com backend
    stats.value = {
      recados: 0,
      atividades: 0,
      eventos: 0,
      documentos: 0
    }
    
    recadosRecentes.value = []
    atividadesPendentes.value = []
  } catch (error) {
    console.error('Erro ao carregar dados do dashboard:', error)
  }
})
</script>

<style scoped>
.stat-card {
  padding: 1.5rem;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.recado-item,
.atividade-item {
  padding: 1rem;
  border-bottom: 1px solid #e9ecef;
  cursor: pointer;
  transition: background-color 0.2s;
}

.recado-item:last-child,
.atividade-item:last-child {
  border-bottom: none;
}

.recado-item:hover,
.atividade-item:hover {
  background-color: #f8f9fa;
}

.evento-card {
  display: flex;
  gap: 1rem;
  padding: 1rem;
  border: 1px solid #dee2e6;
  border-radius: 8px;
  transition: all 0.2s;
}

.evento-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.evento-date {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 0.5rem 1rem;
  border-radius: 8px;
  text-align: center;
  min-width: 60px;
}

.evento-date .day {
  font-size: 1.5rem;
  font-weight: bold;
  line-height: 1;
}

.evento-date .month {
  font-size: 0.75rem;
  text-transform: uppercase;
}

.evento-info {
  flex: 1;
}
</style>

