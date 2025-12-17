# 🏭 The Twelve-Factor App - Guia de Implementação

> Documentação completa sobre a metodologia dos 12 Fatores e seu plano de implementação no projeto quarkus-test.

---

## 📋 Índice

* [Introdução](#introdução)
* [Os 12 Fatores](#os-12-fatores)
  + [I. Base de Código (Codebase)](#i-base-de-código-codebase)
  + [II. Dependências (Dependencies)](#ii-dependências-dependencies)
  + [III. Configurações (Config)](#iii-configurações-config)
  + [IV. Serviços de Apoio (Backing Services)](#iv-serviços-de-apoio-backing-services)
  + [V. Build, Release, Run](#v-build-release-run)
  + [VI. Processos (Processes)](#vi-processos-processes)
  + [VII. Vínculo de Porta (Port Binding)](#vii-vínculo-de-porta-port-binding)
  + [VIII. Concorrência (Concurrency)](#viii-concorrência-concurrency)
  + [IX. Descartabilidade (Disposability)](#ix-descartabilidade-disposability)
  + [X. Paridade Dev/Prod (Dev/Prod Parity)](#x-paridade-devprod-devprod-parity)
  + [XI. Logs](#xi-logs)
  + [XII. Processos Administrativos (Admin Processes)](#xii-processos-administrativos-admin-processes)
* [Análise do Estado Atual](#análise-do-estado-atual)
* [Plano de Implementação](#plano-de-implementação)
* [Checklist de Conformidade](#checklist-de-conformidade)

---

## Introdução

### O que é a Metodologia Twelve-Factor App?

A **Twelve-Factor App** é uma metodologia criada por desenvolvedores da Heroku para construir aplicações **Software as a Service (SaaS)** modernas, escaláveis e de fácil manutenção. Ela define 12 princípios fundamentais que uma aplicação deve seguir para ser verdadeiramente cloud-native.

### Por que adotar os 12 Fatores?

| Benefício | Descrição |
|-----------|-----------|
| **Portabilidade** | Executa em qualquer plataforma de nuvem |
| **Escalabilidade** | Escala horizontal de forma simples |
| **Manutenibilidade** | Código limpo e organizado |
| **Automação** | CI/CD facilitado |
| **Resiliência** | Recuperação rápida de falhas |
| **Observabilidade** | Logs e métricas centralizados |

### Contexto do Projeto

Este documento analisa o projeto **quarkus-test**, uma aplicação de gestão de notas e tarefas construída com:

* **Framework:** Quarkus 3.6.4
* **Linguagem:** Java 17
* **Banco de Dados:** PostgreSQL 15
* **Migrações:** Flyway
* **Templates:** Qute
* **Build:** Maven
* **Containerização:** Docker

---

## Os 12 Fatores

### I. Base de Código (Codebase)

> "Uma base de código rastreada em controle de versão, muitos deploys"

#### 📖 Conceito

Uma aplicação twelve-factor é sempre rastreada em um sistema de controle de versão como Git. Existe exatamente **uma base de código por aplicação**, mas existem muitos **deploys** da aplicação (desenvolvimento, staging, produção).

#### ✅ Requisitos

* [ ] Uma única base de código no repositório
* [ ] Versionamento com Git
* [ ] Múltiplos ambientes derivados da mesma base
* [ ] Sem código compartilhado entre aplicações diferentes (usar bibliotecas)

#### 🔍 Estado Atual no Projeto

```
✅ IMPLEMENTADO

- Repositório Git: robsonbs/quarkus-test
- Branch principal: main
- Branch de desenvolvimento: copilot_head
- Estrutura de diretórios bem definida
```

#### 📊 Diagrama

```
┌──────────────────────────────────────────────────────────────────┐
│                        REPOSITÓRIO GIT                           │
│                     (robsonbs/quarkus-test)                      │
└──────────────────────────────────────────────────────────────────┘
                                │
           ┌────────────────────┼────────────────────┐
           │                    │                    │
           ▼                    ▼                    ▼
    ┌──────────────┐    ┌──────────────┐    ┌──────────────┐
    │     DEV      │    │   STAGING    │    │  PRODUCTION  │
    │   (local)    │    │   (preview)  │    │  (release)   │
    │              │    │              │    │              │
    │ branch: *    │    │ tag: v1.0-rc │    │ tag: v1.0.0  │
    └──────────────┘    └──────────────┘    └──────────────┘
```

---

### II. Dependências (Dependencies)

> "Declare e isole explicitamente as dependências"

#### 📖 Conceito

Uma aplicação twelve-factor **nunca depende da existência implícita** de pacotes no sistema. Ela declara todas as dependências explicitamente através de um **manifesto de dependências** e usa uma ferramenta de **isolamento de dependências** durante a execução.

#### ✅ Requisitos

* [ ] Todas as dependências declaradas no pom.xml
* [ ] Versões específicas (não ranges)
* [ ] Sem dependências de ferramentas do sistema
* [ ] Reprodutibilidade de builds

#### 🔍 Estado Atual no Projeto

```
✅ IMPLEMENTADO

- Maven com pom.xml declarativo
- BOM do Quarkus para gerenciamento de versões
- Maven Wrapper (./mvnw) para isolamento
- Dependências com versões fixas via BOM
```

#### 📋 Dependências Atuais

```xml
<!-- pom.xml - Dependências explícitas -->
<dependencies>
    <!-- Core -->
    <dependency>
        <groupId>io.quarkus</groupId>
        <artifactId>quarkus-hibernate-orm-panache</artifactId>
    </dependency>
    <dependency>
        <groupId>io.quarkus</groupId>
        <artifactId>quarkus-flyway</artifactId>
    </dependency>
    <dependency>
        <groupId>io.quarkus</groupId>
        <artifactId>quarkus-resteasy-reactive-qute</artifactId>
    </dependency>
    
    <!-- Database -->
    <dependency>
        <groupId>io.quarkus</groupId>
        <artifactId>quarkus-jdbc-postgresql</artifactId>
    </dependency>
    
    <!-- Security -->
    <dependency>
        <groupId>io.quarkus</groupId>
        <artifactId>quarkus-elytron-security-jdbc</artifactId>
    </dependency>
    
    <!-- Container -->
    <dependency>
        <groupId>io.quarkus</groupId>
        <artifactId>quarkus-container-image-docker</artifactId>
    </dependency>
</dependencies>
```

---

### III. Configurações (Config)

> "Armazene configurações no ambiente"

#### 📖 Conceito

A **configuração** de uma aplicação é tudo que pode variar entre deploys (staging, produção, desenvolvimento). Isso inclui:

* Credenciais de banco de dados
* URLs de serviços externos
* Chaves de API
* Configurações de feature flags

**Configurações devem ser armazenadas em variáveis de ambiente**, não no código.

#### ✅ Requisitos

* [ ] Configurações sensíveis via variáveis de ambiente
* [ ] Sem credenciais hardcoded no código
* [ ] Sem arquivos de configuração por ambiente no repositório
* [ ] Configurações externalizáveis

#### 🔍 Estado Atual no Projeto

```
⚠️ PARCIALMENTE IMPLEMENTADO

✅ Docker Compose usa variáveis de ambiente
✅ Quarkus suporta override via env vars
⚠️ application.properties tem valores default hardcoded
❌ Chave de criptografia de sessão hardcoded
❌ Credenciais default no arquivo de configuração
```

#### 🔧 Configuração Atual (Problemas)

```properties
# application.properties - VALORES SENSÍVEIS EXPOSTOS
quarkus.datasource.username=quarkus           # ❌ Hardcoded
quarkus.datasource.password=quarkus           # ❌ Hardcoded
quarkus.http.auth.session.encryption-key=ikuFOEQQVWrevWrFMWE7swnLHZaNFVnMimKqhANB1DI=  # ❌ Hardcoded
```

#### 🎯 Configuração Recomendada

```properties
# application.properties - Valores via ambiente
quarkus.datasource.username=${DB_USERNAME:quarkus}
quarkus.datasource.password=${DB_PASSWORD:quarkus}
quarkus.datasource.jdbc.url=${DB_URL:jdbc:postgresql://localhost:5432/quarkusdb}
quarkus.http.auth.session.encryption-key=${SESSION_KEY}
```

#### 📊 Diagrama de Configuração

```
┌───────────────────────────────────────────────────────────────────┐
│                        AMBIENTE                                   │
│                                                                   │
│   DB_USERNAME=prod_user                                           │
│   DB_PASSWORD=************                                        │
│   DB_URL=jdbc:postgresql://db.example.com:5432/proddb            │
│   SESSION_KEY=**********************************                  │
│   QUARKUS_HTTP_PORT=8080                                          │
└───────────────────────────────────────────────────────────────────┘
                               │
                               ▼
┌───────────────────────────────────────────────────────────────────┐
│                    APLICAÇÃO QUARKUS                              │
│                                                                   │
│   ┌─────────────────────────────────────────────────────────────┐ │
│   │              application.properties                          │ │
│   │                                                             │ │
│   │  quarkus.datasource.username=${DB_USERNAME}                 │ │
│   │  quarkus.datasource.password=${DB_PASSWORD}                 │ │
│   │  quarkus.datasource.jdbc.url=${DB_URL}                      │ │
│   └─────────────────────────────────────────────────────────────┘ │
└───────────────────────────────────────────────────────────────────┘
```

---

### IV. Serviços de Apoio (Backing Services)

> "Trate serviços de apoio como recursos anexados"

#### 📖 Conceito

Um **serviço de apoio** é qualquer serviço que a aplicação consome pela rede como parte de sua operação normal:

* Bancos de dados (PostgreSQL, MySQL)
* Sistemas de mensageria (RabbitMQ, Kafka)
* Serviços de cache (Redis, Memcached)
* Serviços de email (SMTP)
* Serviços de armazenamento (S3, MinIO)

A aplicação não deve fazer distinção entre serviços locais e de terceiros. Um banco PostgreSQL local deve poder ser substituído por um Amazon RDS sem alteração de código.

#### ✅ Requisitos

* [ ] Serviços acessados via URL/credenciais em configuração
* [ ] Sem código específico para fornecedores
* [ ] Possibilidade de trocar serviços sem rebuild
* [ ] Conexões via pool gerenciado

#### 🔍 Estado Atual no Projeto

```
✅ IMPLEMENTADO

- PostgreSQL tratado como recurso anexado
- Conexão configurável via URL
- Docker Compose para serviços locais
- Hibernate com pool de conexões
```

#### 📊 Diagrama de Backing Services

```
┌─────────────────────────────────────────────────────────────────┐
│                      APLICAÇÃO                                  │
│                    (quarkus-test)                               │
└─────────────────────────────────────────────────────────────────┘
          │                    │                    │
          │ DB_URL             │ (futuro)           │ (futuro)
          ▼                    ▼                    ▼
┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐
│   PostgreSQL     │  │      Redis       │  │    S3/MinIO      │
│                  │  │                  │  │                  │
│ • Local Docker   │  │ • Cache          │  │ • Arquivos       │
│ • RDS (AWS)      │  │ • Sessions       │  │ • Uploads        │
│ • Cloud SQL     │  │ • ElastiCache    │  │ • Backups        │
└──────────────────┘  └──────────────────┘  └──────────────────┘
```

---

### V. Build, Release, Run

> "Separe estritamente os estágios de build, release e execução"

#### 📖 Conceito

A transformação de código em deploy acontece em três estágios:

1. **Build:** Converte código em executável (compilação, bundling)
2. **Release:** Combina build com configuração do ambiente
3. **Run:** Executa a aplicação no ambiente de execução

Cada release deve ter um **identificador único** (timestamp, versão semântica).

#### ✅ Requisitos

* [ ] Build produz artefato imutável
* [ ] Release combina artefato + configuração
* [ ] Processo de deploy automatizado
* [ ] Versionamento de releases

#### 🔍 Estado Atual no Projeto

```
⚠️ PARCIALMENTE IMPLEMENTADO

✅ Maven produz JAR/artefato
✅ Docker build disponível
⚠️ Sem pipeline CI/CD definido
❌ Sem versionamento automático de releases
❌ Sem separação clara build/release
```

#### 📊 Pipeline Proposto

```
┌─────────────────────────────────────────────────────────────────────┐
│                           BUILD STAGE                               │
│                                                                     │
│  ┌─────────────┐     ┌─────────────┐     ┌─────────────────────┐   │
│  │   Source    │────▶│   ./mvnw    │────▶│  quarkus-app.jar    │   │
│  │   Code      │     │   package   │     │  (imutável)         │   │
│  └─────────────┘     └─────────────┘     └─────────────────────┘   │
│                                                    │                │
└────────────────────────────────────────────────────│────────────────┘
                                                     │
                                                     ▼
┌─────────────────────────────────────────────────────────────────────┐
│                          RELEASE STAGE                              │
│                                                                     │
│  ┌─────────────────────┐     ┌─────────────┐     ┌───────────────┐ │
│  │  quarkus-app.jar    │────▶│   Docker    │────▶│ Image v1.0.0  │ │
│  │                     │     │   Build     │     │               │ │
│  └─────────────────────┘     └─────────────┘     └───────────────┘ │
│                                                          │          │
│  ┌─────────────────────┐                                 │          │
│  │  Environment Vars   │─────────────────────────────────┘          │
│  │  (staging/prod)     │                                            │
│  └─────────────────────┘                                            │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
                                                     │
                                                     ▼
┌─────────────────────────────────────────────────────────────────────┐
│                            RUN STAGE                                │
│                                                                     │
│  ┌───────────────┐     ┌─────────────────────────────────────────┐ │
│  │ Image v1.0.0  │────▶│           Container Runtime             │ │
│  └───────────────┘     │                                         │ │
│                        │  • Kubernetes Pod                       │ │
│                        │  • Docker Container                     │ │
│                        │  • ECS Task                             │ │
│                        └─────────────────────────────────────────┘ │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

---

### VI. Processos (Processes)

> "Execute a aplicação como um ou mais processos stateless"

#### 📖 Conceito

Os processos da aplicação são **stateless** (sem estado) e **share-nothing**. Qualquer dado que precise persistir deve ser armazenado em um **serviço de apoio** (banco de dados, cache distribuído).

**Sticky sessions são uma violação** deste fator. Estado de sessão deve ser armazenado em um datastore com expiração temporal (Redis, Memcached).

#### ✅ Requisitos

* [ ] Processos stateless
* [ ] Sem armazenamento local de sessão
* [ ] Sem arquivos temporários compartilhados
* [ ] Sessões em store externo (Redis)

#### 🔍 Estado Atual no Projeto

```
⚠️ PARCIALMENTE IMPLEMENTADO

✅ Aplicação pode ser executada em múltiplas instâncias
✅ Dados persistidos no PostgreSQL
⚠️ Sessões armazenadas em memória (não distribuídas)
❌ Sem suporte a sessões distribuídas
```

#### 🎯 Melhorias Necessárias

Para escalar horizontalmente, as sessões precisam ser externalizadas:

```properties
# Configuração futura com Redis
quarkus.redis.hosts=redis://localhost:6379
quarkus.http.auth.session.store-type=redis
```

#### 📊 Arquitetura Stateless

```
┌─────────────────────────────────────────────────────────────────┐
│                      LOAD BALANCER                              │
└─────────────────────────────────────────────────────────────────┘
          │              │              │              │
          ▼              ▼              ▼              ▼
    ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐
    │ Process  │  │ Process  │  │ Process  │  │ Process  │
    │    1     │  │    2     │  │    3     │  │    N     │
    │          │  │          │  │          │  │          │
    │ STATELESS│  │ STATELESS│  │ STATELESS│  │ STATELESS│
    └──────────┘  └──────────┘  └──────────┘  └──────────┘
          │              │              │              │
          └──────────────┴──────────────┴──────────────┘
                                │
                    ┌───────────┴───────────┐
                    ▼                       ▼
            ┌──────────────┐        ┌──────────────┐
            │  PostgreSQL  │        │    Redis     │
            │   (dados)    │        │  (sessões)   │
            └──────────────┘        └──────────────┘
```

---

### VII. Vínculo de Porta (Port Binding)

> "Exporte serviços via vínculo de porta"

#### 📖 Conceito

A aplicação twelve-factor é **completamente auto-contida** e não depende de um servidor web externo. Ela exporta HTTP como um serviço através do **binding a uma porta**.

No ambiente de desenvolvimento, o serviço fica acessível via `http://localhost:8080` . Em produção, uma camada de roteamento lida com requisições e as encaminha para o processo vinculado à porta.

#### ✅ Requisitos

* [ ] Aplicação exporta HTTP em porta configurável
* [ ] Servidor web embarcado
* [ ] Sem dependência de container externo (Tomcat, JBoss)
* [ ] Health checks disponíveis

#### 🔍 Estado Atual no Projeto

```
✅ IMPLEMENTADO

- Quarkus com servidor Vert.x embarcado
- Porta configurável via QUARKUS_HTTP_PORT
- Binding em 0.0.0.0 para containers
- Endpoint HTTP exposto diretamente
```

#### 📋 Configuração Atual

```properties
# application.properties
quarkus.http.port=8080
quarkus.http.host=0.0.0.0
```

#### 📊 Port Binding

```
┌─────────────────────────────────────────────────────────────────┐
│                     PROCESSO QUARKUS                            │
│                                                                 │
│   ┌─────────────────────────────────────────────────────────┐   │
│   │                  Vert.x HTTP Server                     │   │
│   │                  (embarcado)                            │   │
│   │                                                         │   │
│   │              BIND: 0.0.0.0:8080                         │   │
│   └─────────────────────────────────────────────────────────┘   │
│                                                                 │
│   ┌─────────────────────────────────────────────────────────┐   │
│   │                  Aplicação                              │   │
│   │                                                         │   │
│   │  • Controllers (JAX-RS)                                │   │
│   │  • Services                                             │   │
│   │  • Templates (Qute)                                     │   │
│   └─────────────────────────────────────────────────────────┘   │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
                            │
                            ▼
               ┌───────────────────────┐
               │    :8080 (exposed)    │
               │                       │
               │  GET /               │
               │  GET /users          │
               │  GET /notes          │
               │  GET /tasks          │
               └───────────────────────┘
```

---

### VIII. Concorrência (Concurrency)

> "Escale através do modelo de processos"

#### 📖 Conceito

Na arquitetura twelve-factor, os processos são cidadãos de primeira classe. A aplicação deve ser arquitetada para escalar **adicionando mais processos** (escala horizontal), não adicionando mais recursos a um único processo (escala vertical).

Diferentes tipos de trabalho podem ser atribuídos a diferentes tipos de processos:

* **Web processes:** Tratam requisições HTTP
* **Worker processes:** Processam tarefas em background
* **Scheduler processes:** Executam jobs agendados

#### ✅ Requisitos

* [ ] Suporte a múltiplas instâncias
* [ ] Sem estado compartilhado entre processos
* [ ] Workloads separáveis (web, worker)
* [ ] Configuração de recursos por tipo de processo

#### 🔍 Estado Atual no Projeto

```
⚠️ PARCIALMENTE IMPLEMENTADO

✅ Aplicação pode rodar em múltiplas instâncias
✅ Arquitetura permite scale-out
⚠️ Sem separação de workloads (web/worker)
❌ Sem configuração de orquestração (k8s HPA)
```

#### 📊 Modelo de Concorrência

```
┌─────────────────────────────────────────────────────────────────┐
│                         WORKLOAD TYPES                          │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                           WEB (HTTP)                            │
│                                                                 │
│   ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐          │
│   │  web.1  │  │  web.2  │  │  web.3  │  │  web.N  │          │
│   │         │  │         │  │         │  │         │          │
│   │  512MB  │  │  512MB  │  │  512MB  │  │  512MB  │          │
│   └─────────┘  └─────────┘  └─────────┘  └─────────┘          │
│                                                                 │
│   Escala: kubectl scale deployment/web --replicas=N             │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                       WORKER (Background)                       │
│                         (futuro)                                │
│                                                                 │
│   ┌─────────┐  ┌─────────┐                                     │
│   │worker.1 │  │worker.2 │                                     │
│   │         │  │         │                                     │
│   │  256MB  │  │  256MB  │                                     │
│   └─────────┘  └─────────┘                                     │
│                                                                 │
│   • Processamento de emails                                     │
│   • Geração de relatórios                                       │
│   • Tarefas agendadas                                           │
└─────────────────────────────────────────────────────────────────┘
```

---

### IX. Descartabilidade (Disposability)

> "Maximize robustez com inicialização rápida e desligamento gracioso"

#### 📖 Conceito

Os processos da aplicação são **descartáveis**, ou seja, podem ser iniciados ou parados a qualquer momento. Isso facilita:

* Escala elástica
* Deploy rápido de código/configuração
* Robustez de deploys de produção

**Inicialização rápida:** O processo deve levar poucos segundos para estar pronto.

**Desligamento gracioso:** O processo deve terminar requisições em andamento antes de parar.

#### ✅ Requisitos

* [ ] Tempo de startup < 10 segundos
* [ ] Graceful shutdown implementado
* [ ] Suporte a SIGTERM
* [ ] Conexões encerradas corretamente

#### 🔍 Estado Atual no Projeto

```
✅ IMPLEMENTADO

- Quarkus tem startup rápido (~2-3s em JVM, <100ms nativo)
- Suporte nativo a SIGTERM
- Graceful shutdown padrão do Quarkus
- Pool de conexões com lifecycle gerenciado
```

#### 📋 Configuração de Graceful Shutdown

```properties
# application.properties (adicionar)
quarkus.shutdown.timeout=30s
```

#### 📊 Ciclo de Vida

```
┌─────────────────────────────────────────────────────────────────┐
│                        STARTUP                                  │
│                                                                 │
│  SIGSTART ──▶ [Vert.x] ──▶ [CDI] ──▶ [Flyway] ──▶ [Ready]      │
│                                                                 │
│              ~2-3 segundos (JVM)                                │
│              ~50-100ms (Native)                                 │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                        RUNNING                                  │
│                                                                 │
│              Processando requisições HTTP                       │
│              Health: /q/health → 200 OK                         │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                       SHUTDOWN                                  │
│                                                                 │
│  SIGTERM ──▶ [Stop accepting] ──▶ [Drain] ──▶ [Close pools]    │
│                                                                 │
│              1. Para de aceitar novas requisições               │
│              2. Aguarda requisições em andamento (30s)          │
│              3. Fecha conexões de banco                         │
│              4. Encerra processo                                │
└─────────────────────────────────────────────────────────────────┘
```

---

### X. Paridade Dev/Prod (Dev/Prod Parity)

> "Mantenha desenvolvimento, staging e produção o mais similares possível"

#### 📖 Conceito

Historicamente, existem gaps substanciais entre desenvolvimento e produção:

| Gap | Tradicional | Twelve-Factor |
|-----|-------------|---------------|
| **Tempo** | Semanas entre deploys | Horas/minutos |
| **Pessoal** | Devs vs Ops diferentes | Mesma equipe |
| **Ferramentas** | SQLite vs PostgreSQL | Mesmo stack |

A aplicação twelve-factor é desenhada para **continuous deployment** mantendo o gap entre dev e prod pequeno.

#### ✅ Requisitos

* [ ] Mesmos backing services em todos os ambientes
* [ ] Infraestrutura como código
* [ ] Containers para paridade de ambiente
* [ ] Configurações externalizadas

#### 🔍 Estado Atual no Projeto

```
✅ IMPLEMENTADO

- Docker Compose para ambiente local
- Mesmo PostgreSQL em dev e prod
- Containerização disponível
- Flyway garante schema consistente
```

#### 📋 Paridade de Stack

| Componente | Desenvolvimento | Produção |
|------------|-----------------|----------|
| Linguagem | Java 17 | Java 17 |
| Framework | Quarkus 3.6.4 | Quarkus 3.6.4 |
| Banco de Dados | PostgreSQL 15 | PostgreSQL 15 |
| Migrações | Flyway | Flyway |
| Container Runtime | Docker | Docker/Kubernetes |

#### 📊 Ambientes Equivalentes

```
┌─────────────────────────────────────────────────────────────────┐
│                      DESENVOLVIMENTO                            │
│                                                                 │
│  ┌─────────────┐    ┌─────────────────────────────────────────┐ │
│  │   ./mvnw    │    │         docker-compose.yml              │ │
│  │  quarkus:   │    │                                         │ │
│  │    dev      │◀──▶│  postgres:15-alpine                     │ │
│  └─────────────┘    │  (localhost:5432)                       │ │
│                     └─────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
                              ≈
┌─────────────────────────────────────────────────────────────────┐
│                        PRODUÇÃO                                 │
│                                                                 │
│  ┌─────────────┐    ┌─────────────────────────────────────────┐ │
│  │  Container  │    │         Managed PostgreSQL              │ │
│  │  quarkus-   │    │                                         │ │
│  │    app      │◀──▶│  AWS RDS / Cloud SQL / Azure DB         │ │
│  └─────────────┘    │  (db.example.com:5432)                  │ │
│                     └─────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

---

### XI. Logs

> "Trate logs como streams de eventos"

#### 📖 Conceito

Uma aplicação twelve-factor **nunca se preocupa** com roteamento ou armazenamento de seu output stream. Ela não deve tentar escrever ou gerenciar logfiles. Em vez disso, cada processo em execução escreve seu event stream, **unbuffered, para stdout**.

Em desenvolvimento, o desenvolvedor visualiza o stream no terminal. Em produção, os streams são capturados pelo ambiente de execução e roteados para destinos de análise (Elasticsearch, Splunk, etc).

#### ✅ Requisitos

* [ ] Logs para stdout/stderr
* [ ] Formato estruturado (JSON)
* [ ] Sem escrita em arquivos locais
* [ ] Informações de contexto (correlation ID, user)

#### 🔍 Estado Atual no Projeto

```
⚠️ PARCIALMENTE IMPLEMENTADO

✅ Logs vão para stdout por padrão
✅ Categoria de log configurada para audit
⚠️ Formato de log não é JSON
❌ Sem correlation ID
❌ Sem agregação centralizada
```

#### 📋 Configuração Atual

```properties
# application.properties
quarkus.log.category."com.robsonbs.filter".level=DEBUG
```

#### 🎯 Configuração Recomendada

```properties
# application.properties - Logs estruturados
quarkus.log.console.format=%d{yyyy-MM-dd HH:mm:ss,SSS} %-5p [%c{3.}] (%t) %s%e%n
quarkus.log.console.json=true
quarkus.log.console.json.pretty-print=false

# Em produção
%prod.quarkus.log.console.json=true
%prod.quarkus.log.level=INFO
```

#### 📊 Pipeline de Logs

```
┌─────────────────────────────────────────────────────────────────┐
│                      APLICAÇÃO                                  │
│                                                                 │
│   Logger.info("User {} created task {}", userId, taskId)        │
│                            │                                    │
│                            ▼                                    │
│   ┌─────────────────────────────────────────────────────────┐   │
│   │                    STDOUT                               │   │
│   │  {"timestamp":"2025-01-15T10:30:00Z",                   │   │
│   │   "level":"INFO",                                       │   │
│   │   "logger":"com.robsonbs.service.TaskService",          │   │
│   │   "message":"User admin@example.com created task 42",   │   │
│   │   "mdc":{"requestId":"abc123","userId":"admin"}}        │   │
│   └─────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                   LOG AGGREGATOR                                │
│                                                                 │
│   ┌─────────────┐  ┌─────────────┐  ┌─────────────┐            │
│   │   Fluentd   │  │   Logstash  │  │ CloudWatch  │            │
│   │   Promtail  │  │   Vector    │  │   Logs      │            │
│   └─────────────┘  └─────────────┘  └─────────────┘            │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                   LOG STORAGE & ANALYSIS                        │
│                                                                 │
│   ┌─────────────────────────────────────────────────────────┐   │
│   │  Elasticsearch / Loki / CloudWatch Insights             │   │
│   │                                                         │   │
│   │  • Busca full-text                                      │   │
│   │  • Dashboards                                           │   │
│   │  • Alertas                                              │   │
│   └─────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
```

---

### XII. Processos Administrativos (Admin Processes)

> "Execute tarefas admin/gestão como processos pontuais"

#### 📖 Conceito

Tarefas administrativas ou de gerenciamento one-off incluem:

* Executar migrações de banco de dados
* Executar um console (REPL) para inspecionar dados
* Executar scripts únicos de correção
* Executar jobs de limpeza de dados

Esses processos devem rodar no **mesmo ambiente** da aplicação, usando a mesma release e configuração.

#### ✅ Requisitos

* [ ] Migrações executam como processo separado
* [ ] Scripts admin usam mesmo codebase
* [ ] Ambiente idêntico à aplicação
* [ ] Comandos versionados no repositório

#### 🔍 Estado Atual no Projeto

```
⚠️ PARCIALMENTE IMPLEMENTADO

✅ Flyway executa migrações automaticamente
✅ Scripts de migração versionados
⚠️ Sem CLI para tarefas administrativas
❌ Sem jobs de manutenção estruturados
```

#### 📋 Migrações Atuais

```
src/main/resources/db/migration/
├── V1__create_user_profiles_table.sql
├── V2__create_users_table.sql
├── V3__create_notes_table.sql
├── V4__create_tasks_table.sql
├── V5__create_audit_logs_table.sql
└── V6__insert_initial_data.sql
```

#### 🎯 Implementação de Admin Tasks

```java
// Quarkus CLI Command para tarefas admin
@Command(name = "admin", description = "Administrative tasks")
public class AdminCommand {
    
    @Command(name = "cleanup-audit-logs")
    public void cleanupAuditLogs(@Option(names = "--days") int days) {
        // Limpa logs de auditoria antigos
    }
    
    @Command(name = "reindex-search")  
    public void reindexSearch() {
        // Recria índices de busca
    }
}
```

#### 📊 Processos Admin

```
┌─────────────────────────────────────────────────────────────────┐
│                    PROCESSOS DE LONGA DURAÇÃO                   │
│                                                                 │
│   ┌──────────────────────────────────────────────────────────┐  │
│   │                    WEB PROCESS                           │  │
│   │                                                          │  │
│   │   docker run quarkus-app:v1.0.0                         │  │
│   │   └── Servidor HTTP (contínuo)                          │  │
│   └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                    PROCESSOS ONE-OFF (Admin)                    │
│                                                                 │
│   ┌──────────────────────────────────────────────────────────┐  │
│   │                  MIGRAÇÃO (automática)                   │  │
│   │                                                          │  │
│   │   Flyway.migrate() → na inicialização                   │  │
│   │   └── Executa V1..Vn sequencialmente                    │  │
│   └──────────────────────────────────────────────────────────┘  │
│                                                                 │
│   ┌──────────────────────────────────────────────────────────┐  │
│   │                    TAREFA ADMIN                          │  │
│   │                                                          │  │
│   │   docker run quarkus-app:v1.0.0 admin cleanup --days=30 │  │
│   │   └── Executa e encerra                                 │  │
│   └──────────────────────────────────────────────────────────┘  │
│                                                                 │
│   ┌──────────────────────────────────────────────────────────┐  │
│   │                    CONSOLE/REPL                          │  │
│   │                                                          │  │
│   │   docker run -it quarkus-app:v1.0.0 console             │  │
│   │   └── Interativo para debug/inspeção                    │  │
│   └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

---

## Análise do Estado Atual

### Resumo de Conformidade

| Fator | Nome | Status | Prioridade |
|-------|------|--------|------------|
| I | Base de Código | ✅ Completo | - |
| II | Dependências | ✅ Completo | - |
| III | Configurações | ⚠️ Parcial | 🔴 Alta |
| IV | Backing Services | ✅ Completo | - |
| V | Build, Release, Run | ⚠️ Parcial | 🟡 Média |
| VI | Processos | ⚠️ Parcial | 🟡 Média |
| VII | Port Binding | ✅ Completo | - |
| VIII | Concorrência | ⚠️ Parcial | 🟢 Baixa |
| IX | Descartabilidade | ✅ Completo | - |
| X | Paridade Dev/Prod | ✅ Completo | - |
| XI | Logs | ⚠️ Parcial | 🟡 Média |
| XII | Admin Processes | ⚠️ Parcial | 🟢 Baixa |

### Score Geral

```
┌─────────────────────────────────────────────────────────────────┐
│                     TWELVE-FACTOR SCORE                         │
│                                                                 │
│   ████████████████████░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░  58%      │
│                                                                 │
│   ✅ Completo: 6/12 (50%)                                       │
│   ⚠️ Parcial:  6/12 (50%)                                       │
│   ❌ Ausente:  0/12 (0%)                                        │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## Plano de Implementação

### Fase 1: Configurações Seguras (Prioridade Alta)

**Objetivo:** Remover credenciais hardcoded e externalizar configurações sensíveis.

#### Sprint 1.1: Externalizar Configurações do Banco de Dados

```properties
# application.properties - ANTES
quarkus.datasource.username=quarkus
quarkus.datasource.password=quarkus
quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/quarkusdb

# application.properties - DEPOIS  
quarkus.datasource.username=${DB_USERNAME:quarkus}
quarkus.datasource.password=${DB_PASSWORD:quarkus}
quarkus.datasource.jdbc.url=${DB_URL:jdbc:postgresql://localhost:5432/quarkusdb}
```

**Estimativa:** 1 hora

#### Sprint 1.2: Externalizar Chave de Sessão

```properties
# application.properties - ANTES
quarkus.http.auth.session.encryption-key=ikuFOEQQVWrevWrFMWE7swnLHZaNFVnMimKqhANB1DI=

# application.properties - DEPOIS
quarkus.http.auth.session.encryption-key=${SESSION_ENCRYPTION_KEY}
```

**Estimativa:** 30 minutos

#### Sprint 1.3: Atualizar Docker Compose

```yaml
# docker-compose.yml
services:
  quarkus-app:
    environment:
      DB_URL: jdbc:postgresql://postgres:5432/quarkusdb
      DB_USERNAME: ${DB_USERNAME:-quarkus}
      DB_PASSWORD: ${DB_PASSWORD:-quarkus}
      SESSION_ENCRYPTION_KEY: ${SESSION_KEY:-default-dev-key-change-in-prod}
```

**Estimativa:** 30 minutos

#### Sprint 1.4: Criar .env.example

```bash
# .env.example - Template para desenvolvedores
DB_USERNAME=quarkus
DB_PASSWORD=quarkus
DB_URL=jdbc:postgresql://localhost:5432/quarkusdb
SESSION_ENCRYPTION_KEY=generate-a-secure-key-here
```

**Estimativa:** 15 minutos

---

### Fase 2: Pipeline CI/CD (Prioridade Média)

**Objetivo:** Implementar separação clara de Build, Release e Run.

#### Sprint 2.1: GitHub Actions - Build

```yaml
# .github/workflows/build.yml
name: Build

on:
  push:
    branches: [main]
  pull_request:
    branches: [main]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      
      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: maven
      
      - name: Build with Maven
        run: ./mvnw -B package -DskipTests
      
      - name: Run tests
        run: ./mvnw -B test
      
      - name: Upload artifact
        uses: actions/upload-artifact@v4
        with:
          name: quarkus-app
          path: target/quarkus-app/
```

**Estimativa:** 2 horas

#### Sprint 2.2: GitHub Actions - Release

```yaml
# .github/workflows/release.yml
name: Release

on:
  push:
    tags:
      - 'v*'

jobs:
  release:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      
      - name: Build Docker image
        run: |
          ./mvnw package -DskipTests
          docker build -f src/main/docker/Dockerfile.jvm \
            -t ghcr.io/${{ github.repository }}:${{ github.ref_name }} .
      
      - name: Push to Registry
        run: |
          echo ${{ secrets.GITHUB_TOKEN }} | docker login ghcr.io -u ${{ github.actor }} --password-stdin
          docker push ghcr.io/${{ github.repository }}:${{ github.ref_name }}
```

**Estimativa:** 2 horas

---

### Fase 3: Logs Estruturados (Prioridade Média)

**Objetivo:** Implementar logging estruturado em JSON com contexto.

#### Sprint 3.1: Configurar JSON Logging

```properties
# application.properties
quarkus.log.console.json=true
quarkus.log.console.json.pretty-print=false
quarkus.log.console.json.date-format=yyyy-MM-dd'T'HH:mm:ss.SSSZ

# Níveis por ambiente
%dev.quarkus.log.console.json=false
%dev.quarkus.log.level=DEBUG

%prod.quarkus.log.console.json=true
%prod.quarkus.log.level=INFO
```

**Estimativa:** 1 hora

#### Sprint 3.2: Adicionar Correlation ID

```java
// RequestIdFilter.java
@Provider
@PreMatching
public class RequestIdFilter implements ContainerRequestFilter {
    
    @Override
    public void filter(ContainerRequestContext ctx) {
        String requestId = ctx.getHeaderString("X-Request-ID");
        if (requestId == null) {
            requestId = UUID.randomUUID().toString();
        }
        MDC.put("requestId", requestId);
    }
}
```

**Estimativa:** 2 horas

---

### Fase 4: Sessões Distribuídas (Prioridade Média)

**Objetivo:** Permitir escala horizontal com sessões externalizadas.

#### Sprint 4.1: Adicionar Redis

```xml
<!-- pom.xml -->
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-redis-client</artifactId>
</dependency>
```

```yaml
# docker-compose.yml
services:
  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
```

**Estimativa:** 3 horas

---

### Fase 5: Health Checks (Prioridade Média)

**Objetivo:** Implementar endpoints de saúde para orquestradores.

#### Sprint 5.1: Adicionar SmallRye Health

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
    
    @Inject
    EntityManager em;
    
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

**Estimativa:** 2 horas

---

### Fase 6: Admin Processes (Prioridade Baixa)

**Objetivo:** Estruturar tarefas administrativas como comandos.

#### Sprint 6.1: CLI para Tarefas Admin

```java
// AdminCommand.java
@ApplicationScoped
public class AdminTasks {
    
    @Inject
    AuditLogDAO auditLogDAO;
    
    @Transactional
    public void cleanupOldAuditLogs(int daysToKeep) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(daysToKeep);
        auditLogDAO.delete("createdAt < ?1", cutoff);
    }
}
```

**Estimativa:** 4 horas

---

## Checklist de Conformidade

### Lista de Verificação

```
FATOR I - BASE DE CÓDIGO
[x] Repositório Git único
[x] Branches para diferentes ambientes
[x] Histórico de commits preservado
[x] .gitignore configurado

FATOR II - DEPENDÊNCIAS
[x] pom.xml com todas as dependências
[x] Maven Wrapper incluído
[x] BOM para gerenciamento de versões
[x] Sem dependências do sistema operacional

FATOR III - CONFIGURAÇÕES
[ ] Variáveis de ambiente para credenciais
[ ] .env.example documentado
[ ] Sem secrets no código
[ ] Configuração por perfil (dev/prod)

FATOR IV - BACKING SERVICES
[x] PostgreSQL via URL configurável
[x] Conexão pool gerenciado
[ ] Redis para sessões (futuro)
[ ] Health checks de conectividade

FATOR V - BUILD, RELEASE, RUN
[x] Maven build produz JAR
[x] Dockerfile disponível
[ ] CI/CD pipeline configurado
[ ] Versionamento semântico automático

FATOR VI - PROCESSOS
[x] Aplicação stateless
[ ] Sessões externalizadas
[x] Dados em banco externo
[ ] Suporte a múltiplas réplicas

FATOR VII - PORT BINDING
[x] Servidor HTTP embarcado
[x] Porta configurável
[x] Bind em 0.0.0.0
[x] Sem dependência de container externo

FATOR VIII - CONCORRÊNCIA
[x] Arquitetura permite scale-out
[ ] Separação web/worker
[ ] HPA configurável
[ ] Métricas de recursos

FATOR IX - DESCARTABILIDADE
[x] Startup rápido (<5s)
[x] Graceful shutdown
[x] Suporte a SIGTERM
[ ] Timeout de shutdown configurado

FATOR X - PARIDADE DEV/PROD
[x] Docker Compose para dev
[x] Mesmo banco em todos ambientes
[x] Flyway para migrações
[x] Containerização disponível

FATOR XI - LOGS
[x] Logs para stdout
[ ] Formato JSON estruturado
[ ] Correlation ID
[ ] MDC configurado

FATOR XII - ADMIN PROCESSES
[x] Migrações Flyway
[x] Scripts versionados
[ ] CLI para tarefas admin
[ ] Jobs de manutenção
```

---

## Roadmap de Implementação

```
┌─────────────────────────────────────────────────────────────────┐
│                    ROADMAP TWELVE-FACTOR                        │
└─────────────────────────────────────────────────────────────────┘

Q1 2025 ────────────────────────────────────────────────────────────
│
├── FASE 1: Configurações (2 dias)
│   ├── Externalizar credenciais de banco
│   ├── Externalizar chave de sessão
│   └── Criar .env.example
│
├── FASE 2: CI/CD (3 dias)
│   ├── GitHub Actions - Build
│   ├── GitHub Actions - Test
│   └── GitHub Actions - Release
│
└── FASE 3: Logs (2 dias)
    ├── JSON logging
    └── Correlation ID

Q2 2025 ────────────────────────────────────────────────────────────
│
├── FASE 4: Sessões Distribuídas (3 dias)
│   ├── Adicionar Redis
│   └── Configurar session store
│
├── FASE 5: Health Checks (1 dia)
│   ├── SmallRye Health
│   └── Custom health checks
│
└── FASE 6: Admin Processes (2 dias)
    ├── CLI commands
    └── Jobs de manutenção

TOTAL ESTIMADO: ~13 dias de desenvolvimento
```

---

## Conclusão

O projeto **quarkus-test** já possui uma base sólida para ser uma aplicação twelve-factor, com 50% dos fatores completamente implementados e os demais parcialmente atendidos. As principais áreas de melhoria são:

1. **Configurações (Fator III):** Prioridade mais alta - remover credenciais hardcoded
2. **CI/CD (Fator V):** Automatizar pipeline de build e release
3. **Logs (Fator XI):** Estruturar logs em JSON com contexto

A arquitetura do Quarkus facilita a adoção dos 12 fatores, oferecendo:

* Startup rápido (sub-segundo em modo nativo)
* Configuração via variáveis de ambiente nativa
* Servidor HTTP embarcado
* Suporte a health checks via SmallRye
* Containerização simplificada

Com a implementação das melhorias propostas, o projeto estará totalmente alinhado com as melhores práticas de aplicações cloud-native modernas.

---

## Referências

* [The Twelve-Factor App](https://12factor.net/) - Documentação original
* [Quarkus Configuration Guide](https://quarkus.io/guides/config) - Configuração Quarkus
* [Quarkus Container Images](https://quarkus.io/guides/container-image) - Containerização
* [Quarkus Health Guide](https://quarkus.io/guides/smallrye-health) - Health Checks
* [Quarkus Logging](https://quarkus.io/guides/logging) - Logging

---

*Documento criado em: Dezembro 2025*  
*Última atualização: Dezembro 2025*  
*Versão: 1.0.0*
