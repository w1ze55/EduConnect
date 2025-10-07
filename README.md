# 🎓 EduConnect - Plataforma de Comunicação Escolar

Sistema completo de gestão escolar desenvolvido com **Vue.js 3** (frontend) e **Spring Boot** (backend), containerizado com **Docker** e banco de dados **MySQL**.

## 🌟 Características

- 🔐 **Autenticação JWT** completa
- 👥 **4 Perfis de usuário**: Aluno, Professor, Responsável, Administrador
- ✉️ **Sistema de Recados** com confirmação de leitura
- 📚 **Gestão de Atividades** escolares
- 📅 **Calendário** de eventos e provas
- 📄 **Documentos Digitais** com assinatura
- 💰 **Módulo Financeiro** para responsáveis
- ⚙️ **Painel Administrativo** completo
- 🎨 **Design Responsivo** (Desktop, Tablet, Mobile)
- 🐳 **100% Dockerizado**

## 🚀 Quick Start (Docker)

### Windows
```powershell
.\start.ps1
```

### Linux/Mac
```bash
chmod +x start.sh
./start.sh
```

### Manual
```bash
docker-compose up -d --build
```

**Aguarde ~60 segundos** e acesse:
- **Frontend**: http://localhost:3000
- **Backend**: http://localhost:8080
- **MySQL**: localhost:4306

## 🔐 Credenciais de Teste

| Perfil | E-mail | Senha |
|--------|--------|-------|
| 👤 **Admin** | admin@educonnect.com | admin123 |
| 👨‍🏫 **Professor** | professor@educonnect.com | prof123 |
| 🎓 **Aluno** | aluno@educonnect.com | aluno123 |
| 👨‍👩‍👧 **Responsável** | responsavel@educonnect.com | resp123 |
| 👨‍🏫 **Diretoria** | diretoria@educonnect.com | dir123 |

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

## 🏗️ Arquitetura

```
┌─────────────────────────────────────────────────────────┐
│                      EduConnect                         │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  ┌─────────────┐      ┌──────────────┐      ┌────────┐│
│  │  Frontend   │      │   Backend    │      │ MySQL  ││
│  │  (Vue.js)   │─────▶│ (Spring Boot)│─────▶│   DB   ││
│  │  Port: 3000 │ HTTP │  Port: 8080  │ JDBC │  4306  ││
│  │  + Nginx    │      │  + JWT Auth  │      │        ││
│  └─────────────┘      └──────────────┘      └────────┘│
│                                                         │
└─────────────────────────────────────────────────────────┘
```

## 📂 Estrutura do Projeto

```
EduConnect/
├── backend/                    # Spring Boot Backend
│   ├── src/main/java/
│   │   └── com/educonnect/EduConnect/
│   │       ├── config/        # Configurações
│   │       ├── controller/    # REST Controllers
│   │       ├── dto/           # Data Transfer Objects
│   │       ├── model/         # Entidades JPA
│   │       ├── repository/    # Repositories
│   │       ├── security/      # JWT + Spring Security
│   │       └── service/       # Regras de Negócio
│   ├── Dockerfile
│   └── pom.xml
│
├── EduConnect/                # Vue.js Frontend
│   ├── src/
│   │   ├── components/        # Componentes reutilizáveis
│   │   ├── views/            # Páginas
│   │   ├── router/           # Rotas
│   │   ├── stores/           # Pinia Stores
│   │   ├── services/         # API Services
│   │   └── data/             # Dados Mock
│   ├── Dockerfile
│   ├── nginx.conf
│   └── package.json
│
├── mysql-init/                # Scripts SQL inicialização
│   └── 01-init.sql
│
├── docker-compose.yml         # Orquestração Docker
├── start.sh                   # Script inicialização (Linux/Mac)
├── start.ps1                  # Script inicialização (Windows)
├── DOCKER-README.md           # Documentação Docker
└── README.md                  # Este arquivo
```

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

## 🛠️ Desenvolvimento Local (Sem Docker)

### Backend
```bash
cd backend
.\mvnw.cmd spring-boot:run
# ou
./mvnw spring-boot:run
```

### Frontend
```bash
cd EduConnect
npm install
npm run dev
```

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

## 🧪 Testes

### Testar API com cURL
```bash
# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@educonnect.com","password":"admin123"}'

# Listar recados (com token)
curl http://localhost:8080/api/recados \
  -H "Authorization: Bearer SEU_TOKEN"
```

### Health Check
```bash
curl http://localhost:8080/actuator/health
```

## 🗄️ Banco de Dados

### Acesso ao MySQL (Docker)
```bash
# Via Docker
docker exec -it educonnect-mysql mysql -u educonnect -peduconnect123 educonnect

# Via Cliente MySQL
mysql -h 127.0.0.1 -P 4306 -u educonnect -peduconnect123 educonnect
```

### Console H2 (Desenvolvimento local)
- URL: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:educonnect`
- Username: `sa`
- Password: *(vazio)*

## 📊 Monitoramento

### Ver logs Docker
```bash
# Todos os serviços
docker-compose logs -f

# Serviço específico
docker-compose logs -f backend
docker-compose logs -f frontend
docker-compose logs -f mysql
```

### Status dos containers
```bash
docker-compose ps
```

### Uso de recursos
```bash
docker stats
```

## 🔄 Comandos Úteis Docker

```bash
# Iniciar
docker-compose up -d

# Parar
docker-compose down

# Rebuild
docker-compose up -d --build

# Ver logs
docker-compose logs -f

# Reiniciar serviço
docker-compose restart backend

# Remover tudo (incluindo volumes)
docker-compose down -v
```

## 🚀 Deploy em Produção

1. Configure variáveis de ambiente seguras
2. Use HTTPS com certificado SSL
3. Configure firewall adequado
4. Use senhas fortes para banco de dados
5. Habilite monitoramento e logs
6. Configure backup automático do MySQL

Ver **DOCKER-README.md** para detalhes completos.

## 📝 Documentação Adicional

- **[DOCKER-README.md](./DOCKER-README.md)** - Guia completo Docker
- **[backend/README.md](./backend/README.md)** - Documentação Backend
- **[EduConnect/README.md](./EduConnect/README.md)** - Documentação Frontend

## 🤝 Contribuindo

1. Fork o projeto
2. Crie sua branch (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

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

Feito com ❤️ usando Vue.js, Spring Boot e Docker

