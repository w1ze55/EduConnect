# 🎓 EduConnect - Plataforma de Comunicação Escolar

Sistema completo de gestão escolar desenvolvido com **Vue.js 3** (frontend) e **Spring Boot** (backend), containerizado com **Docker** e banco de dados **MySQL**.

## 🌟 Características

- 🔐 **Autenticação JWT** completa
- 👥 **5 Perfis de usuário**: Aluno, Professor, Responsável, Diretoria e Administrador
- ✉️ **Sistema de Recados** com confirmação de leitura
- 📚 **Gestão de Atividades** escolares
- 📅 **Calendário** de eventos e provas
- 📄 **Documentos Digitais** com assinatura
- 💰 **Módulo Financeiro** para responsáveis
- ⚙️ **Painel Administrativo** completo
- 🎨 **Design Responsivo** (Em breve)
- 🐳 **100% Dockerizado**

## 🔐 Credenciais de Teste

| Perfil | E-mail | Senha |
|--------|--------|-------|
| 👤 **Admin** | admin@educonnect.com | admin123 |
| 👨‍🏫 **Professor** | professor@educonnect.com | prof123 |
| 🎓 **Aluno** | aluno@educonnect.com | aluno123 |
| 👨‍👩‍👧 **Responsável** | responsavel@educonnect.com | resp123 |
| **Diretoria** | diretoria@educonnect.com | prof123 |

## 📦 Tecnologias

### Frontend
- **Vue.js 3** - Framework JavaScript progressivo
- **Vite** - Build tool ultrarrápido
- **Vue Router** - Roteamento
- **Pinia** - Gerenciamento de estado
- **Axios** - Cliente HTTP
- **Bootstrap 5** - Framework CSS
- **Vue Cal** - Componente de calendário
- **Nginx** - Servidor web (Docker)

### Backend
- **Spring Boot 3.5.6** - Framework Java
- **Spring Security** - Autenticação e autorização
- **JWT (jjwt 0.12.3)** - Tokens de autenticação
- **Spring Data JPA** - Persistência de dados
- **MySQL 8** - Banco de dados
- **Lombok** - Redução de boilerplate
- **ModelMapper** - Mapeamento de DTOs
- **Maven** - Gerenciamento de dependências

### DevOps
- **Docker** - Containerização
- **Docker Compose** - Orquestração
- **Nginx** - Proxy reverso e servidor web
- **MySQL** - Banco de dados persistente

## 🎯 Funcionalidades por Perfil

### 👤 Administrador
- ✅ Gestão completa de usuários (CRUD)
- ✅ Painel de estatísticas
- ✅ Envio de recados
- ✅ Acesso a todos os módulos
- ✅ Relatórios do sistema

### 👨‍🏫 Professor
- ✅ Envio de recados para alunos
- ✅ Criação e gestão de atividades
- ✅ Correção de trabalhos
- ✅ Gerenciamento de turmas
- ✅ Calendário de provas

### 🎓 Aluno
- ✅ Visualização de recados
- ✅ Atividades escolares
- ✅ Upload de respostas
- ✅ Calendário de provas e eventos
- ✅ Consulta de notas
- ✅ Documentos escolares

### 👨‍👩‍👧 Responsável
- ✅ Acompanhamento de recados
- ✅ Visualização de atividades do aluno
- ✅ Calendário escolar
- ✅ Consulta financeira
- ✅ Pagamento de mensalidades
- ✅ Assinatura de documentos

## 📡 API Endpoints

### Autenticação (Público)
- `POST /api/auth/login` - Login
- `POST /api/auth/register` - Cadastro

### Recados (Autenticado)
- `GET /api/recados` - Listar todos
- `GET /api/recados/{id}` - Detalhes
- `POST /api/recados` - Criar (Professor/Admin)
- `POST /api/recados/{id}/confirmar-leitura` - Confirmar

### Usuários (Admin)
- `GET /api/usuarios` - Listar
- `GET /api/usuarios/{id}` - Detalhes
- `PUT /api/usuarios/{id}` - Atualizar
- `DELETE /api/usuarios/{id}` - Deletar

## 🗄️ Banco de Dados

### Acesso ao MySQL (Docker)
```bash
# Via Docker
docker exec -it educonnect-mysql mysql -u <MYSQL_USER> -p<MYSQL_PASSWORD> <MYSQL_DATABASE>

# Via Cliente MySQL
mysql -h 127.0.0.1 -P <MYSQL_HOST_PORT> -u <MYSQL_USER> -p<MYSQL_PASSWORD> <MYSQL_DATABASE>
```

## 📄 Licença

Este projeto foi desenvolvido para fins educacionais.

## 👥 Autores

**EduConnect Team**

## 🐛 Reportar Bugs

Encontrou um bug? Abra uma issue descrevendo:
- O que aconteceu
- O que era esperado
- Passos para reproduzir
- Screenshots (se aplicável)

## ⭐ Agradecimentos

Obrigado por usar o EduConnect!

---

**EduConnect** - Conectando escolas, alunos e famílias 🎓

