# EduConnect Backend - Spring Boot

Backend REST API para o sistema EduConnect de gestão escolar.

## 🚀 Tecnologias

- **Spring Boot 3.5.6**
- **Java 21**
- **Spring Security + JWT**
- **Spring Data JPA**
- **H2 Database** (desenvolvimento)
- **MySQL** (produção)
- **Lombok**
- **ModelMapper**
- **Maven**

## 📋 Pré-requisitos

- Java 21 ou superior
- Maven 3.8+
- MySQL 8+ (opcional, usa H2 por padrão)

## 🔧 Configuração

### Banco de Dados

Por padrão, o projeto usa **H2 Database** (em memória) para desenvolvimento:
- URL: `jdbc:h2:mem:educonnect`
- Console H2: http://localhost:8080/h2-console

Para usar **MySQL**:
1. Crie o banco de dados:
```sql
CREATE DATABASE educonnect;
```

2. No `application.properties`, comente as linhas do H2 e descomente as do MySQL:
```properties
# MySQL
spring.datasource.url=jdbc:mysql://localhost:3306/educonnect?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=root
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
```

### JWT

O token JWT está configurado com:
- Expiração: 24 horas (86400000 ms)
- Secret: Configurável em `application.properties`

## 🚀 Executar o Projeto

### Via Maven
```bash
cd backend
mvn spring-boot:run
```

### Via JAR
```bash
mvn clean package
java -jar target/EduConnect-0.0.1-SNAPSHOT.jar
```

O servidor iniciará em: **http://localhost:8080**

## 🔐 Usuários Pré-cadastrados

O sistema inicializa com os seguintes usuários:

| Perfil | E-mail | Senha |
|--------|--------|-------|
| **Admin** | admin@educonnect.com | admin123 |
| **Professor** | professor@educonnect.com | prof123 |
| **Aluno** | aluno@educonnect.com | aluno123 |
| **Responsável** | responsavel@educonnect.com | resp123 |

## 📡 Endpoints da API

### Autenticação
```
POST /api/auth/login
POST /api/auth/register
```

### Recados
```
GET    /api/recados
GET    /api/recados/{id}
POST   /api/recados                      [PROFESSOR, ADMIN]
POST   /api/recados/{id}/confirmar-leitura
```

### Usuários
```
GET    /api/usuarios                     [ADMIN]
GET    /api/usuarios/{id}                [ADMIN]
PUT    /api/usuarios/{id}                [ADMIN]
DELETE /api/usuarios/{id}                [ADMIN]
```

## 📝 Exemplo de Requisição

### Login
```json
POST /api/auth/login
Content-Type: application/json

{
  "email": "admin@educonnect.com",
  "password": "admin123"
}
```

**Resposta:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR...",
  "user": {
    "id": 1,
    "nome": "Carlos Administrador",
    "email": "admin@educonnect.com",
    "role": "ADMINISTRADOR",
    "ativo": true
  }
}
```

### Criar Recado (com autenticação)
```json
POST /api/recados
Authorization: Bearer {token}
Content-Type: application/json

{
  "titulo": "Aviso Importante",
  "conteudo": "Comunicamos...",
  "categoria": "GERAL",
  "importante": true,
  "exigirConfirmacao": true
}
```

## 🏗️ Estrutura do Projeto

```
src/main/java/com/educonnect/EduConnect/
├── config/              # Configurações (AppConfig, DataInitializer)
├── controller/          # Controllers REST
├── dto/                 # Data Transfer Objects
├── exception/           # Exception Handlers
├── model/               # Entidades JPA
│   └── enums/          # Enums
├── repository/          # Repositories JPA
├── security/            # Configuração de Segurança e JWT
└── service/             # Regras de Negócio
```

## 🔒 Segurança

- Senhas são criptografadas com **BCrypt**
- Autenticação via **JWT Token**
- Proteção de endpoints por **Role** (ALUNO, PROFESSOR, etc)
- CORS configurado para frontend em `localhost:5173`

## 🗄️ Modelo de Dados

### Principais Entidades

- **Usuario**: Alunos, Professores, Responsáveis e Administradores
- **Recado**: Comunicados da escola
- **LeituraRecado**: Controle de confirmação de leitura
- **Atividade**: Tarefas escolares
- **RespostaAtividade**: Respostas dos alunos
- **Evento**: Eventos do calendário escolar

## 🧪 Testes

```bash
mvn test
```

## 📦 Build para Produção

```bash
mvn clean package -DskipTests
```

O JAR será gerado em: `target/EduConnect-0.0.1-SNAPSHOT.jar`

## 🐳 Docker (Opcional)

Crie um `Dockerfile`:
```dockerfile
FROM openjdk:21-jdk-slim
COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

Build e Run:
```bash
docker build -t educonnect-backend .
docker run -p 8080:8080 educonnect-backend
```

## 🔄 Integração com Frontend

1. Frontend está configurado para: `http://localhost:8080/api`
2. Configure CORS em `application.properties` se necessário
3. Token JWT deve ser enviado no header: `Authorization: Bearer {token}`

## ⚙️ Variáveis de Ambiente

Você pode configurar via variáveis de ambiente:

```bash
export JWT_SECRET=sua-secret-key-aqui
export DB_URL=jdbc:mysql://localhost:3306/educonnect
export DB_USERNAME=root
export DB_PASSWORD=root
```

## 📝 Logs

Logs estão configurados para nível DEBUG em desenvolvimento:
- Spring Security: DEBUG
- Com.educonnect: DEBUG

Altere em `application.properties` para produção.

## 🚀 Deploy

### Heroku
```bash
git init
heroku create educonnect-api
git add .
git commit -m "Deploy"
git push heroku main
```

### AWS / Azure / Google Cloud
Use o JAR gerado e configure as variáveis de ambiente apropriadas.

## 📞 Suporte

Para dúvidas ou problemas:
1. Verifique os logs da aplicação
2. Teste os endpoints com Postman/Insomnia
3. Verifique se o banco de dados está acessível

---

**EduConnect Backend** - API REST para gestão escolar 🎓

