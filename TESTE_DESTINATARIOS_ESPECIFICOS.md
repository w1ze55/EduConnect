# 🧪 Guia de Teste - Destinatários Específicos

## 📝 Passo a Passo para Testar

### 1️⃣ Enviar Recado Específico

**Login como Professor:**
```
Email: professor@educonnect.com
Senha: prof123
```

**Criar recado:**
1. Ir em **"Comunicação"** → **"Recados"**
2. Clicar em **"Enviar Novo Recado"**
3. Preencher:
   - **Título:** "Trabalho de Matemática Atrasado"
   - **Categoria:** Acadêmico
   - **Destinatários:** Selecionar **"Específico"** ← AQUI!
   - **Selecionar Alunos:** Marcar ☑ **Ana Carolina Souza (9º A)**
   - **Mensagem:** "Seu trabalho de matemática está atrasado. Por favor, entregar até sexta-feira."
4. Clicar em **"Enviar Recado"**
5. ✅ Sucesso! "Recado enviado com sucesso!"

---

### 2️⃣ Verificar como Aluna (Ana Carolina)

**Fazer logout e login como:**
```
Email: aluno@educonnect.com
Senha: aluno123
```

**Verificar:**
1. Ir em **"Comunicação"** → **"Recados"**
2. ✅ **DEVE VER** o recado "Trabalho de Matemática Atrasado"
3. Clicar no recado para ver detalhes
4. ✅ Recado completo aparece

**✅ SUCESSO:** Ana vê o recado porque ela foi selecionada como destinatária!

---

### 3️⃣ Verificar como Responsável (Mãe da Ana)

**Fazer logout e login como:**
```
Email: responsavel@educonnect.com
Senha: resp123
```

**Verificar:**
1. Ir em **"Comunicação"** → **"Recados"**
2. ✅ **DEVE VER** o recado "Trabalho de Matemática Atrasado"
3. O recado aparece porque Mariana é **MÃE** da Ana!

**✅ SUCESSO:** Responsável vê recados da filha automaticamente!

---

### 4️⃣ Verificar como Outro Aluno (Pedro)

**Fazer logout e login como:**
```
Email: pedro.aluno@educonnect.com
Senha: 123456
```

**Verificar:**
1. Ir em **"Comunicação"** → **"Recados"**
2. ❌ **NÃO DEVE VER** o recado "Trabalho de Matemática Atrasado"
3. Lista de recados está vazia ou sem esse recado

**✅ SUCESSO:** Pedro NÃO vê porque ele não foi selecionado!

---

### 5️⃣ Verificar como Outro Responsável (Pai do Pedro)

**Fazer logout e login como:**
```
Email: roberto.responsavel@educonnect.com
Senha: 123456
```

**Verificar:**
1. Ir em **"Comunicação"** → **"Recados"**
2. ❌ **NÃO DEVE VER** o recado "Trabalho de Matemática Atrasado"
3. Roberto só vê recados do filho (Pedro), não da Ana

**✅ SUCESSO:** Roberto NÃO vê porque o filho dele não foi selecionado!

---

### 6️⃣ Verificar como Diretoria

**Fazer logout e login como:**
```
Email: diretoria@educonnect.com
Senha: dir123
```

**Verificar:**
1. Ir em **"Comunicação"** → **"Recados"**
2. ✅ **DEVE VER** o recado "Trabalho de Matemática Atrasado"
3. Diretoria vê TODOS os recados, inclusive específicos

**✅ SUCESSO:** Gestão tem visão completa!

---

## 📊 Resultado Esperado

| Usuário | Email | Vê o Recado? | Por quê? |
|---------|-------|--------------|----------|
| **Ana Carolina** (Aluna) | aluno@educonnect.com | ✅ **SIM** | É destinatária direta |
| **Mariana** (Mãe da Ana) | responsavel@educonnect.com | ✅ **SIM** | Vinculada à filha |
| **Pedro** (Aluno) | pedro.aluno@educonnect.com | ❌ **NÃO** | Não é destinatário |
| **Roberto** (Pai do Pedro) | roberto.responsavel@educonnect.com | ❌ **NÃO** | Filho não é destinatário |
| **Professor** | professor@educonnect.com | ✅ **SIM** | Vê todos específicos |
| **Diretoria** | diretoria@educonnect.com | ✅ **SIM** | Gestão completa |
| **Admin** | admin@educonnect.com | ✅ **SIM** | Controle total |

---

## 🎯 Teste 2: Múltiplos Alunos

### Enviar para Ana E Pedro

**Login como Professor e criar novo recado:**

1. **Título:** "Reunião de Pais - 9º A e 8º B"
2. **Destinatários:** **Específico**
3. **Selecionar:**
   - ☑ Ana Carolina Souza (9º A)
   - ☑ Pedro Henrique Costa (8º B)
4. Enviar

### Verificar:

| Usuário | Vê? |
|---------|-----|
| Ana | ✅ |
| Mariana (mãe da Ana) | ✅ |
| Pedro | ✅ |
| Roberto (pai do Pedro) | ✅ |
| Outros | ❌ |

**✅ Ambas as famílias veem o recado!**

---

## 🔄 Teste 3: Editar Destinatários

### Parte 1: Criar Recado Geral

**Login como Diretoria:**
1. Criar recado para **"Todos"**
2. Título: "Teste de Edição"

**Verificar:** Todos veem o recado

### Parte 2: Editar para Específico

1. Abrir o recado criado
2. Clicar em **"Editar"**
3. Mudar destinatários para **"Específico"**
4. Selecionar apenas **Ana Carolina**
5. Salvar

**Verificar:**
- ✅ Ana vê
- ✅ Mariana vê
- ❌ Pedro NÃO vê mais
- ❌ Roberto NÃO vê mais

**✅ Destinatários foram atualizados corretamente!**

---

## 🎨 Interface - O que Esperar

### Ao Selecionar "Específico"

```
┌─────────────────────────────────────────────────┐
│ Destinatários: [Específico        ▼]            │
└─────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────┐
│ Selecionar Alunos *                             │
│                                                 │
│ ┌───────────────────────────────────────────┐ │
│ │ ☑ Ana Carolina Souza (9º A)               │ │
│ │   👤 Mariana Souza                        │ │
│ │                                           │ │
│ │ ☐ Pedro Henrique Costa (8º B)           │ │
│ │   👤 Roberto Costa                        │ │
│ └───────────────────────────────────────────┘ │
│                                                 │
│ 1 aluno(s) selecionado(s). O recado será       │
│ enviado para o aluno e seu responsável.        │
└─────────────────────────────────────────────────┘
```

### Elementos:
- ✅ Checkbox para cada aluno
- ✅ Nome do aluno em **negrito**
- ✅ Turma entre parênteses
- ✅ Nome do responsável com ícone 👤
- ✅ Contador de selecionados
- ✅ Mensagem explicativa

---

## ❌ Testes de Erro

### 1. Não Selecionar Nenhum Aluno

**Passos:**
1. Criar recado
2. Destinatários: **Específico**
3. **NÃO** marcar nenhum aluno
4. Tentar enviar

**Resultado Esperado:**
```
❌ Erro: "Selecione pelo menos um aluno para enviar o recado."
```

**✅ Validação funcionando!**

### 2. Usuário sem Permissão

**Passos:**
1. Login como **Aluno**
2. Tentar acessar `/recados/enviar`

**Resultado Esperado:**
```
❌ Redirecionado ou sem acesso ao botão "Enviar Recado"
```

**✅ Segurança funcionando!**

---

## 🎓 Entendendo a Vinculação

### Como Funciona:

```
┌─────────────────────────────────┐
│ RESPONSAVEL: Mariana Souza      │
│ (responsavel@educonnect.com)    │
│                                 │
│    ├─► Campo: alunosVinculados  │
│    │                             │
│    └─► [ Ana Carolina ]         │
│                                 │
│ Quando recado é enviado para    │
│ Ana, o sistema verifica:        │
│                                 │
│ 1. Ana é destinatária? ✅       │
│    → Ana vê o recado            │
│                                 │
│ 2. Mariana tem filhos?  ✅      │
│    → alunosVinculados = [Ana]   │
│                                 │
│ 3. Ana está na lista? ✅        │
│    → Mariana vê o recado        │
└─────────────────────────────────┘
```

---

## 📝 Checklist de Teste

- [ ] Enviar recado específico para 1 aluno
- [ ] Aluno destinatário vê o recado
- [ ] Responsável do aluno vê o recado
- [ ] Outro aluno NÃO vê o recado
- [ ] Outro responsável NÃO vê o recado
- [ ] Professor vê o recado específico
- [ ] Diretoria vê o recado específico
- [ ] Enviar recado para múltiplos alunos
- [ ] Ambos os alunos veem
- [ ] Ambos os responsáveis veem
- [ ] Editar recado de geral para específico
- [ ] Editar recado de específico para geral
- [ ] Validação de seleção vazia funciona
- [ ] Interface mostra nome do responsável
- [ ] Contador de alunos atualiza

---

## 🚀 Pronto para Testar!

**Ordem recomendada:**
1. ✅ Teste básico (Ana)
2. ✅ Verificar como responsável
3. ✅ Verificar que outros não veem
4. ✅ Teste com múltiplos alunos
5. ✅ Teste de edição
6. ✅ Testes de erro

**Tempo estimado:** ~10 minutos

**Boa sorte! 🎉**

