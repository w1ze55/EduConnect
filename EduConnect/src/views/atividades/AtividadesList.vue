<template>
  <div class="container-fluid py-4">
    <div class="row mb-4">
      <div class="col-md-6">
        <h2 class="fw-bold">
          <i class="bi bi-journal-text me-2"></i>Atividades
        </h2>
        <p class="text-muted">Confira suas atividades escolares</p>
      </div>
    </div>
    
    <div class="row g-3 mb-4">
      <div class="col-md-3">
        <div class="stat-card bg-primary text-white">
          <h6 class="text-uppercase mb-1 opacity-75">Pendentes</h6>
          <h2 class="mb-0">5</h2>
        </div>
      </div>
      <div class="col-md-3">
        <div class="stat-card bg-success text-white">
          <h6 class="text-uppercase mb-1 opacity-75">Concluídas</h6>
          <h2 class="mb-0">12</h2>
        </div>
      </div>
      <div class="col-md-3">
        <div class="stat-card bg-warning text-white">
          <h6 class="text-uppercase mb-1 opacity-75">Atrasadas</h6>
          <h2 class="mb-0">2</h2>
        </div>
      </div>
      <div class="col-md-3">
        <div class="stat-card bg-info text-white">
          <h6 class="text-uppercase mb-1 opacity-75">Média</h6>
          <h2 class="mb-0">8.5</h2>
        </div>
      </div>
    </div>
    
    <div class="card shadow-sm mb-4">
      <div class="card-body">
        <div class="row g-3">
          <div class="col-md-4">
            <input type="text" class="form-control" placeholder="Buscar atividades..." v-model="filtros.busca" />
          </div>
          <div class="col-md-3">
            <select class="form-select" v-model="filtros.status">
              <option value="">Todos os status</option>
              <option value="pendente">Pendente</option>
              <option value="entregue">Entregue</option>
              <option value="atrasada">Atrasada</option>
            </select>
          </div>
          <div class="col-md-3">
            <select class="form-select" v-model="filtros.disciplina">
              <option value="">Todas as disciplinas</option>
              <option value="matematica">Matemática</option>
              <option value="portugues">Português</option>
              <option value="historia">História</option>
              <option value="fisica">Física</option>
            </select>
          </div>
        </div>
      </div>
    </div>
    
    <div class="row g-4">
      <div v-for="atividade in atividades" :key="atividade.id" class="col-md-6 col-lg-4">
        <div class="card shadow-sm h-100 atividade-card" @click="router.push(`/atividades/${atividade.id}`)">
          <div class="card-body">
            <div class="d-flex justify-content-between align-items-start mb-3">
              <span :class="`badge bg-${getStatusColor(atividade.status)}`">
                {{ atividade.status }}
              </span>
              <span class="text-muted small">{{ atividade.disciplina }}</span>
            </div>
            <h5 class="card-title">{{ atividade.titulo }}</h5>
            <p class="card-text text-muted small">{{ atividade.descricao }}</p>
            <div class="d-flex justify-content-between align-items-center mt-3">
              <small class="text-muted">
                <i class="bi bi-calendar me-1"></i>{{ atividade.prazo }}
              </small>
              <small v-if="atividade.nota" class="badge bg-info">
                Nota: {{ atividade.nota }}
              </small>
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

const router = useRouter()

const filtros = ref({
  busca: '',
  status: '',
  disciplina: ''
})

const atividades = ref([
  { id: 1, titulo: 'Trabalho de Matemática', disciplina: 'Matemática', descricao: 'Resolver exercícios do capítulo 5', prazo: 'Hoje', status: 'pendente', nota: null },
  { id: 2, titulo: 'Redação sobre História', disciplina: 'História', descricao: 'Dissertação sobre Revolução Industrial', prazo: 'Amanhã', status: 'pendente', nota: null },
  { id: 3, titulo: 'Experimento de Física', disciplina: 'Física', descricao: 'Relatório sobre Lei de Newton', prazo: '3 dias', status: 'entregue', nota: 9.5 },
])

const getStatusColor = (status) => {
  const colors = { pendente: 'warning', entregue: 'success', atrasada: 'danger' }
  return colors[status] || 'secondary'
}
</script>

<style scoped>
.stat-card { padding: 1.5rem; border-radius: 10px; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1); }
.atividade-card { cursor: pointer; transition: all 0.2s; }
.atividade-card:hover { transform: translateY(-4px); box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15) !important; }
</style>

