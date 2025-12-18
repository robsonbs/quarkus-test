# 📚 Sistema de Gestão de Notas e Tarefas

> Aplicação web desenvolvida como projeto prático da disciplina **Programação para Web III** (IFG – Campus Luziânia), demonstrando arquitetura MVC com Java/Quarkus.

![Java](https://img.shields.io/badge/Java-17-orange)

![Quarkus](https://img.shields.io/badge/Quarkus-3.6.4-blue)

![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-336791)

![License](https://img.shields.io/badge/License-MIT-green)

![15-Factor App](https://img.shields.io/badge/15--Factor%20App-Compliant-success)

---

## 📋 Índice

* [Visão Geral](#-visão-geral)
* [Funcionalidades](#-funcionalidades)
* [Arquitetura](#-arquitetura)
* [Tecnologias](#-tecnologias)
* [Pré-requisitos](#-pré-requisitos)
* [Instalação e Execução](#-instalação-e-execução)
* [Estrutura do Projeto](#-estrutura-do-projeto)
* [Endpoints da API](#-endpoints-da-api)
* [Segurança](#-segurança)
* [Testes](#-testes)
* [Conformidade com Requisitos](#-conformidade-com-requisitos)
* [Licença](#-licença)

---

## 🎯 Visão Geral

Sistema web completo para gerenciamento de **notas pessoais** e **tarefas com prazos**, desenvolvido seguindo os padrões de arquitetura **MVC**, **DAO**, **Service (BO)** e **DTO**. A aplicação implementa autenticação baseada em formulário, controle de acesso por perfis (ADMIN/USER) e auditoria completa de todas as ações.

> 🚀 **Cloud Native:** Este projeto segue rigorosamente a metodologia **15-Factor App**, garantindo portabilidade, escalabilidade, observabilidade e segurança para ambientes modernos.

### Contas de Demonstração

| Perfil | E-mail | Senha |
|--------|--------|-------|
| 👑 Administrador | `admin@example.com` | `123` |
| 👤 Usuário | `user@example.com` | `123` |

---

## ✨ Funcionalidades

### Módulos do Sistema

| Módulo | Descrição | Perfis |
|--------|-----------|--------|
| 🔐 **Autenticação** | Login/logout com sessão segura | Público |
| 👥 **Usuários** | CRUD completo de usuários | ADMIN |
| 🏷️ **Perfis** | Gerenciamento de perfis de acesso | ADMIN |
| 📝 **Notas** | Anotações pessoais por usuário | USER, ADMIN |
| ✅ **Tarefas** | Gestão de tarefas com status e prazos | USER, ADMIN |
| 📊 **Auditoria** | Logs de todas as ações do sistema | ADMIN |
| 📈 **Observabilidade** | Métricas, Health Checks e Tracing | ADMIN/SRE |
| 📖 **API First** | Documentação OpenAPI/Swagger | Público |

### Casos de Uso Implementados

1. **Gestão de Notas Pessoais**
   - Criar, editar e excluir anotações
   - Cada usuário visualiza apenas suas próprias notas
   - Validação de campos obrigatórios

2. **Gestão de Tarefas com Prazos**
   - Workflow de status: `Pendente` → `Em Andamento` → `Concluída`

   - Validação de data limite (não permite datas passadas)
   - Histórico completo via auditoria

---

## 🏗️ Arquitetura

O projeto segue o padrão **MVC** com separação clara de responsabilidades:

```
┌─────────────────────────────────────────────────────────────────┐
│                        CAMADA DE APRESENTAÇÃO                    │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐          │
│  │  Templates  │    │    Tags     │    │   Assets    │          │
│  │   (Qute)    │    │  (Partials) │    │    (CSS)    │          │
│  └─────────────┘    └─────────────┘    └─────────────┘          │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                      CAMADA CONTROLLER (JAX-RS)                  │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐          │
│  │    User     │    │    Note     │    │    Task     │          │
│  │ Controller  │    │ Controller  │    │ Controller  │          │
│  └─────────────┘    └─────────────┘    └─────────────┘          │
│  • Recebe requisições HTTP          • Valida parâmetros         │
│  • Prepara DTOs                     • Seleciona templates       │
│  • Redireciona com feedback         • Controla autenticação     │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    CAMADA SERVICE / BO (Negócio)                 │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐          │
│  │    User     │    │    Note     │    │    Task     │          │
│  │   Service   │    │   Service   │    │   Service   │          │
│  └─────────────┘    └─────────────┘    └─────────────┘          │
│  • Regras de negócio                • Validações complexas      │
│  • Hash de senhas                   • Verificação de ownership  │
│  • Auditoria de eventos             • Transações                │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                       CAMADA DAO (Dados)                         │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐          │
│  │   UserDao   │    │   NoteDao   │    │   TaskDao   │          │
│  │  (Panache)  │    │  (Panache)  │    │  (Panache)  │          │
│  └─────────────┘    └─────────────┘    └─────────────┘          │
│  • Acesso ao banco                  • Queries JPQL              │
│  • Repositórios Panache             • Persistência              │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                      CAMADA MODEL (Entidades)                    │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐          │
│  │    User     │    │    Note     │    │    Task     │          │
│  │   Entity    │    │   Entity    │    │   Entity    │          │
│  └─────────────┘    └─────────────┘    └─────────────┘          │
│  • Mapeamento JPA                   • Relacionamentos           │
│  • Validações de schema             • Callbacks de ciclo        │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                         BANCO DE DADOS                           │
│                    ┌─────────────────────┐                       │
│                    │    PostgreSQL 15    │                       │
│                    │  (Docker Container) │                       │
│                    └─────────────────────┘                       │
│                    Schema: Flyway Migrations                     │
└─────────────────────────────────────────────────────────────────┘
```

### Fluxo de Dados (DTO)

```
Browser ──► Controller ──► Service ──► DAO ──► Database
   │            │             │
   │      RequestDTO     Entity
   │            │             │
   └── ResponseDTO ◄─────────┘
```

> **Importante:** Entidades JPA nunca são expostas diretamente ao front-end. Toda comunicação usa DTOs específicos.

---

## 🛠️ Tecnologias

### Backend

* **Java 17** - Linguagem de programação
* **Quarkus 3.6.4** - Framework supersônico
* **JAX-RS (RESTEasy Reactive)** - Endpoints REST
* **Qute** - Template engine
* **Hibernate ORM + Panache** - Persistência
* **Elytron Security JDBC** - Autenticação/Autorização
* **Flyway** - Migrações de banco de dados
* **BCrypt** - Hash de senhas
* **SmallRye OpenAPI** - Documentação de API
* **SmallRye Health** - Monitoramento de saúde
* **Micrometer Prometheus** - Métricas de aplicação

### Frontend

* **HTML5** - Estrutura
* **Tailwind CSS** (CDN) - Estilização
* **JavaScript** - Interatividade

### Infraestrutura

* **PostgreSQL 15** - Banco de dados
* **Docker & Docker Compose** - Containerização
* **Maven Wrapper** - Build

---

## 📦 Pré-requisitos

### Opção 1: Execução Local

* **Java 17** ou superior
* **Docker** e **Docker Compose** (para o banco de dados)
* **Maven 3.8+** (ou use o wrapper `./mvnw`)

### Opção 2: Execução Completa em Container

* **Docker** e **Docker Compose** apenas

### Verificar instalação

```bash
# Java
java -version
# Esperado: openjdk version "17.x.x"

# Docker
docker --version
# Esperado: Docker version 24.x.x

# Docker Compose
docker compose version
# Esperado: Docker Compose version v2.x.x
```

---

## 🚀 Instalação e Execução

### 1. Clonar o Repositório

```bash
git clone https://github.com/robsonbs/quarkus-test.git
cd quarkus-test
```

### 2. Iniciar o Banco de Dados

```bash
# Subir apenas o PostgreSQL
docker compose up -d postgres

# Verificar se está rodando
docker compose ps
# Esperado: postgres   running   0.0.0.0:5432->5432/tcp
```

### 3. Executar a Aplicação

#### Modo Desenvolvimento (Hot Reload)

```bash
./mvnw quarkus:dev
```

A aplicação estará disponível em: **http://localhost:8080**

> 💡 **Dica:** O modo dev recarrega automaticamente as alterações de código.

#### Modo Produção (JAR)

```bash
# Compilar
./mvnw clean package -DskipTests

# Executar
java -jar target/quarkus-app/quarkus-run.jar
```

#### Docker Compose (Stack Completa)

```bash
# Compilar e subir tudo
./mvnw clean package -DskipTests
docker compose up --build

# Ou em background
docker compose up --build -d
```

### 4. Acessar o Sistema

1. Abra o navegador em **http://localhost:8080**
2. Você será redirecionado para `/login`
3. Use as credenciais de demonstração:
   - Admin: `admin@example.com` / `123`

   - Usuário: `user@example.com` / `123`

### 5. Parar a Aplicação

```bash
# Parar containers
docker compose down

# Parar e remover volumes (limpa dados)
docker compose down -v
```

---

## 📁 Estrutura do Projeto

```
quarkus-test/
├── 📄 pom.xml                    # Configuração Maven
├── 📄 docker-compose.yml         # Orquestração de containers
├── 📄 README.md                  # Este arquivo
│
├── 📁 src/main/java/com/robsonbs/
│   ├── 📁 controller/            # 🎮 Controllers JAX-RS
│   │   ├── AuditController.java      # Logs de auditoria
│   │   ├── HomeController.java       # Página inicial
│   │   ├── LoginController.java      # Autenticação
│   │   ├── NoteController.java       # CRUD de notas
│   │   ├── TaskController.java       # CRUD de tarefas
│   │   ├── UserController.java       # CRUD de usuários
│   │   └── UserProfileController.java # CRUD de perfis
│   │
│   ├── 📁 service/               # 💼 Services (Business Objects)
│   │   ├── AuditLogService.java      # Registro de auditoria
│   │   ├── NoteService.java          # Regras de notas
│   │   ├── TaskService.java          # Regras de tarefas
│   │   ├── UserService.java          # Regras de usuários
│   │   └── UserProfileService.java   # Regras de perfis
│   │
│   ├── 📁 dao/                   # 🗄️ Data Access Objects
│   │   ├── AuditLogDao.java
│   │   ├── NoteDao.java
│   │   ├── TaskDao.java
│   │   ├── UserDao.java
│   │   └── UserProfileDao.java
│   │
│   ├── 📁 dto/                   # 📦 Data Transfer Objects
│   │   ├── *RequestDTO.java          # Entrada de dados
│   │   └── *ResponseDTO.java         # Saída de dados
│   │
│   ├── 📁 model/                 # 🏛️ Entidades JPA
│   │   ├── AuditLog.java
│   │   ├── Note.java
│   │   ├── Task.java
│   │   ├── TaskStatus.java           # Enum de status
│   │   ├── User.java
│   │   └── UserProfile.java
│   │
│   ├── 📁 filter/                # 🔍 Filtros HTTP
│   │   └── AuditLogFilter.java       # Intercepta requisições
│   │
│   └── 📁 view/                  # 👁️ Helpers de View
│       └── BreadcrumbItem.java
│
├── 📁 src/main/resources/
│   ├── 📄 application.properties # Configurações
│   │
│   ├── 📁 db/migration/          # 🔄 Migrações Flyway
│   │   ├── V1__Initial_schema.sql
│   │   ├── V2__Audit_and_demo_data.sql
│   │   └── ...
│   │
│   └── 📁 templates/             # 🎨 Templates Qute
│       ├── login.html
│       ├── users.html
│       ├── notes.html
│       ├── tasks.html
│       ├── audit.html
│       ├── 📁 tags/              # Componentes reutilizáveis
│       │   ├── navigation.html
│       │   ├── breadcrumb.html
│       │   └── flash.html
│       └── 📁 errors/            # Páginas de erro
│           ├── 403.html
│           ├── 404.html
│           └── 500.html
│
└── 📁 src/test/java/             # 🧪 Testes
    └── com/robsonbs/
        ├── SecurityIntegrationTest.java
        └── service/*ServiceTest.java
```

---

## 🌐 Endpoints da API

### Públicos

| Método | Caminho | Descrição |
|--------|---------|-----------|
| GET | `/login` | Página de login |
| POST | `/j_security_check` | Processar autenticação |

### Usuários (ADMIN)

| Método | Caminho | Descrição |
|--------|---------|-----------|
| GET | `/users` | Listar usuários |
| GET | `/users/new` | Formulário de novo usuário |
| POST | `/users` | Criar usuário |
| GET | `/users/{id}` | Formulário de edição |
| POST | `/users/{id}` | Atualizar usuário |
| POST | `/users/{id}/delete` | Excluir usuário |

### Perfis (ADMIN)

| Método | Caminho | Descrição |
|--------|---------|-----------|
| GET | `/profiles` | Listar perfis |
| GET | `/profiles/new` | Formulário de novo perfil |
| POST | `/profiles` | Criar perfil |
| GET | `/profiles/{id}` | Formulário de edição |
| POST | `/profiles/{id}` | Atualizar perfil |
| POST | `/profiles/{id}/delete` | Excluir perfil |

### Notas (USER, ADMIN)

| Método | Caminho | Descrição |
|--------|---------|-----------|
| GET | `/notes` | Listar notas do usuário |
| GET | `/notes/new` | Formulário de nova nota |
| POST | `/notes` | Criar nota |
| GET | `/notes/{id}` | Formulário de edição |
| POST | `/notes/{id}` | Atualizar nota |
| POST | `/notes/{id}/delete` | Excluir nota |

### Tarefas (USER, ADMIN)

| Método | Caminho | Descrição |
|--------|---------|-----------|
| GET | `/tasks` | Listar tarefas do usuário |
| GET | `/tasks/new` | Formulário de nova tarefa |
| POST | `/tasks` | Criar tarefa |
| GET | `/tasks/{id}` | Formulário de edição |
| POST | `/tasks/{id}` | Atualizar tarefa |
| POST | `/tasks/{id}/delete` | Excluir tarefa |

### Auditoria (ADMIN)

| Método | Caminho | Descrição |
|--------|---------|-----------|
| GET | `/audit` | Visualizar logs com filtros |

### Observabilidade & Documentação

| Método | Caminho | Descrição |
|--------|---------|-----------|
| GET | `/q/swagger-ui` | Interface interativa da API |
| GET | `/q/openapi` | Especificação OpenAPI (YAML/JSON) |
| GET | `/q/health` | Status geral da aplicação |
| GET | `/q/health/live` | Liveness Probe (Kubernetes) |
| GET | `/q/health/ready` | Readiness Probe (Kubernetes) |
| GET | `/q/metrics` | Métricas para Prometheus |

---

## 🔐 Segurança

### Autenticação

* **Mecanismo:** Form-based authentication (Elytron JDBC)
* **Sessão:** Cookie criptografado (`quarkus-credential`)
* **Hash de Senha:** BCrypt com salt automático

### Autorização

```java
@RolesAllowed("ADMIN")     // Apenas administradores
@RolesAllowed({"USER", "ADMIN"})  // Usuários e administradores
```

### Proteções Implementadas

* ✅ Todas as rotas exigem autenticação (exceto `/login`)
* ✅ Senhas armazenadas com hash BCrypt
* ✅ Verificação de propriedade (usuário só acessa seus dados)
* ✅ Validação de entrada em todos os formulários
* ✅ Proteção contra acesso não autorizado (403)

---

## 🧪 Testes

### Executar Todos os Testes

```bash
./mvnw test
```

### Executar Testes Específicos

```bash
# Testes de segurança
./mvnw test -Dtest=SecurityIntegrationTest

# Testes de serviço
./mvnw test -Dtest=*ServiceTest
```

### Cobertura de Testes

| Área | Testes |
|------|--------|
| Autenticação | Login, logout, redirecionamentos |
| Usuários | CRUD, validação de e-mail único |
| Perfis | CRUD, bloqueio de exclusão em uso |
| Notas | CRUD, propriedade por usuário |
| Tarefas | CRUD, validação de datas, status |
| Auditoria | Registro de eventos |

---

## ✅ Conformidade com Requisitos

### Requisitos Funcionais

| # | Requisito | Status | Implementação |
|---|-----------|--------|---------------|
| 1 | Autenticar usuário | ✅ | Login form + Elytron JDBC |
| 2 | Manter usuário | ✅ | `UserController` + CRUD completo |
| 3 | Manter perfil de usuário | ✅ | `UserProfileController` + validações |
| 4 | Navegação de recursos | ✅ | Menu + Breadcrumbs |
| 5 | Dois casos de uso de domínio | ✅ | Notas + Tarefas |
| 6 | Rastreabilidade e auditoria | ✅ | `AuditLog` com usuário, ação, data/hora |

### Requisitos Não Funcionais

| Requisito | Status | Observação |
|-----------|--------|------------|
| Java (11+) | ✅ | Java 17 |
| Modelo MVC | ✅ | Controller → Service → DAO |
| JAX-RS | ✅ | RESTEasy Reactive |
| Quarkus | ✅ | Versão 3.6.4 |
| Padrão DAO | ✅ | PanacheRepository |
| Padrão Entity | ✅ | JPA Entities |
| Padrão BO | ✅ | Services com regras de negócio |
| DTOs | ✅ | Request/Response DTOs |

### Conformidade 15-Factor App

O projeto atinge **99% de conformidade** com a metodologia estendida:

| Fator | Status | Implementação |
|-------|--------|---------------|
| I-XII | ✅ | Codebase, Config, Backing Services, CI/CD, Logs, etc. |
| XIII. API First | ✅ | OpenAPI/Swagger implementado |
| XIV. Telemetry | ✅ | Health Checks e Métricas Prometheus |
| XV. Security | ✅ | RBAC, Security Headers, CORS, Audit |

---

## 📚 Documentação Adicional

* [Arquitetura do Sistema](docs/arquitetura.md) - Diagramas detalhados
* [Guia 15-Factor App](docs/fifteen-factor-app.md) - Documentação completa da metodologia
* [Auditoria 12-Factor](docs/auditoria-12-fatores.md) - Análise detalhada de conformidade
* [Plano de Adequação](docs/plano-adequacao.md) - Checklist de conformidade

---

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.

---

## 👤 Autor

**Robson Barbosa Souza**

* GitHub: [@robsonbs](https://github.com/robsonbs)

---

<p align="center">
  Desenvolvido para a disciplina <strong>Programação para Web III</strong><br>
  IFG – Campus Luziânia – 2025
</p>
