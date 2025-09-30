# 🔍 DEBUG - Problema de Login

## 🐛 Problema Relatado:
"Estou recebendo 'usuário ou senha inválidos' sendo que os exatos dados estão cadastrados no banco"

---

## ✅ Logs de Debug Adicionados

Adicionei logs detalhados no backend para identificar o problema. Agora o backend vai mostrar:

### Durante o CADASTRO:
```
🔍 DEBUG - Registrando usuário: email@exemplo.com
🔐 Senha antes de criptografar: senha123
🔐 Senha criptografada: $2a$10$abcdef...
✅ Usuário salvo com ID: 1
✅ Email salvo: email@exemplo.com
✅ Role salvo: ALUNO
✅ Ativo: true
```

### Durante o LOGIN:
```
🔍 DEBUG - Tentando login com email: email@exemplo.com
✅ Usuário encontrado: email@exemplo.com
🔐 Senha no banco (hash): $2a$10$abcdef...
🔐 Senha recebida: senha123
👤 Usuário ativo: true
✅ Login bem-sucedido para: email@exemplo.com
```

OU, se der erro:
```
❌ Falha na autenticação: Bad credentials
```

---

## 🧪 Como Testar (PASSO A PASSO):

### **1. Reiniciar o Backend com os Logs**

```bash
cd backend
# Parar o backend atual (Ctrl+C)
mvn clean spring-boot:run
```

### **2. Limpar o Banco de Dados H2**

O backend já vai recriar o banco (por causa do `ddl-auto=create-drop`)

### **3. Fazer um Novo Cadastro**

1. Acesse: http://localhost:5173/cadastro
2. Use estes dados **EXATOS**:
   ```
   Nome: Teste Debug Usuario
   Email: teste.debug@escola.com
   CPF: 111.222.333-44
   Telefone: (11) 99999-9999
   Perfil: ALUNO
   Senha: senha123
   Confirmar Senha: senha123
   ```
3. Clique em "Criar Conta"

### **4. Verificar Logs do Backend (CONSOLE)**

Você deve ver algo como:
```
🔍 DEBUG - Registrando usuário: teste.debug@escola.com
🔐 Senha antes de criptografar: senha123
🔐 Senha criptografada: $2a$10$...
✅ Usuário salvo com ID: 1
✅ Email salvo: teste.debug@escola.com
✅ Role salvo: ALUNO
✅ Ativo: true
```

**COPIE E ME ENVIE ESSES LOGS!**

### **5. Tentar Fazer Login**

A tela de login deve aparecer automaticamente.

Use **EXATAMENTE**:
```
Email: teste.debug@escola.com
Senha: senha123
```

### **6. Verificar Logs do Login no Backend**

Você vai ver:
```
🔍 DEBUG - Tentando login com email: teste.debug@escola.com
✅ Usuário encontrado: teste.debug@escola.com
🔐 Senha no banco (hash): $2a$10$...
🔐 Senha recebida: senha123
👤 Usuário ativo: true
```

E ENTÃO, uma dessas duas opções:
- ✅ `Login bem-sucedido para: teste.debug@escola.com`
- ❌ `Falha na autenticação: Bad credentials`

**COPIE E ME ENVIE ESSES LOGS TAMBÉM!**

---

## 🔎 Possíveis Causas:

### **Causa 1: PasswordEncoder não configurado corretamente**
Se o erro for "There is no PasswordEncoder mapped for the id null", significa que o encoder não está sendo usado.

**Solução:** Já corrigimos isso configurando o encoder no `DaoAuthenticationProvider`.

### **Causa 2: Senha não está sendo criptografada no cadastro**
Se o log mostrar que a senha NÃO começa com `$2a$10$`, então não está sendo criptografada.

**Verificação:** Olhe o log `🔐 Senha criptografada: ...`

### **Causa 3: Email/Senha com espaços extras**
Às vezes o frontend manda espaços no início ou fim.

**Verificação:** Compare os logs:
- `Registrando usuário: teste.debug@escola.com`
- `Tentando login com email: teste.debug@escola.com`

Se tiver espaços, vai aparecer.

### **Causa 4: Campo `role` está null**
Se o role não for salvo, pode dar problema.

**Verificação:** Olhe o log `✅ Role salvo: ALUNO`

Se mostrar `null`, encontramos o problema!

### **Causa 5: Usuário está inativo (ativo=false)**
Se `ativo` for `false`, o login não funciona.

**Verificação:** Olhe o log `✅ Ativo: true`

---

## 📊 Verificar Manualmente no Banco H2

### **1. Acessar H2 Console**
```
http://localhost:8080/h2-console
```

**Credenciais:**
- JDBC URL: `jdbc:h2:mem:educonnect`
- User: `sa`
- Password: *(vazio)*

### **2. Executar Query**
```sql
SELECT id, nome, email, password, role, ativo 
FROM usuarios 
WHERE email = 'teste.debug@escola.com';
```

**Verificar:**
- ✅ `password` deve começar com `$2a$10$` (BCrypt)
- ✅ `role` deve ser `ALUNO`
- ✅ `ativo` deve ser `true`

---

## 📝 Teste Manual via CURL

Se quiser testar direto, use:

### **Cadastro:**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Teste Manual",
    "email": "teste.manual@escola.com",
    "password": "senha123",
    "cpf": "555.666.777-88",
    "telefone": "(11) 98888-8888",
    "role": "ALUNO"
  }'
```

### **Login:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "teste.manual@escola.com",
    "password": "senha123"
  }'
```

---

## 🎯 Próximos Passos

1. **Execute os testes acima**
2. **Copie TODOS os logs do console do backend**
3. **Me envie os logs**
4. **Com os logs, vou identificar exatamente onde está o problema!**

---

**Criado em:** 30/09/2025, 23:55

