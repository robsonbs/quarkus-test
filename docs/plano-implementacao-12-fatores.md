# 🏭 Plano de Execução Rigoroso: Twelve-Factor App

> Este documento detalha as alterações técnicas precisas necessárias para adequar o projeto `quarkus-test` à metodologia 12-Factor App.

---

## 📋 Resumo de Adequação

| Fator | Status Atual | Ação Necessária | Arquivos Impactados |
|-------|--------------|-----------------|---------------------|
| **I. Codebase** | ✅ Conforme | Nenhuma | - |
| **II. Dependencies** | ✅ Conforme | Nenhuma | - |
| **III. Config** | ❌ Violação | **CRÍTICO:** Externalizar credenciais | `application.properties` , `docker-compose.yml` , `.env.example` |
| **IV. Backing Services** | ✅ Conforme | Nenhuma | - |
| **V. Build, Release, Run** | ⚠️ Parcial | Criar Pipelines CI/CD | `.github/workflows/*` |
| **VI. Processes** | ✅ Conforme | Validar Statelessness | - |
| **VII. Port Binding** | ✅ Conforme | Nenhuma | - |
| **VIII. Concurrency** | ✅ Conforme | Nenhuma | - |
| **IX. Disposability** | ⚠️ Parcial | Configurar Graceful Shutdown | `application.properties` |
| **X. Dev/Prod Parity** | ✅ Conforme | Nenhuma | - |
| **XI. Logs** | ❌ Violação | Implementar JSON Logging | `pom.xml` , `application.properties` , `RequestIdFilter.java` |
| **XII. Admin Processes** | ⚠️ Parcial | Desacoplar Migrações | `application.properties` , `MigrationService.java` |

---

## 🛠️ Detalhamento Técnico das Alterações

### 1. Fator III: Configurações (Prioridade Máxima)

**Problema:** Credenciais de banco de dados e chaves de criptografia estão hardcoded no `application.properties` .
**Solução:** Utilizar injeção de variáveis de ambiente com valores default apenas para dev.

#### 📄 `src/main/resources/application.properties`

```properties
# ...existing code...

# Database configuration
quarkus.datasource.db-kind=postgresql
# ALTERAÇÃO: Uso de variáveis de ambiente
quarkus.datasource.username=${DB_USERNAME:quarkus}
quarkus.datasource.password=${DB_PASSWORD:quarkus}
quarkus.datasource.jdbc.url=${DB_URL:jdbc:postgresql://localhost:5432/quarkusdb}

# ...existing code...

# Security configuration
# ...existing code...

# Form-based authentication
# ...existing code...
# ALTERAÇÃO: Chave externa obrigatória em produção
quarkus.http.auth.session.encryption-key=${SESSION_ENCRYPTION_KEY:dev-key-change-in-prod-must-be-32-chars}

# ...existing code...
```

#### 📄 `docker-compose.yml`

```yaml
services:
  postgres:
    # ...existing code...
    environment:
      POSTGRES_DB: ${POSTGRES_DB:-quarkusdb}
      POSTGRES_USER: ${POSTGRES_USER:-quarkus}
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD:-quarkus}
    # ...existing code...

  quarkus-app:
    # ...existing code...
    environment:
      # Passagem explícita de variáveis para o container
      DB_URL: jdbc:postgresql://postgres:5432/${POSTGRES_DB:-quarkusdb}
      DB_USERNAME: ${POSTGRES_USER:-quarkus}
      DB_PASSWORD: ${POSTGRES_PASSWORD:-quarkus}
      SESSION_ENCRYPTION_KEY: ${SESSION_ENCRYPTION_KEY:-dev-key-change-in-prod-must-be-32-chars}
    # ...existing code...
```

#### 📄 `.env.example` (Novo Arquivo)

```bash
# Database
POSTGRES_DB=quarkusdb
POSTGRES_USER=quarkus
POSTGRES_PASSWORD=secret
DB_URL=jdbc:postgresql://localhost:5432/quarkusdb

# Security (Generate with: openssl rand -base64 32)
SESSION_ENCRYPTION_KEY=
```

---

### 2. Fator XI: Logs (Observabilidade)

**Problema:** Logs em formato texto simples dificultam ingestão por ferramentas de análise (Splunk, ELK). Falta de correlação entre requisições.
**Solução:** Adicionar biblioteca de logging JSON e configurar formato estruturado.

#### 📄 `pom.xml`

```xml
<!-- Adicionar dependência -->
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-logging-json</artifactId>
</dependency>
```

#### 📄 `src/main/resources/application.properties`

```properties
# ...existing code...

# Logging Configuration
# Em DEV: Texto simples para legibilidade
%dev.quarkus.log.console.json=false
%dev.quarkus.log.console.format=%d{HH:mm:ss} %-5p [%c{2.}] (%t) %s%e%n

# Em PROD: JSON estruturado para máquinas
%prod.quarkus.log.console.json=true
%prod.quarkus.log.console.json.pretty-print=false
%prod.quarkus.log.level=INFO
```

#### 📄 `src/main/java/com/robsonbs/filter/RequestIdFilter.java` (Novo)

Implementar filtro para adicionar `requestId` ao MDC (Mapped Diagnostic Context).

```java
package com.robsonbs.filter;

import jakarta.ws.rs.container.*;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.MDC;
import java.io.IOException;
import java.util.UUID;

@Provider
public class RequestIdFilter implements ContainerRequestFilter, ContainerResponseFilter {
    
    @Override
    public void filter(ContainerRequestContext requestContext) {
        String requestId = UUID.randomUUID().toString();
        MDC.put("requestId", requestId);
    }

    @Override
    public void filter(ContainerRequestContext req, ContainerResponseContext res) {
        MDC.remove("requestId");
    }
}
```

---

### 3. Fator IX: Descartabilidade (Robustez)

**Problema:** O tempo de shutdown padrão pode abortar requisições em andamento abruptamente.
**Solução:** Configurar timeout de graceful shutdown.

#### 📄 `src/main/resources/application.properties`

```properties
# ...existing code...
# Graceful Shutdown
quarkus.shutdown.timeout=30s
```

---

### 4. Fator XII: Processos Administrativos (Migrações)

**Problema:** Migrações rodando automaticamente no startup ( `migrate-at-start=true` ) acoplam o deploy à migração, o que pode causar problemas em ambientes distribuídos (múltiplas instâncias tentando migrar ao mesmo tempo) ou timeouts de startup.
**Solução:** Permitir execução condicional ou via Job separado em produção.

#### 📄 `src/main/resources/application.properties`

```properties
# ...existing code...
# Flyway
# Em DEV: Automático para conveniência
%dev.quarkus.flyway.migrate-at-start=true

# Em PROD: Manual (via Job) ou controlado via ENV
%prod.quarkus.flyway.migrate-at-start=${FLYWAY_MIGRATE_AT_START:false}
```

---

### 5. Fator V: Build, Release, Run (Automação)

**Problema:** Ausência de pipeline formal de CI/CD.
**Solução:** Implementar GitHub Actions.

#### 📄 `.github/workflows/ci-cd.yml` (Novo)

```yaml
name: CI/CD

on: [push, pull_request]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { java-version: '17', distribution: 'temurin' }
      
      - name: Build & Test
        run: ./mvnw verify
      
      - name: Build Docker Image
        if: github.ref == 'refs/heads/main'
        run: docker build -f src/main/docker/Dockerfile.jvm -t quarkus-test .
```

---

## 🚀 Plano de Execução Imediata

1.  **Aplicar Fator III (Config):** Editar `application.properties` e `docker-compose.yml`.
2.  **Aplicar Fator XI (Logs):** Adicionar dependência no `pom.xml` e configurar propriedades.
3.  **Aplicar Fator IX (Disposability):** Ajustar timeout.
4.  **Aplicar Fator XII (Admin):** Ajustar flag do Flyway.
5.  **Criar Filtros:** Implementar `RequestIdFilter`.

Este plano cobre rigorosamente as lacunas identificadas para conformidade com os 12 Fatores.
