<template>
  <div class="container-fluid py-4">
    <h2 class="fw-bold mb-4">
      <i class="bi bi-graph-up me-2"></i>Estatísticas
    </h2>
    
    <div class="row g-3 mb-4">
      <div class="col-md-3">
        <div class="stat-card bg-primary text-white">
          <h6 class="opacity-75">Total de Usuários</h6>
          <h2>245</h2>
        </div>
      </div>
      <div class="col-md-3">
        <div class="stat-card bg-success text-white">
          <h6 class="opacity-75">Usuários Ativos</h6>
          <h2>198</h2>
        </div>
      </div>
      <div class="col-md-3">
        <div class="stat-card bg-warning text-white">
          <h6 class="opacity-75">Recados Enviados</h6>
          <h2>1,234</h2>
        </div>
      </div>
      <div class="col-md-3">
        <div class="stat-card bg-info text-white">
          <h6 class="opacity-75">Taxa de Pagamento</h6>
          <h2>94%</h2>
        </div>
      </div>
    </div>
    
    <div class="row g-4">
      <div class="col-lg-6">
        <div class="card shadow-sm">
          <div class="card-header bg-white">
            <h5 class="mb-0">Usuários por Perfil</h5>
          </div>
          <div class="card-body">
            <div class="mb-3">
              <div class="d-flex justify-content-between mb-1">
                <span>Alunos</span>
                <strong>120</strong>
              </div>
              <div class="progress">
                <div class="progress-bar bg-primary" style="width: 49%"></div>
              </div>
            </div>
            <div class="mb-3">
              <div class="d-flex justify-content-between mb-1">
                <span>Responsáveis</span>
                <strong>80</strong>
              </div>
              <div class="progress">
                <div class="progress-bar bg-success" style="width: 33%"></div>
              </div>
            </div>
            <div class="mb-3">
              <div class="d-flex justify-content-between mb-1">
                <span>Professores</span>
                <strong>40</strong>
              </div>
              <div class="progress">
                <div class="progress-bar bg-warning" style="width: 16%"></div>
              </div>
            </div>
            <div>
              <div class="d-flex justify-content-between mb-1">
                <span>Administradores</span>
                <strong>5</strong>
              </div>
              <div class="progress">
                <div class="progress-bar bg-danger" style="width: 2%"></div>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <div class="col-lg-6">
        <div class="card shadow-sm">
          <div class="card-header bg-white">
            <h5 class="mb-0">Atividades Recentes</h5>
          </div>
          <div class="card-body">
            <div class="activity-item mb-3">
              <i class="bi bi-person-plus text-success"></i>
              <div>
                <strong>Novo usuário cadastrado</strong>
                <small class="text-muted d-block">Maria Silva - há 2 horas</small>
              </div>
            </div>
            <div class="activity-item mb-3">
              <i class="bi bi-envelope text-primary"></i>
              <div>
                <strong>Recado enviado</strong>
                <small class="text-muted d-block">Reunião de pais - há 5 horas</small>
              </div>
            </div>
            <div class="activity-item">
              <i class="bi bi-credit-card text-warning"></i>
              <div>
                <strong>Pagamento recebido</strong>
                <small class="text-muted d-block">João Santos - há 1 dia</small>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
</script>

<style scoped>
.stat-card { padding: 1.5rem; border-radius: 10px; }
.activity-item { display: flex; gap: 1rem; align-items: start; }
.activity-item i { font-size: 1.5rem; }
</style>

