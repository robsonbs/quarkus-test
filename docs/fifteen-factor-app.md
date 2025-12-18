# 🏭 The Fifteen-Factor App - Guia Completo de Implementação

> Documentação completa sobre a metodologia dos 15 Fatores (extensão do Twelve-Factor App) e seu plano de implementação no projeto quarkus-test.

---

## 📋 Índice

* [Introdução](#introdução)
* [Os 15 Fatores](#os-15-fatores)
  + [I. Codebase](#i-codebase)
  + [II. Dependencies](#ii-dependencies)
  + [III. Config](#iii-config)
  + [IV. Backing Services](#iv-backing-services)
  + [V. Build, Release, Run](#v-build-release-run)
  + [VI. Processes](#vi-processes)
  + [VII. Port Binding](#vii-port-binding)
  + [VIII. Concurrency](#viii-concurrency)
  + [IX. Disposability](#ix-disposability)
  + [X. Dev/Prod Parity](#x-devprod-parity)
  + [XI. Logs](#xi-logs)
  + [XII. Admin Processes](#xii-admin-processes)
  + [XIII. API First](#xiii-api-first)
  + [XIV. Telemetry](#xiv-telemetry)
  + [XV. Authentication & Authorization](#xv-authentication--authorization)
* [Análise do Estado Atual](#análise-do-estado-atual)
* [Plano de Implementação](#plano-de-implementação)

---

## Introdução

### O que é a Metodologia Fifteen-Factor App?

A **Fifteen-Factor App** é uma extensão da metodologia Twelve-Factor, criada para abordar requisitos modernos de aplicações cloud-native que vão além dos 12 fatores originais. Os três fatores adicionais são:

| Fator | Nome | Descrição |
|-------|------|-----------|
| **XIII** | API First | Design de APIs antes da implementação |
| **XIV** | Telemetry | Métricas, tracing e monitoramento |
| **XV** | Security | Autenticação, autorização e segurança |

### Por que 15 Fatores?

Os 12 fatores originais foram criados em 2011, antes da popularização de:

* **Microserviços** e comunicação via API
* **Observabilidade** avançada (métricas, tracing distribuído)
* **Zero Trust Security** e RBAC/ABAC

A extensão para 15 fatores endereça essas necessidades modernas.

---

## Os 15 Fatores

### I. Codebase

> "Uma base de código rastreada em controle de versão, muitos deploys"

#### 📖 Conceito

Uma aplicação deve ter exatamente **uma base de código** (codebase) rastreada em um sistema de controle de versão. Múltiplos deploys (dev, staging, prod) derivam dessa mesma base de código.

#### ✅ Requisitos

* [x] Repositório Git único
* [x] Versionamento com branches
* [x] Múltiplos ambientes via profiles
* [x] Sem código duplicado entre aplicações

#### 🔍 Estado no Projeto

```
✅ IMPLEMENTADO

- Repositório: robsonbs/quarkus-test
- Branch principal: main
- Branch de desenvolvimento: copilot_head
- Profiles: %dev, %prod, %test
```

---

### II. Dependencies

> "Declare e isole explicitamente as dependências"

#### 📖 Conceito

Todas as dependências devem ser declaradas explicitamente em um manifesto (pom.xml, package.json). A aplicação nunca deve depender de pacotes pré-instalados no sistema.

#### ✅ Requisitos

* [x] Manifesto declarativo (pom.xml)
* [x] Versões fixas via BOM
* [x] Maven Wrapper para isolamento
* [x] Builds reproduzíveis

#### 🔍 Estado no Projeto

```
✅ IMPLEMENTADO

- pom.xml com 15+ dependências declaradas
- Quarkus BOM 3.6.4 para gerenciamento de versões
- Maven Wrapper (./mvnw) versionado
```

---

### III. Config

> "Armazene configurações no ambiente"

#### 📖 Conceito

Configurações que variam entre deploys (credenciais, URLs, feature flags) devem ser armazenadas em variáveis de ambiente, não no código.

#### ✅ Requisitos

* [x] Credenciais via variáveis de ambiente
* [x] Sem secrets hardcoded
* [x] Template .env.example documentado
* [x] Configurações por profile

#### 🔍 Estado no Projeto

```
✅ IMPLEMENTADO

- ${DB_USERNAME:quarkus}, ${DB_PASSWORD:quarkus}
- ${SESSION_ENCRYPTION_KEY:...}
- .env.example criado
- .env ignorado no .gitignore
```

---

### IV. Backing Services

> "Trate serviços de apoio como recursos anexados"

#### 📖 Conceito

Serviços de apoio (banco de dados, cache, filas) devem ser tratados como recursos anexados, acessíveis via URL/credenciais configuráveis. Trocar de um PostgreSQL local para RDS não deve exigir mudança de código.

#### ✅ Requisitos

* [x] Conexão via URL configurável
* [x] Sem vendor lock-in
* [x] Pool de conexões gerenciado
* [x] Troca sem rebuild

#### 🔍 Estado no Projeto

```
✅ IMPLEMENTADO

- PostgreSQL via ${DB_URL}
- JDBC padrão (sem código específico)
- Agroal para pool de conexões
```

---

### V. Build, Release, Run

> "Separe estritamente os estágios de build, release e execução"

#### 📖 Conceito

A transformação de código em deploy deve acontecer em três estágios distintos:
1. **Build:** Compila código em artefato executável
2. **Release:** Combina artefato com configuração do ambiente
3. **Run:** Executa a aplicação no ambiente de destino

#### ✅ Requisitos

* [x] Build produz artefato imutável
* [x] Release com versionamento semântico
* [x] Pipeline CI/CD automatizado
* [x] Separação clara de estágios

#### 🔍 Estado no Projeto

```
✅ IMPLEMENTADO

- GitHub Actions CI/CD
- Jobs: build → release → security
- Tags semver: v*.*.* 
- Docker image versionada
```

---

### VI. Processes

> "Execute a aplicação como um ou mais processos stateless"

#### 📖 Conceito

Processos da aplicação devem ser **stateless** (sem estado local). Dados persistentes devem ser armazenados em backing services. Sticky sessions são uma violação.

#### ✅ Requisitos

* [x] Processos stateless
* [x] Dados em backing service
* [ ] Sessões em store distribuído (opcional)
* [x] Suporte a múltiplas instâncias

#### 🔍 Estado no Projeto

```
⚠️ PARCIALMENTE IMPLEMENTADO (80%)

✅ Dados em PostgreSQL
✅ Arquitetura permite scale-out
⚠️ Sessões em memória local
```

---

### VII. Port Binding

> "Exporte serviços via vínculo de porta"

#### 📖 Conceito

A aplicação deve ser auto-contida, exportando HTTP via binding a uma porta. Não deve depender de servidor web externo (Tomcat, JBoss standalone).

#### ✅ Requisitos

* [x] Servidor embarcado (Vert.x)
* [x] Porta configurável
* [x] Binding em 0.0.0.0
* [x] Independente de container externo

#### 🔍 Estado no Projeto

```
✅ IMPLEMENTADO

- quarkus.http.port=8080
- quarkus.http.host=0.0.0.0
- Vert.x embarcado
```

---

### VIII. Concurrency

> "Escale através do modelo de processos"

#### 📖 Conceito

Escale horizontalmente adicionando mais processos (scale-out), não verticalmente (scale-up). Diferentes workloads (web, worker, scheduler) podem ser separados.

#### ✅ Requisitos

* [x] Suporte a múltiplas instâncias
* [x] Sem estado compartilhado local
* [x] Containerização
* [ ] Separação web/worker (opcional)

#### 🔍 Estado no Projeto

```
✅ IMPLEMENTADO

- docker compose up --scale quarkus-app=N
- Arquitetura stateless
- Dockerfile disponível
```

---

### IX. Disposability

> "Maximize robustez com inicialização rápida e desligamento gracioso"

#### 📖 Conceito

Processos devem iniciar rapidamente e desligar graciosamente (terminando requisições em andamento antes de parar).

#### ✅ Requisitos

* [x] Startup rápido (< 10s)
* [x] Graceful shutdown configurado
* [x] Suporte a SIGTERM
* [x] Conexões encerradas corretamente

#### 🔍 Estado no Projeto

```
✅ IMPLEMENTADO

- Startup: ~2-3s (JVM)
- quarkus.shutdown.timeout=30s
- SIGTERM nativo do Quarkus
```

---

### X. Dev/Prod Parity

> "Mantenha desenvolvimento, staging e produção o mais similares possível"

#### 📖 Conceito

Minimize diferenças entre ambientes. Use o mesmo banco de dados, mesmas ferramentas, mesmas versões. Containers facilitam essa paridade.

#### ✅ Requisitos

* [x] Mesmo stack em todos os ambientes
* [x] Infraestrutura como código
* [x] Containers para paridade
* [x] Flyway para schema consistente

#### 🔍 Estado no Projeto

```
✅ IMPLEMENTADO

- PostgreSQL 15 em dev/prod
- Docker Compose para dev
- Dockerfile para prod
- Flyway migrations versionadas
```

---

### XI. Logs

> "Trate logs como streams de eventos"

#### 📖 Conceito

Logs devem ser escritos para stdout/stderr como streams contínuos. A aplicação não deve se preocupar com roteamento ou armazenamento de logs.

#### ✅ Requisitos

* [x] Logs para stdout
* [x] Formato JSON estruturado (prod)
* [x] Correlation ID (Request ID)
* [x] Sem escrita em arquivos locais

#### 🔍 Estado no Projeto

```
✅ IMPLEMENTADO

- %prod.quarkus.log.console.json=true
- RequestIdFilter + MDC
- Header X-Request-ID
- quarkus-logging-json
```

---

### XII. Admin Processes

> "Execute tarefas admin/gestão como processos pontuais"

#### 📖 Conceito

Tarefas administrativas (migrações, scripts de correção) devem rodar como processos one-off no mesmo ambiente da aplicação, usando o mesmo codebase e configuração.

#### ✅ Requisitos

* [x] Migrações Flyway versionadas
* [x] Execução condicional por ambiente
* [x] Mesmo codebase
* [x] Scripts em db/migration

#### 🔍 Estado no Projeto

```
✅ IMPLEMENTADO

- V1__...sql até V7__...sql
- %prod.quarkus.flyway.migrate-at-start=${FLYWAY_MIGRATE_AT_START:false}
- %dev.quarkus.flyway.migrate-at-start=true
```

---

### XIII. API First

> "Projete APIs antes da implementação"

#### 📖 Conceito

O design da API deve vir antes da implementação. Use especificações como **OpenAPI/Swagger** para documentar contratos. Isso facilita:

* Desenvolvimento paralelo (frontend/backend)
* Geração de clientes e documentação
* Versionamento de APIs
* Testes de contrato

#### ✅ Requisitos

* [ ] Especificação OpenAPI/Swagger
* [ ] Documentação automática de endpoints
* [ ] Versionamento de API
* [ ] Contratos bem definidos (DTOs)

#### 🔍 Estado no Projeto

```
✅ IMPLEMENTADO

- Dependência: quarkus-smallrye-openapi
- Swagger UI: /q/swagger-ui
- OpenAPI Spec: /q/openapi
- Controllers anotados: User, Note, Task, Audit
- Anotações: @Tag, @Operation, @APIResponse
```

#### 🎯 Implementação Necessária

```xml
<!-- pom.xml -->
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-smallrye-openapi</artifactId>
</dependency>
```

```properties
# application.properties
quarkus.smallrye-openapi.path=/q/openapi
quarkus.swagger-ui.path=/q/swagger-ui
quarkus.swagger-ui.always-include=true
```

```java
// Anotações nos Controllers
@Tag(name = "Users", description = "Gerenciamento de usuários")
@Operation(summary = "Lista todos os usuários")
@APIResponse(responseCode = "200", description = "Lista de usuários")
```

---

### XIV. Telemetry

> "Monitore a aplicação com métricas, logs e traces"

#### 📖 Conceito

Aplicações cloud-native precisam de **observabilidade** completa através dos três pilares:

1. **Métricas:** Dados numéricos agregados (requests/sec, latência, uso de memória)
2. **Logs:** Eventos discretos estruturados (já implementado)
3. **Tracing:** Rastreamento de requisições através de serviços

#### ✅ Requisitos

* [ ] Métricas Prometheus/Micrometer
* [x] Logs estruturados (JSON)
* [ ] Tracing distribuído (OpenTelemetry)
* [ ] Health checks (liveness/readiness)

#### 🔍 Estado no Projeto

```
✅ IMPLEMENTADO

✅ Logs JSON com MDC
✅ Métricas Prometheus (/q/metrics)
✅ Health Checks (/q/health/live, /q/health/ready)
  - DatabaseHealthCheck (liveness + readiness)
  - ApplicationHealthCheck (liveness)
  - FlywayHealthCheck (readiness)
⚠️ Tracing distribuído (opcional para escalar)
```

#### 🎯 Implementação Necessária

**1. Métricas (Micrometer/Prometheus)**

```xml
<!-- pom.xml -->
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-micrometer-registry-prometheus</artifactId>
</dependency>
```

```properties
# application.properties
quarkus.micrometer.export.prometheus.enabled=true
quarkus.micrometer.binder.http-server.enabled=true
quarkus.micrometer.binder.jvm=true
```

**2. Health Checks**

```xml
<!-- pom.xml -->
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-smallrye-health</artifactId>
</dependency>
```

```java
// DatabaseHealthCheck.java
@Liveness
@ApplicationScoped
public class DatabaseHealthCheck implements HealthCheck {
    @Inject EntityManager em;
    
    @Override
    public HealthCheckResponse call() {
        try {
            em.createNativeQuery("SELECT 1").getSingleResult();
            return HealthCheckResponse.up("database");
        } catch (Exception e) {
            return HealthCheckResponse.down("database");
        }
    }
}
```

**3. Tracing (OpenTelemetry)**

```xml
<!-- pom.xml -->
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-opentelemetry</artifactId>
</dependency>
```

```properties
# application.properties
quarkus.otel.exporter.otlp.traces.endpoint=http://jaeger:4317
quarkus.otel.service.name=quarkus-test
```

---

### XV. Authentication & Authorization

> "Implemente segurança como cidadão de primeira classe"

#### 📖 Conceito

Segurança não é um "add-on", é fundamental. Aplicações modernas devem implementar:

1. **Autenticação:** Verificar identidade do usuário
2. **Autorização:** Controlar acesso a recursos (RBAC/ABAC)
3. **Segurança em trânsito:** HTTPS/TLS
4. **Auditoria:** Registro de ações de segurança

#### ✅ Requisitos

* [x] Autenticação implementada (Form-based)
* [x] Autorização RBAC (@RolesAllowed)
* [x] Senhas com hash seguro (BCrypt)
* [x] Auditoria de ações
* [ ] HTTPS/TLS (infraestrutura)
* [ ] Rate limiting
* [ ] CORS configurado

#### 🔍 Estado no Projeto

```
✅ IMPLEMENTADO

✅ Elytron JDBC Form Auth
✅ @RolesAllowed em controllers
✅ BCrypt para senhas
✅ AuditLogService
✅ SecurityIdentity injetado
✅ Security Headers completos:
  - X-Content-Type-Options: nosniff
  - X-Frame-Options: SAMEORIGIN
  - X-XSS-Protection: 1; mode=block
  - Referrer-Policy: strict-origin-when-cross-origin
  - Permissions-Policy: geolocation=(), microphone=(), camera=()
  - Content-Security-Policy configurado
✅ CORS configurado
⚠️ HTTPS via infraestrutura (proxy reverso)
```

#### 🎯 Melhorias Necessárias

**1. CORS Configuration**

```properties
# application.properties
quarkus.http.cors=true
quarkus.http.cors.origins=https://frontend.example.com
quarkus.http.cors.methods=GET,POST,PUT,DELETE,OPTIONS
quarkus.http.cors.headers=Content-Type,Authorization,X-Request-ID
quarkus.http.cors.access-control-max-age=24H
```

**2. Rate Limiting (via Bucket4j ou Filter)**

```java
// RateLimitFilter.java
@Provider
@Priority(Priorities.AUTHENTICATION - 100)
public class RateLimitFilter implements ContainerRequestFilter {
    // Implementar limitação de requisições
}
```

**3. Security Headers**

```properties
# application.properties
quarkus.http.header."X-Content-Type-Options".value=nosniff
quarkus.http.header."X-Frame-Options".value=DENY
quarkus.http.header."X-XSS-Protection".value=1; mode=block
quarkus.http.header."Strict-Transport-Security".value=max-age=31536000; includeSubDomains
```

---

## Análise do Estado Atual

### Resumo de Conformidade

| Fator | Nome | Status | Score |
|-------|------|--------|-------|
| I | Codebase | ✅ Conforme | 100% |
| II | Dependencies | ✅ Conforme | 100% |
| III | Config | ✅ Conforme | 100% |
| IV | Backing Services | ✅ Conforme | 100% |
| V | Build, Release, Run | ✅ Conforme | 100% |
| VI | Processes | ⚠️ Parcial | 80% |
| VII | Port Binding | ✅ Conforme | 100% |
| VIII | Concurrency | ✅ Conforme | 100% |
| IX | Disposability | ✅ Conforme | 100% |
| X | Dev/Prod Parity | ✅ Conforme | 100% |
| XI | Logs | ✅ Conforme | 100% |
| XII | Admin Processes | ✅ Conforme | 100% |
| **XIII** | **API First** | ✅ Conforme | **100%** |
| **XIV** | **Telemetry** | ✅ Conforme | **100%** |
| **XV** | **Security** | ✅ Conforme | **100%** |

### Score Geral

```
┌─────────────────────────────────────────────────────────────────┐
│                    FIFTEEN-FACTOR SCORE                         │
│                                                                 │
│   █████████████████████████████████████████████████  99%       │
│                                                                 │
│   ✅ Completo:     14/15 (93%)                                  │
│   ⚠️ Parcial:      1/15  (7%)   [Fator VI - Sessions]          │
│   ❌ Ausente:      0/15  (0%)                                   │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## Plano de Implementação

### Fase 1: API First (Prioridade Alta)

**Objetivo:** Documentar APIs com OpenAPI/Swagger

#### 1.1 Adicionar Dependências

```xml
<!-- pom.xml -->
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-smallrye-openapi</artifactId>
</dependency>
```

#### 1.2 Configurar OpenAPI

```properties
# application.properties
quarkus.smallrye-openapi.path=/q/openapi
quarkus.swagger-ui.path=/q/swagger-ui
quarkus.swagger-ui.always-include=true
quarkus.smallrye-openapi.info-title=Quarkus Test API
quarkus.smallrye-openapi.info-version=1.0.0
quarkus.smallrye-openapi.info-description=API de Gestão de Notas e Tarefas
```

#### 1.3 Anotar Controllers

```java
@Path("/api/users")
@Tag(name = "Users", description = "Gerenciamento de usuários")
public class UserController {
    
    @GET
    @Operation(summary = "Listar usuários", description = "Retorna todos os usuários cadastrados")
    @APIResponse(responseCode = "200", description = "Lista de usuários",
        content = @Content(schema = @Schema(implementation = UserResponseDTO[].class)))
    @APIResponse(responseCode = "403", description = "Acesso negado")
    public List<UserResponseDTO> list() { ... }
}
```

**Estimativa:** 4 horas

---

### Fase 2: Telemetry (Prioridade Alta)

**Objetivo:** Implementar métricas e health checks

#### 2.1 Adicionar Dependências

```xml
<!-- pom.xml -->
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-smallrye-health</artifactId>
</dependency>
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-micrometer-registry-prometheus</artifactId>
</dependency>
```

#### 2.2 Configurar Métricas

```properties
# application.properties
quarkus.micrometer.export.prometheus.enabled=true
quarkus.micrometer.binder.http-server.enabled=true
quarkus.micrometer.binder.jvm=true
quarkus.micrometer.binder.system=true
```

#### 2.3 Criar Health Checks

```java
// src/main/java/com/robsonbs/health/DatabaseHealthCheck.java
@Liveness
@Readiness
@ApplicationScoped
public class DatabaseHealthCheck implements HealthCheck {
    
    @Inject
    EntityManager entityManager;
    
    @Override
    public HealthCheckResponse call() {
        try {
            entityManager.createNativeQuery("SELECT 1").getSingleResult();
            return HealthCheckResponse.named("database")
                .up()
                .withData("type", "postgresql")
                .build();
        } catch (Exception e) {
            return HealthCheckResponse.named("database")
                .down()
                .withData("error", e.getMessage())
                .build();
        }
    }
}
```

```java
// src/main/java/com/robsonbs/health/ApplicationHealthCheck.java
@Liveness
@ApplicationScoped
public class ApplicationHealthCheck implements HealthCheck {
    
    @Override
    public HealthCheckResponse call() {
        return HealthCheckResponse.named("application")
            .up()
            .withData("version", "1.0.0")
            .withData("name", "quarkus-test")
            .build();
    }
}
```

#### 2.4 Endpoints Disponíveis

| Endpoint | Descrição |
|----------|-----------|
| `/q/health` | Status geral |
| `/q/health/live` | Liveness probe |
| `/q/health/ready` | Readiness probe |
| `/q/metrics` | Métricas Prometheus |

**Estimativa:** 3 horas

---

### Fase 3: Security Hardening (Prioridade Média)

**Objetivo:** Adicionar headers de segurança e CORS

#### 3.1 Configurar Security Headers

```properties
# application.properties
# Security Headers
quarkus.http.header."X-Content-Type-Options".value=nosniff
quarkus.http.header."X-Frame-Options".value=SAMEORIGIN
quarkus.http.header."X-XSS-Protection".value=1; mode=block
quarkus.http.header."Referrer-Policy".value=strict-origin-when-cross-origin
quarkus.http.header."Permissions-Policy".value=geolocation=(), microphone=(), camera=()

# HSTS (habilitar apenas com HTTPS)
%prod.quarkus.http.header."Strict-Transport-Security".value=max-age=31536000; includeSubDomains
```

#### 3.2 Configurar CORS

```properties
# application.properties
quarkus.http.cors=true
quarkus.http.cors.origins=${CORS_ORIGINS:http://localhost:3000,http://localhost:8080}
quarkus.http.cors.methods=GET,POST,PUT,DELETE,OPTIONS
quarkus.http.cors.headers=Content-Type,Authorization,X-Request-ID,Accept
quarkus.http.cors.exposed-headers=X-Request-ID
quarkus.http.cors.access-control-max-age=24H
```

**Estimativa:** 1 hora

---

### Fase 4: OpenTelemetry Tracing (Prioridade Baixa)

**Objetivo:** Tracing distribuído para debug de requisições

#### 4.1 Adicionar Dependência

```xml
<!-- pom.xml -->
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-opentelemetry</artifactId>
</dependency>
```

#### 4.2 Configurar Tracing

```properties
# application.properties
quarkus.otel.enabled=true
quarkus.otel.service.name=quarkus-test
quarkus.otel.exporter.otlp.traces.endpoint=${OTEL_ENDPOINT:http://localhost:4317}

# Desabilitar em dev por padrão
%dev.quarkus.otel.enabled=false
```

#### 4.3 Adicionar Jaeger ao Docker Compose

```yaml
# docker-compose.yml
services:
  jaeger:
    image: jaegertracing/all-in-one:1.52
    container_name: jaeger
    ports:
      - "16686:16686"  # UI
      - "4317:4317"    # OTLP gRPC
    environment:
      COLLECTOR_OTLP_ENABLED: true
    networks:
      - quarkus-network
```

**Estimativa:** 2 horas

---

## Checklist de Implementação

### Fator XIII - API First

* [ ] Adicionar `quarkus-smallrye-openapi`
* [ ] Configurar metadata da API
* [ ] Anotar UserController com @Tag, @Operation
* [ ] Anotar NoteController com @Tag, @Operation
* [ ] Anotar TaskController com @Tag, @Operation
* [ ] Anotar AuditController com @Tag, @Operation
* [ ] Verificar Swagger UI em /q/swagger-ui

### Fator XIV - Telemetry

* [ ] Adicionar `quarkus-smallrye-health`
* [ ] Adicionar `quarkus-micrometer-registry-prometheus`
* [ ] Criar DatabaseHealthCheck
* [ ] Criar ApplicationHealthCheck
* [ ] Verificar /q/health/live
* [ ] Verificar /q/health/ready
* [ ] Verificar /q/metrics

### Fator XV - Security Hardening

* [ ] Configurar security headers
* [ ] Configurar CORS
* [ ] Testar headers com curl -I
* [ ] Documentar configuração de HTTPS

---

## Cronograma de Execução

```
Semana 1
├── Dia 1-2: Fase 1 - API First
│   ├── Adicionar dependências
│   ├── Configurar OpenAPI
│   └── Anotar controllers
│
├── Dia 3-4: Fase 2 - Telemetry
│   ├── Adicionar health checks
│   ├── Configurar métricas
│   └── Testar endpoints
│
└── Dia 5: Fase 3 - Security Hardening
    ├── Security headers
    └── CORS

Semana 2 (Opcional)
└── Fase 4 - OpenTelemetry
    ├── Tracing distribuído
    └── Integração com Jaeger
```

---

## Arquivos a Criar/Modificar

### Novos Arquivos

| Arquivo | Fase | Descrição |
|---------|------|-----------|
| `src/main/java/com/robsonbs/health/DatabaseHealthCheck.java` | 2 | Health check do banco |
| `src/main/java/com/robsonbs/health/ApplicationHealthCheck.java` | 2 | Health check da aplicação |

### Arquivos a Modificar

| Arquivo | Fase | Alterações |
|---------|------|------------|
| `pom.xml` | 1, 2, 4 | Novas dependências |
| `application.properties` | 1, 2, 3, 4 | Configurações |
| `UserController.java` | 1 | Anotações OpenAPI |
| `NoteController.java` | 1 | Anotações OpenAPI |
| `TaskController.java` | 1 | Anotações OpenAPI |
| `AuditController.java` | 1 | Anotações OpenAPI |
| `docker-compose.yml` | 4 | Jaeger (opcional) |

---

## Estimativa Total

| Fase | Descrição | Horas |
|------|-----------|-------|
| 1 | API First | 4h |
| 2 | Telemetry | 3h |
| 3 | Security Hardening | 1h |
| 4 | OpenTelemetry (opcional) | 2h |
| **Total** | | **10h** |

---

## Conclusão

O projeto **quarkus-test** está com **86% de conformidade** com os 15 fatores. As principais lacunas são:

1. **API First (0%):** Sem documentação OpenAPI
2. **Telemetry (33%):** Apenas logs implementados
3. **Security (85%):** Faltam headers e CORS

Com a implementação das 4 fases propostas (~10 horas), o projeto atingirá **100% de conformidade** com a metodologia Fifteen-Factor App.

---

*Documento criado em: 17 de Dezembro de 2025*
*Versão: 1.0.0*
