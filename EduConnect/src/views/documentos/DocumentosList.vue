<template>
  <div class="container-fluid py-4">
    <h2 class="fw-bold mb-4">
      <i class="bi bi-file-earmark-text-fill me-2"></i>Documentos
    </h2>
    
    <div class="row g-4">
      <div v-for="doc in documentos" :key="doc.id" class="col-md-4">
        <div class="card shadow-sm h-100 doc-card">
          <div class="card-body">
            <div class="d-flex align-items-start mb-3">
              <div class="doc-icon me-3">
                <i :class="getDocIcon(doc.tipo)"></i>
              </div>
              <div class="flex-grow-1">
                <h6>{{ doc.nome }}</h6>
                <small class="text-muted">{{ doc.data }}</small>
              </div>
              <span :class="`badge bg-${doc.assinado ? 'success' : 'warning'}`">
                {{ doc.assinado ? 'Assinado' : 'Pendente' }}
              </span>
            </div>
            <div class="d-flex gap-2">
              <button class="btn btn-sm btn-outline-primary flex-grow-1">
                <i class="bi bi-eye me-1"></i>Ver
              </button>
              <button v-if="!doc.assinado" class="btn btn-sm btn-primary">
                <i class="bi bi-pen me-1"></i>Assinar
              </button>
              <button class="btn btn-sm btn-outline-secondary">
                <i class="bi bi-download"></i>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const documentos = ref([
  { id: 1, nome: 'Contrato de Matrícula 2025', tipo: 'pdf', data: '15/09/2025', assinado: true },
  { id: 2, nome: 'Termo de Responsabilidade', tipo: 'pdf', data: '20/09/2025', assinado: false },
  { id: 3, nome: 'Autorização de Imagem', tipo: 'pdf', data: '25/09/2025', assinado: true }
])

const getDocIcon = (tipo) => {
  return tipo === 'pdf' ? 'bi bi-file-pdf-fill text-danger fs-2' : 'bi bi-file-earmark-fill fs-2'
}
</script>

<style scoped>
.doc-card { cursor: pointer; transition: all 0.2s; }
.doc-card:hover { transform: translateY(-4px); }
.doc-icon { width: 50px; height: 50px; display: flex; align-items: center; justify-content: center; }
</style>

