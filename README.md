docker compose up -d postgres
docker compose up --build

# Aplicação Web com Quarkus

Aplicação didática desenvolvida para a disciplina **Programação para Web III** (IFG – Campus Luziânia). O objetivo é demonstrar um backend Java com Quarkus atendendo aos requisitos comuns descritos no documento *Projeto Prático – Programação para Web – 2025* e dois casos de uso de domínio (gestão de notas pessoais e de tarefas com status e prazos).

## Tecnologias e Arquitetura

* **Quarkus 3.6.4**, rodando em modo JVM com extensões JAX-RS, Qute, Elytron Security JDBC e Panache ORM.
* **Java 17** (linguagem obrigatória do projeto).
* **PostgreSQL 15** (Docker Compose) com schema provisionado via migrações Flyway.
* **Tailwind CSS** servida via CDN para estilização rápida das telas.
* **Maven Wrapper** (`./mvnw`) para builds locais e em containers.

### Organização em camadas (MVC + DAO + Service/BO + DTO)

```
src/main/java/com/robsonbs/
├── controller/   # Camada Controller (JAX-RS) responsável pelas rotas HTML/JSON
├── service/      # Camada de negócio (BO) com regras e validações essenciais
├── dao/          # Objetos de Acesso a Dados (PanacheRepository)
├── dto/          # Objetos de transferência usados pelas telas e APIs
└── model/        # Entidades JPA (User, UserProfile, Note, Task)

src/main/resources/
├── templates/    # Páginas Qute (login, lista de usuários, lista de notas, formulários)
├── application.properties
└── db/migration/ # Scripts versionados Flyway (`V1__Initial_schema.sql`, ...)
```

## Entidades, DTOs e serviços

* **User / UserProfile:** representam credenciais e perfis;  `UserService` aplica hash de senha com `BcryptUtil` antes da persistência.
* **Note:** anotações vinculadas a um usuário;  `NoteService` busca notas do usuário autenticado e controla criação/edição/exclusão.
* **Task:** tarefas com status e data limite associadas ao usuário;  `TaskService` valida prazos e status e registra auditoria completa do ciclo de vida.
* **DTOs:** `UserRequestDTO`,        `UserResponseDTO`,        `NoteRequestDTO`,  `NoteResponseDTO` e `LoginRequestDTO` evitam expor entidades diretamente às views.

## Segurança

* **Autenticação:** formulário em `/login` envia credenciais para `/j_security_check`; Elytron Security JDBC consulta as tabelas `users` e `user_profiles`.
* **Autorização:** rotas protegidas com `@RolesAllowed` (`ADMIN` para administração de usuários;  `USER` ou `ADMIN` para notas).
* **Senhas demo:** geradas com Bcrypt e registradas via migração Flyway.
* **Sessão HTTP:** `quarkus.http.auth.session.encryption-key` definido em `application.properties` para garantir cookies consistentes entre sessões.

### Contas de demonstração

| Perfil | E-mail                | Senha |
|--------|-----------------------|-------|
| ADMIN  | admin@example.com     | 123   |
| USER   | user@example.com      | 123   |

## Conformidade com o documento “Projeto Prático – Programação para Web – 2025”

| ID | Requisito | Status | Implementação / Observações |
|----|-----------|--------|------------------------------|
| 1  | Autenticar usuário | ✅ Concluído | Login form `/login` , Elytron JDBC, proteção a todas as rotas autenticadas. |
| 2  | Manter usuário | ✅ Concluído | `UserController` + `users.html` permitem listar, criar e remover usuários (ADMIN). |
| 3  | Manter perfil de usuário | ✅ Concluído | `UserProfileController` + telas `profiles.html` / `profileForm.html` entregam CRUD completo com validações de uso e mensagens de feedback. |
| 4  | Navegação entre recursos | ✅ Concluído | Menu global reutilizável ( `templates/includes/navigation.html` ), breadcrumbs padronizados e página `/docs` consolidando os recursos. |
| 5  | Dois casos de uso de domínio | ✅ Concluído | 
|    | • Gestão de notas pessoais | | `NoteController` lista/cria/atualiza/exclui notas do usuário autenticado (perfil `USER` ou `ADMIN` ). |
|    | • Gestão de tarefas com prazos | | `TaskController` controla tarefas com status ( `PENDING` , `IN_PROGRESS` , `COMPLETED` ), valida data limite e mantém histórico via auditoria. |
| 6  | Rastreabilidade e auditoria | ✅ Concluído | `AuditLogFilter` registra requisições autenticadas (método, caminho, IP e User-Agent), `LoginAuditRouteFilter` captura eventos de login (sucesso/falha), `LogoutController` registra logout, e serviços enviam eventos de domínio ao `AuditLogService` ; auditoria possui tela `/audit` com filtros completos e paginação. |

### Requisitos não funcionais

* **Java + Quarkus + JAX-RS:** atendidos em toda a stack.
* **MVC + DAO + Service (BO):** camadas separadas conforme estrutura acima.
* **DTO para comunicação:** rotas HTML e JSON consomem/produzem DTOs; entidades não são expostas.
* **Banco de dados persistente:** PostgreSQL com Panache e migrações Flyway.
* **Auditoria completa:** todas as ações (login, logout, CRUD) são registradas com usuário, timestamp e detalhes.

## Execução

* **Pré-requisitos:** Docker + Docker Compose, Java 17 (caso execute sem container).
* **1. Clonar o repositório:**
  

```bash
  git clone https://github.com/robsonbs/quarkus-test.git
  cd quarkus-test
  ```

* **2. Subir somente o banco:**
  

```bash
  docker compose up -d postgres
  ```

* **3. Rodar em desenvolvimento:**
  

```bash
  ./mvnw quarkus:dev
  ```

  Acesse `http://localhost:8080` → será redirecionado para `/login` .
* **4. Executar toda a stack com Docker Compose:**
  

```bash
  ./mvnw clean package
  docker compose up --build
  ```

  A aplicação fica disponível em `http://localhost:8080` e o banco em `localhost:5432` .
* **5. Empacotar para produção (modo JVM):**
  

```bash
  ./mvnw clean package
  java -jar target/quarkus-app/quarkus-run.jar
  ```

* **6. Build nativo opcional:**
  

```bash
  ./mvnw package -Pnative
  ```

## Testes automatizados

Os testes com `@QuarkusTest` cobrem os pontos mais sensíveis do backend:
* Garantem que rotas protegidas redirecionam anônimos para `/login` e que a página pública carrega corretamente.
* Validam criação e validação de usuários (unicidade de e-mail) com auditoria em `AuditLog`.
* Exercitam o fluxo de criação/exclusão de perfis, incluindo bloqueio de remoção quando houver usuários associados.
* Verificam ciclo de vida completo das notas e tarefas (create/update/delete) com auditoria registrada.

## Endpoints principais

| Recurso | Método | Caminho | Papel |
|---------|--------|---------|-------|
| Login | GET | /login | Público |
| Usuários (lista) | GET | /users | ADMIN |
| Novo usuário (form) | GET | /users/new | ADMIN |
| Criar usuário | POST | /users | ADMIN |
| Carregar usuário para edição | GET | /users/{id} | ADMIN |
| Remover usuário | POST | /users/{id}/delete | ADMIN |
| API usuários (JSON) | GET | /users/api | ADMIN/USER |
| Notas (lista) | GET | /notes | ADMIN/USER |
| Nova nota (form) | GET | /notes/new | ADMIN/USER |
| Criar/atualizar nota | POST | /notes, /notes/{id} | ADMIN/USER |
| Excluir nota | POST | /notes/{id}/delete | ADMIN/USER |
| Tarefas (lista) | GET | /tasks | ADMIN/USER |
| Nova tarefa (form) | GET | /tasks/new | ADMIN/USER |
| Criar/atualizar tarefa | POST | /tasks, /tasks/{id} | ADMIN/USER |
| Excluir tarefa | POST | /tasks/{id}/delete | ADMIN/USER |
| Documentação | GET | /docs | ADMIN/USER |

## Dados iniciais e migração

* `src/main/resources/db/migration/V1__Initial_schema.sql` cria perfis `ADMIN` e `USER`, além de usuários e tarefas demo.
* Sequências podem ser ajustadas conforme o banco configurado (ver comentários no arquivo).

## Backlog imediato

* Disponibilizar filtros/pesquisas em notas e usuários (melhor UX).
* Implementar job de retenção/backup automatizado dos registros de auditoria.
* Automatizar exportação/relatórios das tarefas concluídas por período.
* Adicionar validação client-side nos formulários.

## Documentação adicional

* **[Arquitetura do Sistema](docs/arquitetura.md)** - Diagramas e fluxos detalhados
* **[Plano de Adequação](docs/plano-adequacao.md)** - Checklist de conformidade com requisitos

---

Projeto distribuído sob a licença informada em `LICENSE` .
