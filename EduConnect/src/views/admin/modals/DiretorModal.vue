<template>
  <div class="modal fade show" style="display: block; background: rgba(0,0,0,0.5)">
    <div class="modal-dialog">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">Atribuir Diretor Principal</h5>
          <button type="button" class="btn-close" @click="$emit('close')"></button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="handleSubmit">
            <div class="mb-3">
              <p class="fw-bold mb-2">Escola: {{ escola.nome }}</p>
            </div>
            
            <div v-if="!criarNovo" class="mb-3">
              <label class="form-label">Selecionar Diretor Existente</label>
              <select class="form-select" v-model.number="selectedDiretorId">
                <option value="">Selecione um diretor</option>
                <option v-for="diretor in diretoresDisponiveis" :key="diretor.id" :value="diretor.id">
                  {{ diretor.nome }} ({{ diretor.email }})
                </option>
              </select>
              <div class="mt-2">
                <button type="button" class="btn btn-link ps-0" @click="criarNovo = true">
                  Criar novo diretor
                </button>
              </div>
            </div>

            <div v-else>
              <div class="mb-3">
                <label class="form-label">Nome Completo</label>
                <input
                  type="text"
                  class="form-control"
                  v-model="formData.nome"
                  required
                  placeholder="Digite o nome completo"
                />
              </div>
              <div class="mb-3">
                <label class="form-label">Email</label>
                <input
                  type="email"
                  class="form-control"
                  v-model="formData.email"
                  required
                  placeholder="Digite o email"
                />
              </div>
              <div class="mb-3">
                <label class="form-label">CPF</label>
                <input
                  type="text"
                  class="form-control"
                  v-model="formData.cpf"
                  required
                  placeholder="Digite o CPF"
                />
              </div>
              <div class="mb-3">
                <label class="form-label">Telefone</label>
                <input
                  type="tel"
                  class="form-control"
                  v-model="formData.telefone"
                  required
                  placeholder="Digite o telefone"
                />
              </div>
              <div class="mb-3">
                <label class="form-label">Senha</label>
                <input
                  type="password"
                  class="form-control"
                  v-model="formData.senha"
                  required
                  placeholder="Digite a senha"
                />
              </div>
              <div class="mb-3">
                <button type="button" class="btn btn-link ps-0" @click="criarNovo = false">
                  Voltar para seleção de diretor existente
                </button>
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

const props = defineProps({
  escola: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['close', 'save'])

const criarNovo = ref(false)
const selectedDiretorId = ref('')
const diretoresDisponiveis = ref([])

const formData = ref({
  nome: '',
  email: '',
  cpf: '',
  telefone: '',
  senha: ''
})

onMounted(async () => {
  try {
    const response = await fetch('/api/usuarios/diretores-disponiveis', {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
    if (response.ok) {
      diretoresDisponiveis.value = await response.json()
    }
  } catch (error) {
    console.error('Erro ao carregar diretores:', error)
  }
})

const handleSubmit = () => {
  if (criarNovo.value) {
    // Validar campos do formulário
    if (!formData.value.nome || !formData.value.email || !formData.value.cpf || 
        !formData.value.telefone || !formData.value.senha) {
      alert('Por favor, preencha todos os campos')
      return
    }
    
    emit('save', {
      tipo: 'novo',
      dados: {
        ...formData.value,
        escolaId: props.escola.id
      }
    })
  } else {
    // Validar se um diretor foi selecionado
    if (!selectedDiretorId.value) {
      alert('Por favor, selecione um diretor')
      return
    }
    
    emit('save', {
      tipo: 'existente',
      dados: {
        diretorId: selectedDiretorId.value,
        escolaId: props.escola.id
      }
    })
  }
}
</script>
