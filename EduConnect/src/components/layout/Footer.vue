<template>
  <footer class="footer bg-light border-top mt-auto py-3">
    <div class="container-fluid">
      <div class="row align-items-center">
        <div class="col-md-6 text-center text-md-start">
          <span class="text-muted">
            &copy; {{ currentYear }} EduConnect. Todos os direitos reservados.
          </span>
        </div>
        <div class="col-md-6 text-center text-md-end">
          <a href="#" class="text-muted text-decoration-none me-3">
            <i class="bi bi-question-circle me-1"></i>Ajuda
          </a>
          <a href="#" class="text-muted text-decoration-none me-3">
            <i class="bi bi-shield-check me-1"></i>Privacidade
          </a>
          <a href="#" class="text-muted text-decoration-none">
            <i class="bi bi-file-text me-1"></i>Termos
          </a>
        </div>
      </div>
    </div>
  </footer>
</template>

<script setup>
import { computed } from 'vue'

const currentYear = computed(() => new Date().getFullYear())
</script>

<style scoped>
.footer {
  font-size: 0.875rem;
}
</style>

