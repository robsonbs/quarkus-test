# 📚 Glossário e FAQ

> Termos técnicos, acrônimos e perguntas frequentes do Sistema de Gestão de Notas e Tarefas.

---

## 📋 Índice

* [Glossário de Termos](#glossário-de-termos)
* [Acrônimos e Siglas](#acrônimos-e-siglas)
* [FAQ - Perguntas Frequentes](#faq---perguntas-frequentes)
* [Mensagens de Erro](#mensagens-de-erro)

---

## Glossário de Termos

### A

**Audit Log (Log de Auditoria)**
Registro automático de todas as operações realizadas no sistema, incluindo criação, atualização e exclusão de entidades. Armazena informações sobre quem fez a operação, quando e quais dados foram afetados.

**Autenticação**
Processo de verificação da identidade do usuário através de credenciais (e-mail e senha). O sistema utiliza autenticação baseada em formulário com sessões HTTP.

**Autorização**
Processo de verificação se um usuário autenticado tem permissão para acessar determinado recurso. Implementado através de roles (ADMIN, USER).

### B

**Bcrypt**
Algoritmo de hash utilizado para armazenar senhas de forma segura. Inclui salt automático e fator de custo configurável.

**Blocking**
Anotação `@Blocking` que indica que um endpoint deve ser executado em thread de I/O, necessário para operações de banco de dados em RESTEasy Reactive.

**Breadcrumb**
Navegação em trilha que mostra o caminho hierárquico até a página atual. Ex: Home > Usuários > Editar.

### C

**Controller**
Classe que recebe requisições HTTP, coordena chamadas a serviços e retorna respostas (HTML via Qute ou JSON). Responsável pela camada de apresentação.

**CRUD**
Acrônimo para Create, Read, Update, Delete - as quatro operações básicas de persistência de dados.

### D

**DAO (Data Access Object)**
Padrão de projeto que encapsula o acesso ao banco de dados. No Quarkus, implementado estendendo `PanacheRepository` .

**DTO (Data Transfer Object)**
Objeto usado para transferir dados entre camadas, especialmente entre controller e service. Evita expor entidades JPA diretamente.

**Due Date**
Data de vencimento de uma tarefa. O sistema valida que esta data não pode estar no passado.

### E

**Elytron**
Framework de segurança do WildFly/Quarkus usado para autenticação e autorização. Configurado via `application.properties` .

**Entity (Entidade)**
Classe Java mapeada para uma tabela do banco de dados via JPA. Representa um objeto de domínio persistível.

### F

**Flash Message**
Mensagem temporária exibida após uma operação (sucesso ou erro). Transmitida via query parameters nos redirecionamentos.

**Flyway**
Ferramenta de migração de banco de dados. Gerencia a evolução do schema através de scripts SQL versionados.

### H

**Hash**
Transformação criptográfica unidirecional. Usado para armazenar senhas de forma que não possam ser recuperadas.

**Hot Reload**
Recurso do modo dev do Quarkus que recarrega automaticamente as alterações de código sem reiniciar o servidor.

### J

**JAX-RS**
Especificação Java para serviços REST. No Quarkus, implementada pelo RESTEasy Reactive.

**JPA (Java Persistence API)**
Especificação para mapeamento objeto-relacional (ORM). Hibernate é a implementação usada pelo Quarkus.

### M

**Migration (Migração)**
Script SQL que altera o schema do banco de dados. Versionado sequencialmente (V1__, V2__, etc.) e executado automaticamente pelo Flyway.

### O

**Ownership (Propriedade)**
Conceito de que certas entidades (Notas, Tarefas) pertencem a um usuário específico. Validado nos services para garantir que usuários acessem apenas seus próprios dados.

### P

**Panache**
Extensão do Quarkus que simplifica o uso de JPA/Hibernate. Oferece padrão Active Record e Repository.

**Profile (Perfil)**
Entidade que representa um papel/role do usuário no sistema (ADMIN, USER). Define permissões de acesso.

### Q

**Quarkus**
Framework Java nativo para nuvem, otimizado para GraalVM e HotSpot. Oferece tempo de inicialização rápido e baixo consumo de memória.

**Qute**
Motor de templates do Quarkus. Usa sintaxe similar a Mustache/Handlebars com suporte a type-safety.

### R

**RESTEasy Reactive**
Implementação reativa de JAX-RS usada pelo Quarkus. Suporta processamento não-bloqueante por padrão.

**Role**
Papel atribuído a um usuário que define suas permissões. Ex: ADMIN pode gerenciar usuários, USER apenas suas notas/tarefas.

### S

**Service (Serviço)**
Classe que contém lógica de negócio. Coordena operações entre DAOs, realiza validações e gerencia transações.

**Session**
Mecanismo de manutenção de estado entre requisições HTTP. Armazena informações do usuário autenticado.

### T

**Tag (User-Defined Tag)**
Fragmento de template Qute reutilizável. Definido em `templates/tags/` e invocado como `{#tagName /}` .

**Task Status**
Estado de uma tarefa: PENDING (pendente), IN_PROGRESS (em andamento), COMPLETED (concluída).

**Template**
Arquivo HTML com marcações Qute que é renderizado dinamicamente pelo servidor para gerar páginas.

### V

**Validation**
Processo de verificação de dados de entrada. Implementado nos services antes de persistir dados.

---

## Acrônimos e Siglas

| Sigla | Significado |
|-------|-------------|
| **API** | Application Programming Interface |
| **CRUD** | Create, Read, Update, Delete |
| **DAO** | Data Access Object |
| **DTO** | Data Transfer Object |
| **HTML** | HyperText Markup Language |
| **HTTP** | HyperText Transfer Protocol |
| **JDBC** | Java Database Connectivity |
| **JPA** | Java Persistence API |
| **JSON** | JavaScript Object Notation |
| **JVM** | Java Virtual Machine |
| **MVC** | Model-View-Controller |
| **ORM** | Object-Relational Mapping |
| **REST** | Representational State Transfer |
| **SQL** | Structured Query Language |
| **URI** | Uniform Resource Identifier |
| **URL** | Uniform Resource Locator |
| **UUID** | Universally Unique Identifier |

---

## FAQ - Perguntas Frequentes

### Geral

#### O que é este sistema?

É um Sistema de Gestão de Notas e Tarefas desenvolvido em Quarkus que permite gerenciar usuários, perfis, notas pessoais e tarefas, com registro completo de auditoria.

#### Quais são os requisitos mínimos?

* Java 17+
* PostgreSQL 15+
* Maven 3.8+ (ou usar o wrapper `./mvnw`)

#### Como acesso o sistema?

1. Inicie o PostgreSQL: `docker compose up -d postgres`
2. Execute a aplicação: `./mvnw quarkus:dev`
3. Acesse: http://localhost:8080
4. Login: `admin@example.com` / `123`

---

### Autenticação

#### Quais usuários vêm pré-cadastrados?

| E-mail | Senha | Perfil |
|--------|-------|--------|
| `admin@example.com` | `123` | ADMIN |
| `user@example.com` | `123` | USER |

#### Esqueci minha senha, como recupero?

Atualmente não há recuperação de senha self-service. Um administrador deve acessar `/users` e atualizar a senha do usuário.

#### Por que sou deslogado após algum tempo?

A sessão expira por segurança. O timeout padrão é configurável em `application.properties` :

```properties
quarkus.http.auth.session.timeout=PT4H
```

---

### Usuários e Perfis

#### Qual a diferença entre ADMIN e USER?

| Recurso | ADMIN | USER |
|---------|-------|------|
| Gerenciar Usuários | ✅ | ❌ |
| Gerenciar Perfis | ✅ | ❌ |
| Ver Auditoria | ✅ | ❌ |
| Gerenciar Notas | ✅ (todas) | ✅ (próprias) |
| Gerenciar Tarefas | ✅ (todas) | ✅ (próprias) |

#### Posso criar novos perfis?

Sim, administradores podem criar novos perfis em `/profiles` . Porém, a autorização é baseada nos roles `ADMIN` e `USER` definidos no código.

#### Por que não consigo excluir um usuário?

O usuário pode ter registros associados (notas, tarefas) ou ser o último administrador. Verifique a mensagem de erro específica.

---

### Notas

#### Há limite de tamanho para notas?

* Título: até 255 caracteres
* Conteúdo: sem limite definido (TEXT no banco)

#### Posso compartilhar notas com outros usuários?

Não na versão atual. Cada nota pertence exclusivamente ao seu criador.

#### As notas são criptografadas?

Não. O conteúdo é armazenado em texto plano no banco de dados.

---

### Tarefas

#### Quais são os status possíveis?

1. **PENDING** - Tarefa criada, aguardando início
2. **IN_PROGRESS** - Tarefa em andamento
3. **COMPLETED** - Tarefa concluída

#### Por que não consigo definir uma data passada?

Por design, o sistema impede criar tarefas com data de vencimento no passado. Isso garante que as tarefas tenham prazos válidos.

#### Há notificações de tarefas vencidas?

Não na versão atual. Tarefas vencidas aparecem na listagem, mas não há alertas automáticos.

---

### Auditoria

#### O que é registrado na auditoria?

* Criação de usuários, perfis, notas e tarefas
* Atualização de qualquer entidade
* Exclusão de qualquer entidade
* Tentativas de login (sucesso e falha)

#### Posso excluir registros de auditoria?

Não. Os logs de auditoria são imutáveis para garantir rastreabilidade.

#### Por quanto tempo os logs são mantidos?

Indefinidamente. Não há política de retenção automática.

---

### Técnico

#### Como adiciono uma nova funcionalidade?

1. Crie a migration do banco (se necessário)
2. Crie/atualize Model e DAO
3. Crie/atualize Service com lógica de negócio
4. Crie/atualize Controller com endpoints
5. Crie/atualize templates Qute
6. Adicione testes

#### Como debugo um problema?

1. Verifique os logs: `docker compose logs -f app`
2. Ative logs detalhados: `quarkus.log.level=DEBUG`
3. Use o Dev UI: http://localhost:8080/q/dev
4. Inspecione o banco: `docker compose exec postgres psql -U postgres -d quarkus-test`

#### Por que minha migration falhou?

Possíveis causas:
* Sintaxe SQL incorreta
* Conflito com migration anterior
* Checksum diferente de versão anterior

Solução em dev: `./mvnw flyway:repair`

#### Como faço backup do banco?

```bash
docker compose exec postgres pg_dump -U postgres quarkus-test > backup.sql
```

---

### Desenvolvimento

#### Posso usar outro banco de dados?

O sistema foi desenvolvido para PostgreSQL. Outros bancos requerem:
* Alterar dependência no `pom.xml`
* Ajustar `application.properties`
* Revisar migrations (sintaxe pode variar)

#### Como executo apenas um teste específico?

```bash
./mvnw test -Dtest=NomeDoTeste#nomeDoMetodo
```

#### O hot reload não está funcionando, o que fazer?

1. Verifique se está em modo dev: `./mvnw quarkus:dev`
2. Salve o arquivo alterado
3. Aguarde alguns segundos
4. Se persistir, reinicie o Quarkus

---

## Mensagens de Erro

### Erros de Validação

| Mensagem | Causa | Solução |
|----------|-------|---------|
| "Título é obrigatório" | Campo título vazio | Preencha o título |
| "E-mail é obrigatório" | Campo e-mail vazio | Preencha o e-mail |
| "E-mail já cadastrado" | E-mail duplicado | Use outro e-mail |
| "Senha deve ter pelo menos 3 caracteres" | Senha muito curta | Use senha maior |
| "Data de vencimento inválida" | Formato incorreto | Use dd/MM/yyyy |
| "Data de vencimento não pode ser passada" | Data no passado | Use data futura |

### Erros de Autenticação

| Mensagem | Causa | Solução |
|----------|-------|---------|
| "Credenciais inválidas" | Login/senha incorretos | Verifique dados |
| "Sessão expirada" | Timeout de sessão | Faça login novamente |
| "Acesso negado" | Sem permissão | Verifique seu perfil |

### Erros de Sistema

| Código | Descrição | Ação |
|--------|-----------|------|
| 403 | Acesso proibido | Verifique permissões |
| 404 | Página não encontrada | Verifique URL |
| 500 | Erro interno | Verifique logs |

### Erros de Banco de Dados

| Mensagem | Causa | Solução |
|----------|-------|---------|
| "Connection refused" | PostgreSQL offline | Inicie o banco |
| "Duplicate key" | Violação de unicidade | Dados já existem |
| "Foreign key violation" | Referência inválida | Verifique dependências |

---

## Suporte

### Onde buscar ajuda?

1. **Esta documentação** - `/docs/`
2. **Código-fonte** - Comentários nos arquivos
3. **Logs** - `docker compose logs -f`
4. **Dev UI** - http://localhost:8080/q/dev

### Como reportar um bug?

1. Descreva o comportamento esperado
2. Descreva o comportamento atual
3. Passos para reproduzir
4. Logs relevantes
5. Ambiente (SO, versões)

---

*Última atualização: Janeiro 2025*
