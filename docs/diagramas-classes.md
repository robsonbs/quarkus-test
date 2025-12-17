# 📊 Diagramas de Classes

> 20 diagramas Mermaid detalhados do Sistema de Gestão de Notas e Tarefas.

---

## 📋 Índice

01. [Visão Geral do Sistema](#1-visão-geral-do-sistema)
02. [Camada de Modelo (Entidades)](#2-camada-de-modelo-entidades)
03. [Hierarquia de Usuários e Perfis](#3-hierarquia-de-usuários-e-perfis)
04. [Entidades de Domínio](#4-entidades-de-domínio)
05. [Enum TaskStatus](#5-enum-taskstatus)
06. [Camada DAO](#6-camada-dao)
07. [Padrão Repository com Panache](#7-padrão-repository-com-panache)
08. [Camada de Serviços](#8-camada-de-serviços)
09. [UserService Detalhado](#9-userservice-detalhado)
10. [TaskService Detalhado](#10-taskservice-detalhado)
11. [NoteService Detalhado](#11-noteservice-detalhado)
12. [AuditLogService Detalhado](#12-auditlogservice-detalhado)
13. [Camada de Controllers](#13-camada-de-controllers)
14. [UserController Detalhado](#14-usercontroller-detalhado)
15. [TaskController Detalhado](#15-taskcontroller-detalhado)
16. [DTOs de Request](#16-dtos-de-request)
17. [DTOs de Response](#17-dtos-de-response)
18. [Componentes de Segurança](#18-componentes-de-segurança)
19. [Filtros e Interceptadores](#19-filtros-e-interceptadores)
20. [Arquitetura Completa MVC](#20-arquitetura-completa-mvc)

---

## 1. Visão Geral do Sistema

Diagrama de alto nível mostrando as principais camadas e suas relações.

```mermaid
classDiagram
    direction TB
    
    class PresentationLayer {
        <<layer>>
        Controllers
        Templates (Qute)
        DTOs
    }
    
    class BusinessLayer {
        <<layer>>
        Services
        Validation
        Security
    }
    
    class DataAccessLayer {
        <<layer>>
        DAOs
        Repositories
        Panache
    }
    
    class PersistenceLayer {
        <<layer>>
        Entities
        JPA/Hibernate
        PostgreSQL
    }
    
    PresentationLayer --> BusinessLayer : usa
    BusinessLayer --> DataAccessLayer : usa
    DataAccessLayer --> PersistenceLayer : persiste
```

---

## 2. Camada de Modelo (Entidades)

Todas as entidades JPA do sistema e seus relacionamentos.

```mermaid
classDiagram
    direction LR
    
    class User {
        -Long id
        -String name
        -String email
        -String password
        -UserProfile profile
        -LocalDateTime createdAt
        +getId() Long
        +setId(Long) void
        +getName() String
        +setName(String) void
        +getEmail() String
        +setEmail(String) void
        +getPassword() String
        +setPassword(String) void
        +getProfile() UserProfile
        +setProfile(UserProfile) void
        +getCreatedAt() LocalDateTime
        #onCreate() void
    }
    
    class UserProfile {
        -Long id
        -String name
        -List~User~ users
        +getId() Long
        +setId(Long) void
        +getName() String
        +setName(String) void
        +getUsers() List~User~
    }
    
    class Note {
        -Long id
        -String title
        -String content
        -User owner
        -LocalDateTime createdAt
        -LocalDateTime updatedAt
        +getId() Long
        +getTitle() String
        +getContent() String
        +getOwner() User
        +getCreatedAt() LocalDateTime
        +getUpdatedAt() LocalDateTime
        #onCreate() void
        #onUpdate() void
    }
    
    class Task {
        -Long id
        -String title
        -String description
        -LocalDate dueDate
        -TaskStatus status
        -User owner
        -LocalDateTime createdAt
        -LocalDateTime updatedAt
        +getId() Long
        +getTitle() String
        +getDescription() String
        +getDueDate() LocalDate
        +getStatus() TaskStatus
        +getOwner() User
        #onCreate() void
        #onUpdate() void
    }
    
    class AuditLog {
        -Long id
        -String username
        -String action
        -String httpMethod
        -String resourcePath
        -String clientIp
        -Integer statusCode
        -String entityType
        -String entityId
        -String userAgent
        -String details
        -LocalDateTime occurredAt
        +getId() Long
        +getUsername() String
        +getAction() String
        #onPersist() void
    }
    
    class TaskStatus {
        <<enumeration>>
        PENDING
        IN_PROGRESS
        COMPLETED
    }
    
    UserProfile "1" --> "*" User : has
    User "1" --> "*" Note : owns
    User "1" --> "*" Task : owns
    Task --> TaskStatus : has
```

---

## 3. Hierarquia de Usuários e Perfis

Relacionamento detalhado entre User e UserProfile.

```mermaid
classDiagram
    direction TB
    
    class UserProfile {
        <<Entity>>
        -Long id
        -String name
        -List~User~ users
        +UserProfile()
        +UserProfile(String name)
        +getId() Long
        +setId(Long) void
        +getName() String
        +setName(String) void
        +getUsers() List~User~
        +setUsers(List~User~) void
    }
    
    class User {
        <<Entity>>
        -Long id
        -String name
        -String email
        -String password
        -UserProfile profile
        -LocalDateTime createdAt
        +User()
        +User(String, String, String, UserProfile)
        +getId() Long
        +setId(Long) void
        +getName() String
        +setName(String) void
        +getEmail() String
        +setEmail(String) void
        +getPassword() String
        +setPassword(String) void
        +getProfile() UserProfile
        +setProfile(UserProfile) void
        +getCreatedAt() LocalDateTime
        #onCreate() void
    }
    
    class JPAAnnotations {
        <<stereotype>>
        @Entity
        @Table
        @Id
        @GeneratedValue
        @Column
        @ManyToOne
        @OneToMany
        @JoinColumn
        @PrePersist
    }
    
    UserProfile "1" o-- "*" User : contains
    User ..> JPAAnnotations : uses
    UserProfile ..> JPAAnnotations : uses
    
    note for UserProfile "Tabela: user_profiles\nRole: ADMIN, USER"
    note for User "Tabela: users\nSenha: BCrypt hash"
```

---

## 4. Entidades de Domínio

Detalhamento de Note e Task com lifecycle callbacks.

```mermaid
classDiagram
    direction TB
    
    class Note {
        <<Entity>>
        -Long id
        -String title
        -String content
        -User owner
        -LocalDateTime createdAt
        -LocalDateTime updatedAt
        +Note()
        +getId() Long
        +setId(Long) void
        +getTitle() String
        +setTitle(String) void
        +getContent() String
        +setContent(String) void
        +getOwner() User
        +setOwner(User) void
        +getCreatedAt() LocalDateTime
        +getUpdatedAt() LocalDateTime
        #onCreate() void
        #onUpdate() void
    }
    
    class Task {
        <<Entity>>
        -Long id
        -String title
        -String description
        -LocalDate dueDate
        -TaskStatus status
        -User owner
        -LocalDateTime createdAt
        -LocalDateTime updatedAt
        +Task()
        +getId() Long
        +setId(Long) void
        +getTitle() String
        +setTitle(String) void
        +getDescription() String
        +setDescription(String) void
        +getDueDate() LocalDate
        +setDueDate(LocalDate) void
        +getStatus() TaskStatus
        +setStatus(TaskStatus) void
        +getOwner() User
        +setOwner(User) void
        #onCreate() void
        #onUpdate() void
    }
    
    class User {
        <<Entity>>
        -Long id
        -String email
        +getId() Long
        +getEmail() String
    }
    
    class LifecycleCallbacks {
        <<interface>>
        @PrePersist onCreate()
        @PreUpdate onUpdate()
    }
    
    Note "*" --> "1" User : owner
    Task "*" --> "1" User : owner
    Note ..|> LifecycleCallbacks : implements
    Task ..|> LifecycleCallbacks : implements
    
    note for Note "Tabela: notes\nConteúdo: TEXT"
    note for Task "Tabela: tasks\nStatus: ENUM"
```

---

## 5. Enum TaskStatus

Máquina de estados das tarefas.

```mermaid
classDiagram
    direction LR
    
    class TaskStatus {
        <<enumeration>>
        PENDING
        IN_PROGRESS
        COMPLETED
        +name() String
        +ordinal() int
        +valueOf(String) TaskStatus
        +values() TaskStatus[]
    }
    
    class Task {
        <<Entity>>
        -TaskStatus status
        +getStatus() TaskStatus
        +setStatus(TaskStatus) void
    }
    
    class TaskStatusTransitions {
        <<behavior>>
        PENDING → IN_PROGRESS
        IN_PROGRESS → COMPLETED
        COMPLETED → IN_PROGRESS
        Any → PENDING
    }
    
    Task --> TaskStatus : uses
    TaskStatus .. TaskStatusTransitions : follows

    note for TaskStatus "Persistido como STRING\n@Enumerated(EnumType.STRING)"
```

---

## 6. Camada DAO

Todos os DAOs (Data Access Objects) do sistema.

```mermaid
classDiagram
    direction TB
    
    class PanacheRepository~T~ {
        <<interface>>
        +persist(T entity) void
        +delete(T entity) void
        +findById(Long id) T
        +listAll() List~T~
        +find(String query, Object... params) PanacheQuery~T~
        +deleteById(Long id) boolean
        +flush() void
    }
    
    class UserDao {
        <<Repository>>
        +findByEmail(String email) Optional~User~
    }
    
    class UserProfileDao {
        <<Repository>>
        +findByName(String name) Optional~UserProfile~
    }
    
    class NoteDao {
        <<Repository>>
        +findByOwnerId(Long ownerId) List~Note~
    }
    
    class TaskDao {
        <<Repository>>
        +findByOwnerId(Long ownerId) List~Task~
    }
    
    class AuditLogDao {
        <<Repository>>
    }
    
    UserDao ..|> PanacheRepository : implements
    UserProfileDao ..|> PanacheRepository : implements
    NoteDao ..|> PanacheRepository : implements
    TaskDao ..|> PanacheRepository : implements
    AuditLogDao ..|> PanacheRepository : implements
    
    note for PanacheRepository "Quarkus Panache\nSimplifica JPA"
```

---

## 7. Padrão Repository com Panache

Detalhamento do padrão Repository implementado.

```mermaid
classDiagram
    direction TB
    
    class PanacheRepositoryBase~Entity, Id~ {
        <<interface>>
        +persist(Entity entity) void
        +persistAndFlush(Entity entity) void
        +delete(Entity entity) void
        +isPersistent(Entity entity) boolean
        +flush() void
        +findById(Id id) Entity
        +findByIdOptional(Id id) Optional~Entity~
        +find(String query, Object... params) PanacheQuery~Entity~
        +find(String query, Map params) PanacheQuery~Entity~
        +findAll() PanacheQuery~Entity~
        +findAll(Sort sort) PanacheQuery~Entity~
        +list(String query, Object... params) List~Entity~
        +listAll() List~Entity~
        +listAll(Sort sort) List~Entity~
        +stream(String query, Object... params) Stream~Entity~
        +streamAll() Stream~Entity~
        +count() long
        +count(String query, Object... params) long
        +deleteAll() long
        +deleteById(Id id) boolean
        +update(String query, Object... params) int
    }
    
    class PanacheRepository~Entity~ {
        <<interface>>
    }
    
    class UserDao {
        <<ApplicationScoped>>
        +findByEmail(String email) Optional~User~
    }
    
    class TaskDao {
        <<ApplicationScoped>>
        +findByOwnerId(Long ownerId) List~Task~
    }
    
    PanacheRepository~Entity~ --|> PanacheRepositoryBase~Entity, Long~ : extends
    UserDao ..|> PanacheRepository : implements
    TaskDao ..|> PanacheRepository : implements
```

---

## 8. Camada de Serviços

Todos os Services com suas dependências.

```mermaid
classDiagram
    direction TB
    
    class UserService {
        <<ApplicationScoped>>
        -UserDao userDao
        -UserProfileDao userProfileDao
        -AuditLogService auditLogService
        -SecurityIdentity securityIdentity
        +listAll() List~User~
        +save(UserRequestDTO) void
        +findById(Long) User
        +update(Long, UserRequestDTO) void
        +delete(Long) void
        -validatePassword(String) void
        -ensureEmailAvailable(String, Long) void
        -resolveProfile(Long) UserProfile
        -currentActor() String
        -sanitize(String) String
    }
    
    class NoteService {
        <<ApplicationScoped>>
        -NoteDao noteDao
        -UserDao userDao
        -SecurityIdentity securityIdentity
        -AuditLogService auditLogService
        +findByOwner() List~Note~
        +create(NoteRequestDTO) void
        +findById(Long) Note
        +update(Long, NoteRequestDTO) void
        +delete(Long) void
        -currentUser() User
        -verifyOwnership(Note) void
        -validateNote(NoteRequestDTO) void
    }
    
    class TaskService {
        <<ApplicationScoped>>
        -TaskDao taskDao
        -UserDao userDao
        -SecurityIdentity securityIdentity
        -AuditLogService auditLogService
        +findByOwner() List~Task~
        +findById(Long) Task
        +create(TaskRequestDTO) void
        +update(Long, TaskRequestDTO) void
        +delete(Long) void
        -populateTask(Task, TaskRequestDTO) void
        -resolveStatus(String) TaskStatus
        -resolveDueDate(String) LocalDate
        -verifyOwnership(Task) void
    }
    
    class UserProfileService {
        <<ApplicationScoped>>
        -UserProfileDao userProfileDao
        -UserDao userDao
        -AuditLogService auditLogService
        -SecurityIdentity securityIdentity
        +listAll() List~UserProfileResponseDTO~
        +create(UserProfileRequestDTO) void
        +findById(Long) UserProfileResponseDTO
        +update(Long, UserProfileRequestDTO) void
        +delete(Long) void
        -ensureNameAvailable(String, Long) void
        -countUsers(UserProfile) long
    }
    
    class AuditLogService {
        <<ApplicationScoped>>
        -AuditLogDao auditLogDao
        +record(...) void
        +recordDomainEvent(...) void
        +listRecent(int) List~AuditLog~
        +listAll() List~AuditLog~
        +search(AuditLogCriteria) AuditLogPage
    }
    
    UserService --> AuditLogService : audita
    NoteService --> AuditLogService : audita
    TaskService --> AuditLogService : audita
    UserProfileService --> AuditLogService : audita
```

---

## 9. UserService Detalhado

Serviço de usuários com todas as operações.

```mermaid
classDiagram
    direction TB
    
    class UserService {
        <<ApplicationScoped>>
        -UserDao userDao
        -UserProfileDao userProfileDao
        -AuditLogService auditLogService
        -SecurityIdentity securityIdentity
        +listAll() List~User~
        +save(UserRequestDTO dto) void
        +findById(Long id) User
        +update(Long id, UserRequestDTO dto) void
        +delete(Long id) void
        -validatePassword(String password) void
        -ensureEmailAvailable(String email, Long excludeId) void
        -resolveProfile(Long profileId) UserProfile
        -currentActor() String
        -sanitize(String value) String
    }
    
    class UserDao {
        <<Repository>>
        +findByEmail(String) Optional~User~
        +persist(User) void
        +findById(Long) User
        +deleteById(Long) boolean
        +listAll() List~User~
        +flush() void
    }
    
    class UserProfileDao {
        <<Repository>>
        +findById(Long) UserProfile
    }
    
    class AuditLogService {
        <<Service>>
        +recordDomainEvent(...) void
    }
    
    class SecurityIdentity {
        <<Quarkus Security>>
        +getPrincipal() Principal
    }
    
    class BcryptUtil {
        <<Utility>>
        +bcryptHash(String) String$
        +matches(String, String) boolean$
    }
    
    class UserRequestDTO {
        <<DTO>>
        -String name
        -String email
        -String password
        -Long profileId
    }
    
    UserService --> UserDao : injeta
    UserService --> UserProfileDao : injeta
    UserService --> AuditLogService : injeta
    UserService --> SecurityIdentity : injeta
    UserService ..> BcryptUtil : usa
    UserService ..> UserRequestDTO : recebe
    
    note for UserService "@Transactional nos métodos\nde escrita"
```

---

## 10. TaskService Detalhado

Serviço de tarefas com validações.

```mermaid
classDiagram
    direction TB
    
    class TaskService {
        <<ApplicationScoped>>
        -TaskDao taskDao
        -UserDao userDao
        -SecurityIdentity securityIdentity
        -AuditLogService auditLogService
        +findByOwner() List~Task~
        +findById(Long id) Task
        +create(TaskRequestDTO dto) void
        +update(Long id, TaskRequestDTO dto) void
        +delete(Long id) void
        -populateTask(Task task, TaskRequestDTO dto) void
        -resolveStatus(String status) TaskStatus
        -resolveDueDate(String dueDate) LocalDate
        -verifyOwnership(Task task) void
        -currentUser() User
        -currentActor() String
        -sanitize(String value) String
        -trimToNull(String value) String
    }
    
    class TaskDao {
        <<Repository>>
        +findByOwnerId(Long) List~Task~
        +findById(Long) Task
        +persist(Task) void
        +delete(Task) void
        +flush() void
    }
    
    class TaskRequestDTO {
        <<DTO>>
        -String title
        -String description
        -String dueDate
        -String status
        +getTitle() String
        +getDescription() String
        +getDueDate() String
        +getStatus() String
    }
    
    class TaskValidations {
        <<rules>>
        Título obrigatório
        Status válido (enum)
        Data futura
        Ownership check
    }
    
    class TaskStatus {
        <<enum>>
        PENDING
        IN_PROGRESS
        COMPLETED
    }
    
    TaskService --> TaskDao : injeta
    TaskService ..> TaskRequestDTO : recebe
    TaskService ..> TaskStatus : converte
    TaskService .. TaskValidations : aplica
    
    note for TaskService "Validações:\n- Título obrigatório\n- Data não pode ser passada\n- Status válido"
```

---

## 11. NoteService Detalhado

Serviço de notas com ownership.

```mermaid
classDiagram
    direction TB
    
    class NoteService {
        <<ApplicationScoped>>
        -NoteDao noteDao
        -UserDao userDao
        -SecurityIdentity securityIdentity
        -AuditLogService auditLogService
        +findByOwner() List~Note~
        +create(NoteRequestDTO dto) void
        +findById(Long id) Note
        +update(Long id, NoteRequestDTO dto) void
        +delete(Long id) void
        -currentUser() User
        -currentUserEmail() String
        -verifyOwnership(Note note) void
        -validateNote(NoteRequestDTO dto) void
    }
    
    class NoteDao {
        <<Repository>>
        +findByOwnerId(Long ownerId) List~Note~
        +findById(Long id) Note
        +persist(Note note) void
        +delete(Note note) void
        +flush() void
    }
    
    class NoteRequestDTO {
        <<DTO>>
        -String title
        -String content
        +getTitle() String
        +setTitle(String) void
        +getContent() String
        +setContent(String) void
    }
    
    class OwnershipCheck {
        <<security>>
        Verifica se nota pertence ao usuário
        Lança ForbiddenException se não
    }
    
    class AuditEvents {
        <<events>>
        NOTE_CREATED
        NOTE_UPDATED
        NOTE_DELETED
    }
    
    NoteService --> NoteDao : injeta
    NoteService ..> NoteRequestDTO : recebe
    NoteService .. OwnershipCheck : aplica
    NoteService --> AuditEvents : registra
    
    note for NoteService "Apenas o owner\npode acessar suas notas"
```

---

## 12. AuditLogService Detalhado

Serviço de auditoria com busca paginada.

```mermaid
classDiagram
    direction TB
    
    class AuditLogService {
        <<ApplicationScoped>>
        -AuditLogDao auditLogDao
        -Sort DEFAULT_SORT$
        +record(String username, String action, String httpMethod, String resourcePath, String clientIp) void
        +record(String username, String action, String httpMethod, String resourcePath, String clientIp, String entityType, String entityId, String details, String userAgent, Integer statusCode) void
        +recordDomainEvent(String username, String action, String resourcePath, String entityType, String entityId, String details, Integer statusCode) void
        +listRecent(int limit) List~AuditLog~
        +listAll() List~AuditLog~
        +search(AuditLogCriteria criteria) AuditLogPage
    }
    
    class AuditLogDao {
        <<Repository>>
        +persist(AuditLog) void
        +findAll(Sort) PanacheQuery~AuditLog~
        +find(String, Map) PanacheQuery~AuditLog~
    }
    
    class AuditLogCriteria {
        <<record>>
        +username() Optional~String~
        +method() Optional~String~
        +action() Optional~String~
        +from() Optional~LocalDateTime~
        +to() Optional~LocalDateTime~
        +page() int
        +size() int
    }
    
    class AuditLogPage {
        <<record>>
        +items() List~AuditLog~
        +page() int
        +size() int
        +totalItems() long
        +totalPages() int
    }
    
    class AuditLog {
        <<Entity>>
        -Long id
        -String username
        -String action
        -String httpMethod
        -String resourcePath
        -LocalDateTime occurredAt
    }
    
    AuditLogService --> AuditLogDao : injeta
    AuditLogService ..> AuditLogCriteria : recebe
    AuditLogService ..> AuditLogPage : retorna
    AuditLogDao --> AuditLog : persiste
```

---

## 13. Camada de Controllers

Todos os controllers REST do sistema.

```mermaid
classDiagram
    direction TB
    
    class UserController {
        <<JAX-RS>>
        @Path("/users")
        @Blocking
        -UserService userService
        -UserProfileService userProfileService
        -Template users
        -Template userForm
        +listUsers() TemplateInstance
        +newUserForm() TemplateInstance
        +createUser(UserRequestDTO) Response
        +getUser(Long) TemplateInstance
        +updateUser(Long, UserRequestDTO) Response
        +deleteUser(Long) Response
    }
    
    class TaskController {
        <<JAX-RS>>
        @Path("/tasks")
        @RolesAllowed(USER, ADMIN)
        -TaskService taskService
        -Template tasks
        -Template taskForm
        +listTasks() TemplateInstance
        +newTaskForm() TemplateInstance
        +createTask(TaskRequestDTO) Response
        +editTask(Long) TemplateInstance
        +updateTask(Long, TaskRequestDTO) Response
        +deleteTask(Long) Response
    }
    
    class NoteController {
        <<JAX-RS>>
        @Path("/notes")
        @RolesAllowed(USER, ADMIN)
        -NoteService noteService
        -Template notes
        -Template noteForm
        +listNotes() TemplateInstance
        +newNoteForm() TemplateInstance
        +createNote(NoteRequestDTO) Response
        +editNote(Long) TemplateInstance
        +updateNote(Long, NoteRequestDTO) Response
        +deleteNote(Long) Response
    }
    
    class UserProfileController {
        <<JAX-RS>>
        @Path("/profiles")
        @RolesAllowed(ADMIN)
        -UserProfileService service
        -Template profiles
        -Template profileForm
        +listProfiles() TemplateInstance
        +newProfileForm() TemplateInstance
        +createProfile(UserProfileRequestDTO) Response
        +editProfile(Long) TemplateInstance
        +updateProfile(Long, UserProfileRequestDTO) Response
        +deleteProfile(Long) Response
    }
    
    class AuditController {
        <<JAX-RS>>
        @Path("/audit")
        @RolesAllowed(ADMIN)
        -AuditLogService service
        -Template audit
        +listAuditLogs() TemplateInstance
    }
    
    class LoginController {
        <<JAX-RS>>
        @Path("/login")
        -Template login
        +loginPage() TemplateInstance
    }
    
    class LogoutController {
        <<JAX-RS>>
        @Path("/logout")
        +logout() Response
    }
    
    class HomeController {
        <<JAX-RS>>
        @Path("/")
        -Template index
        +home() TemplateInstance
    }
```

---

## 14. UserController Detalhado

Controller de usuários com fluxo de redirecionamento.

```mermaid
classDiagram
    direction TB
    
    class UserController {
        <<JAX-RS Resource>>
        -UserService userService
        -UserProfileService userProfileService
        -Template users
        -Template userForm
        +listUsers(UriInfo) TemplateInstance
        +newUserForm(UriInfo) TemplateInstance
        +createUser(UserRequestDTO) Response
        +getUser(Long, UriInfo) TemplateInstance
        +updateUser(Long, UserRequestDTO) Response
        +deleteUser(Long) Response
        -resolveUsersSuccessMessage(String) String
        -extractFormData(MultivaluedMap, String...) Map
        -determineSelectedProfileId(Map, Long) String
        -hasSelectedProfile(String) boolean
        -resolveInitialValue(String, String) String
        -sanitizeMessage(String) String
        -safeValue(Object) String
    }
    
    class JAXRSAnnotations {
        <<stereotypes>>
        @Path("/users")
        @Blocking
        @GET
        @POST
        @Produces(TEXT_HTML)
        @Consumes(FORM_URLENCODED)
        @RolesAllowed("ADMIN")
        @PathParam
        @BeanParam
        @Context
    }
    
    class UserService {
        <<Service>>
        +listAll() List~User~
        +save(UserRequestDTO) void
        +findById(Long) User
        +update(Long, UserRequestDTO) void
        +delete(Long) void
    }
    
    class Template {
        <<Qute>>
        +data(String, Object) TemplateInstance
        +instance() TemplateInstance
    }
    
    class BreadcrumbItem {
        <<Helper>>
        +link(String, String) BreadcrumbItem$
        +current(String) BreadcrumbItem$
    }
    
    class RedirectFlow {
        <<behavior>>
        Sucesso: /users?success=created
        Erro: /users/new?error=msg&name=...
    }
    
    UserController --> UserService : usa
    UserController --> Template : renderiza
    UserController ..> JAXRSAnnotations : usa
    UserController ..> BreadcrumbItem : cria
    UserController .. RedirectFlow : segue
```

---

## 15. TaskController Detalhado

Controller de tarefas com validação de erros.

```mermaid
classDiagram
    direction TB
    
    class TaskController {
        <<JAX-RS Resource>>
        -TaskService taskService
        -Template tasks
        -Template taskForm
        +listTasks(UriInfo) TemplateInstance
        +newTaskForm(UriInfo) TemplateInstance
        +createTask(TaskRequestDTO) Response
        +editTask(Long, UriInfo) TemplateInstance
        +updateTask(Long, TaskRequestDTO) Response
        +deleteTask(Long) Response
        -resolveSuccessMessage(String) String
        -extractFormData(MultivaluedMap, String...) Map
        -resolveInitialValue(String, String) String
        -sanitizeMessage(String) String
        -safeValue(Object) String
    }
    
    class TaskService {
        <<Service>>
        +findByOwner() List~Task~
        +findById(Long) Task
        +create(TaskRequestDTO) void
        +update(Long, TaskRequestDTO) void
        +delete(Long) void
    }
    
    class TaskRequestDTO {
        <<DTO>>
        @RestForm String title
        @RestForm String description
        @RestForm String dueDate
        @RestForm String status
    }
    
    class TaskResponseDTO {
        <<DTO>>
        -Long id
        -String title
        -String description
        -LocalDate dueDate
        -TaskStatus status
        -LocalDateTime createdAt
        -LocalDateTime updatedAt
    }
    
    class TemplateData {
        <<Qute Data>>
        tasks: List~TaskResponseDTO~
        task: TaskResponseDTO
        statuses: TaskStatus[]
        breadcrumb: List~BreadcrumbItem~
        successMessage: String
        errorMessage: String
        initialTitle: String
        initialDescription: String
        initialDueDate: String
        initialStatus: String
    }
    
    TaskController --> TaskService : usa
    TaskController ..> TaskRequestDTO : recebe
    TaskController ..> TaskResponseDTO : envia
    TaskController ..> TemplateData : prepara
```

---

## 16. DTOs de Request

Todos os DTOs de entrada com anotações @RestForm.

```mermaid
classDiagram
    direction TB
    
    class UserRequestDTO {
        <<Request DTO>>
        @RestForm
        -String name
        -String email
        -String password
        -Long profileId
        +getName() String
        +setName(String) void
        +getEmail() String
        +setEmail(String) void
        +getPassword() String
        +setPassword(String) void
        +getProfileId() Long
        +setProfileId(Long) void
    }
    
    class UserProfileRequestDTO {
        <<Request DTO>>
        @RestForm
        -String name
        +getName() String
        +setName(String) void
    }
    
    class NoteRequestDTO {
        <<Request DTO>>
        @RestForm
        -String title
        -String content
        +getTitle() String
        +setTitle(String) void
        +getContent() String
        +setContent(String) void
    }
    
    class TaskRequestDTO {
        <<Request DTO>>
        @RestForm
        -String title
        -String description
        -String dueDate
        -String status
        +getTitle() String
        +setTitle(String) void
        +getDescription() String
        +setDescription(String) void
        +getDueDate() String
        +setDueDate(String) void
        +getStatus() String
        +setStatus(String) void
    }
    
    class LoginRequestDTO {
        <<Request DTO>>
        @RestForm("j_username")
        -String username
        @RestForm("j_password")
        -String password
        +getUsername() String
        +getPassword() String
    }
    
    class AuditLogFilterDTO {
        <<Request DTO>>
        @RestQuery
        -String username
        -String method
        -String action
        -String from
        -String to
        -Integer page
        -Integer size
    }
    
    note for UserRequestDTO "Usado em:\n@BeanParam UserRequestDTO"
    note for TaskRequestDTO "Converte dueDate String→LocalDate"
```

---

## 17. DTOs de Response

Todos os DTOs de saída para templates.

```mermaid
classDiagram
    direction TB
    
    class UserResponseDTO {
        <<Response DTO>>
        -Long id
        -String name
        -String email
        -String profileName
        -Long profileId
        -LocalDateTime createdAt
        +UserResponseDTO(User user)
        +getId() Long
        +getName() String
        +getEmail() String
        +getProfileName() String
        +getProfileId() Long
        +getCreatedAt() LocalDateTime
    }
    
    class UserProfileResponseDTO {
        <<Response DTO>>
        -Long id
        -String name
        -long userCount
        +UserProfileResponseDTO(UserProfile, long)
        +getId() Long
        +getName() String
        +getUserCount() long
    }
    
    class NoteResponseDTO {
        <<Response DTO>>
        -Long id
        -String title
        -String content
        -LocalDateTime createdAt
        -LocalDateTime updatedAt
        +NoteResponseDTO(Note note)
        +getId() Long
        +getTitle() String
        +getContent() String
        +getCreatedAt() LocalDateTime
        +getUpdatedAt() LocalDateTime
    }
    
    class TaskResponseDTO {
        <<Response DTO>>
        -Long id
        -String title
        -String description
        -LocalDate dueDate
        -TaskStatus status
        -LocalDateTime createdAt
        -LocalDateTime updatedAt
        +TaskResponseDTO(Task task)
        +getId() Long
        +getTitle() String
        +getDescription() String
        +getDueDate() LocalDate
        +getStatus() TaskStatus
        +isOverdue() boolean
    }
    
    class AuditLogResponseDTO {
        <<Response DTO>>
        -Long id
        -String username
        -String action
        -String httpMethod
        -String resourcePath
        -String clientIp
        -Integer statusCode
        -String entityType
        -String entityId
        -String details
        -LocalDateTime occurredAt
        +AuditLogResponseDTO(AuditLog log)
    }
    
    UserResponseDTO --> User : from
    UserProfileResponseDTO --> UserProfile : from
    NoteResponseDTO --> Note : from
    TaskResponseDTO --> Task : from
    AuditLogResponseDTO --> AuditLog : from
```

---

## 18. Componentes de Segurança

Sistema de autenticação e autorização.

```mermaid
classDiagram
    direction TB
    
    class SecurityIdentity {
        <<Quarkus Security>>
        +getPrincipal() Principal
        +getRoles() Set~String~
        +hasRole(String) boolean
        +isAnonymous() boolean
    }
    
    class Principal {
        <<java.security>>
        +getName() String
    }
    
    class ElytronSecurityJDBC {
        <<Provider>>
        Form Authentication
        Session Management
        JDBC Realm
    }
    
    class RolesAllowed {
        <<annotation>>
        @RolesAllowed("ADMIN")
        @RolesAllowed(["USER", "ADMIN"])
    }
    
    class BcryptUtil {
        <<Utility>>
        +bcryptHash(String password) String$
        +bcryptHash(String password, int iterationCount) String$
        +matches(String plaintext, String hash) boolean$
    }
    
    class AuthService {
        <<ApplicationScoped>>
        -UserDao userDao
        +authenticate(String email, String password) Optional~User~
        +findByEmail(String email) Optional~User~
    }
    
    class SecurityConfig {
        <<Properties>>
        quarkus.http.auth.form.enabled=true
        quarkus.http.auth.form.login-page=/login
        quarkus.http.auth.form.error-page=/login?error
        quarkus.http.auth.form.landing-page=/users
        quarkus.security.jdbc.enabled=true
    }
    
    SecurityIdentity --> Principal : has
    ElytronSecurityJDBC --> SecurityIdentity : provides
    AuthService ..> BcryptUtil : usa
    ElytronSecurityJDBC .. SecurityConfig : configures
```

---

## 19. Filtros e Interceptadores

Componentes de interceptação de requisições.

```mermaid
classDiagram
    direction TB
    
    class AuditLogFilter {
        <<ContainerRequestFilter>>
        -AuditLogService auditLogService
        -SecurityIdentity securityIdentity
        +filter(ContainerRequestContext) void
        -extractClientIp(ContainerRequestContext) String
        -shouldSkipAudit(String) boolean
    }
    
    class LoginAuditRouteFilter {
        <<ServerFilter>>
        -AuditLogService auditLogService
        +filter(RoutingContext) void
        -extractClientIp(RoutingContext) String
    }
    
    class AuthenticationAuditObserver {
        <<Observer>>
        -AuditLogService auditLogService
        +onAuthenticationSuccess(AuthenticationSuccessEvent) void
        +onAuthenticationFailure(AuthenticationFailedEvent) void
    }
    
    class ErrorPageMapper {
        <<ExceptionMapper>>
        -Template error403
        -Template error404
        -Template error500
        +toResponse(Exception) Response
    }
    
    class ContainerRequestContext {
        <<JAX-RS>>
        +getUriInfo() UriInfo
        +getMethod() String
        +getHeaderString(String) String
    }
    
    class RoutingContext {
        <<Vert.x>>
        +request() HttpServerRequest
        +next() void
    }
    
    AuditLogFilter ..|> ContainerRequestFilter : implements
    AuditLogFilter --> AuditLogService : usa
    LoginAuditRouteFilter --> AuditLogService : usa
    AuthenticationAuditObserver --> AuditLogService : usa
    ErrorPageMapper ..|> ExceptionMapper : implements
    
    note for AuditLogFilter "@Provider\nIntercepts all requests"
    note for ErrorPageMapper "Custom error pages\n403, 404, 500"
```

---

## 20. Arquitetura Completa MVC

Visão completa da arquitetura Model-View-Controller.

```mermaid
classDiagram
    direction TB
    
    %% Controllers (C)
    class Controllers {
        <<layer>>
        UserController
        TaskController
        NoteController
        UserProfileController
        AuditController
        LoginController
        LogoutController
        HomeController
        DocsController
    }
    
    %% Views (V)
    class Views {
        <<layer>>
        Templates (Qute)
        Tags (navigation, breadcrumb, flash)
        Error Pages (403, 404, 500)
        BreadcrumbItem
    }
    
    %% Models (M)
    class Models {
        <<layer>>
        User
        UserProfile
        Note
        Task
        AuditLog
        TaskStatus
    }
    
    %% Services (Business Logic)
    class Services {
        <<layer>>
        UserService
        UserProfileService
        NoteService
        TaskService
        AuditLogService
        AuthService
    }
    
    %% DAOs (Data Access)
    class DAOs {
        <<layer>>
        UserDao
        UserProfileDao
        NoteDao
        TaskDao
        AuditLogDao
    }
    
    %% DTOs (Data Transfer)
    class DTOs {
        <<layer>>
        RequestDTOs
        ResponseDTOs
        FilterDTOs
    }
    
    %% Security
    class Security {
        <<layer>>
        SecurityIdentity
        BcryptUtil
        @RolesAllowed
        Elytron JDBC
    }
    
    %% Filters
    class Filters {
        <<layer>>
        AuditLogFilter
        LoginAuditRouteFilter
        AuthenticationAuditObserver
        ErrorPageMapper
    }
    
    %% Database
    class Database {
        <<layer>>
        PostgreSQL 15
        Flyway Migrations
        Hibernate/Panache
    }
    
    %% Relationships
    Controllers --> Services : usa
    Controllers --> Views : renderiza
    Controllers --> DTOs : transforma
    Controllers --> Security : protegido
    Services --> DAOs : persiste
    Services --> Models : manipula
    DAOs --> Database : acessa
    Filters --> Services : intercepta
    
    note for Controllers "@Path, @GET, @POST\n@Produces, @Consumes"
    note for Views "Qute Templates\n{#if}, {#for}, {#tags}"
    note for Models "@Entity, @Table\n@ManyToOne, @OneToMany"
    note for Services "@ApplicationScoped\n@Transactional"
    note for DAOs "PanacheRepository\nCustom queries"
    note for Database "Flyway V1__*.sql\nValidate schema"
```

---

## 🔗 Referência Rápida

### Padrões Utilizados

| Padrão | Implementação |
|--------|---------------|
| **MVC** | Controllers + Qute + Entities |
| **Repository** | Panache DAOs |
| **DTO** | Request/Response DTOs |
| **Service Layer** | Business Logic Services |
| **Observer** | Authentication Events |
| **Filter** | Request Interceptors |

### Relacionamentos Entre Entidades

| Entidade | Relacionamento | Entidade |
|----------|----------------|----------|
| UserProfile | 1: N | User |
| User | 1: N | Note |
| User | 1: N | Task |
| Task | N:1 | TaskStatus |

### Fluxo de Dados

```
Request → Filter → Controller → Service → DAO → Database
                       ↓
                  Template (Qute)
                       ↓
                   Response
```

---

*Diagramas criados com Mermaid - Janeiro 2025*
