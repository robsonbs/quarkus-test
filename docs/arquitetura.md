# 🏗️ Arquitetura do Sistema

> Documentação técnica detalhada da arquitetura do Sistema de Gestão de Notas e Tarefas.

---

## 📋 Índice

* [Visão Geral](#visão-geral)
* [Padrões de Arquitetura](#padrões-de-arquitetura)
* [Camadas da Aplicação](#camadas-da-aplicação)
* [Diagrama de Classes](#diagrama-de-classes)
* [Diagrama de Entidade-Relacionamento](#diagrama-de-entidade-relacionamento)
* [Fluxos de Dados](#fluxos-de-dados)
* [Segurança](#segurança)
* [Decisões Arquiteturais](#decisões-arquiteturais)

---

## Visão Geral

O sistema foi desenvolvido seguindo uma arquitetura em camadas baseada no padrão **MVC (Model-View-Controller)**, combinada com os padrões **DAO (Data Access Object)**, **Service/BO (Business Object)** e **DTO (Data Transfer Object)**.

### Princípios Arquiteturais

1. **Separação de Responsabilidades** - Cada camada tem uma função específica
2. **Baixo Acoplamento** - Camadas se comunicam através de interfaces bem definidas
3. **Alta Coesão** - Classes relacionadas agrupadas logicamente
4. **Segurança em Camadas** - Validações em múltiplos níveis
5. **Auditoria Completa** - Rastreabilidade de todas as ações

---

## Padrões de Arquitetura

### MVC (Model-View-Controller)

```
┌─────────────────────────────────────────────────────────────────┐
│                          NAVEGADOR                               │
│                    (Cliente HTTP/HTML)                           │
└─────────────────────────────────────────────────────────────────┘
                              │
                    Request   │   Response (HTML/JSON)
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                       CONTROLLER                                 │
│                     (JAX-RS Endpoints)                           │
│  • Recebe requisições HTTP (GET, POST)                          │
│  • Valida parâmetros de entrada                                 │
│  • Chama serviços de negócio                                    │
│  • Prepara dados para a View                                    │
│  • Retorna Template ou Redirect                                 │
└─────────────────────────────────────────────────────────────────┘
          │                                       │
          │ Dados processados                     │ Dados de template
          ▼                                       ▼
┌──────────────────────┐              ┌──────────────────────────┐
│        MODEL         │              │          VIEW            │
│   (Entidades JPA)    │              │    (Templates Qute)      │
│                      │              │                          │
│  • User              │              │  • login.html            │
│  • UserProfile       │              │  • users.html            │
│  • Note              │              │  • notes.html            │
│  • Task              │              │  • tasks.html            │
│  • AuditLog          │              │  • audit.html            │
└──────────────────────┘              └──────────────────────────┘
```

### Arquitetura em Camadas Detalhada

```
┌─────────────────────────────────────────────────────────────────┐
│                    CAMADA DE APRESENTAÇÃO                        │
│                                                                  │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │                    TEMPLATES (Qute)                        │ │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐   │ │
│  │  │ login    │  │ users    │  │ notes    │  │ tasks    │   │ │
│  │  │ .html    │  │ .html    │  │ .html    │  │ .html    │   │ │
│  │  └──────────┘  └──────────┘  └──────────┘  └──────────┘   │ │
│  └────────────────────────────────────────────────────────────┘ │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │              COMPONENTES REUTILIZÁVEIS (tags/)             │ │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │ │
│  │  │ navigation   │  │ breadcrumb   │  │ flash        │     │ │
│  │  └──────────────┘  └──────────────┘  └──────────────┘     │ │
│  └────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    CAMADA CONTROLLER                             │
│                                                                  │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │ Login        │  │ User         │  │ UserProfile  │          │
│  │ Controller   │  │ Controller   │  │ Controller   │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │ Note         │  │ Task         │  │ Audit        │          │
│  │ Controller   │  │ Controller   │  │ Controller   │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
│                                                                  │
│  Responsabilidades:                                              │
│  • Anotações JAX-RS (@Path, @GET, @POST)                        │
│  • Injeção de dependências (@Inject)                            │
│  • Controle de acesso (@RolesAllowed)                           │
│  • Binding de formulários (@BeanParam, @RestForm)               │
│  • Preparação de TemplateInstance                               │
│  • Redirecionamentos com feedback (query params)                │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    CAMADA DTO                                    │
│                                                                  │
│  ┌─────────────────────────┐  ┌─────────────────────────┐      │
│  │     REQUEST DTOs        │  │     RESPONSE DTOs       │      │
│  │  ┌───────────────────┐  │  │  ┌───────────────────┐  │      │
│  │  │ UserRequestDTO    │  │  │  │ UserResponseDTO   │  │      │
│  │  │ NoteRequestDTO    │  │  │  │ NoteResponseDTO   │  │      │
│  │  │ TaskRequestDTO    │  │  │  │ TaskResponseDTO   │  │      │
│  │  │ LoginRequestDTO   │  │  │  │ AuditLogResponse  │  │      │
│  │  └───────────────────┘  │  │  └───────────────────┘  │      │
│  └─────────────────────────┘  └─────────────────────────┘      │
│                                                                  │
│  Propósito:                                                      │
│  • Isolar entidades JPA do front-end                            │
│  • Transportar apenas dados necessários                         │
│  • Facilitar validação de entrada                               │
│  • Permitir versionamento de API                                │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    CAMADA SERVICE (BO)                           │
│                                                                  │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │ User         │  │ UserProfile  │  │ Auth         │          │
│  │ Service      │  │ Service      │  │ Service      │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │ Note         │  │ Task         │  │ AuditLog     │          │
│  │ Service      │  │ Service      │  │ Service      │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
│                                                                  │
│  Responsabilidades:                                              │
│  • Regras de negócio                                            │
│  • Validações complexas                                         │
│  • Hash de senhas (BcryptUtil)                                  │
│  • Verificação de propriedade (ownership)                       │
│  • Controle de transações (@Transactional)                      │
│  • Registro de auditoria                                        │
│  • Obtenção do usuário autenticado (SecurityIdentity)           │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    CAMADA DAO                                    │
│                                                                  │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │ UserDao      │  │ UserProfile  │  │ NoteDao      │          │
│  │ (Panache)    │  │ Dao          │  │ (Panache)    │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
│  ┌──────────────┐  ┌──────────────┐                             │
│  │ TaskDao      │  │ AuditLogDao  │                             │
│  │ (Panache)    │  │ (Panache)    │                             │
│  └──────────────┘  └──────────────┘                             │
│                                                                  │
│  Responsabilidades:                                              │
│  • Implementar PanacheRepository                                │
│  • Queries JPQL customizadas                                    │
│  • Métodos de busca especializados                              │
│  • Paginação e ordenação                                        │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    CAMADA MODEL                                  │
│                                                                  │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │ User         │  │ UserProfile  │  │ Note         │          │
│  │ @Entity      │  │ @Entity      │  │ @Entity      │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │ Task         │  │ TaskStatus   │  │ AuditLog     │          │
│  │ @Entity      │  │ (Enum)       │  │ @Entity      │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
│                                                                  │
│  Responsabilidades:                                              │
│  • Mapeamento JPA (@Entity, @Table, @Column)                    │
│  • Relacionamentos (@ManyToOne, @OneToMany)                     │
│  • Callbacks de ciclo de vida (@PrePersist)                     │
│  • Validações de schema (nullable, unique)                      │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    BANCO DE DADOS                                │
│                                                                  │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │                     PostgreSQL 15                          │ │
│  │                                                            │ │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐        │ │
│  │  │ users       │  │ user_       │  │ notes       │        │ │
│  │  │             │  │ profiles    │  │             │        │ │
│  │  └─────────────┘  └─────────────┘  └─────────────┘        │ │
│  │  ┌─────────────┐  ┌─────────────┐                          │ │
│  │  │ tasks       │  │ audit_logs  │                          │ │
│  │  │             │  │             │                          │ │
│  │  └─────────────┘  └─────────────┘                          │ │
│  │                                                            │ │
│  │  Schema gerenciado por: Flyway Migrations                  │ │
│  └────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

---

## Camadas da Aplicação

### 1. Camada de Apresentação (View)

**Localização:** `src/main/resources/templates/`

**Tecnologia:** Qute Template Engine

**Componentes:**
* **Templates principais:** Páginas HTML com dados dinâmicos
* **Tags reutilizáveis:** Componentes em `templates/tags/`
* **Páginas de erro:** Tratamento de erros HTTP em `templates/errors/`

**Sintaxe Qute:**

```html
<!-- Variáveis -->
{user.name}

<!-- Condicionais -->
{#if user}Logado{#else}Anônimo{/if}

<!-- Loops -->
{#for item in items}{item.name}{/for}

<!-- Tags customizadas -->
{#navigation /}
{#breadcrumb /}
{#flash /}
```

### 2. Camada Controller

**Localização:** `src/main/java/com/robsonbs/controller/`

**Tecnologia:** JAX-RS (RESTEasy Reactive)

**Responsabilidades:**
* Mapear rotas HTTP (`@Path`,  `@GET`,  `@POST`)
* Controlar acesso (`@RolesAllowed`)
* Binding de formulários (`@BeanParam`)
* Preparar dados para templates
* Gerenciar redirecionamentos

**Exemplo:**

```java
@Path("/notes")
@RolesAllowed({"USER", "ADMIN"})
@Blocking
public class NoteController {

    @Inject
    NoteService noteService;

    @Inject
    Template notes;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance listNotes() {
        List<NoteResponseDTO> userNotes = noteService.findByOwner()
            .stream()
            .map(NoteResponseDTO::new)
            .collect(Collectors.toList());
        return notes.data("notes", userNotes);
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response createNote(@BeanParam NoteRequestDTO dto) {
        noteService.create(dto);
        return Response.seeOther(URI.create("/notes?success=created")).build();
    }
}
```

### 3. Camada Service (Business Object)

**Localização:** `src/main/java/com/robsonbs/service/`

**Responsabilidades:**
* Implementar regras de negócio
* Validar dados de entrada
* Controlar transações
* Registrar auditoria
* Verificar permissões de acesso

**Exemplo:**

```java
@ApplicationScoped
public class NoteService {

    @Inject
    NoteDao noteDao;

    @Inject
    SecurityIdentity securityIdentity;

    @Inject
    AuditLogService auditLogService;

    @Transactional
    public void create(NoteRequestDTO dto) {
        validateNote(dto);
        
        Note note = new Note();
        note.setTitle(dto.getTitle().trim());
        note.setContent(dto.getContent().trim());
        note.setOwner(currentUser());
        noteDao.persist(note);
        
        auditLogService.recordDomainEvent(
            currentUserEmail(),
            "NOTE_CREATED",
            "/notes",
            "Note",
            note.getId().toString(),
            "Nota criada",
            201
        );
    }

    private void validateNote(NoteRequestDTO dto) {
        if (dto.getTitle() == null || dto.getTitle().isBlank()) {
            throw new BadRequestException("Título é obrigatório");
        }
    }
}
```

### 4. Camada DAO (Data Access Object)

**Localização:** `src/main/java/com/robsonbs/dao/`

**Tecnologia:** Panache Repository

**Responsabilidades:**
* Acesso ao banco de dados
* Queries JPQL customizadas
* Métodos de busca especializados

**Exemplo:**

```java
@ApplicationScoped
public class NoteDao implements PanacheRepository<Note> {

    public List<Note> findByOwnerId(Long ownerId) {
        return find("owner.id", ownerId).list();
    }

    public Optional<Note> findByIdAndOwner(Long id, Long ownerId) {
        return find("id = ?1 and owner.id = ?2", id, ownerId).firstResultOptional();
    }
}
```

### 5. Camada Model (Entity)

**Localização:** `src/main/java/com/robsonbs/model/`

**Tecnologia:** JPA (Hibernate)

**Responsabilidades:**
* Mapeamento objeto-relacional
* Definição de relacionamentos
* Callbacks de ciclo de vida

**Exemplo:**

```java
@Entity
@Table(name = "notes")
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
```

### 6. Camada DTO (Data Transfer Object)

**Localização:** `src/main/java/com/robsonbs/dto/`

**Propósito:**
* Isolar entidades do front-end
* Transportar apenas dados necessários
* Facilitar binding de formulários

**Exemplo Request DTO:**

```java
public class NoteRequestDTO {

    @RestForm
    private String title;

    @RestForm
    private String content;

    // Getters e Setters
}
```

**Exemplo Response DTO:**

```java
public class NoteResponseDTO {

    private Long id;
    private String title;
    private String content;
    private String ownerName;
    private LocalDateTime createdAt;

    public NoteResponseDTO(Note entity) {
        this.id = entity.getId();
        this.title = entity.getTitle();
        this.content = entity.getContent();
        this.ownerName = entity.getOwner().getName();
        this.createdAt = entity.getCreatedAt();
    }
}
```

---

## Diagrama de Classes

### Entidades

```
┌───────────────────────────────────────────────────────────────┐
│                         UserProfile                            │
├───────────────────────────────────────────────────────────────┤
│ - id: Long                                                    │
│ - name: String                                                │
├───────────────────────────────────────────────────────────────┤
│ + getId(): Long                                               │
│ + getName(): String                                           │
│ + getUsers(): List<User>                                      │
└───────────────────────────────────────────────────────────────┘
                              │
                              │ 1
                              │
                              │ *
                              ▼
┌───────────────────────────────────────────────────────────────┐
│                            User                                │
├───────────────────────────────────────────────────────────────┤
│ - id: Long                                                    │
│ - name: String                                                │
│ - email: String                                               │
│ - password: String                                            │
│ - profile: UserProfile                                        │
│ - createdAt: LocalDateTime                                    │
├───────────────────────────────────────────────────────────────┤
│ + getId(): Long                                               │
│ + getName(): String                                           │
│ + getEmail(): String                                          │
│ + getProfile(): UserProfile                                   │
└───────────────────────────────────────────────────────────────┘
          │                                    │
          │ 1                                  │ 1
          │                                    │
          │ *                                  │ *
          ▼                                    ▼
┌──────────────────────────┐    ┌──────────────────────────────┐
│          Note            │    │            Task              │
├──────────────────────────┤    ├──────────────────────────────┤
│ - id: Long               │    │ - id: Long                   │
│ - title: String          │    │ - title: String              │
│ - content: String        │    │ - description: String        │
│ - owner: User            │    │ - dueDate: LocalDate         │
│ - createdAt: LocalDateTime│   │ - status: TaskStatus         │
│ - updatedAt: LocalDateTime│   │ - owner: User                │
├──────────────────────────┤    │ - createdAt: LocalDateTime   │
│ + getId(): Long          │    │ - updatedAt: LocalDateTime   │
│ + getTitle(): String     │    ├──────────────────────────────┤
│ + getContent(): String   │    │ + getId(): Long              │
│ + getOwner(): User       │    │ + getTitle(): String         │
└──────────────────────────┘    │ + getStatus(): TaskStatus    │
                                └──────────────────────────────┘

┌───────────────────────────────────────────────────────────────┐
│                         AuditLog                               │
├───────────────────────────────────────────────────────────────┤
│ - id: Long                                                    │
│ - username: String                                            │
│ - action: String                                              │
│ - httpMethod: String                                          │
│ - resourcePath: String                                        │
│ - clientIp: String                                            │
│ - statusCode: Integer                                         │
│ - entityType: String                                          │
│ - entityId: String                                            │
│ - userAgent: String                                           │
│ - details: String                                             │
│ - occurredAt: LocalDateTime                                   │
├───────────────────────────────────────────────────────────────┤
│ + getUsername(): String                                       │
│ + getAction(): String                                         │
│ + getOccurredAt(): LocalDateTime                              │
└───────────────────────────────────────────────────────────────┘

┌───────────────────────────────────────────────────────────────┐
│                       «enum» TaskStatus                        │
├───────────────────────────────────────────────────────────────┤
│ PENDING                                                       │
│ IN_PROGRESS                                                   │
│ COMPLETED                                                     │
└───────────────────────────────────────────────────────────────┘
```

---

## Diagrama de Entidade-Relacionamento

```
┌─────────────────┐       ┌─────────────────┐
│  user_profiles  │       │     users       │
├─────────────────┤       ├─────────────────┤
│ id (PK)         │───┐   │ id (PK)         │
│ name (UNIQUE)   │   │   │ name            │
└─────────────────┘   │   │ email (UNIQUE)  │
                      │   │ password        │
                      └──►│ profile_id (FK) │
                          │ created_at      │
                          └─────────────────┘
                                   │
                    ┌──────────────┼──────────────┐
                    │              │              │
                    ▼              ▼              ▼
            ┌───────────┐  ┌───────────┐  ┌─────────────┐
            │   notes   │  │   tasks   │  │ audit_logs  │
            ├───────────┤  ├───────────┤  ├─────────────┤
            │ id (PK)   │  │ id (PK)   │  │ id (PK)     │
            │ title     │  │ title     │  │ username    │
            │ content   │  │ description│ │ action      │
            │ user_id   │  │ due_date  │  │ http_method │
            │ (FK)      │  │ status    │  │ resource_   │
            │ created_at│  │ owner_id  │  │ path        │
            │ updated_at│  │ (FK)      │  │ client_ip   │
            └───────────┘  │ created_at│  │ status_code │
                           │ updated_at│  │ entity_type │
                           └───────────┘  │ entity_id   │
                                          │ user_agent  │
                                          │ details     │
                                          │ occurred_at │
                                          └─────────────┘
```

### Relacionamentos

| Origem | Destino | Tipo | Descrição |
|--------|---------|------|-----------|
| User | UserProfile | N:1 | Cada usuário tem um perfil |
| Note | User | N:1 | Cada nota pertence a um usuário |
| Task | User | N:1 | Cada tarefa pertence a um usuário |

### Índices

| Tabela | Índice | Colunas |
|--------|--------|---------|
| notes | idx_notes_user_id | user_id |
| tasks | idx_tasks_owner_id | owner_id |
| audit_logs | idx_audit_logs_username | username |
| audit_logs | idx_audit_logs_entity | entity_type, entity_id |

---

## Fluxos de Dados

### Fluxo de Autenticação

```
┌──────────┐     ┌──────────┐     ┌──────────┐     ┌──────────┐
│ Browser  │────►│  Login   │────►│ Elytron  │────►│   DB     │
│          │     │Controller│     │ Security │     │          │
└──────────┘     └──────────┘     └──────────┘     └──────────┘
     │                                                   │
     │  1. GET /login                                    │
     │◄──────────────────────────────────────────────────┤
     │  (form HTML)                                      │
     │                                                   │
     │  2. POST /j_security_check                        │
     │     (email, password)                             │
     │──────────────────────────────────────────────────►│
     │                                                   │
     │  3. Query SQL (busca usuário por email)           │
     │◄──────────────────────────────────────────────────│
     │                                                   │
     │  4. BCrypt verify (compara hash)                  │
     │                                                   │
     │  5. Cria sessão + cookie criptografado            │
     │◄──────────────────────────────────────────────────│
     │                                                   │
     │  6. Redirect para /users (landing page)           │
     │◄──────────────────────────────────────────────────┤
```

### Fluxo de CRUD (Exemplo: Notas)

```
┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐
│ Browser  │───►│Controller│───►│ Service  │───►│   DAO    │───►│    DB    │
└──────────┘    └──────────┘    └──────────┘    └──────────┘    └──────────┘

LISTAR NOTAS:
1. GET /notes
2. noteService.findByOwner()
3. noteDao.findByOwnerId(userId)
4. SELECT * FROM notes WHERE user_id = ?
5. List<Note> → List<NoteResponseDTO>
6. Template notes.html + dados
7. HTML response

CRIAR NOTA:
1. POST /notes (title, content)
2. noteService.create(dto)
3. validateNote(dto) ← regra de negócio
4. noteDao.persist(note)
5. INSERT INTO notes ...
6. auditLogService.recordDomainEvent(...)
7. Redirect /notes?success=created
```

### Fluxo de Auditoria

```
┌──────────────────────────────────────────────────────────────────────────┐
│                           FLUXO DE AUDITORIA                              │
└──────────────────────────────────────────────────────────────────────────┘

                    ┌─────────────────────────────────┐
                    │       Requisição HTTP           │
                    └─────────────────────────────────┘
                                   │
                    ┌──────────────┴──────────────┐
                    │                             │
                    ▼                             ▼
        ┌───────────────────────┐   ┌───────────────────────┐
        │   AuditLogFilter      │   │   Service.method()    │
        │   (todas requisições) │   │   (eventos domínio)   │
        └───────────────────────┘   └───────────────────────┘
                    │                             │
                    │  username                   │  username
                    │  httpMethod                 │  action
                    │  resourcePath               │  entityType
                    │  clientIp                   │  entityId
                    │  userAgent                  │  details
                    │                             │  statusCode
                    │                             │
                    └──────────────┬──────────────┘
                                   │
                                   ▼
                    ┌─────────────────────────────────┐
                    │        AuditLogService          │
                    │        record(...)              │
                    └─────────────────────────────────┘
                                   │
                                   ▼
                    ┌─────────────────────────────────┐
                    │         AuditLogDao             │
                    │         persist(log)            │
                    └─────────────────────────────────┘
                                   │
                                   ▼
                    ┌─────────────────────────────────┐
                    │         audit_logs              │
                    │         (tabela)                │
                    └─────────────────────────────────┘
```

---

## Segurança

### Arquitetura de Segurança

```
┌─────────────────────────────────────────────────────────────────┐
│                    CAMADAS DE SEGURANÇA                          │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│ 1. AUTENTICAÇÃO (Elytron JDBC)                                  │
│    • Form-based login                                           │
│    • Cookie de sessão criptografado                             │
│    • BCrypt password hashing                                    │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│ 2. AUTORIZAÇÃO (Roles)                                          │
│    • @RolesAllowed("ADMIN") → Gestão de usuários/perfis         │
│    • @RolesAllowed({"USER","ADMIN"}) → Notas/Tarefas            │
│    • Perfis armazenados em user_profiles                        │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│ 3. PROPRIEDADE (Ownership)                                      │
│    • Verificação em NoteService/TaskService                     │
│    • Usuário só acessa seus próprios dados                      │
│    • SecurityIdentity para obter usuário atual                  │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│ 4. VALIDAÇÃO                                                    │
│    • DTOs com campos tipados                                    │
│    • Validação no Service (regras de negócio)                   │
│    • Sanitização de entrada                                     │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│ 5. AUDITORIA                                                    │
│    • Registro de todas as ações                                 │
│    • Usuário, timestamp, ação, recurso                          │
│    • Visualização restrita a ADMIN                              │
└─────────────────────────────────────────────────────────────────┘
```

### Configuração de Segurança

```properties
# application.properties

# Habilitar segurança JDBC
quarkus.security.jdbc.enabled=true

# Query para buscar usuário e perfil
quarkus.security.jdbc.principal-query.sql=\
  SELECT u.password, p.name \
  FROM users u \
  JOIN user_profiles p ON u.profile_id = p.id \
  WHERE u.email = ?

# Mapeamento BCrypt
quarkus.security.jdbc.principal-query.bcrypt-password-mapper.enabled=true
quarkus.security.jdbc.principal-query.bcrypt-password-mapper.password-index=1

# Mapeamento de roles
quarkus.security.jdbc.principal-query.attribute-mappings.0.index=2
quarkus.security.jdbc.principal-query.attribute-mappings.0.to=groups

# Form authentication
quarkus.http.auth.form.enabled=true
quarkus.http.auth.form.login-page=/login
quarkus.http.auth.form.error-page=/login?error=true
quarkus.http.auth.form.landing-page=/users

# Sessão criptografada
quarkus.http.auth.session.encryption-key=...
```

---

## Decisões Arquiteturais

### ADR-001: Uso de Qute como Template Engine

**Contexto:** Necessidade de renderizar páginas HTML no servidor.

**Decisão:** Usar Qute (nativo do Quarkus) ao invés de Thymeleaf ou JSP.

**Justificativa:**
* Integração nativa com Quarkus
* Sintaxe simples e segura
* Suporte a componentes reutilizáveis (tags)
* Hot reload no modo dev

### ADR-002: Panache Repository Pattern

**Contexto:** Implementação da camada de acesso a dados.

**Decisão:** Usar PanacheRepository ao invés de Active Record.

**Justificativa:**
* Separação clara entre entidade e repositório
* Facilita injeção de dependência
* Permite queries customizadas
* Alinhado com padrão DAO

### ADR-003: RESTEasy Reactive com @Blocking

**Contexto:** Necessidade de acesso a banco de dados em controllers.

**Decisão:** Usar RESTEasy Reactive com anotação @Blocking.

**Justificativa:**
* RESTEasy Reactive é o padrão do Quarkus 3.x
* @Blocking permite operações síncronas de banco
* Melhor performance para operações CPU-bound

### ADR-004: DTOs com @RestForm

**Contexto:** Binding de formulários HTML para objetos Java.

**Decisão:** Usar @RestForm nos campos e @BeanParam nos parâmetros.

**Justificativa:**
* RESTEasy Reactive não faz auto-binding sem anotações
* @RestForm marca campos para binding de formulário
* @BeanParam agrupa campos em um objeto

### ADR-005: Auditoria em Duas Camadas

**Contexto:** Necessidade de rastrear todas as ações do sistema.

**Decisão:** Auditoria via Filter (requisições) + Service (eventos de domínio).

**Justificativa:**
* Filter captura todas as requisições HTTP
* Service registra eventos de negócio com contexto
* Combinação garante cobertura completa

---

## Referências

* [Quarkus Documentation](https://quarkus.io/guides/)
* [JAX-RS Specification](https://jakarta.ee/specifications/restful-ws/)
* [Panache ORM](https://quarkus.io/guides/hibernate-orm-panache)
* [Qute Templating](https://quarkus.io/guides/qute)
* [Elytron Security](https://quarkus.io/guides/security-jdbc)
