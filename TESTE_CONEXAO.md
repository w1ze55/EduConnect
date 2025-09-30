# 🔗 Guia de Teste de Conexão Frontend-Backend

## ✅ Correções Realizadas

### 1. **Arquivo de Configuração da API**
- Você precisa criar o arquivo `.env.local` no diretório `EduConnect/` com:
```env
VITE_API_URL=http://localhost:8080/api
```

### 2. **Código de Cadastro Atualizado**
- ❌ **Antes:** Usava código mock (apenas simulava cadastro)
- ✅ **Agora:** Faz chamada real ao backend via API

### 3. **Store de Autenticação Corrigida**
- ✅ Agora usa `api` configurado (com baseURL)
- ✅ Após registrar, faz login automaticamente
- ✅ Corrigido mapeamento de resposta do backend (`usuario` ao invés de `user`)

---

## 🚀 Como Testar a Conexão

### **Passo 1: Verificar se o Backend está Rodando**

Execute no terminal:
```bash
cd backend
mvn spring-boot:run
```

Aguarde a mensagem: `Started EduConnectApplication in X.XXX seconds`

### **Passo 2: Testar o Backend Manualmente**

Abra outro terminal e teste:
```bash
# Testar endpoint de health
curl http://localhost:8080/actuator/health

# Resultado esperado:
{"status":"UP"}
```

### **Passo 3: Criar o Arquivo .env.local**

No diretório `EduConnect/`, crie manualmente o arquivo `.env.local`:
```
VITE_API_URL=http://localhost:8080/api
```

**No Windows (PowerShell):**
```powershell
cd EduConnect
echo "VITE_API_URL=http://localhost:8080/api" > .env.local
```

### **Passo 4: Iniciar o Frontend**

```bash
cd EduConnect
npm install  # Se ainda não instalou
npm run dev
```

Acesse: http://localhost:5173

### **Passo 5: Testar o Cadastro**

1. Acesse a tela de cadastro
2. Preencha os dados:
   - **Nome:** Teste Usuario
   - **Email:** teste@teste.com
   - **CPF:** 123.456.789-00
   - **Telefone:** (11) 98888-8888
   - **Perfil:** ALUNO
   - **Senha:** teste123
   - **Confirmar Senha:** teste123
3. Clique em "Criar Conta"

### **Passo 6: Verificar no Console do Navegador**

Abra o DevTools (F12) e veja:
- ✅ Requisição POST para `http://localhost:8080/api/auth/register`
- ✅ Resposta com status 200
- ✅ Redirecionamento para login ou dashboard

### **Passo 7: Verificar no Banco de Dados**

Acesse: http://localhost:8080/h2-console

**Credenciais:**
- JDBC URL: `jdbc:h2:mem:educonnect`
- User: `sa`
- Password: *(deixe em branco)*

Execute:
```sql
SELECT * FROM usuarios;
```

Você deve ver o usuário cadastrado!

---

## 🐛 Problemas Comuns e Soluções

### ❌ Erro: "Network Error" ou "ERR_CONNECTION_REFUSED"

**Causa:** Backend não está rodando ou URL incorreta

**Solução:**
```bash
# Verifique se o backend está rodando
curl http://localhost:8080/actuator/health

# Se não estiver, inicie:
cd backend
mvn spring-boot:run
```

### ❌ Erro: "CORS policy"

**Causa:** CORS não configurado

**Solução:** O CORS já está configurado no `SecurityConfig.java` para:
- http://localhost:5173
- http://localhost:3000

Certifique-se que o frontend está rodando em uma dessas portas.

### ❌ Erro: "401 Unauthorized" no cadastro

**Causa:** Endpoint de registro requer autenticação

**Solução:** Verifique no `SecurityConfig.java` linha 42:
```java
.requestMatchers("/api/auth/**", "/h2-console/**", "/error").permitAll()
```

Deve estar permitindo acesso sem autenticação.

### ❌ Arquivo .env.local não está sendo lido

**Causa:** Vite não recarregou as variáveis

**Solução:**
```bash
# Pare o servidor (Ctrl+C) e reinicie:
npm run dev
```

---

## 📊 Verificar Logs

### **Backend (console onde rodou mvn spring-boot:run):**
Você verá:
```
2025-09-30 ... : POST "/api/auth/register", parameters={}
2025-09-30 ... : Hibernate: insert into usuarios ...
```

### **Frontend (DevTools Console):**
```javascript
POST http://localhost:8080/api/auth/register 200 OK
```

---

## ✅ Checklist Final

- [ ] Backend rodando na porta 8080
- [ ] Frontend rodando na porta 5173
- [ ] Arquivo `.env.local` criado com VITE_API_URL
- [ ] Console do backend mostra requisições
- [ ] DevTools mostra requisições com status 200
- [ ] H2 Console mostra usuário cadastrado
- [ ] Redirecionamento após cadastro funciona

---

## 🎯 Próximos Passos

Após confirmar que está funcionando:

1. **Testar Login** com o usuário cadastrado
2. **Verificar Autenticação JWT** (token no localStorage)
3. **Testar outros endpoints** protegidos
4. **Implementar outras funcionalidades** (CRUD de alunos, professores, etc.)

---

**Última atualização:** 30/09/2025

