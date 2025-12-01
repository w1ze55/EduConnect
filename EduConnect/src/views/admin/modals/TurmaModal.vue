<template>
  <div class="modal fade show" style="display: block; background: rgba(0,0,0,0.5)" @click.self="fechar">
    <div class="modal-dialog modal-lg">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">{{ readonly ? 'Detalhes da Turma' : (turma ? 'Editar Turma' : 'Nova Turma') }}</h5>
          <button type="button" class="btn-close" @click="fechar"></button>
        </div>

        <div class="modal-body">
          <LoadingSpinner v-if="loading" />

          <form v-else @submit.prevent="salvar">
            <!-- Nav Tabs -->
            <ul class="nav nav-tabs mb-3">
              <li class="nav-item">
                <button
                  type="button"
                  class="nav-link"
                  :class="{ active: abaAtiva === 'dados' }"
                  @click="abaAtiva = 'dados'"
                >
                  <i class="bi bi-info-circle me-2"></i>
                  Dados Básicos
                </button>
              </li>
              <li class="nav-item">
                <button
                  type="button"
                  class="nav-link"
                  :class="{ active: abaAtiva === 'professores' }"
                  @click="abaAtiva = 'professores'"
                >
                  <i class="bi bi-person-badge me-2"></i>
                  Professores
                  <span class="badge bg-primary ms-2">{{ form.professoresIds.length }}</span>
                </button>
              </li>
              <li class="nav-item">
                <button
                  type="button"
                  class="nav-link"
                  :class="{ active: abaAtiva === 'alunos' }"
                  @click="abaAtiva = 'alunos'"
                >
                  <i class="bi bi-people me-2"></i>
                  Alunos
                  <span class="badge bg-success ms-2">{{ form.alunosIds.length }}</span>
                </button>
              </li>
            </ul>

            <!-- Tab: Dados Básicos -->
            <div v-show="abaAtiva === 'dados'">
              <div class="row g-3">
                <div class="col-md-8">
                  <label class="form-label">Nome da Turma *</label>
                  <input
                    v-model="form.nome"
                    type="text"
                    class="form-control"
                    placeholder="Ex: 3º Ano A"
                    :disabled="readonly"
                    required
                  />
                </div>

                <div class="col-md-4">
                  <label class="form-label">Ano Letivo *</label>
                  <input
                    v-model="form.anoLetivo"
                    type="text"
                    class="form-control"
                    placeholder="Ex: 2025"
                    :disabled="readonly"
                    required
                  />
                </div>

                <div class="col-md-6">
                  <label class="form-label">Série *</label>
                  <select v-model="form.serie" class="form-select" :disabled="readonly" required>
                    <option value="">Selecione...</option>
                    <option value="1º Ano">1º Ano</option>
                    <option value="2º Ano">2º Ano</option>
                    <option value="3º Ano">3º Ano</option>
                    <option value="4º Ano">4º Ano</option>
                    <option value="5º Ano">5º Ano</option>
                    <option value="6º Ano">6º Ano</option>
                    <option value="7º Ano">7º Ano</option>
                    <option value="8º Ano">8º Ano</option>
                    <option value="9º Ano">9º Ano</option>
                    <option value="Ensino Médio 1º">Ensino Médio 1º</option>
                    <option value="Ensino Médio 2º">Ensino Médio 2º</option>
                    <option value="Ensino Médio 3º">Ensino Médio 3º</option>
                  </select>
                </div>

                <div class="col-md-6">
                  <label class="form-label">Turno *</label>
                  <select v-model="form.turno" class="form-select" :disabled="readonly" required>
                    <option value="">Selecione...</option>
                    <option value="MANHÃ">Manhã</option>
                    <option value="TARDE">Tarde</option>
                    <option value="NOITE">Noite</option>
                    <option value="INTEGRAL">Integral</option>
                  </select>
                </div>

                <div class="col-12" v-if="isAdmin">
                  <label class="form-label">Escola</label>
                  <select v-model="form.escolaId" class="form-select" :disabled="readonly">
                    <option value="">Selecione...</option>
                    <option
                      v-for="escola in escolas"
                      :key="escola.id"
                      :value="escola.id"
                    >
                      {{ escola.nome }}
                    </option>
                  </select>
                </div>

                <div class="col-12">
                  <label class="form-label">Descrição</label>
                  <textarea
                    v-model="form.descricao"
                    class="form-control"
                    rows="3"
                    placeholder="Informações adicionais sobre a turma..."
                    :disabled="readonly"
                  ></textarea>
                </div>

                <div class="col-12">
                  <div class="form-check">
                    <input 
                      type="checkbox" 
                      v-model="form.ativa" 
                      class="form-check-input"
                      id="turmaAtiva"
                      :disabled="readonly"
                    />
                    <label class="form-check-label" for="turmaAtiva">
                      Turma ativa
                    </label>
                  </div>
                </div>
              </div>
            </div>

            <!-- Tab: Professores -->
            <div v-show="abaAtiva === 'professores'">
              <div class="mb-3">
                <input
                  v-model="buscaProfessor"
                  type="text"
                  placeholder="Buscar professor..."
                  class="form-control"
                  :disabled="readonly"
                />
              </div>

              <div class="border rounded p-2 bg-light" style="max-height: 400px; overflow-y: auto;">
                <div
                  v-for="professor in professoresFiltrados"
                  :key="professor.id"
                  class="p-2 mb-2 bg-white rounded border-start border-3 border-primary usuario-item"
                  :class="{ 'usuario-selecionado': form.professoresIds.includes(professor.id) }"
                >
                  <div class="form-check">
                    <input
                      type="checkbox"
                      :value="professor.id"
                      v-model="form.professoresIds"
                      class="form-check-input"
                      :id="`prof-${professor.id}`"
                      :disabled="readonly"
                    />
                    <label class="form-check-label w-100 cursor-pointer" :for="`prof-${professor.id}`">
                      <div class="d-flex align-items-center">
                        <div class="flex-grow-1">
                          <strong class="d-block">{{ professor.nome }}</strong>
                          <small class="text-muted">
                            <i class="bi bi-envelope me-1"></i>{{ professor.email }}
                          </small>
                          <span v-if="professor.telefone" class="ms-2">
                            <small class="text-muted">
                              <i class="bi bi-telephone me-1"></i>{{ professor.telefone }}
                            </small>
                          </span>
                        </div>
                        <i v-if="form.professoresIds.includes(professor.id)" class="bi bi-check-circle-fill text-primary fs-5"></i>
                      </div>
                    </label>
                  </div>
                </div>

                <div v-if="professoresFiltrados.length === 0" class="text-center text-muted py-4">
                  <i class="bi bi-search" style="font-size: 2rem; opacity: 0.3;"></i>
                  <p class="mt-2 mb-0">Nenhum professor encontrado</p>
                </div>
              </div>
            </div>

            <!-- Tab: Alunos -->
            <div v-show="abaAtiva === 'alunos'">
              <div class="mb-3">
                <input
                  v-model="buscaAluno"
                  type="text"
                  placeholder="Buscar aluno..."
                  class="form-control"
                  :disabled="readonly"
                />
              </div>

              <div class="border rounded p-2 bg-light" style="max-height: 400px; overflow-y: auto;">
                <div
                  v-for="aluno in alunosFiltrados"
                  :key="aluno.id"
                  class="p-2 mb-2 bg-white rounded border-start border-3 border-success usuario-item"
                  :class="{ 'usuario-selecionado': form.alunosIds.includes(aluno.id) }"
                >
                  <div class="form-check">
                    <input
                      type="checkbox"
                      :value="aluno.id"
                      v-model="form.alunosIds"
                      class="form-check-input"
                      :id="`aluno-${aluno.id}`"
                      :disabled="readonly"
                    />
                    <label class="form-check-label w-100 cursor-pointer" :for="`aluno-${aluno.id}`">
                      <div class="d-flex align-items-center">
                        <div class="flex-grow-1">
                          <strong class="d-block">{{ aluno.nome }}</strong>
                          <small class="text-muted">
                            <i class="bi bi-envelope me-1"></i>{{ aluno.email }}
                            <span v-if="aluno.matricula" class="badge bg-info ms-2">
                              Mat: {{ aluno.matricula }}
                            </span>
                          </small>
                          <div v-if="aluno.responsavel">
                            <small class="text-muted">
                              <i class="bi bi-person-fill me-1"></i>
                              Responsável: {{ aluno.responsavel.nome }}
                            </small>
                          </div>
                        </div>
                        <i v-if="form.alunosIds.includes(aluno.id)" class="bi bi-check-circle-fill text-success fs-5"></i>
                      </div>
                    </label>
                  </div>
                </div>

                <div v-if="alunosFiltrados.length === 0" class="text-center text-muted py-4">
                  <i class="bi bi-search" style="font-size: 2rem; opacity: 0.3;"></i>
                  <p class="mt-2 mb-0">Nenhum aluno encontrado</p>
                </div>
              </div>
            </div>
          </form>
        </div>

        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" @click="fechar">
            {{ readonly ? 'Fechar' : 'Cancelar' }}
          </button>
          <button
            v-if="!readonly"
            type="button"
            class="btn btn-primary"
            @click="salvar"
            :disabled="salvando"
          >
            <span v-if="salvando" class="spinner-border spinner-border-sm me-2"></span>
            <i v-else class="bi bi-check-circle me-2"></i>
            {{ salvando ? 'Salvando...' : 'Salvar' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useNotificationStore } from '@/stores/notifications'
import turmasService from '@/services/turmasService'
import usuariosService from '@/services/usuariosService'
import escolasService from '@/services/escolasService'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'

export default {
  name: 'TurmaModal',
  components: {
    LoadingSpinner
  },
  props: {
    turma: {
      type: Object,
      default: null
    },
    readonly: {
      type: Boolean,
      default: false
    }
  },
  emits: ['close', 'saved'],
  setup(props, { emit }) {
    const authStore = useAuthStore()
    const notificationStore = useNotificationStore()

    const loading = ref(false)
    const salvando = ref(false)
    const abaAtiva = ref('dados')

    const form = ref({
      nome: '',
      anoLetivo: new Date().getFullYear().toString(),
      serie: '',
      turno: '',
      descricao: '',
      ativa: true,
      escolaId: null,
      professoresIds: [],
      alunosIds: []
    })

    const professores = ref([])
    const alunos = ref([])
    const escolas = ref([])

    const buscaProfessor = ref('')
    const buscaAluno = ref('')

    const isAdmin = computed(() => authStore.user?.role === 'ADMINISTRADOR')

    const professoresFiltrados = computed(() => {
      if (!buscaProfessor.value) return professores.value
      const busca = buscaProfessor.value.toLowerCase()
      return professores.value.filter(p =>
        p.nome.toLowerCase().includes(busca) ||
        p.email.toLowerCase().includes(busca)
      )
    })

    const alunosFiltrados = computed(() => {
      if (!buscaAluno.value) return alunos.value
      const busca = buscaAluno.value.toLowerCase()
      return alunos.value.filter(a =>
        a.nome.toLowerCase().includes(busca) ||
        a.email.toLowerCase().includes(busca) ||
        (a.matricula && a.matricula.toLowerCase().includes(busca))
      )
    })

    const carregarDados = async () => {
      loading.value = true
      try {
        // Carregar usuários
        const usuariosResponse = await usuariosService.getAllUsuarios()
        const usuarios = usuariosResponse

        // Filtrar professores e alunos
        professores.value = usuarios.filter(u => u.role === 'PROFESSOR')
        
        // Buscar alunos com responsáveis
        alunos.value = usuarios
          .filter(u => u.role === 'ALUNO')
          .map(aluno => {
            // Encontrar responsável
            const responsavel = usuarios.find(u => u.id === aluno.responsavelId)
            return {
              ...aluno,
              responsavel: responsavel
            }
          })

        // Carregar escolas se for admin
        if (isAdmin.value) {
          escolas.value = await escolasService.listarTodas()
        }

        // Se for edição, carregar dados da turma
        if (props.turma) {
          const turmaDetalhada = await turmasService.buscarTurmaPorId(props.turma.id)
          const turmaData = turmaDetalhada.data

          form.value = {
            nome: turmaData.nome,
            anoLetivo: turmaData.anoLetivo,
            serie: turmaData.serie,
            turno: turmaData.turno,
            descricao: turmaData.descricao || '',
            ativa: turmaData.ativa,
            escolaId: turmaData.escolaId,
            professoresIds: turmaData.professores.map(p => p.id),
            alunosIds: turmaData.alunos.map(a => a.id)
          }
        }
      } catch (error) {
        console.error('Erro ao carregar dados:', error)
        notificationStore.addNotification('Erro ao carregar dados', 'error')
      } finally {
        loading.value = false
      }
    }

    const salvar = async () => {
      if (salvando.value) return

      // Validação básica
      if (!form.value.nome || !form.value.anoLetivo || !form.value.serie || !form.value.turno) {
        notificationStore.addNotification('Preencha todos os campos obrigatórios', 'error')
        abaAtiva.value = 'dados'
        return
      }

      salvando.value = true
      try {
        if (props.turma) {
          await turmasService.atualizarTurma(props.turma.id, form.value)
          notificationStore.addNotification('Turma atualizada com sucesso', 'success')
        } else {
          await turmasService.criarTurma(form.value)
          notificationStore.addNotification('Turma criada com sucesso', 'success')
        }
        emit('saved')
      } catch (error) {
        console.error('Erro ao salvar turma:', error)
        notificationStore.addNotification(
          error.response?.data?.message || 'Erro ao salvar turma',
          'error'
        )
      } finally {
        salvando.value = false
      }
    }

    const fechar = () => {
      emit('close')
    }

    onMounted(() => {
      carregarDados()
    })

    return {
      loading,
      salvando,
      abaAtiva,
      form,
      professores,
      alunos,
      escolas,
      buscaProfessor,
      buscaAluno,
      isAdmin,
      professoresFiltrados,
      alunosFiltrados,
      readonly: computed(() => props.readonly),
      salvar,
      fechar
    }
  }
}
</script>

<style scoped>
.nav-link {
  cursor: pointer;
  border: none;
  background: none;
}

.cursor-pointer {
  cursor: pointer;
}

.usuario-item {
  transition: all 0.2s ease;
  border-left-width: 3px !important;
}

.usuario-item:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transform: translateX(4px);
}

.usuario-selecionado {
  background-color: #f8f9fa !important;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.usuario-selecionado.border-primary {
  border-left-color: #0d6efd !important;
  border-left-width: 4px !important;
}

.usuario-selecionado.border-success {
  border-left-color: #198754 !important;
  border-left-width: 4px !important;
}

.form-check-input:checked {
  background-color: #0d6efd;
  border-color: #0d6efd;
}
</style>

