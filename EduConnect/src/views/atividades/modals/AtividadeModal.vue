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
                  <option v-for="turma in turmasDisponiveis" :key="turma.id" :value="turma.nome">
                    {{ turma.nome }} - {{ turma.anoLetivo || turma.ano }}
                  </option>
                </select>
                <small v-if="turmasDisponiveis.length === 0" class="text-muted">
                  Nenhuma turma cadastrada. Cadastre uma turma primeiro.
                </small>
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
import turmasService from '@/services/turmasService'

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
const carregandoTurmas = ref(false)

const carregarTurmas = async () => {
  carregandoTurmas.value = true
  try {
    console.log('🔄 Carregando turmas do banco...')
    const response = await turmasService.listarTurmas()
    let turmas = response.data || []
    
    console.log('📋 Turmas recebidas do backend:', turmas)
    
    // O backend já filtra corretamente:
    // - ADMINISTRADOR: todas as turmas
    // - DIRETORIA: turmas da sua escola
    // - PROFESSOR: turmas da sua escola (agora ajustado)
    
    // Garantir que apenas turmas ativas sejam mostradas (se aplicável)
    turmas = turmas.filter(t => t.ativa !== false)
    
    turmasDisponiveis.value = turmas
    console.log('✅ Turmas disponíveis após filtro:', turmasDisponiveis.value)
  } catch (error) {
    console.error('❌ Erro ao carregar turmas:', error)
    turmasDisponiveis.value = []
  } finally {
    carregandoTurmas.value = false
  }
}

onMounted(async () => {
  // Carregar disciplinas do professor logado
  const user = authStore.user
  
  if (user.role === 'PROFESSOR') {
    disciplinasDisponiveis.value = user.disciplinas || []
  } else {
    // Para diretores e admins, mostrar opções genéricas
    disciplinasDisponiveis.value = [
      'Matemática', 'Português', 'História', 'Geografia',
      'Ciências', 'Física', 'Química', 'Biologia',
      'Inglês', 'Educação Física', 'Artes'
    ]
  }

  // Carregar turmas do banco de dados
  await carregarTurmas()

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

