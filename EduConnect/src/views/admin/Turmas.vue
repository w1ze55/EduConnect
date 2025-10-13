<template>
  <div class="container-fluid py-4">
    <!-- Header -->
    <div class="row mb-4">
      <div class="col-md-6">
        <h2 class="fw-bold">
          <i class="bi bi-diagram-3-fill me-2"></i>Gestão de Turmas
        </h2>
      </div>
      <div class="col-md-6 text-end">
        <button 
          v-if="podeGerenciar"
          class="btn btn-primary" 
          @click="abrirModalNovaTurma">
          <i class="bi bi-plus-circle me-2"></i>
          Nova Turma
        </button>
      </div>
    </div>

    <!-- Card com Filtros e Tabela -->
    <div class="card shadow-sm">
      <div class="card-body">
        <!-- Filtros -->
        <div class="row g-3 mb-3">
          <div class="col-md-3">
            <input
              v-model="filtros.busca"
              type="text"
              placeholder="Buscar turma..."
              class="form-control"
            />
          </div>
          <div class="col-md-3">
            <select v-model="filtros.anoLetivo" class="form-select">
              <option value="">Todos os anos</option>
              <option value="2025">2025</option>
              <option value="2024">2024</option>
              <option value="2023">2023</option>
            </select>
          </div>
          <div class="col-md-3">
            <select v-model="filtros.turno" class="form-select">
              <option value="">Todos os turnos</option>
              <option value="MANHÃ">Manhã</option>
              <option value="TARDE">Tarde</option>
              <option value="NOITE">Noite</option>
              <option value="INTEGRAL">Integral</option>
            </select>
          </div>
          <div class="col-md-3">
            <div class="form-check mt-2">
              <input 
                type="checkbox" 
                v-model="filtros.apenasAtivas" 
                class="form-check-input"
                id="apenasAtivas"
              />
              <label class="form-check-label" for="apenasAtivas">
                Apenas ativas
              </label>
            </div>
          </div>
        </div>

        <!-- Loading -->
        <LoadingSpinner v-if="loading" />

        <!-- Tabela -->
        <div v-else class="table-responsive">
          <table class="table table-hover">
            <thead>
              <tr>
                <th>Turma</th>
                <th>Ano/Série</th>
                <th>Turno</th>
                <th>Escola</th>
                <th>Professores</th>
                <th>Alunos</th>
                <th>Status</th>
                <th>Ações</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="turma in turmasFiltradas" :key="turma.id">
                <td>
                  <strong>{{ turma.nome }}</strong>
                  <div v-if="turma.descricao" class="small text-muted">
                    {{ turma.descricao }}
                  </div>
                </td>
                <td>{{ turma.anoLetivo }} - {{ turma.serie }}</td>
                <td>
                  <span class="badge bg-info">{{ turma.turno }}</span>
                </td>
                <td>
                  <span v-if="turma.escolaNome">{{ turma.escolaNome }}</span>
                  <span v-else class="text-muted">-</span>
                </td>
                <td>
                  <span class="badge bg-primary">{{ turma.totalProfessores || 0 }}</span>
                </td>
                <td>
                  <span class="badge bg-success">{{ turma.totalAlunos || 0 }}</span>
                </td>
                <td>
                  <span :class="`badge bg-${turma.ativa ? 'success' : 'secondary'}`">
                    {{ turma.ativa ? 'Ativa' : 'Inativa' }}
                  </span>
                </td>
                <td>
                  <button 
                    class="btn btn-sm btn-outline-info me-1" 
                    @click="verDetalhes(turma)"
                    title="Ver detalhes"
                  >
                    <i class="bi bi-eye"></i>
                  </button>
                  <button 
                    v-if="podeGerenciar"
                    class="btn btn-sm btn-outline-primary me-1" 
                    @click="editarTurma(turma)"
                    title="Editar"
                  >
                    <i class="bi bi-pencil"></i>
                  </button>
                  <button 
                    v-if="podeGerenciar"
                    class="btn btn-sm btn-outline-danger" 
                    @click="confirmarExclusao(turma)"
                    title="Excluir"
                  >
                    <i class="bi bi-trash"></i>
                  </button>
                </td>
              </tr>

              <!-- Empty State -->
              <tr v-if="turmasFiltradas.length === 0">
                <td colspan="8" class="text-center py-5 text-muted">
                  <i class="bi bi-inbox" style="font-size: 3rem; opacity: 0.3;"></i>
                  <p class="mt-2 mb-0">
                    <strong>Nenhuma turma encontrada</strong>
                  </p>
                  <p class="small">
                    {{ filtros.busca ? 'Tente ajustar os filtros' : 'Comece criando uma nova turma' }}
                  </p>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- Modal de Turma -->
    <TurmaModal
      v-if="mostrarModal"
      :turma="turmaSelecionada"
      @close="fecharModal"
      @saved="turmaAtualizada"
    />
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useNotificationStore } from '@/stores/notifications'
import turmasService from '@/services/turmasService'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'
import TurmaModal from './modals/TurmaModal.vue'

export default {
  name: 'Turmas',
  components: {
    LoadingSpinner,
    TurmaModal
  },
  setup() {
    const authStore = useAuthStore()
    const notificationStore = useNotificationStore()

    const turmas = ref([])
    const loading = ref(false)
    const mostrarModal = ref(false)
    const turmaSelecionada = ref(null)

    const filtros = ref({
      busca: '',
      anoLetivo: '',
      turno: '',
      apenasAtivas: false
    })

    const podeGerenciar = computed(() => {
      return ['DIRETORIA', 'ADMINISTRADOR'].includes(authStore.user?.role)
    })

    const turmasFiltradas = computed(() => {
      let resultado = turmas.value

      if (filtros.value.busca) {
        const busca = filtros.value.busca.toLowerCase()
        resultado = resultado.filter(t =>
          t.nome.toLowerCase().includes(busca) ||
          t.serie.toLowerCase().includes(busca)
        )
      }

      if (filtros.value.anoLetivo) {
        resultado = resultado.filter(t => t.anoLetivo === filtros.value.anoLetivo)
      }

      if (filtros.value.turno) {
        resultado = resultado.filter(t => t.turno === filtros.value.turno)
      }

      if (filtros.value.apenasAtivas) {
        resultado = resultado.filter(t => t.ativa)
      }

      return resultado
    })

    const carregarTurmas = async () => {
      loading.value = true
      try {
        const response = await turmasService.listarTurmas()
        turmas.value = response.data
      } catch (error) {
        console.error('Erro ao carregar turmas:', error)
        notificationStore.addNotification('Erro ao carregar turmas', 'error')
      } finally {
        loading.value = false
      }
    }

    const abrirModalNovaTurma = () => {
      turmaSelecionada.value = null
      mostrarModal.value = true
    }

    const editarTurma = (turma) => {
      turmaSelecionada.value = turma
      mostrarModal.value = true
    }

    const verDetalhes = (turma) => {
      turmaSelecionada.value = turma
      mostrarModal.value = true
    }

    const fecharModal = () => {
      mostrarModal.value = false
      turmaSelecionada.value = null
    }

    const turmaAtualizada = () => {
      fecharModal()
      carregarTurmas()
    }

    const confirmarExclusao = async (turma) => {
      if (!confirm(`Tem certeza que deseja excluir a turma "${turma.nome}"?`)) {
        return
      }

      try {
        await turmasService.deletarTurma(turma.id)
        notificationStore.addNotification('Turma excluída com sucesso', 'success')
        carregarTurmas()
      } catch (error) {
        console.error('Erro ao excluir turma:', error)
        notificationStore.addNotification(
          error.response?.data?.message || 'Erro ao excluir turma',
          'error'
        )
      }
    }

    onMounted(() => {
      carregarTurmas()
    })

    return {
      turmas,
      loading,
      mostrarModal,
      turmaSelecionada,
      filtros,
      podeGerenciar,
      turmasFiltradas,
      abrirModalNovaTurma,
      editarTurma,
      verDetalhes,
      fecharModal,
      turmaAtualizada,
      confirmarExclusao
    }
  }
}
</script>


