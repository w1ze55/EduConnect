# 🗄️ Configuração MySQL + Autenticação do Backend

## ✅ Mudanças Aplicadas:

### **Backend:**
1. ✅ MySQL configurado como banco principal
2. ✅ H2 desabilitado
3. ✅ Criado endpoint `/api/auth/me` para buscar usuário
4. ✅ Logs detalhados no login e registro

### **Frontend:**
1. ✅ **Removido localStorage** completamente
2. ✅ Usa apenas **sessionStorage** para token (temporário)
3. ✅ **Busca dados do usuário do backend** via `/api/auth/me`
4. ✅ Após login, faz requisição automática para buscar dados do MySQL

---

## 🚀 Como Configurar:

### **1. Instalar MySQL (se não tiver)**

#### **Windows:**
1. Baixe: https://dev.mysql.com/downloads/installer/
2. Instale MySQL Server 8.0+
3. Configure senha root (use `root` ou anote a senha)

#### **Linux:**
```bash
sudo apt update
sudo apt install mysql-server
sudo mysql_secure_installation
```

#### **Mac:**
```bash
brew install mysql
brew services start mysql
```

### **2. Criar o Banco de Dados**

```bash
# Conectar ao MySQL
mysql -u root -p

# Dentro do MySQL:
CREATE DATABASE educonnect;
SHOW DATABASES;
EXIT;
```

### **3. Ajustar Configurações do Backend (se necessário)**

Edite `backend/src/main/resources/application.properties`:

```properties
# Ajuste a senha se não for 'root'
spring.datasource.password=SUA_SENHA_AQUI
```

### **4. Reiniciar o Backend**

```bash
cd backend
mvn clean spring-boot:run
```

**Aguarde ver:**
```
Hibernate: create table usuarios (...)
Started EduConnectApplication in X.XXX seconds
```

---

## 🧪 Como Testar:

### **Teste 1: Verificar Conexão MySQL**

```bash
mysql -u root -p
USE educonnect;
SHOW TABLES;
```

Deve mostrar:
```
+------------------------+
| Tables_in_educonnect   |
+------------------------+
| usuarios               |
| professor_disciplinas  |
| professor_turmas       |
+------------------------+
```

### **Teste 2: Cadastrar Usuário**

1. Limpe o navegador:
```javascript
// F12 > Console
localStorage.clear()
sessionStorage.clear()
location.reload()
```

2. Acesse: http://localhost:5173/cadastro

3. Cadastre:
```
Nome: Teste MySQL
Email: teste.mysql@escola.com
CPF: 123.456.789-00
Telefone: (11) 98888-8888
Perfil: ALUNO
Senha: senha123
Confirmar: senha123
```

4. **Verifique o Console do Backend:**
```
🔍 DEBUG - Registrando usuário: teste.mysql@escola.com
🔐 Senha antes de criptografar: senha123
🔐 Senha criptografada: $2a$10$...
Hibernate: insert into usuarios (...) values (...)
✅ Usuário salvo com ID: 1
✅ Email salvo: teste.mysql@escola.com
✅ Role salvo: ALUNO
✅ Ativo: true
```

5. **Verifique no MySQL:**
```sql
SELECT id, nome, email, role, ativo FROM usuarios;
```

Deve mostrar o usuário cadastrado!

### **Teste 3: Fazer Login**

1. Faça login com:
```
Email: teste.mysql@escola.com
Senha: senha123
```

2. **Verifique o Console do Navegador (F12):**
```
🔍 [AUTH] Tentando login com: teste.mysql@escola.com
✅ [AUTH] Resposta do login: {token: "eyJ...", usuario: {...}}
🔑 [AUTH] Salvando token
🔄 [AUTH] Buscando dados do usuário do backend...
🔄 [AUTH] GET /auth/me
✅ [AUTH] Dados do usuário recebidos: {id: 1, nome: "Teste MySQL", role: "ALUNO", ...}
✅ [AUTH] Login completo!
✅ [AUTH] User: {id: 1, nome: "Teste MySQL", role: "ALUNO", ...}
✅ [AUTH] UserRole: ALUNO
```

Depois na Sidebar:
```
🔍 Debug Sidebar - User: {id: 1, nome: "Teste MySQL", role: "ALUNO", ...}
🔍 Debug Sidebar - UserRole: ALUNO
✅ Itens de menu filtrados: 5
```

3. **Verificar Menus Apareceram:**
- ✅ Dashboard
- ✅ Recados
- ✅ Atividades
- ✅ Calendário
- ✅ Documentos

4. **Ir em Perfil e verificar dados:**
- ✅ Nome: Teste MySQL
- ✅ Email: teste.mysql@escola.com
- ✅ CPF: 123.456.789-00
- ✅ Perfil: Aluno

---

## 🔍 Fluxo Completo de Autenticação:

```
1. Usuário faz LOGIN
   ↓
2. Frontend envia POST /api/auth/login
   ↓
3. Backend valida no MySQL
   ↓
4. Backend retorna TOKEN
   ↓
5. Frontend salva token no sessionStorage
   ↓
6. Frontend faz GET /api/auth/me (com token no header)
   ↓
7. Backend busca usuário do MySQL via token
   ↓
8. Backend retorna dados completos do usuário
   ↓
9. Frontend salva user no Pinia store (memória)
   ↓
10. Sidebar e Perfil usam dados do store
```

---

## 🎯 Vantagens Desta Abordagem:

1. ✅ **Dados sempre atualizados do MySQL**
2. ✅ **Não usa localStorage** (mais seguro)
3. ✅ **sessionStorage** apenas para token (limpa ao fechar aba)
4. ✅ **Dados do usuário em memória** (store Pinia)
5. ✅ **Backend é a fonte única da verdade**
6. ✅ **Ao recarregar página, busca dados novamente do MySQL**

---

## 🐛 Troubleshooting:

### **Erro: Access denied for user 'root'@'localhost'**

Ajuste a senha no `application.properties`:
```properties
spring.datasource.password=sua_senha_real
```

### **Erro: Unknown database 'educonnect'**

Crie o banco:
```sql
CREATE DATABASE educonnect;
```

### **Erro: Table 'usuarios' doesn't exist**

O backend cria automaticamente. Verifique:
```properties
spring.jpa.hibernate.ddl-auto=update
```

### **Frontend não mostra menus**

1. Verifique console do navegador
2. Veja se apareceu: `✅ [AUTH] Dados do usuário recebidos`
3. Se não, verifique se o endpoint `/api/auth/me` está funcionando:

```bash
# Pegue o token do sessionStorage
# F12 > Application > Session Storage > token

curl http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer SEU_TOKEN_AQUI"
```

---

## 📊 Verificar Estado Atual:

### **No Console do Navegador (F12):**
```javascript
// Verificar token
console.log('Token:', sessionStorage.getItem('token'))

// Verificar store
const { useAuthStore } = await import('./stores/auth')
const authStore = useAuthStore()
console.log('User:', authStore.user)
console.log('Role:', authStore.userRole)
console.log('Authenticated:', authStore.isAuthenticated)
```

### **No MySQL:**
```sql
USE educonnect;
SELECT id, nome, email, role, ativo, DATE_FORMAT(data_cadastro, '%d/%m/%Y %H:%i') as cadastro 
FROM usuarios 
ORDER BY id DESC;
```

---

**Última atualização:** 01/10/2025, 00:30

