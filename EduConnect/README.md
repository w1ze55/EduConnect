# EduConnect - Plataforma de Comunicação Escolar

Sistema de gestão escolar completo desenvolvido com Vue.js 3, Vite, Vue Router, Pinia, Axios e Bootstrap 5.

## 🚀 Tecnologias Utilizadas

- **Vue.js 3** - Framework JavaScript progressivo
- **Vite** - Build tool e dev server ultrarrápido
- **Vue Router** - Roteamento oficial do Vue.js
- **Pinia** - Gerenciamento de estado moderno
- **Axios** - Cliente HTTP para chamadas de API
- **Bootstrap 5** - Framework CSS responsivo
- **Bootstrap Icons** - Biblioteca de ícones
- **Vue Cal** - Componente de calendário
- **JWT Decode** - Decodificação de tokens JWT

## 📋 Funcionalidades

### Autenticação
- Login com JWT
- Cadastro de usuários
- Controle de sessão
- Guarda de rotas por perfil

### Dashboard
- Visão geral personalizada
- Estatísticas em tempo real
- Notificações e alertas
- Atalhos rápidos

### Comunicação (Recados)
- Listagem de recados recebidos
- Visualização detalhada
- Confirmação de leitura
- Envio de recados (Professor/Admin)
- Anexos e imagens
- Filtros e busca

### Atividades
- Lista de tarefas escolares
- Detalhes com prazos e anexos
- Upload de respostas
- Acompanhamento de notas
- Status de entrega

### Calendário
- Visualização mensal
- Eventos escolares
- Provas e reuniões
- Integração com vue-cal

### Documentos
- Gestão de documentos digitais
- Visualização e download
- Assinatura digital
- Organização por categoria

### Financeiro (Responsáveis)
- Lista de mensalidades
- Status de pagamento
- Geração de boleto
- Histórico financeiro

### Administração
- CRUD de usuários
- Painel de estatísticas
- Gestão de perfis
- Relatórios

## 🎨 Perfis de Usuário

- **Aluno**: Acesso a recados, atividades, calendário e documentos
- **Responsável**: Acesso adicional ao módulo financeiro
- **Professor**: Pode enviar recados e gerenciar atividades
- **Administrador**: Acesso completo ao sistema

## 📦 Instalação

### Pré-requisitos
- Node.js 16+ instalado
- NPM ou Yarn

### Passos

1. **Clone o repositório** (se aplicável)
```bash
git clone <url-do-repositorio>
cd EduConnect
```

2. **Instale as dependências**
```bash
npm install
```

3. **Configure as variáveis de ambiente**
```bash
cp .env.example .env
```

Edite o arquivo `.env` com a URL do seu backend:
```
VITE_API_URL=http://localhost:8080/api
```

4. **Execute o servidor de desenvolvimento**
```bash
npm run dev
```

5. **Acesse a aplicação**
```
http://localhost:5173
```

## 🔧 Scripts Disponíveis

```bash
# Servidor de desenvolvimento
npm run dev

# Build para produção
npm run build

# Preview do build de produção
npm run preview
```

## 📂 Estrutura de Pastas

```
src/
├── assets/           # Arquivos estáticos (imagens, fontes)
├── components/       # Componentes reutilizáveis
│   ├── common/      # Componentes comuns (Loading, Toast)
│   └── layout/      # Componentes de layout (Navbar, Sidebar, Footer)
├── router/          # Configuração de rotas
├── services/        # Serviços de API
├── stores/          # Stores Pinia (gerenciamento de estado)
├── views/           # Páginas/Views da aplicação
│   ├── auth/        # Páginas de autenticação
│   ├── atividades/  # Módulo de atividades
│   ├── comunicacao/ # Módulo de comunicação
│   ├── documentos/  # Módulo de documentos
│   ├── financeiro/  # Módulo financeiro
│   └── admin/       # Módulo administrativo
├── App.vue          # Componente raiz
├── main.js          # Ponto de entrada da aplicação
└── style.css        # Estilos globais
```

## 🔐 Autenticação

O sistema utiliza JWT (JSON Web Tokens) para autenticação. O token é armazenado no `sessionStorage` e incluído automaticamente em todas as requisições via interceptor do Axios.

### Integração com Backend:
O sistema está preparado para integração completa com o backend Spring Boot. Configure a `VITE_API_URL` no arquivo `.env` para conectar com a API real.

## 🎨 Design System

### Cores Principais
- **Primary**: #667eea (Azul)
- **Secondary**: #764ba2 (Roxo)
- **Success**: #28a745 (Verde)
- **Warning**: #ffc107 (Amarelo)
- **Danger**: #dc3545 (Vermelho)
- **Info**: #17a2b8 (Ciano)

### Responsividade
O sistema é totalmente responsivo e funciona em:
- Desktop (1200px+)
- Tablet (768px - 1199px)
- Mobile (até 767px)

## 🔌 Integração com Backend

O frontend está totalmente preparado para integrar com o backend Spring Boot. As chamadas de API estão configuradas nos arquivos da pasta `services/`.

Para conectar com o backend real:
1. Configure a `VITE_API_URL` no arquivo `.env`
2. O sistema já está configurado para usar as APIs reais
3. Ajuste os DTOs conforme necessário

## 📝 Notas de Desenvolvimento

- O sistema está pronto para integração com backend real
- Implemente tratamento de erros adequado conforme necessário
- Adicione validações de formulário conforme necessário
- Configure CORS no backend para aceitar requisições do frontend

## 🚀 Deploy

### Build de Produção
```bash
npm run build
```

Os arquivos otimizados serão gerados na pasta `dist/`.

### Hospedagem Recomendada
- Vercel
- Netlify
- AWS S3 + CloudFront
- Nginx
- Apache

## 📄 Licença

Este projeto foi desenvolvido para fins educacionais.

## 👥 Suporte

Para dúvidas e suporte, entre em contato com a equipe de desenvolvimento.

---

**EduConnect** - Conectando escolas, alunos e famílias 🎓
