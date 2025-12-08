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
              <div class="d-flex gap-2">
                <button 
                  v-if="podeEditar" 
                  class="btn btn-outline-primary btn-sm"
                  @click="editarRecado"
                >
                  <i class="bi bi-pencil me-1"></i>Editar
                </button>
                <button 
                  v-if="podeDeletar" 
                  class="btn btn-outline-danger btn-sm"
                  @click="confirmarDelecao"
                >
                  <i class="bi bi-trash me-1"></i>Deletar
                </button>
                <button class="btn btn-outline-secondary btn-sm">
                  <i class="bi bi-printer me-1"></i>Imprimir
                </button>
              </div>
            </div>
          </div>
          
          <div class="card-body">
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
            
            <div class="recado-conteudo my-4">
              <p v-html="recado.conteudo"></p>
            </div>
            
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
                      <a class="btn btn-sm btn-outline-primary" :href="anexo.url" target="_blank">
                        <i class="bi bi-download"></i>
                      </a>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            
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
            
            <div v-if="podeConfirmarLeitura" class="alert alert-info mt-4">
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
            
            <div v-else-if="recado.lido" class="alert alert-success mt-4">
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
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import { useNotificationStore } from '../../stores/notifications'
import recadosService from '../../services/recadosService'
import LoadingSpinner from '../../components/common/LoadingSpinner.vue'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const notificationStore = useNotificationStore()

const loading = ref(false)
const recado = ref(null)
const confirmandoLeitura = ref(false)

const role = computed(() => authStore.user?.role)

const podeEditar = computed(() => {
  if (!recado.value || !authStore.user) return false
  const isAutor = recado.value.remetenteId === authStore.user.id
  const isAdmin = role.value === 'ADMINISTRADOR' || role.value === 'DIRETORIA'
  return isAutor || isAdmin
})

const podeDeletar = computed(() => podeEditar.value)

const podeConfirmarLeitura = computed(() => {
  if (!recado.value || recado.value.lido) return false
  if (!recado.value.exigirConfirmacao) return false
  return role.value === 'ALUNO' || role.value === 'RESPONSAVEL'
})

onMounted(async () => {
  loading.value = true
  try {
    const response = await recadosService.getRecadoById(route.params.id)
    const recadoData = response.data
    
    recado.value = {
      id: recadoData.id,
      titulo: recadoData.titulo,
      remetente: recadoData.remetente,
      dataEnvio: formatarData(recadoData.dataEnvio),
      horaEnvio: formatarHora(recadoData.dataEnvio),
      categoria: recadoData.categoria.toLowerCase(),
      importante: recadoData.importante,
      exigirConfirmacao: recadoData.exigirConfirmacao,
      lido: recadoData.lido,
      dataLeitura: recadoData.dataLeitura ? formatarDataHora(recadoData.dataLeitura) : null,
      conteudo: formatarConteudo(recadoData.conteudo),
      anexos: recadoData.anexos ? recadoData.anexos.map((url, index) => ({
        id: index,
        nome: extrairNomeAnexo(url),
        tipo: extrairTipoAnexo(url),
        tamanho: 'N/A',
        url: url
      })) : [],
      imagens: [],
      remetenteId: recadoData.remetenteId
    }
  } catch (error) {
    console.error('Erro ao carregar recado:', error)
    notificationStore.error('Erro ao carregar recado')
    router.push('/recados')
  } finally {
    loading.value = false
  }
})

const formatarData = (dataISO) => {
  const data = new Date(dataISO)
  return data.toLocaleDateString('pt-BR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric'
  })
}

const formatarHora = (dataISO) => {
  const data = new Date(dataISO)
  return data.toLocaleTimeString('pt-BR', {
    hour: '2-digit',
    minute: '2-digit'
  })
}

const formatarDataHora = (dataISO) => {
  const data = new Date(dataISO)
  return data.toLocaleString('pt-BR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const formatarConteudo = (conteudo) => conteudo.replace(/\n/g, '<br>')

const extrairNomeAnexo = (url) => {
  const partes = url.split('/')
  return partes[partes.length - 1] || 'anexo'
}

const extrairTipoAnexo = (url) => {
  const extensao = url.split('.').pop().toLowerCase()
  if (['pdf'].includes(extensao)) return 'pdf'
  if (['doc', 'docx'].includes(extensao)) return 'doc'
  if (['xls', 'xlsx'].includes(extensao)) return 'xls'
  if (['jpg', 'jpeg', 'png', 'gif'].includes(extensao)) return 'image'
  return 'default'
}

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
    await recadosService.confirmarLeitura(recado.value.id)
    recado.value.lido = true
    recado.value.dataLeitura = new Date().toLocaleString('pt-BR')
    notificationStore.success('Leitura confirmada com sucesso!')
  } catch (error) {
    console.error('Erro ao confirmar leitura:', error)
    notificationStore.error(error.response?.data?.message || 'Erro ao confirmar leitura')
  } finally {
    confirmandoLeitura.value = false
  }
}

const visualizarImagem = (imagem) => {
  window.open(imagem, '_blank')
}

const editarRecado = () => {
  router.push(`/recados/editar/${recado.value.id}`)
}

const confirmarDelecao = async () => {
  if (!confirm('Tem certeza que deseja deletar este recado? Esta acao nao pode ser desfeita.')) {
    return
  }
  
  try {
    await recadosService.deletarRecado(recado.value.id)
    notificationStore.success('Recado deletado com sucesso!')
    router.push('/recados')
  } catch (error) {
    console.error('Erro ao deletar recado:', error)
    notificationStore.error(error.response?.data?.message || 'Erro ao deletar recado')
  }
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
