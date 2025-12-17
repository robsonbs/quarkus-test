# 🧪 Documentação de Testes

> Estratégia, execução e cobertura de testes do Sistema de Gestão de Notas e Tarefas.

---

## 📋 Índice

* [Visão Geral](#visão-geral)
* [Arquitetura de Testes](#arquitetura-de-testes)
* [Tipos de Testes](#tipos-de-testes)
* [Executando Testes](#executando-testes)
* [Testes por Módulo](#testes-por-módulo)
* [Mocking e Test Security](#mocking-e-test-security)
* [Cobertura de Código](#cobertura-de-código)
* [Boas Práticas](#boas-práticas)
* [Troubleshooting](#troubleshooting)

---

## Visão Geral

### Estatísticas Atuais

| Métrica | Valor |
|---------|-------|
| Total de Testes | 44 |
| Testes de Unidade | 25 |
| Testes de Integração | 19 |
| Taxa de Sucesso | 100% |

### Ferramentas Utilizadas

| Ferramenta | Propósito |
|------------|-----------|
| **JUnit 5** | Framework de testes |
| **REST Assured** | Testes de endpoints HTTP |
| **Quarkus Test** | Integração com Quarkus |
| **@TestSecurity** | Mock de autenticação |
| **AssertJ** | Assertions fluentes |

---

## Arquitetura de Testes

### Estrutura de Diretórios

```
src/test/
├── java/
│   └── com/
│       └── robsonbs/
│           ├── GreetingResourceTest.java       # Teste de exemplo
│           ├── BcryptTest.java                 # Testes de hash
│           ├── UserServiceTest.java            # Testes antigos
│           ├── UserProfileServiceTest.java     # Testes de perfil
│           ├── SecurityIntegrationTest.java    # Testes de segurança
│           └── service/
│               ├── AuditLogServiceTest.java    # Testes de auditoria
│               ├── NoteServiceTest.java        # Testes de notas
│               ├── TaskServiceTest.java        # Testes de tarefas
│               └── UserServiceTest.java        # Testes de usuários
└── resources/
    └── application.properties                  # Configurações de teste
```

### Configuração de Teste

```properties
# src/test/resources/application.properties

# Banco de dados de teste (mesmo PostgreSQL)
quarkus.datasource.db-kind=postgresql
quarkus.datasource.username=postgres
quarkus.datasource.password=postgres
quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/quarkus-test

# Validar esquema (não recriar)
quarkus.hibernate-orm.database.generation=validate

# Flyway ativo para migrations
quarkus.flyway.migrate-at-start=true
```

---

## Tipos de Testes

### 1. Testes de Unidade

Testam componentes isolados (Services, Validators).

**Características:**
* Rápidos (< 100ms cada)
* Sem dependências externas
* Uso de mocks quando necessário

**Exemplo:**

```java
@QuarkusTest
class BcryptTest {

    @Test
    void passwordShouldBeVerifiedCorrectly() {
        String password = "senha123";
        String hash = BcryptUtil.bcryptHash(password);
        
        assertTrue(BcryptUtil.matches(password, hash));
        assertFalse(BcryptUtil.matches("outra_senha", hash));
    }
}
```

### 2. Testes de Integração

Testam fluxos completos com banco de dados real.

**Características:**
* Usam `@QuarkusTest`
* Banco PostgreSQL real
* Transações gerenciadas

**Exemplo:**

```java
@QuarkusTest
class TaskServiceTest {

    @Inject
    TaskService taskService;

    @Inject
    TaskDao taskDao;

    @Test
    @TestSecurity(user = "user@example.com", roles = {"USER"})
    @Transactional
    void shouldValidateLifecycleAndAuditEntries() {
        // Criar tarefa
        TaskRequestDTO dto = new TaskRequestDTO();
        dto.setTitle("Tarefa Teste " + UUID.randomUUID());
        dto.setDescription("Descrição");
        dto.setDueDate(LocalDate.now().plusDays(3).toString());
        
        taskService.create(dto);
        
        // Verificar persistência
        Task task = taskDao.find("title", dto.getTitle()).firstResult();
        assertNotNull(task);
    }
}
```

### 3. Testes de Endpoint (REST)

Testam controllers via HTTP com REST Assured.

**Características:**
* Simulam requisições reais
* Validam status codes e headers
* Verificam redirecionamentos

**Exemplo:**

```java
@QuarkusTest
class SecurityIntegrationTest {

    @Test
    void anonymousUserIsRedirectedToLogin() {
        given()
            .redirects().follow(false)
        .when()
            .get("/users")
        .then()
            .statusCode(302)
            .header("Location", containsString("/login"));
    }

    @Test
    void loginPageLoadsSuccessfully() {
        given()
        .when()
            .get("/login")
        .then()
            .statusCode(200)
            .body(containsString("Login"));
    }
}
```

---

## Executando Testes

### Comandos Básicos

```bash
# Executar todos os testes
./mvnw test

# Executar teste específico
./mvnw test -Dtest=TaskServiceTest

# Executar por padrão de nome
./mvnw test -Dtest="*Service*"

# Executar método específico
./mvnw test -Dtest=TaskServiceTest#shouldValidateLifecycleAndAuditEntries

# Pular testes no build
./mvnw package -DskipTests
```

### Modo Contínuo

```bash
# Executar testes em modo watch (reexecuta ao salvar)
./mvnw quarkus:test

# Ou em modo dev com testes
./mvnw quarkus:dev -Dquarkus.test.continuous-testing=enabled
```

### Saída de Testes

```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.robsonbs.service.TaskServiceTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.robsonbs.SecurityIntegrationTest
[INFO] Tests run: 19, Failures: 0, Errors: 0, Skipped: 0
...
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 44, Failures: 0, Errors: 0, Skipped: 0
```

### Relatórios

Os relatórios são gerados em:

```
target/surefire-reports/
├── TEST-com.robsonbs.service.TaskServiceTest.xml
├── com.robsonbs.service.TaskServiceTest.txt
└── ...
```

---

## Testes por Módulo

### Módulo de Segurança

**Arquivo:** `SecurityIntegrationTest.java`

| Teste | Descrição |
|-------|-----------|
| `anonymousUserIsRedirectedToLoginWhenAccessingProtectedResource` | Redireciona `/users` para login |
| `anonymousUserIsRedirectedFromNotesPage` | Redireciona `/notes` para login |
| `anonymousUserIsRedirectedFromTasksPage` | Redireciona `/tasks` para login |
| `anonymousUserIsRedirectedFromAuditPage` | Redireciona `/audit` para login |
| `loginPageLoadsSuccessfully` | Página de login carrega |
| `loginWithValidCredentials` | Login com credenciais válidas |
| `loginWithInvalidCredentials` | Login com credenciais inválidas |
| `logoutClearsSession` | Logout limpa sessão |

### Módulo de Usuários

**Arquivo:** `service/UserServiceTest.java`

| Teste | Descrição |
|-------|-----------|
| `shouldCreateUserWithValidData` | Cria usuário com dados válidos |
| `shouldHashPasswordOnCreate` | Senha é hasheada ao criar |
| `shouldValidateUniqueEmail` | E-mail deve ser único |
| `shouldUpdateUserWithoutChangingPassword` | Atualiza sem alterar senha |
| `shouldRecordAuditOnCreate` | Auditoria registrada na criação |
| `shouldRecordAuditOnUpdate` | Auditoria registrada na atualização |
| `shouldRecordAuditOnDelete` | Auditoria registrada na exclusão |

### Módulo de Perfis

**Arquivo:** `UserProfileServiceTest.java`

| Teste | Descrição |
|-------|-----------|
| `shouldCreateProfile` | Cria perfil com nome válido |
| `shouldNotAllowDuplicateName` | Nome de perfil único |
| `shouldListAllProfiles` | Lista todos os perfis |

### Módulo de Notas

**Arquivo:** `service/NoteServiceTest.java`

| Teste | Descrição |
|-------|-----------|
| `shouldCreateNoteForAuthenticatedUser` | Cria nota para usuário autenticado |
| `shouldOnlyListUserOwnNotes` | Lista apenas notas do próprio usuário |
| `shouldNotAllowEditOtherUserNote` | Não permite editar nota de outro |
| `shouldValidateRequiredFields` | Valida campos obrigatórios |
| `shouldTrimTitleAndContent` | Remove espaços extras |

### Módulo de Tarefas

**Arquivo:** `service/TaskServiceTest.java`

| Teste | Descrição |
|-------|-----------|
| `shouldValidateLifecycleAndAuditEntries` | Testa ciclo completo com auditoria |
| `shouldRejectPastDueDate` | Rejeita data de vencimento passada |
| `shouldAllowStatusTransitions` | Permite transições de status |

### Módulo de Auditoria

**Arquivo:** `service/AuditLogServiceTest.java`

| Teste | Descrição |
|-------|-----------|
| `shouldRecordDomainEvent` | Registra evento de domínio |
| `shouldIncludeUserEmail` | Inclui e-mail do usuário |
| `shouldPersistAllFields` | Persiste todos os campos |

---

## Mocking e Test Security

### @TestSecurity

Simula usuário autenticado nos testes:

```java
@Test
@TestSecurity(user = "admin@example.com", roles = {"ADMIN"})
void adminCanAccessUsers() {
    // Executa como admin
}

@Test
@TestSecurity(user = "user@example.com", roles = {"USER"})
void userCanAccessOwnNotes() {
    // Executa como usuário comum
}
```

### Injeção de Dependências

```java
@QuarkusTest
class MyTest {
    
    @Inject
    MyService myService;  // Implementação real
    
    @Inject
    MyDao myDao;  // DAO real com banco
}
```

### @Transactional em Testes

```java
@Test
@Transactional
void shouldPersistData() {
    // Dados são commitados ao final
    // Rollback não automático em @QuarkusTest
}
```

### Limpeza de Dados

Para testes isolados, use UUIDs únicos:

```java
@Test
void createUniqueEntity() {
    String uniqueEmail = "test-" + UUID.randomUUID() + "@example.com";
    // Evita conflitos entre execuções
}
```

---

## Cobertura de Código

### Configuração JaCoCo

Adicionar ao `pom.xml` :

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.11</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

### Gerar Relatório

```bash
./mvnw test jacoco:report
```

Relatório em: `target/site/jacoco/index.html`

### Metas de Cobertura

| Camada | Meta | Atual |
|--------|------|-------|
| Services | 80% | ~85% |
| Controllers | 70% | ~75% |
| DAOs | 60% | ~70% |
| Models | 50% | ~60% |

---

## Boas Práticas

### 1. Nomenclatura de Testes

Use nomes descritivos que explicam o comportamento:

```java
// ✅ Bom
@Test
void shouldRejectTaskWithPastDueDate() { }

// ❌ Ruim
@Test
void testTask() { }
```

### 2. Arrange-Act-Assert (AAA)

```java
@Test
void shouldCalculateTotal() {
    // Arrange (Preparar)
    Order order = new Order();
    order.addItem(new Item("Produto", 100.0));
    
    // Act (Executar)
    double total = order.calculateTotal();
    
    // Assert (Verificar)
    assertEquals(100.0, total);
}
```

### 3. Um Assert por Conceito

```java
// ✅ Bom - testa um conceito
@Test
void shouldCreateUserWithHashedPassword() {
    User user = userService.create(dto);
    
    assertTrue(BcryptUtil.matches(dto.getPassword(), user.getPasswordHash()));
}

// ❌ Ruim - testa muitos conceitos
@Test
void testEverything() {
    User user = userService.create(dto);
    assertNotNull(user.getId());
    assertEquals("João", user.getName());
    assertTrue(BcryptUtil.matches(...));
    // ... muitos outros asserts
}
```

### 4. Dados de Teste Isolados

```java
// Use UUIDs para evitar conflitos
String email = "user-" + UUID.randomUUID() + "@test.com";
String title = "Tarefa-" + System.currentTimeMillis();
```

### 5. Evite Sleep em Testes

```java
// ❌ Ruim
Thread.sleep(1000);
assertTrue(result.isReady());

// ✅ Bom - use polling ou callbacks
await().atMost(5, SECONDS).until(() -> result.isReady());
```

---

## Troubleshooting

### Problema: Testes Falhando com Conexão

**Erro:**

```
Unable to connect to database
```

**Solução:**

```bash
# Verificar se PostgreSQL está rodando
docker compose ps

# Se não estiver, iniciar
docker compose up -d postgres

# Aguardar inicialização
sleep 5
./mvnw test
```

### Problema: Dados de Testes Anteriores

**Erro:**

```
Duplicate key violation
```

**Solução:**
Use identificadores únicos:

```java
String email = "test-" + UUID.randomUUID() + "@example.com";
```

### Problema: Testes Paralelos Falham

**Erro:**

```
Concurrent modification exception
```

**Solução:**
Desabilitar paralelismo no `pom.xml` :

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <forkCount>1</forkCount>
        <reuseForks>true</reuseForks>
    </configuration>
</plugin>
```

### Problema: @TestSecurity Não Funciona

**Erro:**

```
403 Forbidden
```

**Solução:**
Verificar que a anotação está no método:

```java
@Test
@TestSecurity(user = "admin@example.com", roles = {"ADMIN"})
void test() {
    // ...
}
```

### Problema: Testes Lentos

**Diagnóstico:**

```bash
./mvnw test -Dsurefire.printSummary=true
```

**Otimizações:**
1. Usar `@QuarkusTest` apenas quando necessário
2. Agrupar testes que compartilham setup
3. Evitar reinicialização do contexto

---

## Matriz de Testes

### Cenários Críticos

| Funcionalidade | Cenário | Status |
|----------------|---------|--------|
| Login | Credenciais válidas | ✅ |
| Login | Credenciais inválidas | ✅ |
| Login | Usuário inexistente | ✅ |
| Logout | Limpa sessão | ✅ |
| Usuários | CRUD completo | ✅ |
| Usuários | E-mail duplicado | ✅ |
| Usuários | Auditoria | ✅ |
| Notas | CRUD completo | ✅ |
| Notas | Ownership | ✅ |
| Tarefas | CRUD completo | ✅ |
| Tarefas | Status transitions | ✅ |
| Tarefas | Data passada | ✅ |
| Auditoria | Listagem | ✅ |
| Auditoria | Filtros | ✅ |

### Cenários de Borda

| Cenário | Teste |
|---------|-------|
| Título vazio | `shouldRejectEmptyTitle` |
| Título muito longo | `shouldRejectOverlyLongTitle` |
| E-mail inválido | `shouldRejectInvalidEmail` |
| Senha muito curta | `shouldRejectShortPassword` |
| ID inexistente | `shouldReturn404ForMissingId` |
| Acesso não autorizado | `shouldReturn403ForUnauthorized` |

---

## Checklist de Qualidade

### Antes do Commit

* [ ] Todos os testes passando (`./mvnw test`)
* [ ] Novos testes para novas funcionalidades
* [ ] Nenhum `@Disabled` sem justificativa
* [ ] Nomenclatura seguindo padrão

### Antes do Merge

* [ ] Testes de regressão passando
* [ ] Cobertura não diminuiu
* [ ] Testes de integração incluídos
* [ ] Code review dos testes

---

*Última atualização: Janeiro 2025*
