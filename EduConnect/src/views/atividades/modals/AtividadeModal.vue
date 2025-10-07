<template>
  <div class="modal fade show" style="display: block; background: rgba(0,0,0,0.5)">
    <div class="modal-dialog modal-lg">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">{{ mode === 'create' ? 'Nova Atividade' : 'Editar Atividade' }}</h5>
          <button type="button" class="btn-close" @click="$emit('close')"></button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="handleSubmit">
            <div class="row">
              <div class="col-md-8 mb-3">
                <label class="form-label">Título *</label>
                <input
                  type="text"
                  class="form-control"
                  v-model="formData.titulo"
                  required
                  placeholder="Digite o título da atividade"
                />
              </div>
              
              <div class="col-md-4 mb-3">
                <label class="form-label">Valor (pontos) *</label>
                <input
                  type="number"
                  step="0.5"
                  class="form-control"
                  v-model.number="formData.valor"
                  required
                  min="0"
                />
              </div>
            </div>

            <div class="mb-3">
              <label class="form-label">Descrição</label>
              <textarea
                class="form-control"
                v-model="formData.descricao"
                rows="4"
                placeholder="Descreva a atividade..."
              ></textarea>
            </div>

            <div class="row">
              <div class="col-md-4 mb-3">
                <label class="form-label">Disciplina *</label>
                <select class="form-select" v-model="formData.disciplina" required>
                  <option value="">Selecione</option>
                  <option v-for="disc in disciplinasDisponiveis" :key="disc" :value="disc">
                    {{ disc }}
                  </option>
                </select>
              </div>

              <div class="col-md-4 mb-3">
                <label class="form-label">Turma *</label>
                <select class="form-select" v-model="formData.turma" required>
                  <option value="">Selecione</option>
                  <option v-for="turma in turmasDisponiveis" :key="turma" :value="turma">
                    {{ turma }}
                  </option>
                </select>
              </div>

              <div class="col-md-4 mb-3">
                <label class="form-label">Data de Entrega *</label>
                <input
                  type="datetime-local"
                  class="form-control"
                  v-model="formData.dataEntrega"
                  required
                />
              </div>
            </div>

            <div class="row">
              <div class="col-md-6 mb-3">
                <label class="form-label">Peso</label>
                <input
                  type="number"
                  class="form-control"
                  v-model.number="formData.peso"
                  min="1"
                />
                <small class="text-muted">Peso da atividade na média final</small>
              </div>

              <div class="col-md-6 mb-3">
                <label class="form-label">Tentativas Permitidas</label>
                <input
                  type="number"
                  class="form-control"
                  v-model.number="formData.tentativasPermitidas"
                  min="1"
                />
                <small class="text-muted">Número de envios que o aluno pode fazer</small>
              </div>
            </div>
          </form>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" @click="$emit('close')">Cancelar</button>
          <button type="button" class="btn btn-primary" @click="handleSubmit">Salvar</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()

const props = defineProps({
  atividade: {
    type: Object,
    default: null
  },
  mode: {
    type: String,
    required: true,
    validator: value => ['create', 'edit'].includes(value)
  }
})

const emit = defineEmits(['close', 'save'])

const formData = ref({
  titulo: '',
  descricao: '',
  disciplina: '',
  turma: '',
  dataEntrega: '',
  valor: 10.0,
  peso: 1,
  tentativasPermitidas: 1
})

const disciplinasDisponiveis = ref([])
const turmasDisponiveis = ref([])

onMounted(() => {
  // Carregar disciplinas e turmas do professor logado
  const user = authStore.user
  
  if (user.role === 'PROFESSOR') {
    disciplinasDisponiveis.value = user.disciplinas || []
    turmasDisponiveis.value = user.turmas || []
  } else {
    // Para diretores e admins, mostrar opções genéricas
    disciplinasDisponiveis.value = [
      'Matemática', 'Português', 'História', 'Geografia',
      'Ciências', 'Física', 'Química', 'Biologia',
      'Inglês', 'Educação Física', 'Artes'
    ]
    turmasDisponiveis.value = [
      '1A', '1B', '2A', '2B', '3A', '3B',
      '4A', '4B', '5A', '5B', '6A', '6B',
      '7A', '7B', '8A', '8B', '9A', '9B'
    ]
  }

  // Se estiver editando, carregar dados
  if (props.atividade) {
    formData.value = {
      ...props.atividade,
      dataEntrega: formatarDataParaInput(props.atividade.dataEntrega)
    }
  }
})

const formatarDataParaInput = (data) => {
  const date = new Date(data)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day}T${hours}:${minutes}`
}

const handleSubmit = () => {
  // Validações
  if (!formData.value.titulo || !formData.value.disciplina || !formData.value.turma || !formData.value.dataEntrega) {
    alert('Por favor, preencha todos os campos obrigatórios')
    return
  }

  // Converter data para formato ISO
  const dadosParaEnviar = {
    ...formData.value,
    dataEntrega: new Date(formData.value.dataEntrega).toISOString()
  }

  emit('save', dadosParaEnviar)
}
</script>

<style scoped>
.modal {
  z-index: 1050;
}
</style>

