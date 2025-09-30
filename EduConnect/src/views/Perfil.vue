<template>
  <div class="container-fluid py-4">
    <h2 class="fw-bold mb-4">
      <i class="bi bi-person-circle me-2"></i>Meu Perfil
    </h2>
    
    <div class="row">
      <div class="col-lg-8">
        <div class="card shadow-sm">
          <div class="card-body">
            <form>
              <div class="row">
                <div class="col-md-6 mb-3">
                  <label class="form-label">Nome Completo</label>
                  <input type="text" class="form-control" v-model="perfil.nome" />
                </div>
                <div class="col-md-6 mb-3">
                  <label class="form-label">E-mail</label>
                  <input type="email" class="form-control" v-model="perfil.email" />
                </div>
              </div>
              <div class="row">
                <div class="col-md-6 mb-3">
                  <label class="form-label">Telefone</label>
                  <input type="text" class="form-control" v-model="perfil.telefone" />
                </div>
                <div class="col-md-6 mb-3">
                  <label class="form-label">CPF</label>
                  <input type="text" class="form-control" v-model="perfil.cpf" disabled />
                </div>
              </div>
              <div class="row">
                <div class="col-md-6 mb-3">
                  <label class="form-label">Perfil</label>
                  <input type="text" class="form-control" :value="roleLabel" disabled />
                </div>
                <div class="col-md-6 mb-3" v-if="perfil.turma">
                  <label class="form-label">Turma</label>
                  <input type="text" class="form-control" v-model="perfil.turma" disabled />
                </div>
                <div class="col-md-6 mb-3" v-if="perfil.matricula">
                  <label class="form-label">Matrícula</label>
                  <input type="text" class="form-control" v-model="perfil.matricula" disabled />
                </div>
              </div>
              <button type="submit" class="btn btn-primary">
                <i class="bi bi-check2 me-2"></i>Salvar Alterações
              </button>
            </form>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useAuthStore } from '../stores/auth'

const authStore = useAuthStore()

const perfil = ref({
  nome: authStore.user?.nome || '',
  email: authStore.user?.email || '',
  telefone: authStore.user?.telefone || '',
  cpf: authStore.user?.cpf || '',
  role: authStore.user?.role || '',
  turma: authStore.user?.turma || '',
  matricula: authStore.user?.matricula || ''
})

// Tradução do role para exibição
const roleLabel = computed(() => {
  const roles = {
    'ALUNO': 'Aluno',
    'PROFESSOR': 'Professor',
    'RESPONSAVEL': 'Responsável',
    'ADMINISTRADOR': 'Administrador'
  }
  return roles[perfil.value.role] || perfil.value.role
})
</script>

