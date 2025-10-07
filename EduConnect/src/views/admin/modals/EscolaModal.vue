<template>
  <div class="modal fade show" style="display: block; background: rgba(0,0,0,0.5)">
    <div class="modal-dialog">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">{{ mode === 'create' ? 'Nova Escola' : 'Editar Escola' }}</h5>
          <button type="button" class="btn-close" @click="$emit('close')"></button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="handleSubmit">
            <div class="mb-3">
              <label class="form-label">Nome da Escola</label>
              <input
                type="text"
                class="form-control"
                v-model="formData.nome"
                required
                placeholder="Digite o nome da escola"
              />
            </div>
            <div class="mb-3">
              <label class="form-label">Endereço</label>
              <input
                type="text"
                class="form-control"
                v-model="formData.endereco"
                required
                placeholder="Digite o endereço da escola"
              />
            </div>
            <div class="mb-3">
              <label class="form-label">Telefone</label>
              <input
                type="tel"
                class="form-control"
                v-model="formData.telefone"
                required
                placeholder="Digite o telefone da escola"
              />
            </div>
            <div class="mb-3">
              <label class="form-label">Email</label>
              <input
                type="email"
                class="form-control"
                v-model="formData.email"
                required
                placeholder="Digite o email da escola"
              />
            </div>
            <div class="mb-3">
              <div class="form-check">
                <input
                  type="checkbox"
                  class="form-check-input"
                  v-model="formData.ativo"
                  id="escolaAtivo"
                />
                <label class="form-check-label" for="escolaAtivo">Escola Ativa</label>
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
  nome: '',
  endereco: '',
  telefone: '',
  email: '',
  ativo: true
})

onMounted(() => {
  if (props.escola) {
    formData.value = { ...props.escola }
  }
})

const handleSubmit = () => {
  emit('save', formData.value)
}
</script>
