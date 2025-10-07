// Usuários mock para testes
export const mockUsers = [
  // ADMINISTRADOR
  {
    id: 1,
    nome: 'Carlos Administrador',
    email: 'admin@educonnect.com',
    password: 'admin123',
    cpf: '111.111.111-11',
    telefone: '(11) 98888-8888',
    role: 'ADMINISTRADOR',
    ativo: true,
    dataCadastro: '2024-01-15',
    foto: null
  },
  
  // DIRETORIA
  {
    id: 8,
    nome: 'Patricia Direção',
    email: 'diretoria@educonnect.com',
    password: 'dir123',
    cpf: '888.888.888-88',
    telefone: '(11) 98999-9999',
    role: 'DIRETORIA',
    ativo: true,
    dataCadastro: '2024-01-20',
    foto: null
  },
  
  // PROFESSORES
  {
    id: 2,
    nome: 'Maria Silva Santos',
    email: 'professor@educonnect.com',
    password: 'prof123',
    cpf: '222.222.222-22',
    telefone: '(11) 98777-7777',
    role: 'PROFESSOR',
    ativo: true,
    dataCadastro: '2024-02-10',
    disciplinas: ['Matemática', 'Física'],
    turmas: ['9º A', '9º B', '1º Ano']
  },
  {
    id: 3,
    nome: 'João Pedro Oliveira',
    email: 'joao.professor@educonnect.com',
    password: '123456',
    cpf: '333.333.333-33',
    telefone: '(11) 98666-6666',
    role: 'PROFESSOR',
    ativo: true,
    dataCadastro: '2024-02-15',
    disciplinas: ['Português', 'Literatura'],
    turmas: ['8º A', '9º A']
  },
  
  // ALUNOS
  {
    id: 4,
    nome: 'Ana Carolina Souza',
    email: 'aluno@educonnect.com',
    password: 'aluno123',
    cpf: '444.444.444-44',
    telefone: '(11) 98555-5555',
    role: 'ALUNO',
    ativo: true,
    dataCadastro: '2024-03-01',
    turma: '9º A',
    matricula: '2024001',
    responsavelId: 6
  },
  {
    id: 5,
    nome: 'Pedro Henrique Costa',
    email: 'pedro.aluno@educonnect.com',
    password: '123456',
    cpf: '555.555.555-55',
    telefone: '(11) 98444-4444',
    role: 'ALUNO',
    ativo: true,
    dataCadastro: '2024-03-01',
    turma: '8º B',
    matricula: '2024002',
    responsavelId: 7
  },
  
  // RESPONSÁVEIS
  {
    id: 6,
    nome: 'Mariana Souza',
    email: 'responsavel@educonnect.com',
    password: 'resp123',
    cpf: '666.666.666-66',
    telefone: '(11) 98333-3333',
    role: 'RESPONSAVEL',
    ativo: true,
    dataCadastro: '2024-03-01',
    alunosVinculados: [4]
  },
  {
    id: 7,
    nome: 'Roberto Costa',
    email: 'roberto.responsavel@educonnect.com',
    password: '123456',
    cpf: '777.777.777-77',
    telefone: '(11) 98222-2222',
    role: 'RESPONSAVEL',
    ativo: true,
    dataCadastro: '2024-03-01',
    alunosVinculados: [5]
  }
]

// Função para buscar usuário por email e senha
export const findUserByCredentials = (email, password) => {
  return mockUsers.find(user => 
    user.email === email && user.password === password && user.ativo
  )
}

// Função para buscar usuário por ID
export const findUserById = (id) => {
  return mockUsers.find(user => user.id === id)
}

// Função para buscar usuário por email
export const findUserByEmail = (email) => {
  return mockUsers.find(user => user.email === email)
}

// Função para gerar token JWT mock
export const generateMockToken = (user) => {
  const tokenData = {
    id: user.id,
    email: user.email,
    nome: user.nome,
    role: user.role,
    iat: Date.now(),
    exp: Date.now() + (24 * 60 * 60 * 1000) // 24 horas
  }
  
  // Simula um JWT (não é real, apenas para demonstração)
  const header = btoa(JSON.stringify({ alg: 'HS256', typ: 'JWT' }))
  const payload = btoa(JSON.stringify(tokenData))
  const signature = btoa('mock-signature')
  
  return `${header}.${payload}.${signature}`
}

// Dados mock para diferentes perfis
export const mockDataByRole = {
  ALUNO: {
    stats: {
      recados: 8,
      atividades: 5,
      eventos: 3,
      documentos: 12
    },
    recadosRecentes: [
      {
        id: 1,
        titulo: 'Reunião de Pais e Mestres',
        remetente: 'Coordenação Pedagógica',
        data: 'Hoje',
        preview: 'Informamos que a reunião será realizada no próximo sábado...',
        lido: false,
        categoria: 'evento',
        importante: true,
        anexos: 1
      },
      {
        id: 2,
        titulo: 'Material necessário para aula prática',
        remetente: 'Prof. Maria Silva',
        data: 'Ontem',
        preview: 'Para a próxima aula prática, os alunos devem trazer...',
        lido: false,
        categoria: 'academico',
        importante: false,
        anexos: 0
      },
      {
        id: 3,
        titulo: 'Aviso sobre feriado',
        remetente: 'Administração',
        data: '2 dias atrás',
        preview: 'Comunicamos que na próxima segunda-feira não haverá aula...',
        lido: true,
        categoria: 'geral',
        importante: false,
        anexos: 0
      }
    ],
    atividadesPendentes: [
      {
        id: 1,
        titulo: 'Trabalho de Matemática',
        disciplina: 'Matemática',
        prazo: 'Hoje',
        status: 'pendente'
      },
      {
        id: 2,
        titulo: 'Redação sobre História do Brasil',
        disciplina: 'História',
        prazo: 'Amanhã',
        status: 'pendente'
      },
      {
        id: 3,
        titulo: 'Exercícios de Física',
        disciplina: 'Física',
        prazo: '3 dias',
        status: 'pendente'
      }
    ]
  },
  
  PROFESSOR: {
    stats: {
      recados: 15,
      atividades: 23,
      eventos: 5,
      turmas: 4
    },
    recadosRecentes: [
      {
        id: 11,
        titulo: 'Atividade Corrigida - 9º A',
        remetente: 'Sistema',
        data: 'Hoje',
        preview: 'A atividade de matemática foi corrigida...',
        lido: false,
        categoria: 'academico',
        importante: false,
        anexos: 0
      }
    ],
    atividadesPendentes: [
      {
        id: 11,
        titulo: 'Correção de Provas - 9º A',
        disciplina: 'Matemática',
        prazo: 'Hoje',
        status: 'pendente'
      }
    ]
  },
  
  RESPONSAVEL: {
    stats: {
      recados: 6,
      eventos: 3,
      documentos: 8,
      mensalidades: 2
    },
    recadosRecentes: [
      {
        id: 21,
        titulo: 'Comunicado Financeiro',
        remetente: 'Setor Financeiro',
        data: 'Hoje',
        preview: 'Lembramos sobre o vencimento da mensalidade...',
        lido: false,
        categoria: 'financeiro',
        importante: true,
        anexos: 1
      }
    ],
    mensalidadesPendentes: [
      {
        id: 1,
        mes: 'Outubro/2025',
        vencimento: '10/10/2025',
        valor: 850.00,
        status: 'Pendente'
      }
    ]
  },
  
  DIRETORIA: {
    stats: {
      totalUsuarios: 245,
      usuariosAtivos: 198,
      recadosEnviados: 856,
      taxaPagamento: 94
    },
    usuariosPorPerfil: {
      alunos: 120,
      responsaveis: 80,
      professores: 40,
      diretoria: 2,
      administradores: 3
    },
    atividadesRecentes: [
      {
        id: 1,
        tipo: 'novo_usuario',
        descricao: 'Novo usuário cadastrado',
        detalhe: 'Maria Silva - há 2 horas',
        icone: 'bi-person-plus',
        cor: 'success'
      },
      {
        id: 2,
        tipo: 'recado',
        descricao: 'Recado enviado',
        detalhe: 'Reunião de pais - há 5 horas',
        icone: 'bi-envelope',
        cor: 'primary'
      },
      {
        id: 3,
        tipo: 'evento',
        descricao: 'Evento criado',
        detalhe: 'Feira de Ciências - há 1 dia',
        icone: 'bi-calendar-event',
        cor: 'info'
      }
    ]
  },
  
  ADMINISTRADOR: {
    stats: {
      totalUsuarios: 245,
      usuariosAtivos: 198,
      recadosEnviados: 1234,
      taxaPagamento: 94
    },
    usuariosPorPerfil: {
      alunos: 120,
      responsaveis: 80,
      professores: 40,
      diretoria: 2,
      administradores: 3
    },
    atividadesRecentes: [
      {
        id: 1,
        tipo: 'novo_usuario',
        descricao: 'Novo usuário cadastrado',
        detalhe: 'Maria Silva - há 2 horas',
        icone: 'bi-person-plus',
        cor: 'success'
      },
      {
        id: 2,
        tipo: 'recado',
        descricao: 'Recado enviado',
        detalhe: 'Reunião de pais - há 5 horas',
        icone: 'bi-envelope',
        cor: 'primary'
      },
      {
        id: 3,
        tipo: 'pagamento',
        descricao: 'Pagamento recebido',
        detalhe: 'João Santos - há 1 dia',
        icone: 'bi-credit-card',
        cor: 'warning'
      }
    ]
  }
}

// Função para obter dados mock baseado no perfil do usuário
export const getMockDataForUser = (user) => {
  return mockDataByRole[user.role] || mockDataByRole.ALUNO
}

