<template>
  <div class="container-fluid py-4">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h2 class="fw-bold mb-0">
        <i class="bi bi-file-earmark-text-fill me-2"></i>Documentos
      </h2>
      <button 
        v-if="podeCriar"
        class="btn btn-primary"
        @click="abrirModalCriar"
      >
        <i class="bi bi-plus-circle me-2"></i>Novo Documento
      </button>
    </div>
    
    <div v-if="carregando" class="text-center py-5">
      <div class="spinner-border text-primary" role="status">
        <span class="visually-hidden">Carregando...</span>
      </div>
    </div>
    
    <div v-else-if="documentos.length === 0" class="text-center py-5">
      <i class="bi bi-file-earmark-text fs-1 text-muted"></i>
      <p class="text-muted mt-3">Nenhum documento encontrado</p>
    </div>
    
    <div v-else class="row g-4">
      <div v-for="doc in documentos" :key="doc.id" class="col-md-4">
        <div class="card shadow-sm h-100 doc-card">
          <div class="card-body">
            <div class="d-flex align-items-start mb-3">
              <div class="doc-icon me-3">
                <i :class="getDocIcon(doc.tipoArquivo)"></i>
              </div>
              <div class="flex-grow-1">
                <h6 class="mb-1">{{ doc.nome }}</h6>
                <small class="text-muted d-block">{{ formatarData(doc.dataCriacao) }}</small>
                <small class="text-muted d-block">{{ formatarTamanho(doc.tamanhoArquivo) }}</small>
                <small v-if="doc.escolaNome" class="text-muted d-block">
                  <i class="bi bi-building me-1"></i>{{ doc.escolaNome }}
                </small>
              </div>
              <span :class="`badge bg-${doc.assinado ? 'success' : 'warning'}`">
                {{ doc.assinado ? 'Assinado' : 'Pendente' }}
              </span>
            </div>
            
            <div v-if="doc.descricao" class="mb-3">
              <small class="text-muted">{{ doc.descricao }}</small>
            </div>
            
            <div v-if="doc.assinaturas && doc.assinaturas.length > 0" class="mb-3">
              <small class="text-muted d-block">
                <i class="bi bi-pen-fill me-1"></i>
                Assinado por: {{ doc.assinaturas.map(a => a.usuarioNome).join(', ') }}
              </small>
            </div>
            
            <div class="d-flex gap-2 flex-wrap">
              <button 
                class="btn btn-sm btn-outline-primary"
                @click="visualizarDocumento(doc)"
              >
                <i class="bi bi-eye me-1"></i>Ver
              </button>
              <button 
                v-if="doc.requerAssinatura && !doc.assinado && podeAssinar(doc)"
                class="btn btn-sm btn-primary"
                @click="abrirModalAssinatura(doc)"
              >
                <i class="bi bi-pen me-1"></i>Assinar
              </button>
              <button 
                class="btn btn-sm btn-outline-secondary"
                @click="downloadDocumento(doc.id)"
              >
                <i class="bi bi-download"></i>
              </button>
              <button 
                v-if="podeDeletar(doc)"
                class="btn btn-sm btn-outline-danger"
                @click="deletarDocumento(doc.id)"
              >
                <i class="bi bi-trash"></i>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- Modal Criar Documento -->
    <div 
      v-if="mostrarModalCriar"
      class="modal fade show d-block"
      tabindex="-1"
      style="background-color: rgba(0,0,0,0.5);"
      @click.self="fecharModalCriar"
    >
      <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">
              <i class="bi bi-file-earmark-plus me-2"></i>Novo Documento
            </h5>
            <button type="button" class="btn-close" @click="fecharModalCriar"></button>
          </div>
          <div class="modal-body">
            <form @submit.prevent="criarDocumento">
              <div class="mb-3">
                <label class="form-label">Nome do Documento *</label>
                <input
                  type="text"
                  class="form-control"
                  v-model="formCriar.nome"
                  required
                  placeholder="Ex: Contrato de Matrícula 2025"
                />
              </div>
              
              <div class="mb-3">
                <label class="form-label">Descrição</label>
                <textarea
                  class="form-control"
                  v-model="formCriar.descricao"
                  rows="3"
                  placeholder="Descrição do documento (opcional)"
                ></textarea>
              </div>
              
              <div class="mb-3">
                <label class="form-label">Arquivo Principal *</label>
                <input
                  type="file"
                  class="form-control"
                  ref="arquivoInput"
                  @change="handleArquivoChange"
                  accept=".pdf,.doc,.docx,.jpg,.jpeg,.png"
                  required
                />
                <small class="text-muted">PDF, DOC, DOCX, JPG, PNG (máx. 10MB)</small>
              </div>
              
              <div class="mb-3">
                <label class="form-label">Anexos</label>
                <input
                  type="file"
                  class="form-control"
                  ref="anexosInput"
                  @change="handleAnexosChange"
                  multiple
                  accept=".pdf,.doc,.docx,.jpg,.jpeg,.png"
                />
                <small class="text-muted">Você pode selecionar múltiplos arquivos (máx. 10MB cada)</small>
              </div>
              
              <div v-if="anexosSelecionados.length > 0" class="mb-3">
                <label class="form-label">Anexos Selecionados:</label>
                <ul class="list-group">
                  <li 
                    v-for="(anexo, index) in anexosSelecionados" 
                    :key="index"
                    class="list-group-item d-flex justify-content-between align-items-center"
                  >
                    <span>{{ anexo.name }} ({{ formatarTamanho(anexo.size) }})</span>
                    <button 
                      type="button"
                      class="btn btn-sm btn-link text-danger"
                      @click="removerAnexo(index)"
                    >
                      <i class="bi bi-x"></i>
                    </button>
                  </li>
                </ul>
              </div>
              
              <div class="mb-3 form-check">
                <input
                  type="checkbox"
                  class="form-check-input"
                  id="requerAssinatura"
                  v-model="formCriar.requerAssinatura"
                />
                <label class="form-check-label" for="requerAssinatura">
                  Requer assinatura digital
                </label>
              </div>
              
              <div class="d-flex justify-content-end gap-2">
                <button type="button" class="btn btn-secondary" @click="fecharModalCriar">
                  Cancelar
                </button>
                <button type="submit" class="btn btn-primary" :disabled="enviando">
                  <span v-if="enviando" class="spinner-border spinner-border-sm me-2"></span>
                  <i v-else class="bi bi-upload me-2"></i>
                  {{ enviando ? 'Enviando...' : 'Criar Documento' }}
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
    
    <!-- Modal Visualizar/Assinar Documento -->
    <div 
      v-if="mostrarModalVisualizar"
      class="modal fade show d-block"
      tabindex="-1"
      style="background-color: rgba(0,0,0,0.5);"
      @click.self="fecharModalVisualizar"
    >
      <div class="modal-dialog modal-xl">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">
              <i class="bi bi-file-earmark-text me-2"></i>{{ documentoSelecionado?.nome }}
            </h5>
            <button type="button" class="btn-close" @click="fecharModalVisualizar"></button>
          </div>
          <div class="modal-body">
            <div v-if="carregandoDocumento" class="text-center py-5">
              <div class="spinner-border text-primary" role="status">
                <span class="visually-hidden">Carregando...</span>
              </div>
            </div>
            
            <div v-else>
              <div class="mb-3">
                <div class="d-flex justify-content-between align-items-center mb-2">
                  <div>
                    <span :class="`badge bg-${documentoSelecionado?.assinado ? 'success' : 'warning'}`">
                      {{ documentoSelecionado?.assinado ? 'Assinado' : 'Pendente de Assinatura' }}
                    </span>
                  </div>
                  <button 
                    class="btn btn-sm btn-outline-primary"
                    @click="downloadDocumento(documentoSelecionado.id)"
                  >
                    <i class="bi bi-download me-1"></i>Download
                  </button>
                </div>
                
                <div v-if="documentoSelecionado?.descricao" class="mb-3">
                  <p class="text-muted">{{ documentoSelecionado.descricao }}</p>
                </div>
                
                <div v-if="documentoSelecionado?.assinaturas && documentoSelecionado.assinaturas.length > 0" class="mb-3">
                  <h6>Assinaturas:</h6>
                  <div class="row g-2">
                    <div 
                      v-for="assinatura in documentoSelecionado.assinaturas" 
                      :key="assinatura.id"
                      class="col-md-6"
                    >
                      <div class="card">
                        <div class="card-body p-2">
                          <small class="d-block"><strong>{{ assinatura.usuarioNome }}</strong></small>
                          <small class="text-muted">{{ formatarData(assinatura.dataAssinatura) }}</small>
                          <div v-if="assinatura.assinaturaBase64" class="mt-2">
                            <img 
                              :src="`data:image/png;base64,${assinatura.assinaturaBase64}`" 
                              alt="Assinatura"
                              style="max-width: 200px; max-height: 100px; border: 1px solid #ddd;"
                            />
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
                
                <div v-if="documentoSelecionado?.anexos && documentoSelecionado.anexos.length > 0" class="mb-3">
                  <h6>Anexos:</h6>
                  <ul class="list-group">
                    <li 
                      v-for="(nomeAnexo, index) in documentoSelecionado.nomesAnexos" 
                      :key="index"
                      class="list-group-item d-flex justify-content-between align-items-center"
                    >
                      <span>{{ nomeAnexo }}</span>
                      <button 
                        class="btn btn-sm btn-outline-primary"
                        @click="downloadAnexo(documentoSelecionado.id, index)"
                      >
                        <i class="bi bi-download me-1"></i>Download
                      </button>
                    </li>
                  </ul>
                </div>
              </div>
              
              <!-- Visualizador de PDF/Imagem -->
              <div class="border rounded p-3" style="min-height: 500px; max-height: 600px; overflow: auto; background: #f5f5f5;">
                <div v-if="!urlVisualizacao && !erroVisualizacao" class="text-center py-5">
                  <div class="spinner-border text-primary" role="status">
                    <span class="visually-hidden">Carregando...</span>
                  </div>
                </div>
                
                <!-- PDF -->
                <iframe 
                  v-if="urlVisualizacao && podeVisualizarPDF"
                  :src="urlVisualizacao"
                  style="width: 100%; height: 100%; min-height: 500px; border: none;"
                  type="application/pdf"
                ></iframe>
                
                <!-- Imagem -->
                <img 
                  v-else-if="urlVisualizacao && podeVisualizarImagem"
                  :src="urlVisualizacao"
                  style="max-width: 100%; max-height: 600px; display: block; margin: 0 auto;"
                  alt="Visualização do documento"
                />
                
                <!-- DOCX ou outros formatos não visualizáveis -->
                <div v-else-if="urlVisualizacao && !podeVisualizarPDF && !podeVisualizarImagem" class="text-center py-5">
                  <i class="bi bi-file-earmark-text fs-1 text-muted"></i>
                  <p class="text-muted mt-3">
                    Este tipo de arquivo não pode ser visualizado no navegador.
                  </p>
                  <p class="text-muted">
                    Faça o download para visualizar o documento.
                  </p>
                  <button 
                    class="btn btn-primary"
                    @click="downloadDocumento(documentoSelecionado.id)"
                  >
                    <i class="bi bi-download me-2"></i>Download do Documento
                  </button>
                </div>
                
                <!-- Erro -->
                <div v-if="erroVisualizacao" class="text-center py-5">
                  <i class="bi bi-exclamation-triangle fs-1 text-warning"></i>
                  <p class="text-danger mt-3">{{ erroVisualizacao }}</p>
                  <button 
                    class="btn btn-primary"
                    @click="downloadDocumento(documentoSelecionado.id)"
                  >
                    <i class="bi bi-download me-2"></i>Download do Documento
                  </button>
                </div>
              </div>
              
              <!-- Área de Assinatura -->
              <div v-if="documentoSelecionado?.requerAssinatura && !documentoSelecionado.assinado && podeAssinar(documentoSelecionado)" class="mt-4">
                <hr>
                <h6>Assinar Documento</h6>
                <div class="card">
                  <div class="card-body">
                    <div class="mb-3">
                      <label class="form-label">Desenhe sua assinatura:</label>
                      <div class="border rounded p-2" style="background: white;">
                        <canvas
                          ref="canvasAssinatura"
                          width="600"
                          height="200"
                          style="border: 1px solid #ddd; cursor: crosshair; touch-action: none;"
                          @mousedown="iniciarDesenho"
                          @mousemove="desenhar"
                          @mouseup="pararDesenho"
                          @mouseleave="pararDesenho"
                          @touchstart="iniciarDesenhoTouch"
                          @touchmove="desenharTouch"
                          @touchend="pararDesenho"
                        ></canvas>
                      </div>
                      <div class="d-flex gap-2 mt-2">
                        <button 
                          type="button"
                          class="btn btn-sm btn-outline-secondary"
                          @click="limparAssinatura"
                        >
                          <i class="bi bi-eraser me-1"></i>Limpar
                        </button>
                      </div>
                    </div>
                    
                    <div class="mb-3">
                      <label class="form-label">Observações (opcional)</label>
                      <textarea
                        class="form-control"
                        v-model="observacoesAssinatura"
                        rows="2"
                        placeholder="Observações sobre a assinatura..."
                      ></textarea>
                    </div>
                    
                    <button 
                      class="btn btn-primary"
                      @click="assinarDocumento"
                      :disabled="!assinaturaValida || assinando"
                    >
                      <span v-if="assinando" class="spinner-border spinner-border-sm me-2"></span>
                      <i v-else class="bi bi-check-circle me-2"></i>
                      {{ assinando ? 'Assinando...' : 'Assinar Documento' }}
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import { useNotificationStore } from '../../stores/notifications'
import documentosService from '../../services/documentosService'

const router = useRouter()
const authStore = useAuthStore()
const notificationStore = useNotificationStore()

const documentos = ref([])
const carregando = ref(false)
const mostrarModalCriar = ref(false)
const mostrarModalVisualizar = ref(false)
const documentoSelecionado = ref(null)
const carregandoDocumento = ref(false)
const urlVisualizacao = ref(null)
const erroVisualizacao = ref(null)
const enviando = ref(false)
const assinando = ref(false)

const arquivoInput = ref(null)
const anexosInput = ref(null)
const arquivoSelecionado = ref(null)
const anexosSelecionados = ref([])
const observacoesAssinatura = ref('')

const formCriar = ref({
  nome: '',
  descricao: '',
  requerAssinatura: false
})

const podeCriar = computed(() => {
  const role = authStore.user?.role
  return ['PROFESSOR', 'DIRETORIA', 'ADMINISTRADOR'].includes(role)
})

const podeAssinar = (doc) => {
  if (!doc.requerAssinatura || doc.assinado) return false
  // Verificar se já assinou
  if (doc.assinaturas && doc.assinaturas.some(a => a.usuarioId === authStore.user?.id)) {
    return false
  }
  return true
}

const podeDeletar = (doc) => {
  const role = authStore.user?.role
  if (role === 'ADMINISTRADOR') return true
  return doc.criadorId === authStore.user?.id
}

const podeVisualizarPDF = computed(() => {
  if (!documentoSelecionado.value) return false
  const tipo = documentoSelecionado.value.tipoArquivo?.toLowerCase() || ''
  const nomeArquivo = documentoSelecionado.value.nomeArquivo?.toLowerCase() || ''
  return tipo.includes('pdf') || nomeArquivo.endsWith('.pdf')
})

const podeVisualizarImagem = computed(() => {
  if (!documentoSelecionado.value) return false
  const tipo = documentoSelecionado.value.tipoArquivo?.toLowerCase() || ''
  const nomeArquivo = documentoSelecionado.value.nomeArquivo?.toLowerCase() || ''
  return tipo.includes('image') || 
         nomeArquivo.endsWith('.jpg') || 
         nomeArquivo.endsWith('.jpeg') || 
         nomeArquivo.endsWith('.png') || 
         nomeArquivo.endsWith('.gif')
})

// Canvas para assinatura
const canvasAssinatura = ref(null)
const desenhando = ref(false)
const ultimoX = ref(0)
const ultimoY = ref(0)

const iniciarDesenho = (e) => {
  desenhando.value = true
  const rect = canvasAssinatura.value.getBoundingClientRect()
  ultimoX.value = e.clientX - rect.left
  ultimoY.value = e.clientY - rect.top
}

const desenhar = (e) => {
  if (!desenhando.value) return
  
  const canvas = canvasAssinatura.value
  const ctx = canvas.getContext('2d')
  const rect = canvas.getBoundingClientRect()
  const x = e.clientX - rect.left
  const y = e.clientY - rect.top
  
  ctx.strokeStyle = '#000'
  ctx.lineWidth = 2
  ctx.lineCap = 'round'
  ctx.lineJoin = 'round'
  
  ctx.beginPath()
  ctx.moveTo(ultimoX.value, ultimoY.value)
  ctx.lineTo(x, y)
  ctx.stroke()
  
  ultimoX.value = x
  ultimoY.value = y
}

const pararDesenho = () => {
  desenhando.value = false
}

const iniciarDesenhoTouch = (e) => {
  e.preventDefault()
  const touch = e.touches[0]
  const rect = canvasAssinatura.value.getBoundingClientRect()
  ultimoX.value = touch.clientX - rect.left
  ultimoY.value = touch.clientY - rect.top
  desenhando.value = true
}

const desenharTouch = (e) => {
  e.preventDefault()
  if (!desenhando.value) return
  
  const canvas = canvasAssinatura.value
  const ctx = canvas.getContext('2d')
  const rect = canvas.getBoundingClientRect()
  const touch = e.touches[0]
  const x = touch.clientX - rect.left
  const y = touch.clientY - rect.top
  
  ctx.strokeStyle = '#000'
  ctx.lineWidth = 2
  ctx.lineCap = 'round'
  ctx.lineJoin = 'round'
  
  ctx.beginPath()
  ctx.moveTo(ultimoX.value, ultimoY.value)
  ctx.lineTo(x, y)
  ctx.stroke()
  
  ultimoX.value = x
  ultimoY.value = y
}

const limparAssinatura = () => {
  const canvas = canvasAssinatura.value
  const ctx = canvas.getContext('2d')
  ctx.clearRect(0, 0, canvas.width, canvas.height)
}

const assinaturaValida = computed(() => {
  if (!canvasAssinatura.value) return false
  const canvas = canvasAssinatura.value
  const ctx = canvas.getContext('2d')
  const imageData = ctx.getImageData(0, 0, canvas.width, canvas.height)
  const data = imageData.data
  // Verificar se há pixels desenhados (não é apenas branco)
  for (let i = 0; i < data.length; i += 4) {
    if (data[i] !== 255 || data[i + 1] !== 255 || data[i + 2] !== 255) {
      return true
    }
  }
  return false
})

const carregarDocumentos = async () => {
  carregando.value = true
  try {
    const response = await documentosService.getDocumentos()
    documentos.value = response.data || []
  } catch (error) {
    console.error('Erro ao carregar documentos:', error)
    notificationStore.error('Erro ao carregar documentos')
  } finally {
    carregando.value = false
  }
}

const abrirModalCriar = () => {
  mostrarModalCriar.value = true
  formCriar.value = {
    nome: '',
    descricao: '',
    requerAssinatura: false
  }
  arquivoSelecionado.value = null
  anexosSelecionados.value = []
}

const fecharModalCriar = () => {
  mostrarModalCriar.value = false
}

const handleArquivoChange = (e) => {
  const file = e.target.files[0]
  if (file) {
    if (file.size > 10 * 1024 * 1024) {
      notificationStore.error('Arquivo muito grande. Máximo 10MB')
      return
    }
    arquivoSelecionado.value = file
  }
}

const handleAnexosChange = (e) => {
  const files = Array.from(e.target.files)
  files.forEach(file => {
    if (file.size > 10 * 1024 * 1024) {
      notificationStore.error(`Arquivo ${file.name} muito grande. Máximo 10MB`)
      return
    }
    anexosSelecionados.value.push(file)
  })
}

const removerAnexo = (index) => {
  anexosSelecionados.value.splice(index, 1)
}

const criarDocumento = async () => {
  if (!arquivoSelecionado.value) {
    notificationStore.error('Selecione um arquivo principal')
    return
  }
  
  enviando.value = true
  try {
    const formData = new FormData()
    formData.append('nome', formCriar.value.nome)
    if (formCriar.value.descricao) {
      formData.append('descricao', formCriar.value.descricao)
    }
    formData.append('requerAssinatura', formCriar.value.requerAssinatura ? 'true' : 'false')
    formData.append('arquivo', arquivoSelecionado.value)
    
    anexosSelecionados.value.forEach(anexo => {
      formData.append('anexos', anexo)
    })
    
    await documentosService.uploadDocumento(formData)
    notificationStore.success('Documento criado com sucesso!')
    fecharModalCriar()
    carregarDocumentos()
  } catch (error) {
    console.error('Erro ao criar documento:', error)
    notificationStore.error(error.response?.data?.message || 'Erro ao criar documento')
  } finally {
    enviando.value = false
  }
}

const visualizarDocumento = async (doc) => {
  documentoSelecionado.value = doc
  mostrarModalVisualizar.value = true
  carregandoDocumento.value = true
  urlVisualizacao.value = null
  erroVisualizacao.value = null
  
  try {
    // Carregar documento completo
    const response = await documentosService.getDocumentoById(doc.id)
    documentoSelecionado.value = response.data
    
    // Criar URL para visualização apenas se for PDF ou imagem
    const tipoArquivo = documentoSelecionado.value.tipoArquivo?.toLowerCase() || ''
    const nomeArquivo = documentoSelecionado.value.nomeArquivo?.toLowerCase() || ''
    const ehPDF = tipoArquivo.includes('pdf') || nomeArquivo.endsWith('.pdf')
    const ehImagem = tipoArquivo.includes('image') || 
                     nomeArquivo.endsWith('.jpg') || 
                     nomeArquivo.endsWith('.jpeg') || 
                     nomeArquivo.endsWith('.png') || 
                     nomeArquivo.endsWith('.gif')
    
    if (ehPDF || ehImagem) {
      try {
        const response = await documentosService.downloadDocumento(doc.id)
        const blob = response.data
        urlVisualizacao.value = URL.createObjectURL(blob)
      } catch (downloadError) {
        console.error('Erro ao baixar documento para visualização:', downloadError)
        erroVisualizacao.value = 'Erro ao carregar documento para visualização'
      }
    } else {
      // Para DOCX e outros formatos, não tentamos visualizar
      urlVisualizacao.value = null
    }
  } catch (error) {
    console.error('Erro ao carregar documento:', error)
    notificationStore.error('Erro ao carregar documento')
    erroVisualizacao.value = 'Erro ao carregar informações do documento'
  } finally {
    carregandoDocumento.value = false
  }
  
  // Limpar canvas
  if (canvasAssinatura.value) {
    limparAssinatura()
  }
}

const fecharModalVisualizar = () => {
  mostrarModalVisualizar.value = false
  documentoSelecionado.value = null
  if (urlVisualizacao.value) {
    URL.revokeObjectURL(urlVisualizacao.value)
    urlVisualizacao.value = null
  }
  erroVisualizacao.value = null
}

const abrirModalAssinatura = (doc) => {
  visualizarDocumento(doc)
}

const assinarDocumento = async () => {
  if (!assinaturaValida.value) {
    notificationStore.error('Desenhe sua assinatura antes de assinar')
    return
  }
  
  assinando.value = true
  try {
    const canvas = canvasAssinatura.value
    const assinaturaBase64 = canvas.toDataURL('image/png').split(',')[1]
    
    await documentosService.assinarDocumento(documentoSelecionado.value.id, {
      assinatura: assinaturaBase64,
      observacoes: observacoesAssinatura.value
    })
    
    notificationStore.success('Documento assinado com sucesso!')
    await visualizarDocumento(documentoSelecionado.value)
  } catch (error) {
    console.error('Erro ao assinar documento:', error)
    notificationStore.error(error.response?.data?.message || 'Erro ao assinar documento')
  } finally {
    assinando.value = false
  }
}

const downloadDocumento = async (id) => {
  try {
    const response = await documentosService.downloadDocumento(id)
    const blob = response.data
    const documento = documentos.value.find(d => d.id === id) || documentoSelecionado.value
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = documento?.nomeArquivo || 'documento'
    document.body.appendChild(a)
    a.click()
    window.URL.revokeObjectURL(url)
    document.body.removeChild(a)
  } catch (error) {
    console.error('Erro ao baixar documento:', error)
    notificationStore.error('Erro ao baixar documento')
  }
}

const downloadAnexo = async (documentoId, indice) => {
  try {
    const response = await documentosService.downloadAnexo(documentoId, indice)
    const blob = response.data
    const documento = documentoSelecionado.value
    const nomeAnexo = documento.nomesAnexos[indice]
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = nomeAnexo
    document.body.appendChild(a)
    a.click()
    window.URL.revokeObjectURL(url)
    document.body.removeChild(a)
  } catch (error) {
    console.error('Erro ao baixar anexo:', error)
    notificationStore.error('Erro ao baixar anexo')
  }
}

const deletarDocumento = async (id) => {
  if (!confirm('Tem certeza que deseja deletar este documento?')) {
    return
  }
  
  try {
    await documentosService.deletarDocumento(id)
    notificationStore.success('Documento deletado com sucesso!')
    carregarDocumentos()
  } catch (error) {
    console.error('Erro ao deletar documento:', error)
    notificationStore.error(error.response?.data?.message || 'Erro ao deletar documento')
  }
}

const getDocIcon = (tipoArquivo) => {
  if (!tipoArquivo) return 'bi bi-file-earmark-fill fs-2'
  if (tipoArquivo.includes('pdf')) return 'bi bi-file-pdf-fill text-danger fs-2'
  if (tipoArquivo.includes('word') || tipoArquivo.includes('document')) return 'bi bi-file-word-fill text-primary fs-2'
  if (tipoArquivo.includes('image')) return 'bi bi-file-image-fill text-success fs-2'
  return 'bi bi-file-earmark-fill fs-2'
}

const formatarData = (data) => {
  if (!data) return ''
  const date = new Date(data)
  return date.toLocaleDateString('pt-BR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const formatarTamanho = (bytes) => {
  if (!bytes) return '0 Bytes'
  const k = 1024
  const sizes = ['Bytes', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i]
}

onMounted(() => {
  carregarDocumentos()
})
</script>

<style scoped>
.doc-card {
  cursor: pointer;
  transition: all 0.2s;
}

.doc-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 0.5rem 1rem rgba(0, 0, 0, 0.15) !important;
}

.doc-icon {
  width: 50px;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
