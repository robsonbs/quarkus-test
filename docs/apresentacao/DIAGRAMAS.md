# 🏗️ Diagramas de Arquitetura

## Visão Geral da Arquitetura MVC

```mermaid
flowchart TB
    subgraph Apresentacao["📱 Camada de Apresentação"]
        direction LR
        T[Templates Qute]
        Tags[Tags Reutilizáveis]
        CSS[Tailwind CSS]
    end

    subgraph Controller["🎮 Camada Controller"]
        direction LR
        UC[UserController]
        NC[NoteController]
        TC[TaskController]
        AC[AuditController]
        LC[LoginController]
    end

    subgraph Service["⚙️ Camada Service/BO"]
        direction LR
        US[UserService]
        NS[NoteService]
        TS[TaskService]
        AS[AuditLogService]
        Auth[AuthService]
    end

    subgraph DAO["💾 Camada DAO"]
        direction LR
        UD[UserDao]
        ND[NoteDao]
        TD[TaskDao]
        AD[AuditLogDao]
    end

    subgraph Model["📦 Camada Model"]
        direction LR
        UE[User]
        NE[Note]
        TE[Task]
        AE[AuditLog]
        UP[UserProfile]
    end

    subgraph DB["🗄️ Banco de Dados"]
        PG[(PostgreSQL 15)]
    end

    Apresentacao --> Controller
    Controller --> Service
    Service --> DAO
    DAO --> Model
    Model --> DB
```

---

## Fluxo de Autenticação

```mermaid
sequenceDiagram
    participant U as Usuário
    participant L as LoginController
    participant E as Elytron JDBC
    participant DB as PostgreSQL
    participant S as Sessão

    U->>L: GET /login
    L->>U: Página de login

    U->>E: POST /j_security_check<br/>(email + senha)
    E->>DB: SELECT user WHERE email=?
    DB->>E: User + hash BCrypt
    E->>E: Verificar senha
    
    alt Credenciais válidas
        E->>S: Criar sessão
        S->>U: Redirect /users (302)
    else Credenciais inválidas
        E->>U: Redirect /login?error (302)
    end
```

---

## Fluxo de Criação de Tarefa

```mermaid
sequenceDiagram
    participant U as Usuário
    participant C as TaskController
    participant S as TaskService
    participant D as TaskDao
    participant A as AuditLogService
    participant DB as PostgreSQL

    U->>C: POST /tasks<br/>(título, descrição, prazo)
    C->>C: Validar parâmetros
    C->>S: create(TaskRequestDTO, ownerEmail)
    
    S->>S: Validar título não vazio
    S->>S: Validar descrição não vazia
    S->>S: Validar prazo >= hoje
    
    alt Validação OK
        S->>D: persist(Task)
        D->>DB: INSERT INTO tasks...
        DB->>D: Task com ID
        D->>S: Task persistida
        S->>A: recordDomainEvent("TASK_CREATED", ...)
        A->>DB: INSERT INTO audit_logs...
        S->>C: TaskResponseDTO
        C->>U: Redirect /tasks?success=...
    else Validação falhou
        S->>C: WebApplicationException
        C->>U: Redirect /tasks/new?error=...
    end
```

---

## Workflow de Status de Tarefas

```mermaid
stateDiagram-v2
    [*] --> PENDENTE: Criar tarefa
    
    PENDENTE --> EM_ANDAMENTO: Iniciar trabalho
    PENDENTE --> CONCLUIDA: Concluir direto
    
    EM_ANDAMENTO --> CONCLUIDA: Finalizar
    EM_ANDAMENTO --> PENDENTE: Pausar
    
    CONCLUIDA --> [*]
    
    note right of PENDENTE
        Status inicial
        Aguardando início
    end note
    
    note right of EM_ANDAMENTO
        Trabalho em progresso
        Pode voltar a pendente
    end note
    
    note right of CONCLUIDA
        Tarefa finalizada
        Estado terminal
    end note
```

---

## Modelo de Dados (ERD)

```mermaid
erDiagram
    USER_PROFILE ||--o{ USER : "tem"
    USER ||--o{ NOTE : "possui"
    USER ||--o{ TASK : "possui"
    USER ||--o{ AUDIT_LOG : "gera"

    USER_PROFILE {
        bigint id PK
        varchar name UK
        timestamp created_at
        timestamp updated_at
    }

    USER {
        bigint id PK
        varchar email UK
        varchar password
        varchar name
        bigint profile_id FK
        timestamp created_at
        timestamp updated_at
    }

    NOTE {
        bigint id PK
        varchar title
        text content
        varchar owner_email FK
        timestamp created_at
        timestamp updated_at
    }

    TASK {
        bigint id PK
        varchar title
        text description
        varchar status
        date due_date
        varchar owner_email FK
        timestamp created_at
        timestamp updated_at
    }

    AUDIT_LOG {
        bigint id PK
        varchar action
        varchar entity_type
        varchar entity_id
        varchar actor_email
        text details
        timestamp timestamp
    }
```

---

## Estrutura de Pacotes

```mermaid
graph TB
    subgraph com.robsonbs
        direction TB
        
        subgraph controller["controller/"]
            C1[AuditController]
            C2[DocsController]
            C3[HomeController]
            C4[LoginController]
            C5[LogoutController]
            C6[NoteController]
            C7[TaskController]
            C8[UserController]
            C9[UserProfileController]
        end
        
        subgraph service["service/"]
            S1[AuthService]
            S2[AuditLogService]
            S3[NoteService]
            S4[TaskService]
            S5[UserService]
            S6[UserProfileService]
        end
        
        subgraph dao["dao/"]
            D1[AuditLogDao]
            D2[NoteDao]
            D3[TaskDao]
            D4[UserDao]
            D5[UserProfileDao]
        end
        
        subgraph model["model/"]
            M1[AuditLog]
            M2[Note]
            M3[Task]
            M4[TaskStatus]
            M5[User]
            M6[UserProfile]
        end
        
        subgraph dto["dto/"]
            DTO1[*RequestDTO]
            DTO2[*ResponseDTO]
        end
        
        subgraph view["view/"]
            V1[BreadcrumbItem]
        end
    end
    
    controller --> service
    service --> dao
    dao --> model
    controller --> dto
    service --> dto
```

---

## Fluxo de Requisição HTTP

```mermaid
flowchart LR
    subgraph Cliente
        Browser[🌐 Browser]
    end
    
    subgraph Quarkus["Quarkus Server"]
        direction TB
        
        Filter[SecurityFilter]
        Router[JAX-RS Router]
        
        subgraph Controllers
            UC[UserController]
            NC[NoteController]
            TC[TaskController]
        end
        
        subgraph Templates
            Qute[Qute Engine]
            HTML[HTML Response]
        end
    end
    
    Browser -->|HTTP Request| Filter
    Filter -->|Authenticated| Router
    Router --> Controllers
    Controllers --> Qute
    Qute --> HTML
    HTML -->|HTTP Response| Browser
    
    Filter -->|Not Authenticated| Login[/login]
    Login --> Browser
```

---

## Controle de Acesso por Perfil

```mermaid
flowchart TB
    subgraph Roles["Perfis de Acesso"]
        ADMIN[👑 ADMIN]
        USER[👤 USER]
        PUBLIC[🌐 Público]
    end
    
    subgraph Features["Funcionalidades"]
        direction TB
        
        subgraph AdminOnly["Apenas ADMIN"]
            F1[Gerenciar Usuários]
            F2[Gerenciar Perfis]
            F3[Visualizar Auditoria]
        end
        
        subgraph UserFeatures["USER + ADMIN"]
            F4[Minhas Notas]
            F5[Minhas Tarefas]
        end
        
        subgraph PublicPages["Público"]
            F6[Login]
            F7[Documentação]
        end
    end
    
    ADMIN --> AdminOnly
    ADMIN --> UserFeatures
    USER --> UserFeatures
    PUBLIC --> PublicPages
```

---

## Pipeline de Testes

```mermaid
flowchart LR
    subgraph Tests["185 Testes Automatizados"]
        direction TB
        
        subgraph Unit["Testes Unitários"]
            UT1[TaskServiceTest - 25]
            UT2[NoteServiceTest - 23]
            UT3[UserServiceTest - 23]
            UT4[UserProfileServiceTest - 28]
            UT5[AuditLogServiceTest - 15]
            UT6[DTOValidationTest - 26]
        end
        
        subgraph Integration["Testes de Integração"]
            IT1[AccessControlTest - 16]
            IT2[SecurityTest - 19]
        end
    end
    
    subgraph CI["Execução"]
        Maven[./mvnw test]
        Report[Relatório JUnit]
    end
    
    Tests --> Maven
    Maven --> Report
    Report -->|✅ 185/185| Success[Sucesso]
```

---

## Infraestrutura Docker

```mermaid
flowchart TB
    subgraph Docker["Docker Compose"]
        direction TB
        
        subgraph Containers
            PG[(PostgreSQL 15<br/>porta 5432)]
            
        end
        
        subgraph Volumes
            V1[pgdata]
        end
        
        subgraph Network
            N1[quarkus-network]
        end
    end
    
    subgraph Host["Máquina Host"]
        App[Quarkus App<br/>porta 8080]
        Browser[Browser]
    end
    
    PG --> V1
    PG --> N1
    App -->|JDBC| PG
    Browser -->|HTTP| App
```

---

## Como usar estes diagramas

### No GitHub/GitLab

Os diagramas Mermaid são renderizados automaticamente em arquivos `.md` .

### Em apresentações

1. Acesse [Mermaid Live Editor](https://mermaid.live/)
2. Cole o código do diagrama
3. Exporte como PNG ou SVG

### No VS Code

Instale a extensão "Markdown Preview Mermaid Support" para visualizar.

### Ferramentas alternativas

* [Draw.io](https://draw.io) - Para edição visual
* [PlantUML](https://plantuml.com) - Sintaxe alternativa
* [Lucidchart](https://lucidchart.com) - Edição colaborativa
