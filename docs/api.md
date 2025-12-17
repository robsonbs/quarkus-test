# 🔌 Documentação da API

> Referência completa dos endpoints REST do Sistema de Gestão de Notas e Tarefas.

---

## 📋 Índice

* [Visão Geral](#visão-geral)
* [Autenticação](#autenticação)
* [Endpoints de Usuários](#endpoints-de-usuários)
* [Endpoints de Perfis](#endpoints-de-perfis)
* [Endpoints de Notas](#endpoints-de-notas)
* [Endpoints de Tarefas](#endpoints-de-tarefas)
* [Endpoints de Auditoria](#endpoints-de-auditoria)
* [DTOs](#dtos)
* [Códigos de Erro](#códigos-de-erro)

---

## Visão Geral

### Base URL

```
http://localhost:8080
```

### Content Types

| Tipo | Uso |
|------|-----|
| `text/html` | Respostas de páginas HTML |
| `application/x-www-form-urlencoded` | Envio de formulários |
| `application/json` | Endpoints de API (quando disponível) |

### Autenticação

Todas as rotas (exceto `/login` ) requerem autenticação via sessão HTTP.

### Roles (Perfis)

| Role | Descrição |
|------|-----------|
| `ADMIN` | Acesso total |
| `USER` | Acesso a notas e tarefas próprias |

---

## Autenticação

### Página de Login

```http
GET /login
```

**Resposta:** HTML da página de login

---

### Processar Login

```http
POST /j_security_check
Content-Type: application/x-www-form-urlencoded

j_username=email@example.com&j_password=senha123
```

**Parâmetros:**

| Campo | Tipo | Obrigatório | Descrição |
|-------|------|-------------|-----------|
| `j_username` | string | Sim | E-mail do usuário |
| `j_password` | string | Sim | Senha do usuário |

**Respostas:**

| Status | Descrição |
|--------|-----------|
| `302` | Sucesso - Redireciona para `/users` |
| `302` | Falha - Redireciona para `/login?error=true` |

**Exemplo com cURL:**

```bash
curl -X POST \
  -d "j_username=admin@example.com&j_password=123" \
  -c cookies.txt \
  http://localhost:8080/j_security_check
```

---

### Logout

```http
POST /logout
```

**Resposta:** Redireciona para `/login`

**Exemplo com cURL:**

```bash
curl -X POST \
  -b cookies.txt \
  http://localhost:8080/logout
```

---

## Endpoints de Usuários

> 🔒 **Requer:** Role `ADMIN`

### Listar Usuários

```http
GET /users
```

**Query Parameters:**

| Parâmetro | Tipo | Descrição |
|-----------|------|-----------|
| `success` | string | Mensagem de sucesso |
| `error` | string | Mensagem de erro |

**Resposta:** HTML com lista de usuários

---

### Formulário Novo Usuário

```http
GET /users/new
```

**Resposta:** HTML com formulário de criação

---

### Criar Usuário

```http
POST /users
Content-Type: application/x-www-form-urlencoded

name=João Silva&email=joao@example.com&password=senha123&profileId=2
```

**Parâmetros:**

| Campo | Tipo | Obrigatório | Descrição |
|-------|------|-------------|-----------|
| `name` | string | Sim | Nome completo |
| `email` | string | Sim | E-mail (único) |
| `password` | string | Sim | Senha (mín. 3 caracteres) |
| `profileId` | long | Sim | ID do perfil |

**Respostas:**

| Status | Descrição |
|--------|-----------|
| `303` | Sucesso - Redireciona para `/users?success=...` |
| `303` | Erro - Redireciona para `/users/new?error=...` |

**Exemplo com cURL:**

```bash
curl -X POST \
  -b cookies.txt \
  -d "name=João Silva&email=joao@example.com&password=123&profileId=2" \
  http://localhost:8080/users
```

---

### Formulário Editar Usuário

```http
GET /users/{id}
```

**Path Parameters:**

| Parâmetro | Tipo | Descrição |
|-----------|------|-----------|
| `id` | long | ID do usuário |

**Resposta:** HTML com formulário de edição

---

### Atualizar Usuário

```http
POST /users/{id}
Content-Type: application/x-www-form-urlencoded

name=João Silva Atualizado&email=joao@example.com&password=&profileId=2
```

**Parâmetros:**

| Campo | Tipo | Obrigatório | Descrição |
|-------|------|-------------|-----------|
| `name` | string | Sim | Nome completo |
| `email` | string | Sim | E-mail (único) |
| `password` | string | Não | Nova senha (vazio = manter) |
| `profileId` | long | Sim | ID do perfil |

**Respostas:**

| Status | Descrição |
|--------|-----------|
| `303` | Sucesso - Redireciona para `/users?success=...` |
| `303` | Erro - Redireciona para `/users/{id}?error=...` |

---

### Excluir Usuário

```http
POST /users/{id}/delete
```

**Path Parameters:**

| Parâmetro | Tipo | Descrição |
|-----------|------|-----------|
| `id` | long | ID do usuário |

**Respostas:**

| Status | Descrição |
|--------|-----------|
| `303` | Sucesso - Redireciona para `/users?success=...` |
| `303` | Erro - Redireciona para `/users?error=...` |

---

## Endpoints de Perfis

> 🔒 **Requer:** Role `ADMIN`

### Listar Perfis

```http
GET /profiles
```

**Resposta:** HTML com lista de perfis

---

### Formulário Novo Perfil

```http
GET /profiles/new
```

**Resposta:** HTML com formulário de criação

---

### Criar Perfil

```http
POST /profiles
Content-Type: application/x-www-form-urlencoded

name=MANAGER
```

**Parâmetros:**

| Campo | Tipo | Obrigatório | Descrição |
|-------|------|-------------|-----------|
| `name` | string | Sim | Nome do perfil (único) |

**Respostas:**

| Status | Descrição |
|--------|-----------|
| `303` | Sucesso - Redireciona para `/profiles?success=...` |
| `303` | Erro - Redireciona para `/profiles/new?error=...` |

---

### Formulário Editar Perfil

```http
GET /profiles/{id}
```

**Resposta:** HTML com formulário de edição

---

### Atualizar Perfil

```http
POST /profiles/{id}
Content-Type: application/x-www-form-urlencoded

name=SUPERVISOR
```

**Respostas:**

| Status | Descrição |
|--------|-----------|
| `303` | Sucesso - Redireciona para `/profiles?success=...` |
| `303` | Erro - Redireciona para `/profiles/{id}?error=...` |

---

### Excluir Perfil

```http
POST /profiles/{id}/delete
```

**Respostas:**

| Status | Descrição |
|--------|-----------|
| `303` | Sucesso - Redireciona para `/profiles?success=...` |
| `303` | Erro - Redireciona para `/profiles?error=...` (se houver usuários vinculados) |

---

## Endpoints de Notas

> 🔒 **Requer:** Roles `USER` ou `ADMIN`

### Listar Notas

```http
GET /notes
```

**Query Parameters:**

| Parâmetro | Tipo | Descrição |
|-----------|------|-----------|
| `success` | string | Mensagem de sucesso |
| `error` | string | Mensagem de erro |

**Resposta:** HTML com lista de notas do usuário autenticado

---

### Formulário Nova Nota

```http
GET /notes/new
```

**Resposta:** HTML com formulário de criação

---

### Criar Nota

```http
POST /notes
Content-Type: application/x-www-form-urlencoded

title=Minha Nota&content=Conteúdo da nota
```

**Parâmetros:**

| Campo | Tipo | Obrigatório | Descrição |
|-------|------|-------------|-----------|
| `title` | string | Sim | Título (máx. 200 caracteres) |
| `content` | string | Não | Conteúdo da nota |

**Respostas:**

| Status | Descrição |
|--------|-----------|
| `303` | Sucesso - Redireciona para `/notes?success=created` |
| `303` | Erro - Redireciona para `/notes/new?error=...` |

**Exemplo com cURL:**

```bash
curl -X POST \
  -b cookies.txt \
  -d "title=Minha Nota&content=Conteúdo da nota" \
  http://localhost:8080/notes
```

---

### Formulário Editar Nota

```http
GET /notes/{id}
```

**Path Parameters:**

| Parâmetro | Tipo | Descrição |
|-----------|------|-----------|
| `id` | long | ID da nota |

**Resposta:** HTML com formulário de edição

**Erros:**

| Status | Descrição |
|--------|-----------|
| `404` | Nota não encontrada |
| `403` | Nota pertence a outro usuário |

---

### Atualizar Nota

```http
POST /notes/{id}
Content-Type: application/x-www-form-urlencoded

title=Nota Atualizada&content=Novo conteúdo
```

**Respostas:**

| Status | Descrição |
|--------|-----------|
| `303` | Sucesso - Redireciona para `/notes?success=updated` |
| `303` | Erro - Redireciona para `/notes/{id}?error=...` |

---

### Excluir Nota

```http
POST /notes/{id}/delete
```

**Respostas:**

| Status | Descrição |
|--------|-----------|
| `303` | Sucesso - Redireciona para `/notes?success=deleted` |
| `303` | Erro - Redireciona para `/notes?error=...` |

---

## Endpoints de Tarefas

> 🔒 **Requer:** Roles `USER` ou `ADMIN`

### Listar Tarefas

```http
GET /tasks
```

**Query Parameters:**

| Parâmetro | Tipo | Descrição |
|-----------|------|-----------|
| `success` | string | Mensagem de sucesso |
| `error` | string | Mensagem de erro |

**Resposta:** HTML com lista de tarefas do usuário autenticado

---

### Formulário Nova Tarefa

```http
GET /tasks/new
```

**Resposta:** HTML com formulário de criação

---

### Criar Tarefa

```http
POST /tasks
Content-Type: application/x-www-form-urlencoded

title=Minha Tarefa&description=Descrição&dueDate=2025-12-31&status=PENDING
```

**Parâmetros:**

| Campo | Tipo | Obrigatório | Descrição |
|-------|------|-------------|-----------|
| `title` | string | Sim | Título (máx. 200 caracteres) |
| `description` | string | Não | Descrição detalhada |
| `dueDate` | date | Não | Data limite (formato: YYYY-MM-DD) |
| `status` | enum | Não | `PENDING` , `IN_PROGRESS` , `COMPLETED` |

**Validações:**
* `dueDate` não pode ser data passada

**Respostas:**

| Status | Descrição |
|--------|-----------|
| `303` | Sucesso - Redireciona para `/tasks?success=created` |
| `303` | Erro - Redireciona para `/tasks/new?error=...` |

**Exemplo com cURL:**

```bash
curl -X POST \
  -b cookies.txt \
  -d "title=Estudar Quarkus&description=Capítulos 1-5&dueDate=2025-12-31&status=PENDING" \
  http://localhost:8080/tasks
```

---

### Formulário Editar Tarefa

```http
GET /tasks/{id}
```

**Resposta:** HTML com formulário de edição

---

### Atualizar Tarefa

```http
POST /tasks/{id}
Content-Type: application/x-www-form-urlencoded

title=Tarefa Atualizada&description=Nova descrição&dueDate=2025-12-31&status=IN_PROGRESS
```

**Respostas:**

| Status | Descrição |
|--------|-----------|
| `303` | Sucesso - Redireciona para `/tasks?success=updated` |
| `303` | Erro - Redireciona para `/tasks/{id}?error=...` |

---

### Excluir Tarefa

```http
POST /tasks/{id}/delete
```

**Respostas:**

| Status | Descrição |
|--------|-----------|
| `303` | Sucesso - Redireciona para `/tasks?success=deleted` |
| `303` | Erro - Redireciona para `/tasks?error=...` |

---

## Endpoints de Auditoria

> 🔒 **Requer:** Role `ADMIN`

### Listar Logs de Auditoria

```http
GET /audit
```

**Query Parameters:**

| Parâmetro | Tipo | Descrição |
|-----------|------|-----------|
| `username` | string | Filtrar por e-mail do usuário |
| `method` | string | Filtrar por método HTTP |
| `resource` | string | Filtrar por caminho do recurso |
| `entityType` | string | Filtrar por tipo de entidade |
| `entityId` | string | Filtrar por ID da entidade |
| `from` | date | Data inicial (YYYY-MM-DD) |
| `to` | date | Data final (YYYY-MM-DD) |
| `page` | int | Número da página (0-based) |
| `size` | int | Itens por página (padrão: 20) |

**Resposta:** HTML com tabela paginada de logs

**Exemplo com cURL:**

```bash
# Listar logs do usuário admin
curl -b cookies.txt \
  "http://localhost:8080/audit?username=admin@example.com&page=0&size=10"

# Listar logs de criação de notas
curl -b cookies.txt \
  "http://localhost:8080/audit?entityType=Note&method=POST"
```

---

## DTOs

### UserRequestDTO

```java
{
  "name": "string",       // Nome completo (obrigatório)
  "email": "string",      // E-mail único (obrigatório)
  "password": "string",   // Senha (obrigatório na criação)
  "profileId": "long"     // ID do perfil (obrigatório)
}
```

### UserResponseDTO

```java
{
  "id": "long",
  "name": "string",
  "email": "string",
  "profileId": "long",
  "profileName": "string",
  "createdAt": "datetime"
}
```

### UserProfileRequestDTO

```java
{
  "name": "string"        // Nome do perfil (obrigatório, único)
}
```

### UserProfileResponseDTO

```java
{
  "id": "long",
  "name": "string",
  "userCount": "int"      // Quantidade de usuários vinculados
}
```

### NoteRequestDTO

```java
{
  "title": "string",      // Título (obrigatório, máx. 200)
  "content": "string"     // Conteúdo (opcional)
}
```

### NoteResponseDTO

```java
{
  "id": "long",
  "title": "string",
  "content": "string",
  "ownerName": "string",
  "createdAt": "datetime",
  "updatedAt": "datetime"
}
```

### TaskRequestDTO

```java
{
  "title": "string",      // Título (obrigatório, máx. 200)
  "description": "string", // Descrição (opcional)
  "dueDate": "date",      // Data limite YYYY-MM-DD (opcional)
  "status": "string"      // PENDING, IN_PROGRESS, COMPLETED
}
```

### TaskResponseDTO

```java
{
  "id": "long",
  "title": "string",
  "description": "string",
  "dueDate": "date",
  "status": "TaskStatus",
  "ownerName": "string",
  "createdAt": "datetime",
  "updatedAt": "datetime"
}
```

### TaskStatus (Enum)

```java
{
  "PENDING": "Pendente",
  "IN_PROGRESS": "Em andamento",
  "COMPLETED": "Concluída"
}
```

### AuditLogResponseDTO

```java
{
  "id": "long",
  "username": "string",
  "action": "string",
  "httpMethod": "string",
  "resourcePath": "string",
  "clientIp": "string",
  "statusCode": "int",
  "entityType": "string",
  "entityId": "string",
  "userAgent": "string",
  "details": "string",
  "occurredAt": "datetime"
}
```

### AuditLogFilterDTO

```java
{
  "username": "string",
  "method": "string",
  "resource": "string",
  "entityType": "string",
  "entityId": "string",
  "from": "date",
  "to": "date",
  "page": "int",
  "size": "int"
}
```

---

## Códigos de Erro

### Códigos HTTP

| Código | Nome | Descrição |
|--------|------|-----------|
| `200` | OK | Requisição bem-sucedida |
| `302` | Found | Redirecionamento (login) |
| `303` | See Other | Redirecionamento (após POST) |
| `400` | Bad Request | Dados inválidos |
| `401` | Unauthorized | Não autenticado |
| `403` | Forbidden | Sem permissão |
| `404` | Not Found | Recurso não encontrado |
| `405` | Method Not Allowed | Método HTTP inválido |
| `415` | Unsupported Media Type | Content-Type inválido |
| `500` | Internal Server Error | Erro interno |

### Mensagens de Erro Comuns

| Mensagem | Causa |
|----------|-------|
| "Título é obrigatório" | Campo title vazio ou nulo |
| "Nome é obrigatório" | Campo name vazio ou nulo |
| "E-mail é obrigatório" | Campo email vazio ou nulo |
| "E-mail já cadastrado" | E-mail duplicado |
| "Senha é obrigatória" | Campo password vazio (na criação) |
| "Senha deve ter pelo menos 3 caracteres" | Senha muito curta |
| "Data limite não pode ser no passado" | dueDate anterior à data atual |
| "Recurso não encontrado" | ID inexistente |
| "Acesso negado" | Tentativa de acessar recurso de outro usuário |
| "Perfil possui usuários vinculados" | Tentativa de excluir perfil em uso |

---

## Exemplos de Fluxo

### Fluxo Completo: Criar e Listar Notas

```bash
# 1. Login
curl -c cookies.txt -X POST \
  -d "j_username=user@example.com&j_password=123" \
  http://localhost:8080/j_security_check

# 2. Criar nota
curl -b cookies.txt -X POST \
  -d "title=Minha Primeira Nota&content=Conteúdo aqui" \
  http://localhost:8080/notes

# 3. Listar notas
curl -b cookies.txt http://localhost:8080/notes

# 4. Logout
curl -b cookies.txt -X POST http://localhost:8080/logout
```

### Fluxo Completo: Gerenciar Tarefa

```bash
# Login como usuário
curl -c cookies.txt -X POST \
  -d "j_username=user@example.com&j_password=123" \
  http://localhost:8080/j_security_check

# Criar tarefa
curl -b cookies.txt -X POST \
  -d "title=Estudar Java&description=Capítulo 1&dueDate=2025-12-31&status=PENDING" \
  http://localhost:8080/tasks

# Atualizar status para Em Andamento
curl -b cookies.txt -X POST \
  -d "title=Estudar Java&description=Capítulo 1&dueDate=2025-12-31&status=IN_PROGRESS" \
  http://localhost:8080/tasks/1

# Marcar como Concluída
curl -b cookies.txt -X POST \
  -d "title=Estudar Java&description=Capítulo 1&dueDate=2025-12-31&status=COMPLETED" \
  http://localhost:8080/tasks/1
```

---

## Ferramentas Recomendadas

### Testar Endpoints

* **cURL** - Linha de comando
* **Postman** - Interface gráfica
* **Insomnia** - Interface gráfica
* **HTTPie** - Linha de comando amigável

### Importar no Postman

1. Crie uma nova Collection
2. Configure variável `{{baseUrl}}` = `http://localhost:8080`
3. Configure variável `{{cookies}}` após login

---

<p align="center">
  <strong>API versão 1.0</strong><br>
  Documentação gerada em 2025
</p>
