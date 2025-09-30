<template>
  <div class="not-found">
    <div class="text-center">
      <i class="bi bi-exclamation-triangle-fill text-warning" style="font-size: 5rem;"></i>
      <h1 class="mt-4">404 - Página não encontrada</h1>
      <p class="text-muted">A página que você está procurando não existe.</p>
      <router-link to="/dashboard" class="btn btn-primary mt-3">
        <i class="bi bi-house-door me-2"></i>Voltar ao Início
      </router-link>
    </div>
  </div>
</template>

<style scoped>
.not-found {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>

