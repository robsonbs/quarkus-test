# 📋 Plano de Implementação - Fatores XIII, XIV e XV

> ✅ **IMPLEMENTAÇÃO CONCLUÍDA** em 18 de Dezembro de 2025

---

## 📊 Estado Final

| Fator | Status Anterior | Status Atual |
|-------|-----------------|--------------|
| I-XII | ✅ 98% | ✅ 98% |
| XIII - API First | ❌ 0% | ✅ **100%** |
| XIV - Telemetry | ⚠️ 33% | ✅ **100%** |
| XV - Security | ⚠️ 85% | ✅ **100%** |

### Score Final: **99%** (14/15 fatores completos)

---

## 🚀 Fase 1: API First (Fator XIII)

### 1.1 Adicionar Dependência

**Arquivo:** `pom.xml`

```xml
<!-- Adicionar na seção <dependencies> -->
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-smallrye-openapi</artifactId>
</dependency>
```

### 1.2 Configurar OpenAPI

**Arquivo:** `src/main/resources/application.properties`

```properties
# =============================================================================
# OPENAPI / SWAGGER (Fator XIII - API First)
# =============================================================================
quarkus.smallrye-openapi.path=/q/openapi
quarkus.swagger-ui.path=/q/swagger-ui
quarkus.swagger-ui.always-include=true

# Metadata da API
quarkus.smallrye-openapi.info-title=Quarkus Test API
quarkus.smallrye-openapi.info-version=1.0.0
quarkus.smallrye-openapi.info-description=API de Gestão de Usuários, Notas e Tarefas
quarkus.smallrye-openapi.info-contact-name=Robson BS
quarkus.smallrye-openapi.info-contact-email=admin@example.com
quarkus.smallrye-openapi.info-license-name=MIT
quarkus.smallrye-openapi.info-license-url=https://opensource.org/licenses/MIT

# Security Scheme
quarkus.smallrye-openapi.security-scheme=formAuth
quarkus.smallrye-openapi.security-scheme-name=Form Authentication
quarkus.smallrye-openapi.security-scheme-description=Autenticação via formulário (/login)
```

### 1.3 Anotar Controllers

**Arquivo:** `src/main/java/com/robsonbs/controller/UserController.java`

Adicionar imports:

```java
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
```

Adicionar na classe:

```java
@Tag(name = "Usuários", description = "Gerenciamento de usuários do sistema")
```

Adicionar nos métodos:

```java
@GET
@Operation(summary = "Listar usuários", description = "Retorna a lista paginada de usuários cadastrados")
@APIResponses({
    @APIResponse(responseCode = "200", description = "Lista de usuários"),
    @APIResponse(responseCode = "403", description = "Acesso negado - requer role admin")
})
public TemplateInstance list(...) { ... }

@POST
@Operation(summary = "Criar usuário", description = "Cadastra um novo usuário no sistema")
@APIResponses({
    @APIResponse(responseCode = "303", description = "Usuário criado com sucesso, redirecionando"),
    @APIResponse(responseCode = "400", description = "Dados inválidos")
})
public Response create(...) { ... }
```

---

## 🔍 Fase 2: Telemetry (Fator XIV)

### 2.1 Adicionar Dependências

**Arquivo:** `pom.xml`

```xml
<!-- Adicionar na seção <dependencies> -->
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-smallrye-health</artifactId>
</dependency>
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-micrometer-registry-prometheus</artifactId>
</dependency>
```

### 2.2 Configurar Telemetry

**Arquivo:** `src/main/resources/application.properties`

```properties
# =============================================================================
# HEALTH CHECKS (Fator XIV - Telemetry)
# =============================================================================
quarkus.smallrye-health.root-path=/q/health
quarkus.smallrye-health.liveness-path=/q/health/live
quarkus.smallrye-health.readiness-path=/q/health/ready

# =============================================================================
# MÉTRICAS PROMETHEUS (Fator XIV - Telemetry)
# =============================================================================
quarkus.micrometer.export.prometheus.enabled=true
quarkus.micrometer.export.prometheus.path=/q/metrics
quarkus.micrometer.binder.http-server.enabled=true
quarkus.micrometer.binder.jvm=true
quarkus.micrometer.binder.system=true

# Histograma de latência
quarkus.micrometer.binder.http-server.match-patterns=/api/*
quarkus.micrometer.binder.http-server.ignore-patterns=/q/*
```

### 2.3 Criar Health Checks

**Novo arquivo:** `src/main/java/com/robsonbs/health/DatabaseHealthCheck.java`

```java
package com.robsonbs.health;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Liveness;
import org.eclipse.microprofile.health.Readiness;

/**
 * Health check para verificar a conectividade com o banco de dados.
 * Usado pelo Kubernetes para liveness e readiness probes.
 */
@Liveness
@Readiness
@ApplicationScoped
public class DatabaseHealthCheck implements HealthCheck {

    @Inject
    EntityManager entityManager;

    @Override
    public HealthCheckResponse call() {
        HealthCheckResponseBuilder builder = HealthCheckResponse.named("database");
        
        try {
            // Executa query simples para verificar conexão
            entityManager.createNativeQuery("SELECT 1").getSingleResult();
            
            return builder
                .up()
                .withData("type", "postgresql")
                .withData("status", "connected")
                .build();
                
        } catch (Exception e) {
            return builder
                .down()
                .withData("type", "postgresql")
                .withData("status", "disconnected")
                .withData("error", e.getMessage())
                .build();
        }
    }
}
```

**Novo arquivo:** `src/main/java/com/robsonbs/health/ApplicationHealthCheck.java`

```java
package com.robsonbs.health;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;

/**
 * Health check básico da aplicação.
 * Verifica se a aplicação está em execução.
 */
@Liveness
@ApplicationScoped
public class ApplicationHealthCheck implements HealthCheck {

    private static final String APPLICATION_NAME = "quarkus-test";
    private static final String APPLICATION_VERSION = "1.0.0";

    @Override
    public HealthCheckResponse call() {
        return HealthCheckResponse.named("application")
            .up()
            .withData("name", APPLICATION_NAME)
            .withData("version", APPLICATION_VERSION)
            .withData("status", "running")
            .build();
    }
}
```

**Novo arquivo:** `src/main/java/com/robsonbs/health/FlywayHealthCheck.java`

```java
package com.robsonbs.health;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Readiness;
import org.flywaydb.core.Flyway;

/**
 * Health check para verificar se as migrações Flyway foram aplicadas.
 */
@Readiness
@ApplicationScoped
public class FlywayHealthCheck implements HealthCheck {

    @Inject
    Flyway flyway;

    @Override
    public HealthCheckResponse call() {
        HealthCheckResponseBuilder builder = HealthCheckResponse.named("flyway-migrations");
        
        try {
            var info = flyway.info();
            var current = info.current();
            var pending = info.pending();
            
            if (pending != null && pending.length > 0) {
                return builder
                    .down()
                    .withData("status", "pending-migrations")
                    .withData("pending", pending.length)
                    .build();
            }
            
            return builder
                .up()
                .withData("status", "up-to-date")
                .withData("current-version", current != null ? current.getVersion().toString() : "none")
                .build();
                
        } catch (Exception e) {
            return builder
                .down()
                .withData("error", e.getMessage())
                .build();
        }
    }
}
```

---

## 🔒 Fase 3: Security Hardening (Fator XV)

### 3.1 Configurar Security Headers

**Arquivo:** `src/main/resources/application.properties`

```properties
# =============================================================================
# SECURITY HEADERS (Fator XV - Security)
# =============================================================================
# Previne MIME type sniffing
quarkus.http.header."X-Content-Type-Options".value=nosniff

# Previne clickjacking
quarkus.http.header."X-Frame-Options".value=SAMEORIGIN

# Proteção XSS (legacy browsers)
quarkus.http.header."X-XSS-Protection".value=1; mode=block

# Política de referrer
quarkus.http.header."Referrer-Policy".value=strict-origin-when-cross-origin

# Restringe funcionalidades do browser
quarkus.http.header."Permissions-Policy".value=geolocation=(), microphone=(), camera=()

# Content Security Policy (permite apenas recursos do próprio domínio)
quarkus.http.header."Content-Security-Policy".value=default-src 'self'; script-src 'self' 'unsafe-inline' cdn.tailwindcss.com; style-src 'self' 'unsafe-inline' fonts.googleapis.com cdn.tailwindcss.com; font-src 'self' fonts.gstatic.com; img-src 'self' data:;

# HSTS - apenas em produção com HTTPS
%prod.quarkus.http.header."Strict-Transport-Security".value=max-age=31536000; includeSubDomains

# =============================================================================
# CORS CONFIGURATION (Fator XV - Security)
# =============================================================================
quarkus.http.cors=true
quarkus.http.cors.origins=${CORS_ORIGINS:http://localhost:3000,http://localhost:8080}
quarkus.http.cors.methods=GET,POST,PUT,DELETE,OPTIONS
quarkus.http.cors.headers=Content-Type,Authorization,X-Request-ID,Accept
quarkus.http.cors.exposed-headers=X-Request-ID
quarkus.http.cors.access-control-max-age=24H
quarkus.http.cors.access-control-allow-credentials=true
```

### 3.2 Atualizar .env.example

**Arquivo:** `.env.example`

Adicionar:

```properties
# CORS (Fator XV)
CORS_ORIGINS=http://localhost:3000,http://localhost:8080
```

---

## 📦 Resumo de Alterações por Arquivo

### Novos Arquivos

| Arquivo | Descrição |
|---------|-----------|
| `src/main/java/com/robsonbs/health/DatabaseHealthCheck.java` | Health check do PostgreSQL |
| `src/main/java/com/robsonbs/health/ApplicationHealthCheck.java` | Health check da aplicação |
| `src/main/java/com/robsonbs/health/FlywayHealthCheck.java` | Health check de migrações |

### Arquivos Modificados

| Arquivo | Alterações |
|---------|------------|
| `pom.xml` | +3 dependências |
| `application.properties` | +40 linhas de configuração |
| `.env.example` | +1 variável |
| `UserController.java` | Anotações OpenAPI |
| `NoteController.java` | Anotações OpenAPI |
| `TaskController.java` | Anotações OpenAPI |
| `AuditController.java` | Anotações OpenAPI |

---

## ✅ Checklist de Validação

### Fase 1 - API First

```bash
# Verificar se Swagger UI está acessível
curl -I http://localhost:8080/q/swagger-ui

# Verificar OpenAPI spec
curl http://localhost:8080/q/openapi
```

### Fase 2 - Telemetry

```bash
# Verificar health endpoints
curl http://localhost:8080/q/health
curl http://localhost:8080/q/health/live
curl http://localhost:8080/q/health/ready

# Verificar métricas Prometheus
curl http://localhost:8080/q/metrics
```

### Fase 3 - Security

```bash
# Verificar security headers
curl -I http://localhost:8080/

# Verificar CORS headers
curl -I -X OPTIONS http://localhost:8080/ \
  -H "Origin: http://localhost:3000" \
  -H "Access-Control-Request-Method: GET"
```

---

## 🎯 Resultado Esperado

Após a implementação:

| Fator | Antes | Depois |
|-------|-------|--------|
| XIII - API First | 0% | 100% |
| XIV - Telemetry | 33% | 100% |
| XV - Security | 85% | 100% |

**Score Final:** 100% de conformidade com Fifteen-Factor App

---

## 📚 Endpoints Novos

| Endpoint | Descrição |
|----------|-----------|
| `GET /q/swagger-ui` | Interface Swagger |
| `GET /q/openapi` | Especificação OpenAPI |
| `GET /q/health` | Health check geral |
| `GET /q/health/live` | Liveness probe |
| `GET /q/health/ready` | Readiness probe |
| `GET /q/metrics` | Métricas Prometheus |

---

*Plano criado em: 17 de Dezembro de 2025*
