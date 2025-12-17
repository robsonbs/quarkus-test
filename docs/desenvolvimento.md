# 💻 Guia de Desenvolvimento

> Guia completo para desenvolvedores que desejam contribuir ou entender o código do projeto.

---

## 📋 Índice

* [Configuração do Ambiente](#configuração-do-ambiente)
* [Estrutura do Projeto](#estrutura-do-projeto)
* [Padrões de Código](#padrões-de-código)
* [Convenções de Nomenclatura](#convenções-de-nomenclatura)
* [Criando Novos Recursos](#criando-novos-recursos)
* [Templates Qute](#templates-qute)
* [Banco de Dados](#banco-de-dados)
* [Debugging](#debugging)
* [Troubleshooting](#troubleshooting)

---

## Configuração do Ambiente

### Pré-requisitos

| Ferramenta | Versão Mínima | Verificar |
|------------|---------------|-----------|
| Java JDK | 17 | `java -version` |
| Maven | 3.8+ | `mvn -version` |
| Docker | 20+ | `docker --version` |
| Docker Compose | 2.0+ | `docker compose version` |
| Git | 2.30+ | `git --version` |

### IDE Recomendada

**Visual Studio Code** com extensões:
* Extension Pack for Java
* Quarkus Tools
* Docker
* PostgreSQL (ckolkman)

**IntelliJ IDEA** com plugins:
* Quarkus Tools
* Docker
* Database Tools

### Setup Inicial

```bash
# 1. Clonar repositório
git clone https://github.com/robsonbs/quarkus-test.git
cd quarkus-test

# 2. Verificar Java
java -version
# Esperado: openjdk version "17.x.x"

# 3. Iniciar banco de dados
docker compose up -d postgres

# 4. Verificar conexão
docker compose ps
# postgres deve estar "running"

# 5. Executar em modo desenvolvimento
./mvnw quarkus:dev

# 6. Acessar aplicação
# http://localhost:8080
```

### Variáveis de Ambiente (Opcionais)

```bash
# Sobrescrever configurações padrão
export QUARKUS_DATASOURCE_USERNAME=myuser
export QUARKUS_DATASOURCE_PASSWORD=mypass
export QUARKUS_DATASOURCE_JDBC_URL=jdbc:postgresql://myhost:5432/mydb
```

---

## Estrutura do Projeto

```
quarkus-test/
│
├── 📁 src/main/java/com/robsonbs/
│   │
│   ├── 📁 controller/          # Endpoints JAX-RS
│   │   ├── AuditController.java
│   │   ├── HomeController.java
│   │   ├── LoginController.java
│   │   ├── LogoutController.java
│   │   ├── NoteController.java
│   │   ├── TaskController.java
│   │   ├── UserController.java
│   │   └── UserProfileController.java
│   │
│   ├── 📁 service/             # Business Objects (Regras)
│   │   ├── AuditLogService.java
│   │   ├── AuthService.java
│   │   ├── NoteService.java
│   │   ├── TaskService.java
│   │   ├── UserProfileService.java
│   │   └── UserService.java
│   │
│   ├── 📁 dao/                 # Data Access Objects
│   │   ├── AuditLogDao.java
│   │   ├── NoteDao.java
│   │   ├── TaskDao.java
│   │   ├── UserDao.java
│   │   └── UserProfileDao.java
│   │
│   ├── 📁 dto/                 # Data Transfer Objects
│   │   ├── AuditLogFilterDTO.java
│   │   ├── AuditLogResponseDTO.java
│   │   ├── LoginRequestDTO.java
│   │   ├── NoteRequestDTO.java
│   │   ├── NoteResponseDTO.java
│   │   ├── TaskRequestDTO.java
│   │   ├── TaskResponseDTO.java
│   │   ├── UserProfileRequestDTO.java
│   │   ├── UserProfileResponseDTO.java
│   │   ├── UserRequestDTO.java
│   │   └── UserResponseDTO.java
│   │
│   ├── 📁 model/               # Entidades JPA
│   │   ├── AuditLog.java
│   │   ├── Note.java
│   │   ├── Task.java
│   │   ├── TaskStatus.java
│   │   ├── User.java
│   │   └── UserProfile.java
│   │
│   ├── 📁 filter/              # Filtros HTTP
│   │   ├── AuditLogFilter.java
│   │   └── LoginAuditRouteFilter.java
│   │
│   └── 📁 view/                # Helpers de View
│       ├── BreadcrumbItem.java
│       └── ErrorPageMapper.java
│
├── 📁 src/main/resources/
│   ├── 📄 application.properties
│   │
│   ├── 📁 db/migration/        # Migrações Flyway
│   │   ├── V1__Initial_schema.sql
│   │   ├── V2__Audit_and_demo_data.sql
│   │   └── ...
│   │
│   └── 📁 templates/           # Templates Qute
│       ├── index.html
│       ├── login.html
│       ├── users.html
│       ├── userForm.html
│       ├── notes.html
│       ├── noteForm.html
│       ├── tasks.html
│       ├── taskForm.html
│       ├── profiles.html
│       ├── profileForm.html
│       ├── audit.html
│       ├── docs.html
│       │
│       ├── 📁 tags/            # Componentes reutilizáveis
│       │   ├── navigation.html
│       │   ├── breadcrumb.html
│       │   └── flash.html
│       │
│       └── 📁 errors/          # Páginas de erro
│           ├── 403.html
│           ├── 404.html
│           └── 500.html
│
└── 📁 src/test/java/           # Testes
    └── com/robsonbs/
        ├── BcryptTest.java
        ├── SecurityIntegrationTest.java
        └── service/
            ├── AuditLogServiceTest.java
            ├── NoteServiceTest.java
            ├── TaskServiceTest.java
            └── UserServiceTest.java
```

---

## Padrões de Código

### Controller

```java
@Path("/recurso")
@RolesAllowed({"USER", "ADMIN"})  // Controle de acesso
@Blocking                          // Permite operações síncronas de DB
public class RecursoController {

    @Inject
    RecursoService recursoService;  // Injeção de serviço

    @Inject
    Template recursoTemplate;       // Injeção de template

    // LISTAR
    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance listar(@Context UriInfo uriInfo) {
        var params = uriInfo.getQueryParameters();
        String successMessage = params.getFirst("success");
        String errorMessage = params.getFirst("error");
        
        List<RecursoResponseDTO> itens = recursoService.findAll()
            .stream()
            .map(RecursoResponseDTO::new)
            .collect(Collectors.toList());
            
        return recursoTemplate
            .data("itens", itens)
            .data("successMessage", successMessage)
            .data("errorMessage", errorMessage)
            .data("breadcrumb", List.of(
                BreadcrumbItem.link("Início", "/"),
                BreadcrumbItem.current("Recursos")
            ));
    }

    // FORMULÁRIO NOVO
    @GET
    @Path("/new")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance formularioNovo() {
        return recursoForm
            .data("recurso", null)
            .data("breadcrumb", List.of(
                BreadcrumbItem.link("Início", "/"),
                BreadcrumbItem.link("Recursos", "/recursos"),
                BreadcrumbItem.current("Novo")
            ));
    }

    // CRIAR
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response criar(@BeanParam RecursoRequestDTO dto) {
        try {
            recursoService.create(dto);
            return Response.seeOther(
                URI.create("/recursos?success=Recurso criado com sucesso")
            ).build();
        } catch (WebApplicationException ex) {
            return Response.seeOther(
                UriBuilder.fromPath("/recursos/new")
                    .queryParam("error", ex.getMessage())
                    .build()
            ).build();
        }
    }

    // FORMULÁRIO EDIÇÃO
    @GET
    @Path("/{id}")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance formularioEdicao(@PathParam("id") Long id) {
        RecursoResponseDTO recurso = new RecursoResponseDTO(
            recursoService.findById(id)
        );
        return recursoForm
            .data("recurso", recurso)
            .data("breadcrumb", List.of(
                BreadcrumbItem.link("Início", "/"),
                BreadcrumbItem.link("Recursos", "/recursos"),
                BreadcrumbItem.current("Editar")
            ));
    }

    // ATUALIZAR
    @POST
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response atualizar(
            @PathParam("id") Long id,
            @BeanParam RecursoRequestDTO dto) {
        try {
            recursoService.update(id, dto);
            return Response.seeOther(
                URI.create("/recursos?success=Recurso atualizado")
            ).build();
        } catch (WebApplicationException ex) {
            return Response.seeOther(
                UriBuilder.fromPath("/recursos/{id}")
                    .resolveTemplate("id", id)
                    .queryParam("error", ex.getMessage())
                    .build()
            ).build();
        }
    }

    // EXCLUIR
    @POST
    @Path("/{id}/delete")
    public Response excluir(@PathParam("id") Long id) {
        try {
            recursoService.delete(id);
            return Response.seeOther(
                URI.create("/recursos?success=Recurso excluído")
            ).build();
        } catch (WebApplicationException ex) {
            return Response.seeOther(
                UriBuilder.fromPath("/recursos")
                    .queryParam("error", ex.getMessage())
                    .build()
            ).build();
        }
    }
}
```

### Service (Business Object)

```java
@ApplicationScoped
public class RecursoService {

    @Inject
    RecursoDao recursoDao;

    @Inject
    SecurityIdentity securityIdentity;

    @Inject
    AuditLogService auditLogService;

    // BUSCAR TODOS (do usuário atual)
    public List<Recurso> findByOwner() {
        User owner = currentUser();
        return recursoDao.findByOwnerId(owner.getId());
    }

    // BUSCAR POR ID (com verificação de propriedade)
    public Recurso findById(Long id) {
        Recurso recurso = recursoDao.findById(id);
        if (recurso == null) {
            throw new NotFoundException("Recurso não encontrado");
        }
        verifyOwnership(recurso);
        return recurso;
    }

    // CRIAR
    @Transactional
    public void create(RecursoRequestDTO dto) {
        validateRecurso(dto);
        
        Recurso recurso = new Recurso();
        recurso.setNome(dto.getNome().trim());
        recurso.setOwner(currentUser());
        recursoDao.persist(recurso);
        recursoDao.flush();

        auditLogService.recordDomainEvent(
            currentUserEmail(),
            "RECURSO_CREATED",
            "/recursos",
            "Recurso",
            recurso.getId().toString(),
            "Recurso criado",
            Response.Status.CREATED.getStatusCode()
        );
    }

    // ATUALIZAR
    @Transactional
    public void update(Long id, RecursoRequestDTO dto) {
        Recurso recurso = findById(id);
        validateRecurso(dto);
        
        recurso.setNome(dto.getNome().trim());
        recursoDao.persist(recurso);

        auditLogService.recordDomainEvent(
            currentUserEmail(),
            "RECURSO_UPDATED",
            "/recursos/" + id,
            "Recurso",
            recurso.getId().toString(),
            "Recurso atualizado",
            Response.Status.OK.getStatusCode()
        );
    }

    // EXCLUIR
    @Transactional
    public void delete(Long id) {
        Recurso recurso = findById(id);
        recursoDao.delete(recurso);

        auditLogService.recordDomainEvent(
            currentUserEmail(),
            "RECURSO_DELETED",
            "/recursos/" + id,
            "Recurso",
            id.toString(),
            "Recurso excluído",
            Response.Status.OK.getStatusCode()
        );
    }

    // VALIDAÇÕES (Regras de Negócio)
    private void validateRecurso(RecursoRequestDTO dto) {
        if (dto.getNome() == null || dto.getNome().isBlank()) {
            throw new BadRequestException("Nome é obrigatório");
        }
        if (dto.getNome().length() > 200) {
            throw new BadRequestException("Nome muito longo (máx 200 caracteres)");
        }
    }

    // VERIFICAÇÃO DE PROPRIEDADE
    private void verifyOwnership(Recurso recurso) {
        User currentUser = currentUser();
        if (!recurso.getOwner().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Acesso negado");
        }
    }

    // OBTER USUÁRIO ATUAL
    private User currentUser() {
        String email = securityIdentity.getPrincipal().getName();
        return userDao.find("email", email).firstResult();
    }

    private String currentUserEmail() {
        return securityIdentity.getPrincipal().getName();
    }
}
```

### DAO (Data Access Object)

```java
@ApplicationScoped
public class RecursoDao implements PanacheRepository<Recurso> {

    // Busca por proprietário
    public List<Recurso> findByOwnerId(Long ownerId) {
        return find("owner.id", ownerId).list();
    }

    // Busca por ID e proprietário
    public Optional<Recurso> findByIdAndOwner(Long id, Long ownerId) {
        return find("id = ?1 and owner.id = ?2", id, ownerId)
            .firstResultOptional();
    }

    // Busca com ordenação
    public List<Recurso> findByOwnerIdOrdered(Long ownerId) {
        return find("owner.id", Sort.by("createdAt").descending(), ownerId)
            .list();
    }

    // Contagem
    public long countByOwner(Long ownerId) {
        return count("owner.id", ownerId);
    }

    // Busca paginada
    public List<Recurso> findByOwnerPaged(Long ownerId, int page, int size) {
        return find("owner.id", ownerId)
            .page(page, size)
            .list();
    }
}
```

### DTO (Data Transfer Object)

**Request DTO (entrada):**

```java
public class RecursoRequestDTO {

    @RestForm
    private String nome;

    @RestForm
    private String descricao;

    // Getters
    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    // Setters
    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}
```

**Response DTO (saída):**

```java
public class RecursoResponseDTO {

    private Long id;
    private String nome;
    private String descricao;
    private String ownerName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Construtor a partir da entidade
    public RecursoResponseDTO(Recurso entity) {
        this.id = entity.getId();
        this.nome = entity.getNome();
        this.descricao = entity.getDescricao();
        this.ownerName = entity.getOwner().getName();
        this.createdAt = entity.getCreatedAt();
        this.updatedAt = entity.getUpdatedAt();
    }

    // Getters (sem setters - imutável)
    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getDescricao() { return descricao; }
    public String getOwnerName() { return ownerName; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
```

### Entity (Entidade JPA)

```java
@Entity
@Table(name = "recursos")
public class Recurso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String nome;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Callbacks de ciclo de vida
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Construtores
    public Recurso() {}

    public Recurso(String nome, String descricao, User owner) {
        this.nome = nome;
        this.descricao = descricao;
        this.owner = owner;
    }

    // Getters e Setters
    // ...
}
```

---

## Convenções de Nomenclatura

### Classes

| Tipo | Padrão | Exemplo |
|------|--------|---------|
| Controller | `{Nome}Controller` | `UserController` |
| Service | `{Nome}Service` | `UserService` |
| DAO | `{Nome}Dao` | `UserDao` |
| Entity | `{Nome}` (singular) | `User` |
| Request DTO | `{Nome}RequestDTO` | `UserRequestDTO` |
| Response DTO | `{Nome}ResponseDTO` | `UserResponseDTO` |
| Enum | `{Nome}Status` ou `{Nome}Type` | `TaskStatus` |

### Métodos

| Operação | Padrão | Exemplo |
|----------|--------|---------|
| Buscar todos | `findAll()` ou `findBy{Criterio}()` | `findByOwner()` |
| Buscar um | `findById(id)` | `findById(1L)` |
| Criar | `create(dto)` | `create(userDTO)` |
| Atualizar | `update(id, dto)` | `update(1L, userDTO)` |
| Excluir | `delete(id)` | `delete(1L)` |
| Validar | `validate{Nome}(dto)` | `validateUser(dto)` |
| Verificar | `verify{Condicao}(param)` | `verifyOwnership(note)` |

### Variáveis

| Tipo | Padrão | Exemplo |
|------|--------|---------|
| Lista | `{nome}s` ou `{nome}List` | `users` , `noteList` |
| Single | `{nome}` (singular) | `user` , `note` |
| DTO | `{nome}DTO` ou `dto` | `userDTO` , `dto` |
| ID | `{nome}Id` ou `id` | `userId` , `id` |

### Pacotes

```
com.robsonbs
├── controller    # Endpoints HTTP
├── service       # Regras de negócio
├── dao           # Acesso a dados
├── dto           # Transferência de dados
├── model         # Entidades JPA
├── filter        # Filtros HTTP
└── view          # Helpers de view
```

---

## Criando Novos Recursos

### Checklist para Novo CRUD

01. [ ] **Entity** - `model/{Nome}.java`
02. [ ] **DAO** - `dao/{Nome}Dao.java`
03. [ ] **Request DTO** - `dto/{Nome}RequestDTO.java`
04. [ ] **Response DTO** - `dto/{Nome}ResponseDTO.java`
05. [ ] **Service** - `service/{Nome}Service.java`
06. [ ] **Controller** - `controller/{Nome}Controller.java`
07. [ ] **Migration** - `db/migration/V{N}__{descricao}.sql`
08. [ ] **Template Lista** - `templates/{nomes}.html`
09. [ ] **Template Form** - `templates/{nome}Form.html`
10. [ ] **Testes** - `test/java/.../service/{Nome}ServiceTest.java`
11. [ ] **Navegação** - Atualizar `tags/navigation.html`

### Exemplo: Adicionando "Categoria"

**1. Entity:**

```java
// model/Categoria.java
@Entity
@Table(name = "categorias")
public class Categoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nome;

    // getters, setters
}
```

**2. Migration:**

```sql
-- db/migration/V8__Create_categorias_table.sql
CREATE TABLE categorias (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL UNIQUE
);
```

**3. DAO:**

```java
// dao/CategoriaDao.java
@ApplicationScoped
public class CategoriaDao implements PanacheRepository<Categoria> {
    public Optional<Categoria> findByNome(String nome) {
        return find("nome", nome).firstResultOptional();
    }
}
```

**4. DTOs:**

```java
// dto/CategoriaRequestDTO.java
public class CategoriaRequestDTO {
    @RestForm
    private String nome;
    // getter, setter
}

// dto/CategoriaResponseDTO.java
public class CategoriaResponseDTO {
    private Long id;
    private String nome;

    public CategoriaResponseDTO(Categoria entity) {
        this.id = entity.getId();
        this.nome = entity.getNome();
    }
    // getters
}
```

**5. Service:**

```java
// service/CategoriaService.java
@ApplicationScoped
public class CategoriaService {
    @Inject CategoriaDao categoriaDao;
    @Inject AuditLogService auditLogService;

    public List<Categoria> findAll() {
        return categoriaDao.listAll();
    }

    @Transactional
    public void create(CategoriaRequestDTO dto) {
        if (dto.getNome() == null || dto.getNome().isBlank()) {
            throw new BadRequestException("Nome é obrigatório");
        }
        Categoria cat = new Categoria();
        cat.setNome(dto.getNome().trim());
        categoriaDao.persist(cat);
        // auditoria...
    }
    // update, delete...
}
```

**6. Controller:**

```java
// controller/CategoriaController.java
@Path("/categorias")
@RolesAllowed("ADMIN")
@Blocking
public class CategoriaController {
    @Inject CategoriaService categoriaService;
    @Inject Template categorias;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance listar() {
        return categorias.data("categorias", 
            categoriaService.findAll().stream()
                .map(CategoriaResponseDTO::new)
                .toList());
    }
    // create, update, delete...
}
```

---

## Templates Qute

### Sintaxe Básica

```html
<!-- Variáveis -->
{nome}
{usuario.email}

<!-- Condicionais -->
{#if condicao}
  conteúdo
{#else}
  alternativa
{/if}

<!-- Loops -->
{#for item in lista}
  {item.nome}
{/for}

<!-- Loop vazio -->
{#for item in lista}
  {item.nome}
{#else}
  Nenhum item encontrado
{/for}

<!-- Operador ternário (EVITAR - usar #if) -->
{#if task}Editar{#else}Novo{/if}

<!-- Operador Elvis (valor padrão) -->
{valor ?: 'padrão'}

<!-- Formatação de data -->
{data.format('dd/MM/yyyy')}
{data.format('dd/MM/yyyy HH:mm')}

<!-- Tags customizadas -->
{#navigation /}
{#breadcrumb /}
{#flash /}
```

### Estrutura de Template

```html
<!DOCTYPE html>
<html lang="pt-BR">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>{titulo} • Quarkus Demo</title>
    <script src="https://cdn.tailwindcss.com"></script>
</head>

<body class="bg-gray-100 min-h-screen">
    {#navigation /}
    
    <div class="container mx-auto px-4 py-8">
        <div class="max-w-6xl mx-auto">
            {#breadcrumb /}
            {#flash /}
            
            <!-- Conteúdo da página -->
            <div class="bg-white rounded-lg shadow-md p-6">
                <h1 class="text-2xl font-bold">{titulo}</h1>
                <!-- ... -->
            </div>
        </div>
    </div>
</body>
</html>
```

### Tags Customizadas

As tags ficam em `templates/tags/` e são chamadas pelo nome do arquivo:

```html
<!-- templates/tags/flash.html -->
{#if successMessage}
<div class="bg-green-100 border border-green-400 text-green-700 px-4 py-3 rounded mb-4">
    {successMessage}
</div>
{/if}

{#if errorMessage}
<div class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
    {errorMessage}
</div>
{/if}
```

**Uso:**

```html
{#flash /}
```

---

## Banco de Dados

### Flyway Migrations

**Convenção de nomes:**

```
V{numero}__{descricao}.sql
```

**Exemplos:**

```
V1__Initial_schema.sql
V2__Add_audit_table.sql
V3__Insert_demo_data.sql
```

**Estrutura de migration:**

```sql
-- V8__Create_categorias.sql

-- Criar tabela
CREATE TABLE IF NOT EXISTS categorias (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Criar índice
CREATE INDEX IF NOT EXISTS idx_categorias_nome ON categorias(nome);

-- Inserir dados iniciais (opcional)
INSERT INTO categorias (nome) VALUES ('Geral') ON CONFLICT DO NOTHING;
```

### Comandos Úteis

```bash
# Verificar status das migrations
./mvnw flyway:info

# Executar migrations pendentes
./mvnw flyway:migrate

# Reparar histórico (se necessário)
./mvnw flyway:repair

# Limpar banco (CUIDADO - apaga tudo)
./mvnw flyway:clean
```

### Conectar ao Banco

```bash
# Via Docker
docker exec -it quarkus-test-postgres-1 psql -U quarkus -d quarkusdb

# Comandos úteis
\dt          # Listar tabelas
\d+ users    # Descrever tabela
\q           # Sair
```

---

## Debugging

### Logs

```properties
# application.properties

# Habilitar logs SQL
quarkus.hibernate-orm.log.sql=true

# Log de segurança
quarkus.log.category."io.quarkus.security".level=DEBUG

# Log da aplicação
quarkus.log.category."com.robsonbs".level=DEBUG
```

### Dev UI

Acesse `http://localhost:8080/q/dev-ui` para:
* Visualizar endpoints
* Executar queries
* Ver configurações
* Testar templates

### Breakpoints

No VS Code:
01. Coloque breakpoints no código Java
02. Execute com `./mvnw quarkus:dev -Ddebug`
03. Attach debugger na porta 5005

---

## Troubleshooting

### Erro: "Port 8080 already in use"

```bash
# Encontrar processo
lsof -i :8080

# Matar processo
kill -9 <PID>
```

### Erro: "Connection refused" (PostgreSQL)

```bash
# Verificar se container está rodando
docker compose ps

# Reiniciar banco
docker compose restart postgres

# Ver logs
docker compose logs postgres
```

### Erro: "Template not found"

01. Verificar se template existe em `src/main/resources/templates/`
02. Nome do template deve corresponder ao campo `@Inject Template nomeTemplate;`
03. Reiniciar Quarkus em modo dev

### Erro: "405 Method Not Allowed"

01. Verificar se método HTTP está correto (GET vs POST)
02. Verificar se `@Consumes` está definido para POST
03. Verificar se formulário usa `method="post"`

### Erro: "415 Unsupported Media Type"

01. Adicionar `@RestForm` nos campos do DTO
02. Adicionar `@BeanParam` no parâmetro do controller
03. Verificar `@Consumes(MediaType.APPLICATION_FORM_URLENCODED)`

### Erro: "403 Forbidden"

01. Verificar se usuário está autenticado
02. Verificar se perfil tem permissão (`@RolesAllowed`)
03. Verificar se cookie de sessão está válido

---

## Referências

* [Quarkus Guides](https://quarkus.io/guides/)
* [Qute Reference](https://quarkus.io/guides/qute-reference)
* [Panache Guide](https://quarkus.io/guides/hibernate-orm-panache)
* [Security Guide](https://quarkus.io/guides/security-jdbc)
* [RESTEasy Reactive](https://quarkus.io/guides/resteasy-reactive)
