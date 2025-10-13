import api from './api'

export default {
  // Listar todas as turmas
  listarTurmas() {
    return api.get('/turmas')
  },

  // Buscar turma por ID
  buscarTurmaPorId(id) {
    return api.get(`/turmas/${id}`)
  },

  // Criar nova turma
  criarTurma(turma) {
    return api.post('/turmas', turma)
  },

  // Atualizar turma
  atualizarTurma(id, turma) {
    return api.put(`/turmas/${id}`, turma)
  },

  // Deletar turma
  deletarTurma(id) {
    return api.delete(`/turmas/${id}`)
  },

  // Adicionar professor à turma
  adicionarProfessor(turmaId, professorId) {
    return api.post(`/turmas/${turmaId}/professores/${professorId}`)
  },

  // Remover professor da turma
  removerProfessor(turmaId, professorId) {
    return api.delete(`/turmas/${turmaId}/professores/${professorId}`)
  },

  // Adicionar aluno à turma
  adicionarAluno(turmaId, alunoId) {
    return api.post(`/turmas/${turmaId}/alunos/${alunoId}`)
  },

  // Remover aluno da turma
  removerAluno(turmaId, alunoId) {
    return api.delete(`/turmas/${turmaId}/alunos/${alunoId}`)
  }
}

