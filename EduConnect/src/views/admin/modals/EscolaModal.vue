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
                :class="{ 'is-invalid': formData.telefone && !validarTelefone(formData.telefone) }"
                v-model="formData.telefone"
                required
                placeholder="(00) 00000-0000"
                maxlength="15"
                @input="aplicarMascaraTelefone"
              />
              <div v-if="formData.telefone && !validarTelefone(formData.telefone)" class="invalid-feedback d-block">
                <small class="text-danger">Digite um telefone válido (00) 00000-0000 ou (00) 0000-0000</small>
              </div>
            </div>
            <div class="mb-3">
              <label class="form-label">Email</label>
              <input
                type="email"
                class="form-control"
                :class="{ 'is-invalid': formData.email && !validarEmail(formData.email) }"
                v-model="formData.email"
                required
                placeholder="exemplo@email.com"
                @blur="validarEmailEmTempoReal"
              />
              <div v-if="formData.email && !validarEmail(formData.email)" class="invalid-feedback d-block">
                <small class="text-danger">Digite um email válido (ex: exemplo@email.com)</small>
              </div>
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

const aplicarMascaraTelefone = (event) => {
  let valor = event.target.value.replace(/\D/g, '') // Remove tudo que não é dígito
  
  if (valor.length <= 11) {
    // Aplica a máscara: (00) 00000-0000 ou (00) 0000-0000
    if (valor.length <= 10) {
      // Telefone fixo: (00) 0000-0000
      valor = valor.replace(/(\d{2})(\d)/, '($1) $2')
      valor = valor.replace(/(\d{4})(\d)/, '$1-$2')
    } else {
      // Celular: (00) 00000-0000
      valor = valor.replace(/(\d{2})(\d)/, '($1) $2')
      valor = valor.replace(/(\d{5})(\d)/, '$1-$2')
    }
    formData.value.telefone = valor
  }
}

const validarTelefone = (telefone) => {
  if (!telefone) return false
  
  // Remove caracteres não numéricos
  const telefoneLimpo = telefone.replace(/\D/g, '')
  
  // Telefone deve ter 10 ou 11 dígitos (fixo ou celular)
  return telefoneLimpo.length === 10 || telefoneLimpo.length === 11
}

const validarEmail = (email) => {
  if (!email) return false
  
  // Regex para validar email
  const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return regex.test(email)
}

const validarEmailEmTempoReal = () => {
  // Função chamada quando o usuário sai do campo de email
}

const handleSubmit = () => {
  // Validar formato do email
  if (!validarEmail(formData.value.email)) {
    alert('Por favor, digite um email válido')
    return
  }
  
  // Validar formato do telefone
  if (!validarTelefone(formData.value.telefone)) {
    alert('Por favor, digite um telefone válido')
    return
  }
  
  emit('save', formData.value)
}
</script>
