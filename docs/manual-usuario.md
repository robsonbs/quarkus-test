# 📖 Manual do Usuário

> Guia completo para utilização do Sistema de Gestão de Notas e Tarefas.

---

## 📋 Índice

* [Introdução](#introdução)
* [Primeiro Acesso](#primeiro-acesso)
* [Navegação](#navegação)
* [Módulo de Notas](#módulo-de-notas)
* [Módulo de Tarefas](#módulo-de-tarefas)
* [Administração](#administração)
* [Auditoria](#auditoria)
* [Dicas e Atalhos](#dicas-e-atalhos)
* [Solução de Problemas](#solução-de-problemas)

---

## Introdução

O **Sistema de Gestão de Notas e Tarefas** é uma aplicação web que permite:

* 📝 **Criar e gerenciar anotações pessoais**
* ✅ **Organizar tarefas com prazos e status**
* 👥 **Administrar usuários e perfis** (apenas administradores)
* 📊 **Visualizar logs de auditoria** (apenas administradores)

### Perfis de Usuário

| Perfil | Permissões |
|--------|------------|
| **USER** | Gerenciar suas próprias notas e tarefas |
| **ADMIN** | Todas as permissões + gerenciar usuários, perfis e visualizar auditoria |

---

## Primeiro Acesso

### 1. Acessar o Sistema

1. Abra o navegador e acesse: `http://localhost:8080`
2. Você será redirecionado para a tela de login

### 2. Fazer Login

![Tela de Login](../assets/login-screen.png)

1. Digite seu **e-mail** no primeiro campo
2. Digite sua **senha** no segundo campo
3. Clique em **"Entrar"**

### Contas de Demonstração

| Tipo | E-mail | Senha |
|------|--------|-------|
| Administrador | `admin@example.com` | `123` |
| Usuário comum | `user@example.com` | `123` |

### 3. Fazer Logout

1. Clique no link **"Sair"** no canto superior direito do menu
2. Você será redirecionado para a tela de login

---

## Navegação

### Menu Principal

O menu de navegação está sempre visível no topo da página:

```
┌─────────────────────────────────────────────────────────────────┐
│  🏠 Quarkus Demo    Usuários  Perfis  Notas  Tarefas  Docs  Sair│
└─────────────────────────────────────────────────────────────────┘
```

| Link | Descrição | Perfis |
|------|-----------|--------|
| **Quarkus Demo** | Página inicial | Todos |
| **Usuários** | Gerenciar usuários | ADMIN |
| **Perfis** | Gerenciar perfis de acesso | ADMIN |
| **Notas** | Suas anotações pessoais | USER, ADMIN |
| **Tarefas** | Suas tarefas | USER, ADMIN |
| **Auditoria** | Logs do sistema | ADMIN |
| **Docs** | Documentação | Todos |
| **Sair** | Encerrar sessão | Todos |

### Breadcrumbs

Os breadcrumbs mostram sua localização atual no sistema:

```
Início > Notas > Nova nota
```

Clique em qualquer item para voltar àquela página.

### Mensagens de Feedback

O sistema exibe mensagens coloridas após cada ação:

* 🟢 **Verde**: Sucesso (ex: "Nota criada com sucesso")
* 🔴 **Vermelho**: Erro (ex: "Título é obrigatório")

---

## Módulo de Notas

O módulo de notas permite criar, editar e excluir anotações pessoais.

### Listar Notas

**Caminho:** Menu → Notas

A página exibe todas as suas notas em uma tabela:

| Coluna | Descrição |
|--------|-----------|
| Título | Nome da nota |
| Criada em | Data de criação |
| Ações | Botões Editar e Excluir |

### Criar Nova Nota

1. Na página de notas, clique em **"+ Nova nota"**
2. Preencha os campos:
   - **Título** (obrigatório): Nome da nota
   - **Conteúdo**: Texto da nota
3. Clique em **"Salvar nota"**

### Editar Nota

1. Na lista de notas, clique em **"Editar"** na linha desejada
2. Altere os campos necessários
3. Clique em **"Atualizar nota"**

### Excluir Nota

1. Na lista de notas, clique em **"Excluir"** na linha desejada
2. Confirme a exclusão no popup
3. A nota será removida permanentemente

### Validações

| Campo | Regra |
|-------|-------|
| Título | Obrigatório, máximo 200 caracteres |
| Conteúdo | Opcional, sem limite |

---

## Módulo de Tarefas

O módulo de tarefas permite organizar atividades com status e prazos.

### Listar Tarefas

**Caminho:** Menu → Tarefas

A página exibe todas as suas tarefas em uma tabela:

| Coluna | Descrição |
|--------|-----------|
| Título | Nome da tarefa |
| Prazo | Data limite (se definida) |
| Status | Pendente, Em andamento ou Concluída |
| Criada em | Data de criação |
| Ações | Botões Editar e Excluir |

### Status das Tarefas

| Status | Cor | Descrição |
|--------|-----|-----------|
| 🟡 **Pendente** | Cinza | Tarefa não iniciada |
| 🟠 **Em andamento** | Âmbar | Tarefa em execução |
| 🟢 **Concluída** | Verde | Tarefa finalizada |

### Criar Nova Tarefa

1. Na página de tarefas, clique em **"+ Nova tarefa"**
2. Preencha os campos:
   - **Título** (obrigatório): Nome da tarefa
   - **Descrição**: Detalhes adicionais
   - **Data limite**: Prazo para conclusão
   - **Status**: Estado atual da tarefa
3. Clique em **"Salvar tarefa"**

### Editar Tarefa

1. Na lista de tarefas, clique em **"Editar"** na linha desejada
2. Altere os campos necessários (incluindo status)
3. Clique em **"Atualizar tarefa"**

### Excluir Tarefa

1. Na lista de tarefas, clique em **"Excluir"** na linha desejada
2. Confirme a exclusão no popup
3. A tarefa será removida permanentemente

### Validações

| Campo | Regra |
|-------|-------|
| Título | Obrigatório, máximo 200 caracteres |
| Descrição | Opcional |
| Data limite | Não pode ser data passada |
| Status | Obrigatório (padrão: Pendente) |

### Fluxo de Status

```
┌──────────┐     ┌──────────────┐     ┌───────────┐
│ Pendente │ ──► │ Em andamento │ ──► │ Concluída │
└──────────┘     └──────────────┘     └───────────┘
      ▲                 │                    │
      └─────────────────┴────────────────────┘
            (pode voltar a qualquer status)
```

---

## Administração

> ⚠️ **Atenção:** Esta seção é acessível apenas para usuários com perfil **ADMIN**.

### Gerenciar Usuários

**Caminho:** Menu → Usuários

#### Listar Usuários

A página exibe todos os usuários cadastrados:

| Coluna | Descrição |
|--------|-----------|
| Nome | Nome completo do usuário |
| E-mail | E-mail de acesso |
| Perfil | ADMIN ou USER |
| Ações | Botões Editar e Excluir |

#### Criar Novo Usuário

1. Clique em **"+ Novo usuário"**
2. Preencha os campos:
   - **Nome** (obrigatório)
   - **E-mail** (obrigatório, único)
   - **Senha** (obrigatório, mínimo 3 caracteres)
   - **Perfil** (selecione ADMIN ou USER)
3. Clique em **"Salvar usuário"**

#### Editar Usuário

1. Clique em **"Editar"** na linha do usuário
2. Altere os campos necessários
3. Deixe a senha em branco para manter a atual
4. Clique em **"Atualizar usuário"**

#### Excluir Usuário

1. Clique em **"Excluir"** na linha do usuário
2. Confirme a exclusão
3. Notas e tarefas do usuário também serão excluídas

### Gerenciar Perfis

**Caminho:** Menu → Perfis

#### Listar Perfis

A página exibe todos os perfis de acesso:

| Coluna | Descrição |
|--------|-----------|
| Nome | Nome do perfil (ADMIN, USER) |
| Ações | Botões Editar e Excluir |

#### Criar Novo Perfil

1. Clique em **"+ Novo perfil"**
2. Digite o nome do perfil
3. Clique em **"Salvar perfil"**

#### Editar Perfil

1. Clique em **"Editar"** na linha do perfil
2. Altere o nome
3. Clique em **"Atualizar perfil"**

#### Excluir Perfil

1. Clique em **"Excluir"** na linha do perfil
2. **Nota:** Perfis com usuários vinculados não podem ser excluídos

---

## Auditoria

> ⚠️ **Atenção:** Esta seção é acessível apenas para usuários com perfil **ADMIN**.

**Caminho:** Menu → Auditoria

### Visualizar Logs

A página de auditoria exibe todas as ações executadas no sistema:

| Coluna | Descrição |
|--------|-----------|
| Data/Hora | Quando a ação ocorreu |
| Usuário | Quem executou a ação |
| Ação | O que foi feito |
| Método | GET, POST, etc. |
| Recurso | Caminho acessado |
| Status | Código HTTP |
| Entidade | Tipo de dado afetado |

### Filtros Disponíveis

| Filtro | Descrição |
|--------|-----------|
| **Usuário** | Filtrar por e-mail do usuário |
| **Método HTTP** | GET, POST, DELETE, etc. |
| **Recurso** | Caminho da URL |
| **Tipo de Entidade** | User, Note, Task, etc. |
| **ID da Entidade** | ID específico |
| **Data inicial** | A partir de quando |
| **Data final** | Até quando |

### Usando os Filtros

1. Preencha um ou mais filtros
2. Clique em **"Filtrar"**
3. Use **"Limpar"** para remover os filtros

### Paginação

* Use os botões **"Anterior"** e **"Próxima"** para navegar entre páginas
* O total de registros é exibido no topo da tabela

---

## Dicas e Atalhos

### Boas Práticas

1. **Notas**
   - Use títulos descritivos para facilitar a busca
   - Organize informações importantes no início do conteúdo

2. **Tarefas**
   - Defina datas limite realistas
   - Atualize o status conforme progride
   - Marque como concluída ao finalizar

3. **Segurança**
   - Não compartilhe suas credenciais
   - Faça logout ao terminar de usar
   - Use senhas fortes

### Teclas de Atalho

| Tecla | Ação |
|-------|------|
| `Tab` | Navegar entre campos |
| `Enter` | Submeter formulário |
| `Esc` | Cancelar operação |

---

## Solução de Problemas

### "Não consigo fazer login"

1. Verifique se o e-mail está correto
2. Verifique se a senha está correta (maiúsculas/minúsculas)
3. Tente com as contas demo: `admin@example.com` ou `user@example.com`
4. Limpe os cookies do navegador

### "Acesso negado (403)"

1. Você pode estar tentando acessar uma área restrita
2. Faça login com uma conta com permissão adequada
3. Administração requer perfil ADMIN

### "Página não encontrada (404)"

1. Verifique se o endereço está correto
2. O recurso pode ter sido excluído
3. Volte para a página inicial e navegue novamente

### "Erro interno (500)"

1. Tente novamente em alguns segundos
2. Se persistir, entre em contato com o administrador
3. Anote o identificador do incidente exibido na página

### "Não consigo excluir um perfil"

O perfil não pode ser excluído se houver usuários vinculados a ele. Primeiro, altere o perfil desses usuários ou exclua-os.

### "Data limite inválida"

A data limite da tarefa não pode ser uma data que já passou. Selecione uma data futura.

### "E-mail já cadastrado"

Cada e-mail só pode ser usado por um usuário. Use um e-mail diferente ou edite o usuário existente.

---

## Contato e Suporte

Em caso de dúvidas ou problemas:

1. Consulte a documentação em **Menu → Docs**
2. Verifique os logs de auditoria (se for ADMIN)
3. Entre em contato com o administrador do sistema

---

<p align="center">
  <strong>Sistema de Gestão de Notas e Tarefas</strong><br>
  Versão 1.0 • 2025
</p>
