<template>
  <div class="container-fluid py-4">
    <div class="row mb-4">
      <div class="col">
        <button class="btn btn-link text-decoration-none ps-0" @click="router.back()">
          <i class="bi bi-arrow-left me-2"></i>Voltar
        </button>
      </div>
    </div>
    
    <div v-if="loading" class="text-center py-5">
      <loading-spinner message="Carregando recado..." />
    </div>
    
    <div v-else-if="recado" class="row justify-content-center">
      <div class="col-lg-8">
        <div class="card shadow-sm">
          <div class="card-header bg-white py-3">
            <div class="d-flex justify-content-between align-items-start">
              <div>
                <h3 class="mb-2">{{ recado.titulo }}</h3>
                <div class="d-flex gap-2 flex-wrap">
                  <span :class="`badge bg-${getCategoriaColor(recado.categoria)}`">
                    {{ recado.categoria }}
                  </span>
                  <span v-if="recado.importante" class="badge bg-danger">
                    <i class="bi bi-exclamation-triangle me-1"></i>Importante
                  </span>
                </div>
              </div>
              <button class="btn btn-outline-secondary btn-sm">
                <i class="bi bi-printer me-1"></i>Imprimir
              </button>
            </div>
          </div>
          
          <div class="card-body">
            <!-- Informações do remetente -->
            <div class="remetente-info mb-4">
              <div class="d-flex align-items-center">
                <div class="avatar me-3">
                  {{ recado.remetente.charAt(0) }}
                </div>
                <div>
                  <h6 class="mb-0">{{ recado.remetente }}</h6>
                  <small class="text-muted">
                    <i class="bi bi-calendar me-1"></i>{{ recado.dataEnvio }}
                    <span class="mx-2">•</span>
                    <i class="bi bi-clock me-1"></i>{{ recado.horaEnvio }}
                  </small>
                </div>
              </div>
            </div>
            
            <hr>
            
            <!-- Conteúdo do recado -->
            <div class="recado-conteudo my-4">
              <p v-html="recado.conteudo"></p>
            </div>
            
            <!-- Anexos -->
            <div v-if="recado.anexos && recado.anexos.length > 0" class="anexos-section mt-4">
              <h6 class="mb-3">
                <i class="bi bi-paperclip me-2"></i>Anexos ({{ recado.anexos.length }})
              </h6>
              <div class="row g-3">
                <div 
                  v-for="anexo in recado.anexos" 
                  :key="anexo.id"
                  class="col-md-6"
                >
                  <div class="anexo-card">
                    <div class="d-flex align-items-center">
                      <div class="anexo-icon me-3">
                        <i :class="getFileIcon(anexo.tipo)"></i>
                      </div>
                      <div class="flex-grow-1">
                        <h6 class="mb-0">{{ anexo.nome }}</h6>
                        <small class="text-muted">{{ anexo.tamanho }}</small>
                      </div>
                      <button class="btn btn-sm btn-outline-primary">
                        <i class="bi bi-download"></i>
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            
            <!-- Imagens -->
            <div v-if="recado.imagens && recado.imagens.length > 0" class="imagens-section mt-4">
              <h6 class="mb-3">
                <i class="bi bi-image me-2"></i>Imagens
              </h6>
              <div class="row g-3">
                <div 
                  v-for="(imagem, index) in recado.imagens" 
                  :key="index"
                  class="col-md-4"
                >
                  <img 
                    :src="imagem" 
                    class="img-fluid rounded shadow-sm"
                    style="cursor: pointer;"
                    @click="visualizarImagem(imagem)"
                  />
                </div>
              </div>
            </div>
            
            <!-- Confirmação de leitura -->
            <div v-if="!recado.lido" class="alert alert-info mt-4">
              <div class="d-flex justify-content-between align-items-center">
                <div>
                  <i class="bi bi-info-circle me-2"></i>
                  <strong>Confirme a leitura deste recado</strong>
                </div>
                <button 
                  class="btn btn-primary btn-sm"
                  @click="confirmarLeitura"
                  :disabled="confirmandoLeitura"
                >
                  <span v-if="confirmandoLeitura">
                    <span class="spinner-border spinner-border-sm me-2"></span>
                    Confirmando...
                  </span>
                  <span v-else>
                    <i class="bi bi-check2 me-1"></i>
                    Confirmar Leitura
                  </span>
                </button>
              </div>
            </div>
            
            <div v-else class="alert alert-success mt-4">
              <i class="bi bi-check-circle me-2"></i>
              Leitura confirmada em {{ recado.dataLeitura }}
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useNotificationStore } from '../../stores/notifications'
import LoadingSpinner from '../../components/common/LoadingSpinner.vue'

const router = useRouter()
const route = useRoute()
const notificationStore = useNotificationStore()

const loading = ref(false)
const recado = ref(null)
const confirmandoLeitura = ref(false)

// Mock de dados - substituir por chamada real da API
onMounted(async () => {
  loading.value = true
  await new Promise(resolve => setTimeout(resolve, 1000))
  
  recado.value = {
    id: route.params.id,
    titulo: 'Reunião de Pais e Mestres',
    remetente: 'Coordenação Pedagógica',
    dataEnvio: '30/09/2025',
    horaEnvio: '14:30',
    categoria: 'evento',
    importante: true,
    lido: false,
    conteudo: `
      <p>Prezados pais e responsáveis,</p>
      <p>Informamos que a reunião de pais e mestres do terceiro bimestre será realizada no próximo sábado, dia 05 de outubro de 2025, das 9h às 12h.</p>
      <p>Durante a reunião, os professores estarão disponíveis para:</p>
      <ul>
        <li>Apresentar o desempenho acadêmico dos alunos</li>
        <li>Discutir comportamento e participação em sala</li>
        <li>Tirar dúvidas sobre o cronograma das próximas atividades</li>
        <li>Tratar de assuntos específicos, se necessário</li>
      </ul>
      <p><strong>Local:</strong> Instalações da escola<br>
      <strong>Horário:</strong> 9h às 12h<br>
      <strong>Data:</strong> 05/10/2025</p>
      <p>A presença é muito importante para acompanharmos juntos o desenvolvimento dos alunos.</p>
      <p>Atenciosamente,<br>Coordenação Pedagógica</p>
    `,
    anexos: [
      {
        id: 1,
        nome: 'cronograma-reuniao.pdf',
        tipo: 'pdf',
        tamanho: '245 KB'
      },
      {
        id: 2,
        nome: 'mapa-escola.jpg',
        tipo: 'image',
        tamanho: '1.2 MB'
      }
    ],
    imagens: []
  }
  
  loading.value = false
})

const getCategoriaColor = (categoria) => {
  const colors = {
    geral: 'secondary',
    academico: 'primary',
    financeiro: 'success',
    evento: 'warning'
  }
  return colors[categoria] || 'secondary'
}

const getFileIcon = (tipo) => {
  const icons = {
    pdf: 'bi bi-file-pdf-fill text-danger',
    doc: 'bi bi-file-word-fill text-primary',
    xls: 'bi bi-file-excel-fill text-success',
    image: 'bi bi-file-image-fill text-info',
    default: 'bi bi-file-earmark-fill text-secondary'
  }
  return icons[tipo] || icons.default
}

const confirmarLeitura = async () => {
  confirmandoLeitura.value = true
  
  try {
    await new Promise(resolve => setTimeout(resolve, 1000))
    recado.value.lido = true
    recado.value.dataLeitura = new Date().toLocaleString('pt-BR')
    notificationStore.success('Leitura confirmada com sucesso!')
  } catch (error) {
    notificationStore.error('Erro ao confirmar leitura')
  } finally {
    confirmandoLeitura.value = false
  }
}

const visualizarImagem = (imagem) => {
  // Implementar visualizador de imagem (modal)
  window.open(imagem, '_blank')
}
</script>

<style scoped>
.avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 1.25rem;
}

.recado-conteudo {
  line-height: 1.8;
  font-size: 1rem;
}

.anexo-card {
  padding: 1rem;
  border: 1px solid #dee2e6;
  border-radius: 8px;
  transition: all 0.2s;
}

.anexo-card:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.anexo-icon {
  font-size: 2rem;
}
</style>

