<template>
  <div class="container-fluid py-4">
    <div class="row mb-4">
      <div class="col">
        <button class="btn btn-link text-decoration-none ps-0" @click="router.back()">
          <i class="bi bi-arrow-left me-2"></i>Voltar
        </button>
      </div>
    </div>
    
    <div class="row justify-content-center">
      <div class="col-lg-8">
        <div class="card shadow-sm">
          <div class="card-header bg-white">
            <h4 class="mb-0">
              <i class="bi bi-send me-2"></i>Enviar Novo Recado
            </h4>
          </div>
          
          <div class="card-body">
            <form @submit.prevent="enviarRecado">
              <div class="mb-3">
                <label for="titulo" class="form-label">Título *</label>
                <input
                  type="text"
                  class="form-control"
                  id="titulo"
                  v-model="form.titulo"
                  placeholder="Digite o título do recado"
                  required
                />
              </div>
              
              <div class="row">
                <div class="col-md-6 mb-3">
                  <label for="categoria" class="form-label">Categoria *</label>
                  <select class="form-select" id="categoria" v-model="form.categoria" required>
                    <option value="">Selecione...</option>
                    <option value="geral">Geral</option>
                    <option value="academico">Acadêmico</option>
                    <option value="financeiro">Financeiro</option>
                    <option value="evento">Evento</option>
                  </select>
                </div>
                
                <div class="col-md-6 mb-3">
                  <label for="destinatarios" class="form-label">Destinatários *</label>
                  <select class="form-select" id="destinatarios" v-model="form.destinatarios" required>
                    <option value="">Selecione...</option>
                    <option value="todos">Todos</option>
                    <option value="alunos">Alunos</option>
                    <option value="responsaveis">Responsáveis</option>
                    <option value="professores">Professores</option>
                    <option value="especifico">Específico</option>
                  </select>
                </div>
              </div>
              
              <div class="mb-3">
                <label for="conteudo" class="form-label">Mensagem *</label>
                <textarea
                  class="form-control"
                  id="conteudo"
                  rows="10"
                  v-model="form.conteudo"
                  placeholder="Digite a mensagem do recado..."
                  required
                ></textarea>
              </div>
              
              <div class="mb-3">
                <label class="form-label">Anexos</label>
                <div class="upload-area" @click="triggerFileInput">
                  <input
                    type="file"
                    ref="fileInput"
                    @change="handleFileUpload"
                    multiple
                    hidden
                  />
                  <i class="bi bi-cloud-upload fs-1 text-muted"></i>
                  <p class="mb-0 mt-2">Clique para selecionar arquivos</p>
                  <small class="text-muted">PDF, DOC, XLS, imagens (máx. 10MB cada)</small>
                </div>
                
                <div v-if="form.anexos.length > 0" class="mt-3">
                  <div 
                    v-for="(anexo, index) in form.anexos" 
                    :key="index"
                    class="anexo-preview"
                  >
                    <i class="bi bi-file-earmark me-2"></i>
                    <span>{{ anexo.name }}</span>
                    <small class="text-muted ms-2">({{ formatFileSize(anexo.size) }})</small>
                    <button 
                      type="button" 
                      class="btn btn-sm btn-link text-danger ms-auto"
                      @click="removerAnexo(index)"
                    >
                      <i class="bi bi-x"></i>
                    </button>
                  </div>
                </div>
              </div>
              
              <div class="mb-3 form-check">
                <input
                  type="checkbox"
                  class="form-check-input"
                  id="importante"
                  v-model="form.importante"
                />
                <label class="form-check-label" for="importante">
                  Marcar como importante
                </label>
              </div>
              
              <div class="mb-3 form-check">
                <input
                  type="checkbox"
                  class="form-check-input"
                  id="exigirConfirmacao"
                  v-model="form.exigirConfirmacao"
                />
                <label class="form-check-label" for="exigirConfirmacao">
                  Exigir confirmação de leitura
                </label>
              </div>
              
              <hr>
              
              <div class="d-flex gap-2">
                <button 
                  type="submit" 
                  class="btn btn-primary"
                  :disabled="enviando"
                >
                  <span v-if="enviando">
                    <span class="spinner-border spinner-border-sm me-2"></span>
                    Enviando...
                  </span>
                  <span v-else>
                    <i class="bi bi-send me-2"></i>
                    Enviar Recado
                  </span>
                </button>
                <button 
                  type="button" 
                  class="btn btn-outline-secondary"
                  @click="router.back()"
                >
                  Cancelar
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useNotificationStore } from '../../stores/notifications'

const router = useRouter()
const notificationStore = useNotificationStore()

const form = ref({
  titulo: '',
  categoria: '',
  destinatarios: '',
  conteudo: '',
  anexos: [],
  importante: false,
  exigirConfirmacao: false
})

const fileInput = ref(null)
const enviando = ref(false)

const triggerFileInput = () => {
  fileInput.value.click()
}

const handleFileUpload = (event) => {
  const files = Array.from(event.target.files)
  
  files.forEach(file => {
    if (file.size > 10 * 1024 * 1024) {
      notificationStore.warning(`Arquivo ${file.name} excede 10MB`)
      return
    }
    form.value.anexos.push(file)
  })
}

const removerAnexo = (index) => {
  form.value.anexos.splice(index, 1)
}

const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 Bytes'
  const k = 1024
  const sizes = ['Bytes', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i]
}

const enviarRecado = async () => {
  enviando.value = true
  
  try {
    // Mock - substituir por chamada real da API
    await new Promise(resolve => setTimeout(resolve, 2000))
    
    notificationStore.success('Recado enviado com sucesso!')
    router.push('/recados')
  } catch (error) {
    notificationStore.error('Erro ao enviar recado')
  } finally {
    enviando.value = false
  }
}
</script>

<style scoped>
.upload-area {
  border: 2px dashed #dee2e6;
  border-radius: 8px;
  padding: 2rem;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s;
}

.upload-area:hover {
  border-color: #0d6efd;
  background-color: #f8f9fa;
}

.anexo-preview {
  display: flex;
  align-items: center;
  padding: 0.75rem;
  background-color: #f8f9fa;
  border-radius: 6px;
  margin-bottom: 0.5rem;
}
</style>

