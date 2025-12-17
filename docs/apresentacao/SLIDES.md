# 📊 Apresentação do Projeto

## Sistema de Gestão de Notas e Tarefas

### Programação para Web III - IFG Campus Luziânia

---

# 🎯 Slide 1: Visão Geral

## Sistema de Gestão de Notas e Tarefas

**Objetivo:** Demonstrar domínio de arquitetura MVC com tecnologias Java modernas

### Principais Características

* 🔐 **Autenticação segura** com controle de acesso por perfis
* 👥 **Gestão de usuários e perfis** (ADMIN)
* 📝 **Notas pessoais** por usuário
* ✅ **Tarefas com workflow** de status e prazos
* 📊 **Auditoria completa** de todas as ações

### Contas de Demonstração

| Perfil | E-mail | Senha |
|--------|--------|-------|
| 👑 Admin | `admin@example.com` | `123` |
| 👤 Usuário | `user@example.com` | `123` |

---

# 🏗️ Slide 2: Arquitetura MVC

```
┌────────────────────────────────────────────────────────────────┐
│                    CAMADA DE APRESENTAÇÃO                       │
│         Templates Qute + Tags Reutilizáveis + Tailwind CSS      │
└────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌────────────────────────────────────────────────────────────────┐
│                   CAMADA CONTROLLER (JAX-RS)                    │
│   • Recebe requisições HTTP    • Prepara DTOs para templates    │
│   • Controla fluxo             • Redireciona com feedback       │
└────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌────────────────────────────────────────────────────────────────┐
│                  CAMADA SERVICE/BO (Negócio)                    │
│   • Regras de negócio          • Validações complexas           │
│   • Hash de senhas             • Verificação de ownership       │
│   • Auditoria de eventos       • Controle transacional          │
└────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌────────────────────────────────────────────────────────────────┐
│                      CAMADA DAO (Dados)                         │
│   • Acesso ao banco            • Queries JPQL                   │
│   • Repositórios Panache       • Operações CRUD                 │
└────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌────────────────────────────────────────────────────────────────┐
│                    CAMADA MODEL (Entidades)                     │
│   • Mapeamento JPA             • Relacionamentos                │
│   • Validações de schema       • Callbacks de ciclo de vida     │
└────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌────────────────────────────────────────────────────────────────┐
│                      PostgreSQL 15                              │
│              Migrações controladas por Flyway                   │
└────────────────────────────────────────────────────────────────┘
```

---

# 🛠️ Slide 3: Stack Tecnológica

## Backend

| Tecnologia | Versão | Propósito |
|------------|--------|-----------|
| **Java** | 17 LTS | Linguagem de programação |
| **Quarkus** | 3.6.4 | Framework Java moderno |
| **JAX-RS** | 3.x | Endpoints REST/MVC |
| **Qute** | - | Template engine |
| **Hibernate/Panache** | 6.x | ORM e repositórios |
| **Elytron Security** | - | Autenticação JDBC |
| **Flyway** | - | Migrações de banco |
| **BCrypt** | - | Hash de senhas |

## Frontend

| Tecnologia | Propósito |
|------------|-----------|
| **HTML5** | Estrutura semântica |
| **Tailwind CSS** | Estilização responsiva |
| **JavaScript** | Interatividade |

## Infraestrutura

| Tecnologia | Propósito |
|------------|-----------|
| **PostgreSQL 15** | Banco de dados |
| **Docker Compose** | Containerização |
| **Maven Wrapper** | Build e dependências |

---

# 📁 Slide 4: Estrutura do Projeto

```
src/main/java/com/robsonbs/
├── controller/          # 9 Controllers (JAX-RS)
│   ├── AuditController.java
│   ├── DocsController.java
│   ├── HomeController.java
│   ├── LoginController.java
│   ├── LogoutController.java
│   ├── NoteController.java
│   ├── TaskController.java
│   ├── UserController.java
│   └── UserProfileController.java
│
├── service/             # 6 Services (Camada BO)
│   ├── AuthService.java
│   ├── AuditLogService.java
│   ├── NoteService.java
│   ├── TaskService.java
│   ├── UserService.java
│   └── UserProfileService.java
│
├── dao/                 # 5 DAOs (Panache Repositories)
│   ├── AuditLogDao.java
│   ├── NoteDao.java
│   ├── TaskDao.java
│   ├── UserDao.java
│   └── UserProfileDao.java
│
├── model/               # 6 Entities (JPA)
│   ├── AuditLog.java
│   ├── Note.java
│   ├── Task.java
│   ├── TaskStatus.java
│   ├── User.java
│   └── UserProfile.java
│
├── dto/                 # 12 DTOs
│   ├── *RequestDTO.java   # Entrada de dados
│   └── *ResponseDTO.java  # Saída de dados
│
└── view/                # Helpers de visualização
    └── BreadcrumbItem.java
```

```
src/main/resources/templates/
├── *.html               # 12 páginas principais
├── tags/                # Componentes reutilizáveis
│   ├── navigation.html
│   ├── breadcrumb.html
│   └── flash.html
└── errors/              # Páginas de erro
    ├── 403.html
    ├── 404.html
    └── 500.html
```

---

# 📊 Slide 5: Métricas do Projeto

## Código Fonte

| Categoria | Quantidade |
|-----------|------------|
| Classes Java | 44 |
| Controllers | 9 |
| Services/BO | 6 |
| DAOs | 5 |
| Entities | 6 |
| DTOs | 12 |
| Templates HTML | 15 |

## Testes Automatizados

| Tipo de Teste | Quantidade |
|---------------|------------|
| Testes de Service | 114 |
| Testes de DTO/Validação | 26 |
| Testes de Integração | 35 |
| Testes de Segurança | 19 |
| **TOTAL** | **185 testes** |

## Cobertura

* ✅ **100%** dos Services testados
* ✅ **100%** dos DTOs validados
* ✅ **100%** das rotas protegidas
* ✅ **100%** dos fluxos de autenticação

---

# ✅ Slide 6: Requisitos Atendidos

## Requisitos Funcionais

| RF | Descrição | Status |
|----|-----------|--------|
| RF1 | Autenticar Usuário | ✅ Form-based auth |
| RF2 | Manter Usuário | ✅ CRUD completo |
| RF3 | Manter Perfil de Usuário | ✅ CRUD completo |
| RF4 | Exibir Opções de Navegação | ✅ Menu + Breadcrumbs |
| RF5 | Dois Casos de Uso de Domínio | ✅ Notas + Tarefas |
| RF6 | Auditoria e Rastreabilidade | ✅ Logs completos |

## Requisitos Não-Funcionais

| Padrão | Implementação |
|--------|---------------|
| Java EE / Jakarta EE | ✅ Quarkus 3.x |
| MVC | ✅ Controller → Service → DAO |
| JAX-RS | ✅ RESTEasy Reactive |
| DAO | ✅ Panache Repositories |
| Entity | ✅ JPA/Hibernate |
| BO (Business Object) | ✅ Service Layer |
| DTO | ✅ Request/Response DTOs |

---

# 🔐 Slide 7: Segurança

## Autenticação Form-Based

```
┌─────────────┐       ┌───────────────┐       ┌─────────────┐
│   Usuário   │──────►│ /j_security   │──────►│  Elytron    │
│   (Login)   │       │    _check     │       │   JDBC      │
└─────────────┘       └───────────────┘       └─────────────┘
                                                    │
                      ┌───────────────┐             │
                      │   Sessão      │◄────────────┘
                      │   Criada      │
                      └───────────────┘
```

## Controle de Acesso

```java
@RolesAllowed("ADMIN")     // Apenas administradores
public class UserController { ... }

@RolesAllowed({"USER", "ADMIN"})  // Usuários e admins
public class NoteController { ... }
```

## Funcionalidades de Segurança

* 🔒 Senhas hasheadas com **BCrypt**
* 🛡️ Proteção CSRF via sessão
* 👤 Ownership de recursos (usuário só vê seus dados)
* 📝 Auditoria de todas as ações sensíveis

---

# 📝 Slide 8: Caso de Uso - Notas

## Funcionalidade

Sistema de anotações pessoais onde cada usuário gerencia apenas suas próprias notas.

## Fluxo

```
┌──────────┐    ┌────────────────┐    ┌─────────────┐    ┌─────────┐
│ Usuário  │───►│ NoteController │───►│ NoteService │───►│ NoteDao │
│ (HTTP)   │    │ (Validação)    │    │ (Ownership) │    │ (CRUD)  │
└──────────┘    └────────────────┘    └─────────────┘    └─────────┘
```

## Características

| Feature | Implementação |
|---------|---------------|
| CRUD Completo | ✅ Create, Read, Update, Delete |
| Ownership | ✅ Filtro por `ownerEmail` |
| Validação | ✅ Título e conteúdo obrigatórios |
| Auditoria | ✅ Eventos NOTE_CREATED, NOTE_UPDATED, NOTE_DELETED |

## Código Exemplo

```java
// Service garante que usuário só acessa suas notas
public List<NoteResponseDTO> findByOwner(String ownerEmail) {
    return noteDao.find("ownerEmail", ownerEmail)
        .stream()
        .map(this::toResponseDTO)
        .toList();
}
```

---

# ✅ Slide 9: Caso de Uso - Tarefas

## Funcionalidade

Gestão de tarefas com workflow de status e validação de prazos.

## Workflow de Status

```
┌──────────┐       ┌──────────────┐       ┌───────────┐
│ PENDENTE │──────►│ EM_ANDAMENTO │──────►│ CONCLUIDA │
└──────────┘       └──────────────┘       └───────────┘
```

## Validações Implementadas

| Validação | Descrição |
|-----------|-----------|
| Título obrigatório | Mínimo 1 caractere |
| Descrição obrigatória | Mínimo 1 caractere |
| Data limite futura | Não permite datas passadas |
| Ownership | Apenas dono pode editar/excluir |

## Código Exemplo

```java
// Validação de data limite
public void create(TaskRequestDTO dto, String ownerEmail) {
    if (dto.getDueDate() != null && dto.getDueDate().isBefore(LocalDate.now())) {
        throw new WebApplicationException(
            "Data limite deve ser futura", 
            Response.Status.BAD_REQUEST
        );
    }
    // ... criação da tarefa
}
```

---

# 📊 Slide 10: Sistema de Auditoria

## Objetivo

Rastrear todas as ações sensíveis do sistema para conformidade e debugging.

## Eventos Auditados

| Módulo | Eventos |
|--------|---------|
| Usuários | USER_CREATED, USER_UPDATED, USER_DELETED |
| Perfis | PROFILE_CREATED, PROFILE_UPDATED, PROFILE_DELETED |
| Notas | NOTE_CREATED, NOTE_UPDATED, NOTE_DELETED |
| Tarefas | TASK_CREATED, TASK_UPDATED, TASK_DELETED |
| Auth | USER_LOGIN, USER_LOGOUT |

## Estrutura do Log

```java
public class AuditLog {
    private Long id;
    private String action;        // Ex: "TASK_CREATED"
    private String entityType;    // Ex: "Task"
    private String entityId;      // Ex: "123"
    private String actorEmail;    // Ex: "admin@example.com"
    private LocalDateTime timestamp;
    private String details;       // JSON com dados adicionais
}
```

## Interface de Consulta

* Filtro por ação, entidade, ator e período
* Paginação automática
* Ordenação cronológica reversa

---

# 🧪 Slide 11: Testes Automatizados

## Estratégia de Testes

```
┌─────────────────────────────────────────────────────────────┐
│                    PIRÂMIDE DE TESTES                       │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│                      ┌─────────┐                            │
│                     /│   E2E   │\                           │
│                    / │ (Manual)│ \                          │
│                   /  └─────────┘  \                         │
│                  /   ┌───────────┐ \                        │
│                 /   /│Integration│\ \                       │
│                /   / │  (35)     │ \ \                      │
│               /   /  └───────────┘  \ \                     │
│              /   /   ┌─────────────┐ \ \                    │
│             /   /   /│   Unit      │\ \ \                   │
│            /   /   / │  (150)      │ \ \ \                  │
│           /   /   /  └─────────────┘  \ \ \                 │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

## Arquivos de Teste

| Arquivo | Testes | Cobertura |
|---------|--------|-----------|
| TaskServiceCompleteTest | 25 | CRUD + Validações |
| NoteServiceCompleteTest | 23 | CRUD + Ownership |
| UserServiceCompleteTest | 23 | CRUD + Senhas |
| UserProfileServiceCompleteTest | 28 | CRUD + Associações |
| AuditLogServiceTest | 15 | Logging + Consultas |
| DTOValidationTest | 26 | Todos os DTOs |
| AccessControlIntegrationTest | 16 | Proteção de rotas |
| SecurityIntegrationTest | 19 | Auth + Sessões |

---

# 🚀 Slide 12: Demonstração ao Vivo

## Roteiro de Demonstração

### 1. Autenticação (2 min)

* [ ] Login como `admin@example.com`
* [ ] Verificar menu de navegação
* [ ] Logout e login como `user@example.com`
* [ ] Verificar permissões diferentes

### 2. Gestão de Usuários (3 min) - ADMIN

* [ ] Listar usuários existentes
* [ ] Criar novo usuário
* [ ] Editar usuário
* [ ] Associar perfil

### 3. Gestão de Notas (3 min) - USER

* [ ] Criar nova nota
* [ ] Editar nota existente
* [ ] Verificar que só vê suas notas

### 4. Gestão de Tarefas (3 min) - USER

* [ ] Criar tarefa com prazo
* [ ] Alterar status (Pendente → Em Andamento → Concluída)
* [ ] Tentar criar tarefa com data passada (erro)

### 5. Auditoria (2 min) - ADMIN

* [ ] Visualizar logs de ações
* [ ] Filtrar por tipo de evento
* [ ] Verificar rastreabilidade

### 6. Testes (2 min)

* [ ] Executar `./mvnw test`
* [ ] Mostrar 185 testes passando

---

# 📈 Slide 13: Conclusão

## Objetivos Alcançados

✅ **Arquitetura MVC** completa e bem estruturada

✅ **Padrões de projeto** (DAO, DTO, Service/BO) implementados

✅ **Segurança** com autenticação e autorização por perfis

✅ **Dois casos de uso** de domínio (Notas e Tarefas)

✅ **Auditoria** completa de todas as ações

✅ **185 testes automatizados** cobrindo todo o sistema

✅ **Interface responsiva** com Tailwind CSS

## Diferenciais do Projeto

* 🧪 Alta cobertura de testes
* 📝 Documentação completa
* 🔄 Workflow de status em tarefas
* 🔒 Ownership de recursos por usuário
* 📊 Sistema de auditoria robusto

---

# ❓ Slide 14: Perguntas

## Recursos Disponíveis

* **Código fonte:** GitHub
* **Documentação:** `/docs` no projeto
* **README:** Instruções completas de instalação

## Contato

* Professor: [Nome]
* Disciplina: Programação para Web III
* Período: 2025

---

## Comandos Úteis para Demonstração

```bash
# Iniciar banco de dados
docker compose up -d postgres

# Executar aplicação em modo dev
./mvnw quarkus:dev

# Executar todos os testes
./mvnw test

# Acessar aplicação
open http://localhost:8080
```
