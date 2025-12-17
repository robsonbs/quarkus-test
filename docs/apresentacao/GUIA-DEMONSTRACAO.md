# 🎬 Guia de Demonstração

## Preparação Antes da Apresentação

### 1. Verificar Ambiente (5 minutos antes)

```bash
# Terminal 1: Verificar se o banco está rodando
docker compose ps

# Se não estiver, iniciar:
docker compose up -d postgres

# Aguardar banco ficar pronto
docker compose logs -f postgres
# Ctrl+C quando ver "ready to accept connections"
```

### 2. Iniciar Aplicação

```bash
# Terminal 2: Iniciar Quarkus em modo dev
./mvnw quarkus:dev

# Aguardar mensagem:
# Listening on: http://localhost:8080
```

### 3. Abrir Browser

```bash
# Abrir navegador com a aplicação
open http://localhost:8080
```

### 4. Preparar Janelas

* **Janela 1:** Browser com aplicação
* **Janela 2:** VS Code com código (opcional)
* **Janela 3:** Terminal com logs (opcional)

---

## Roteiro de Demonstração (15 minutos)

### 🔐 Parte 1: Autenticação (2 min)

#### Passo 1.1: Tela de Login

* [ ] Mostrar página de login
* [ ] Destacar campos de e-mail e senha
* [ ] Mencionar que usa autenticação Form-based (padrão Jakarta EE)

#### Passo 1.2: Login como Administrador

```
E-mail: admin@example.com
Senha: 123
```

* [ ] Fazer login
* [ ] Mostrar redirecionamento para `/users`
* [ ] Destacar menu de navegação com todas as opções

#### Passo 1.3: Verificar Menu Admin

* [ ] Mostrar que admin vê: Usuários, Perfis, Notas, Tarefas, Auditoria
* [ ] Destacar breadcrumbs funcionando

#### Passo 1.4: Logout

* [ ] Clicar em "Sair"
* [ ] Mostrar redirecionamento para login

---

### 👥 Parte 2: Gestão de Usuários - ADMIN (3 min)

#### Passo 2.1: Login Admin Novamente

```
E-mail: admin@example.com
Senha: 123
```

#### Passo 2.2: Listar Usuários

* [ ] Navegar para "Usuários"
* [ ] Mostrar tabela com usuários existentes
* [ ] Destacar colunas: Nome, E-mail, Perfil, Ações

#### Passo 2.3: Criar Novo Usuário

* [ ] Clicar em "+ Novo usuário"
* [ ] Preencher formulário:

```
Nome: João Silva
E-mail: joao@example.com
Senha: 123456
Perfil: USER
```

* [ ] Clicar em "Salvar"
* [ ] Mostrar mensagem de sucesso
* [ ] Mostrar usuário na lista

#### Passo 2.4: Editar Usuário

* [ ] Clicar no ícone de edição do usuário criado
* [ ] Alterar nome para "João Silva Junior"
* [ ] Salvar
* [ ] Mostrar mensagem de sucesso

#### Passo 2.5: Tentar Criar Usuário com E-mail Duplicado

* [ ] Clicar em "+ Novo usuário"
* [ ] Preencher com e-mail já existente
* [ ] Mostrar mensagem de erro de validação

---

### 🏷️ Parte 3: Gestão de Perfis - ADMIN (2 min)

#### Passo 3.1: Listar Perfis

* [ ] Navegar para "Perfis"
* [ ] Mostrar perfis existentes (ADMIN, USER)
* [ ] Destacar contagem de usuários por perfil

#### Passo 3.2: Criar Novo Perfil

* [ ] Clicar em "+ Novo perfil"
* [ ] Preencher: `GUEST`
* [ ] Salvar
* [ ] Mostrar novo perfil na lista

#### Passo 3.3: Validação de Nome Único

* [ ] Tentar criar perfil com nome existente
* [ ] Mostrar mensagem de erro

---

### 📝 Parte 4: Gestão de Notas - USER (3 min)

#### Passo 4.1: Login como Usuário Comum

* [ ] Fazer logout do admin
* [ ] Login com:

```
E-mail: user@example.com
Senha: 123
```

#### Passo 4.2: Verificar Menu Diferente

* [ ] Mostrar que menu tem menos opções
* [ ] Destacar que não aparece "Usuários", "Perfis", "Auditoria"

#### Passo 4.3: Criar Nova Nota

* [ ] Navegar para "Minhas Notas"
* [ ] Clicar em "+ Nova nota"
* [ ] Preencher:

```
Título: Reunião de projeto
Conteúdo: Discutir cronograma e entregas pendentes.
```

* [ ] Salvar
* [ ] Mostrar nota na lista

#### Passo 4.4: Editar Nota

* [ ] Clicar no ícone de edição
* [ ] Adicionar mais conteúdo
* [ ] Salvar

#### Passo 4.5: Demonstrar Ownership

* [ ] Destacar que usuário só vê suas próprias notas
* [ ] Mencionar que ownership é verificado no Service

---

### ✅ Parte 5: Gestão de Tarefas - USER (3 min)

#### Passo 5.1: Criar Nova Tarefa

* [ ] Navegar para "Minhas Tarefas"
* [ ] Clicar em "+ Nova tarefa"
* [ ] Preencher:

```
Título: Preparar apresentação
Descrição: Finalizar slides e testar demonstração
Status: PENDENTE
Data Limite: [data futura]
```

* [ ] Salvar

#### Passo 5.2: Workflow de Status

* [ ] Editar tarefa
* [ ] Mudar status para "EM_ANDAMENTO"
* [ ] Salvar
* [ ] Mostrar mudança na lista

* [ ] Editar novamente
* [ ] Mudar status para "CONCLUIDA"
* [ ] Salvar

#### Passo 5.3: Validação de Data Passada

* [ ] Criar nova tarefa
* [ ] Tentar colocar data passada
* [ ] Mostrar mensagem de erro

#### Passo 5.4: Demonstrar Validações

* [ ] Tentar salvar tarefa sem título
* [ ] Mostrar mensagem de erro

---

### 📊 Parte 6: Auditoria - ADMIN (2 min)

#### Passo 6.1: Login como Admin

* [ ] Logout do user
* [ ] Login como admin

#### Passo 6.2: Visualizar Logs de Auditoria

* [ ] Navegar para "Auditoria"
* [ ] Mostrar lista de eventos
* [ ] Destacar colunas: Ação, Entidade, Ator, Data

#### Passo 6.3: Filtrar Eventos

* [ ] Filtrar por ação "TASK_CREATED"
* [ ] Mostrar apenas eventos de criação de tarefas

#### Passo 6.4: Destacar Rastreabilidade

* [ ] Mostrar que todas as ações estão registradas
* [ ] Mencionar que inclui quem fez, quando e o que

---

### 🧪 Parte 7: Testes Automatizados (2 min)

#### Passo 7.1: Parar Aplicação

* [ ] Voltar ao terminal com Quarkus
* [ ] Pressionar `q` para parar (ou Ctrl+C)

#### Passo 7.2: Executar Testes

```bash
./mvnw test
```

#### Passo 7.3: Mostrar Resultado

* [ ] Aguardar execução (aproximadamente 30 segundos)
* [ ] Destacar:

```
Tests run: 185, Failures: 0, Errors: 0
```

#### Passo 7.4: Destacar Cobertura

* [ ] Mencionar tipos de testes:
  + Testes de Service (114)
  + Testes de DTO/Validação (26)
  + Testes de Integração (35)
  + Testes de Segurança (19)

---

## Pontos para Destacar Durante a Demo

### Arquitetura MVC

> "O projeto segue rigorosamente o padrão MVC. Controllers apenas orquestram requisições, Services contêm regras de negócio, DAOs fazem acesso ao banco."

### Padrão DTO

> "Nunca exponho entidades JPA diretamente. Todo dado de entrada usa RequestDTO, toda resposta usa ResponseDTO."

### Segurança

> "Autenticação via Elytron JDBC com senhas hasheadas em BCrypt. Controle de acesso via @RolesAllowed."

### Ownership

> "Usuários só acessam seus próprios dados. O Service valida ownership antes de cada operação."

### Auditoria

> "Toda ação sensível é registrada em audit_log com timestamp, ator e detalhes."

### Testes

> "185 testes automatizados garantem que o sistema funciona conforme especificado."

---

## Troubleshooting Durante Demo

### Problema: Banco não conecta

```bash
# Verificar se container está rodando
docker compose ps

# Reiniciar se necessário
docker compose down
docker compose up -d postgres
```

### Problema: Aplicação não inicia

```bash
# Verificar porta em uso
lsof -i :8080

# Matar processo se necessário
kill -9 <PID>

# Reiniciar
./mvnw quarkus:dev
```

### Problema: Dados não aparecem

```bash
# Verificar se migrations rodaram
docker compose logs postgres | grep flyway

# Reiniciar aplicação limpa
./mvnw quarkus:dev -Dquarkus.flyway.clean-at-start=true
```

### Problema: Login não funciona

* Verificar se está usando e-mail correto (case-sensitive)
* Verificar se senha é exatamente `123`
* Limpar cookies do navegador

---

## Checklist Final

### Antes da Apresentação

* [ ] Banco PostgreSQL rodando
* [ ] Aplicação iniciada sem erros
* [ ] Browser aberto em localhost:8080
* [ ] Testou login com admin e user
* [ ] Testou criar nota e tarefa
* [ ] Executou testes (185 passando)

### Durante a Apresentação

* [ ] Falar devagar e explicar cada ação
* [ ] Destacar padrões de projeto usados
* [ ] Mostrar código quando relevante
* [ ] Responder perguntas com calma

### Após a Apresentação

* [ ] Parar aplicação (`q` ou Ctrl+C)
* [ ] Parar banco se necessário (`docker compose down`)
