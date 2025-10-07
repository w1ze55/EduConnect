<template>
  <div class="container-fluid py-4">
    <div class="row mb-4">
      <div class="col-md-6">
        <h2 class="fw-bold">
          <i class="bi bi-people-fill me-2"></i>Gestão de Usuários
        </h2>
      </div>
      <div class="col-md-6 text-end">
        <button class="btn btn-primary" @click="modalCriar = true">
          <i class="bi bi-plus-circle me-2"></i>Novo Usuário
        </button>
      </div>
    </div>
    
    <div class="card shadow-sm">
      <div class="card-body">
        <div class="row g-3 mb-3">
          <div class="col-md-4">
            <input type="text" class="form-control" placeholder="Buscar usuário..." v-model="filtros.busca" />
          </div>
          <div class="col-md-3">
            <select class="form-select" v-model="filtros.role">
              <option value="">Todos os perfis</option>
              <option value="ALUNO">Aluno</option>
              <option value="RESPONSAVEL">Responsável</option>
              <option value="PROFESSOR">Professor</option>
              <option value="DIRETORIA">Diretoria</option>
              <option value="ADMINISTRADOR">Administrador</option>
            </select>
          </div>
        </div>
        
        <div class="table-responsive">
          <table class="table table-hover">
            <thead>
              <tr>
                <th>Nome</th>
                <th>E-mail</th>
                <th>Perfil</th>
                <th>Status</th>
                <th>Ações</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="usuario in usuarios" :key="usuario.id">
                <td>{{ usuario.nome }}</td>
                <td>{{ usuario.email }}</td>
                <td><span class="badge bg-info">{{ usuario.role }}</span></td>
                <td>
                  <span :class="`badge bg-${usuario.ativo ? 'success' : 'secondary'}`">
                    {{ usuario.ativo ? 'Ativo' : 'Inativo' }}
                  </span>
                </td>
                <td>
                  <button class="btn btn-sm btn-outline-primary me-1">
                    <i class="bi bi-pencil"></i>
                  </button>
                  <button class="btn btn-sm btn-outline-danger">
                    <i class="bi bi-trash"></i>
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

const filtros = ref({ busca: '', role: '' })
const modalCriar = ref(false)

const usuarios = ref([
  { id: 1, nome: 'João Silva', email: 'joao@example.com', role: 'ALUNO', ativo: true },
  { id: 2, nome: 'Maria Santos', email: 'maria@example.com', role: 'PROFESSOR', ativo: true },
  { id: 3, nome: 'Carlos Oliveira', email: 'carlos@example.com', role: 'RESPONSAVEL', ativo: true }
])
</script>

