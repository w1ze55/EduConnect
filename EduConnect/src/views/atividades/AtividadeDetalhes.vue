<template>
  <div class="container-fluid py-4">
    <button class="btn btn-link ps-0 mb-4" @click="router.back()">
      <i class="bi bi-arrow-left me-2"></i>Voltar
    </button>
    
    <div class="row">
      <div class="col-lg-8">
        <div class="card shadow-sm mb-4">
          <div class="card-header bg-white">
            <div class="d-flex justify-content-between align-items-start">
              <div>
                <h3>{{ atividade.titulo }}</h3>
                <span class="badge bg-primary">{{ atividade.disciplina }}</span>
              </div>
              <span :class="`badge bg-${getStatusColor(atividade.status)}`">
                {{ atividade.status }}
              </span>
            </div>
          </div>
          <div class="card-body">
            <div class="mb-4">
              <h6>Descrição</h6>
              <p>{{ atividade.descricao }}</p>
            </div>
            
            <div class="row mb-4">
              <div class="col-md-6">
                <small class="text-muted d-block">Data de entrega</small>
                <strong><i class="bi bi-calendar me-2"></i>{{ atividade.dataEntrega }}</strong>
              </div>
              <div class="col-md-6">
                <small class="text-muted d-block">Professor</small>
                <strong><i class="bi bi-person me-2"></i>{{ atividade.professor }}</strong>
              </div>
            </div>
            
            <div v-if="atividade.anexos.length > 0">
              <h6>Anexos do Professor</h6>
              <div class="list-group">
                <a v-for="anexo in atividade.anexos" :key="anexo.id" href="#" class="list-group-item list-group-item-action">
                  <i class="bi bi-file-pdf me-2"></i>{{ anexo.nome }}
                </a>
              </div>
            </div>
          </div>
        </div>
        
        <div v-if="atividade.status === 'pendente'" class="card shadow-sm">
          <div class="card-header bg-white">
            <h5 class="mb-0">Enviar Resposta</h5>
          </div>
          <div class="card-body">
            <form @submit.prevent="enviarResposta">
              <div class="mb-3">
                <label class="form-label">Comentário</label>
                <textarea class="form-control" rows="4" v-model="resposta.comentario" placeholder="Adicione um comentário..."></textarea>
              </div>
              <div class="mb-3">
                <label class="form-label">Anexar arquivo</label>
                <input type="file" class="form-control" @change="handleFileUpload" multiple />
              </div>
              <button type="submit" class="btn btn-primary" :disabled="enviando">
                <span v-if="enviando"><span class="spinner-border spinner-border-sm me-2"></span>Enviando...</span>
                <span v-else><i class="bi bi-send me-2"></i>Enviar Atividade</span>
              </button>
            </form>
          </div>
        </div>
        
        <div v-else-if="atividade.status === 'entregue'" class="card shadow-sm">
          <div class="card-header bg-white">
            <h5 class="mb-0">Resposta Enviada</h5>
          </div>
          <div class="card-body">
            <div class="alert alert-success">
              <i class="bi bi-check-circle me-2"></i>
              Atividade entregue em {{ atividade.dataEnvio }}
            </div>
            <p><strong>Comentário:</strong> {{ atividade.respostaAluno }}</p>
            <div v-if="atividade.nota">
              <h4 class="text-primary">Nota: {{ atividade.nota }}</h4>
              <p><strong>Feedback do professor:</strong> {{ atividade.feedback }}</p>
            </div>
          </div>
        </div>
      </div>
      
      <div class="col-lg-4">
        <div class="card shadow-sm">
          <div class="card-header bg-white">
            <h6 class="mb-0">Informações</h6>
          </div>
          <div class="card-body">
            <div class="mb-3">
              <small class="text-muted">Valor</small>
              <h5>{{ atividade.valor }} pontos</h5>
            </div>
            <div class="mb-3">
              <small class="text-muted">Peso</small>
              <h5>{{ atividade.peso }}</h5>
            </div>
            <div>
              <small class="text-muted">Tentativas permitidas</small>
              <h5>{{ atividade.tentativas }}</h5>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useNotificationStore } from '../../stores/notifications'

const router = useRouter()
const route = useRoute()
const notificationStore = useNotificationStore()

const atividade = ref({
  id: route.params.id,
  titulo: '',
  disciplina: '',
  descricao: '',
  dataEntrega: '',
  professor: '',
  status: 'pendente',
  valor: 0,
  peso: 0,
  tentativas: 0,
  anexos: [],
  nota: null,
  dataEnvio: null,
  respostaAluno: null,
  feedback: null
})

// TODO: Carregar dados da atividade do backend baseado no ID

const resposta = ref({
  comentario: '',
  arquivos: []
})

const enviando = ref(false)

const getStatusColor = (status) => {
  const colors = { pendente: 'warning', entregue: 'success', atrasada: 'danger' }
  return colors[status] || 'secondary'
}

const handleFileUpload = (event) => {
  resposta.value.arquivos = Array.from(event.target.files)
}

const enviarResposta = async () => {
  enviando.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 1500))
    notificationStore.success('Atividade enviada com sucesso!')
    router.push('/atividades')
  } catch (error) {
    notificationStore.error('Erro ao enviar atividade')
  } finally {
    enviando.value = false
  }
}
</script>

