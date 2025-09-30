<template>
  <div class="container-fluid py-4">
    <h2 class="fw-bold mb-4">
      <i class="bi bi-wallet2 me-2"></i>Financeiro
    </h2>
    
    <div class="row g-3 mb-4">
      <div class="col-md-4">
        <div class="stat-card bg-success text-white">
          <h6 class="text-uppercase opacity-75">Pagas</h6>
          <h2>8</h2>
        </div>
      </div>
      <div class="col-md-4">
        <div class="stat-card bg-warning text-white">
          <h6 class="text-uppercase opacity-75">Pendentes</h6>
          <h2>2</h2>
        </div>
      </div>
      <div class="col-md-4">
        <div class="stat-card bg-danger text-white">
          <h6 class="text-uppercase opacity-75">Atrasadas</h6>
          <h2>1</h2>
        </div>
      </div>
    </div>
    
    <div class="card shadow-sm">
      <div class="card-header bg-white">
        <h5 class="mb-0">Mensalidades</h5>
      </div>
      <div class="card-body">
        <div class="table-responsive">
          <table class="table table-hover">
            <thead>
              <tr>
                <th>Mês</th>
                <th>Vencimento</th>
                <th>Valor</th>
                <th>Status</th>
                <th>Ações</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="mensalidade in mensalidades" :key="mensalidade.id">
                <td>{{ mensalidade.mes }}</td>
                <td>{{ mensalidade.vencimento }}</td>
                <td><strong>{{ mensalidade.valor }}</strong></td>
                <td>
                  <span :class="`badge bg-${getStatusColor(mensalidade.status)}`">
                    {{ mensalidade.status }}
                  </span>
                </td>
                <td>
                  <button v-if="mensalidade.status !== 'Paga'" class="btn btn-sm btn-primary me-2">
                    <i class="bi bi-credit-card me-1"></i>Pagar
                  </button>
                  <button class="btn btn-sm btn-outline-secondary">
                    <i class="bi bi-receipt"></i>
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const mensalidades = ref([
  { id: 1, mes: 'Setembro/2025', vencimento: '10/09/2025', valor: 'R$ 850,00', status: 'Paga' },
  { id: 2, mes: 'Outubro/2025', vencimento: '10/10/2025', valor: 'R$ 850,00', status: 'Pendente' },
  { id: 3, mes: 'Novembro/2025', vencimento: '10/11/2025', valor: 'R$ 850,00', status: 'Pendente' }
])

const getStatusColor = (status) => {
  const colors = { 'Paga': 'success', 'Pendente': 'warning', 'Atrasada': 'danger' }
  return colors[status] || 'secondary'
}
</script>

<style scoped>
.stat-card { padding: 1.5rem; border-radius: 10px; }
</style>

