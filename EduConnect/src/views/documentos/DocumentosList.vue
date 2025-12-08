<template>
  <div class="container-fluid py-4">
    <h2 class="fw-bold mb-4">
      <i class="bi bi-file-earmark-text-fill me-2"></i>Documentos
    </h2>
    
    <div v-if="carregando" class="text-center py-4 text-muted">
      <div class="spinner-border text-primary mb-2" role="status"></div>
      <div>Carregando documentos...</div>
    </div>

    <div v-else-if="documentos.length === 0" class="text-center py-5 text-muted">
      <i class="bi bi-inbox fs-1"></i>
      <p class="mt-2 mb-0">Nenhum documento disponível.</p>
    </div>

    <div v-else class="row g-4">
      <div v-for="doc in documentos" :key="doc.id" class="col-md-4">
        <div class="card shadow-sm h-100 doc-card">
          <div class="card-body">
            <div class="d-flex align-items-start mb-3">
              <div class="doc-icon me-3">
                <i :class="getDocIcon(doc.tipo || doc.extensao)"></i>
              </div>
              <div class="flex-grow-1">
                <h6 class="mb-1">{{ doc.nome || doc.titulo }}</h6>
                <small class="text-muted">{{ formatarData(doc.dataEnvio || doc.createdAt) }}</small>
              </div>
              <span :class="`badge bg-${doc.assinado ? 'success' : 'warning'}`">
                {{ doc.assinado ? 'Assinado' : 'Pendente' }}
              </span>
            </div>
            <div class="d-flex gap-2">
              <button class="btn btn-sm btn-outline-primary flex-grow-1" @click="baixarDocumento(doc)">
                <i class="bi bi-eye me-1"></i>Ver
              </button>
              <button v-if="!doc.assinado" class="btn btn-sm btn-primary" @click="assinarDocumento(doc)">
                <i class="bi bi-pen me-1"></i>Assinar
              </button>
              <button class="btn btn-sm btn-outline-secondary" @click="baixarDocumento(doc)">
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
import { ref, onMounted } from 'vue'
import documentosService from '@/services/documentosService'
import { useNotificationStore } from '@/stores/notifications'

const documentos = ref([])
const carregando = ref(false)
const notifications = useNotificationStore()

const carregarDocumentos = async () => {
  carregando.value = true
  try {
    const response = await documentosService.getDocumentos()
    documentos.value = response.data || response || []
  } catch (error) {
    console.error('Erro ao carregar documentos:', error)
    notifications.error(error.response?.data?.message || 'Erro ao carregar documentos')
    documentos.value = []
  } finally {
    carregando.value = false
  }
}

const formatarData = (data) => {
  if (!data) return ''
  const d = new Date(data)
  return d.toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit', year: 'numeric' })
}

const getDocIcon = (tipo) => {
  if (tipo && String(tipo).toLowerCase().includes('pdf')) return 'bi bi-file-pdf-fill text-danger fs-2'
  if (tipo && String(tipo).toLowerCase().match(/(doc|docx)/)) return 'bi bi-file-word-fill text-primary fs-2'
  return 'bi bi-file-earmark-fill fs-2'
}

const baixarDocumento = async (doc) => {
  try {
    const response = await documentosService.downloadDocumento(doc.id)
    const blobUrl = window.URL.createObjectURL(new Blob([response.data]))
    const link = document.createElement('a')
    link.href = blobUrl
    link.download = doc.nome || 'documento'
    link.click()
    window.URL.revokeObjectURL(blobUrl)
  } catch (error) {
    console.error('Erro ao baixar documento:', error)
    notifications.error(error.response?.data?.message || 'Erro ao baixar documento')
  }
}

const assinarDocumento = (doc) => {
  notifications.info('Fluxo de assinatura será implementado.')
}

onMounted(() => {
  carregarDocumentos()
})
</script>

<style scoped>
.doc-card { cursor: pointer; transition: all 0.2s; }
.doc-card:hover { transform: translateY(-4px); }
.doc-icon { width: 50px; height: 50px; display: flex; align-items: center; justify-content: center; }
</style>
