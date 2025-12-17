# 📖 Documentação do Sistema

> **Sistema de Gestão de Notas e Tarefas** - Documentação completa do projeto.

---

## 🗂️ Índice de Documentos

| Documento | Descrição | Público-Alvo |
|-----------|-----------|--------------|
| [📐 Arquitetura](arquitetura.md) | Estrutura técnica, camadas, padrões | Desenvolvedores, Arquitetos |
| [💻 Desenvolvimento](desenvolvimento.md) | Setup, convenções, boas práticas | Desenvolvedores |
| [👤 Manual do Usuário](manual-usuario.md) | Como usar o sistema | Usuários finais |
| [🔌 API](api.md) | Endpoints REST, DTOs, exemplos | Desenvolvedores, Integradores |
| [🚀 Implantação](implantacao.md) | Deploy, Docker, Kubernetes | DevOps, SREs |
| [🧪 Testes](testes.md) | Estratégia, execução, cobertura | Desenvolvedores, QA |
| [📚 Glossário](glossario.md) | Termos técnicos e FAQ | Todos |

---

## 🚀 Início Rápido

```bash
# 1. Clonar repositório
git clone <repository-url>
cd quarkus-test

# 2. Iniciar banco de dados
docker compose up -d postgres

# 3. Executar aplicação
./mvnw quarkus:dev

# 4. Acessar
open http://localhost:8080
# Login: admin@example.com / 123
```

---

## 📊 Visão Geral do Projeto

### Stack Tecnológica

| Componente | Tecnologia | Versão |
|------------|------------|--------|
| Framework | Quarkus | 3.6.4 |
| Linguagem | Java | 17 |
| Banco | PostgreSQL | 15 |
| ORM | Hibernate/Panache | - |
| Templates | Qute | - |
| Segurança | Elytron | - |
| Migrations | Flyway | - |
| Build | Maven | 3.8+ |

### Funcionalidades Principais

* ✅ **Autenticação** - Login baseado em formulário com sessões
* ✅ **Usuários** - CRUD completo com hash de senhas
* ✅ **Perfis** - Gerenciamento de roles (ADMIN/USER)
* ✅ **Notas** - CRUD com ownership por usuário
* ✅ **Tarefas** - CRUD com status workflow e validação de datas
* ✅ **Auditoria** - Log automático de todas as operações

### Arquitetura

```
┌─────────────────────────────────────────────────────────┐
│                      CLIENTE                            │
│                   (Navegador Web)                       │
└────────────────────────┬────────────────────────────────┘
                         │ HTTP
┌────────────────────────▼────────────────────────────────┐
│                    CONTROLLER                           │
│           (JAX-RS + Qute Templates)                     │
├─────────────────────────────────────────────────────────┤
│                     SERVICE                             │
│          (Lógica de Negócio + Validação)               │
├─────────────────────────────────────────────────────────┤
│                       DAO                               │
│              (Panache Repository)                       │
├─────────────────────────────────────────────────────────┤
│                   POSTGRESQL                            │
│                (Flyway Migrations)                      │
└─────────────────────────────────────────────────────────┘
```

---

## 🔐 Segurança

### Contas de Demonstração

| E-mail | Senha | Perfil | Permissões |
|--------|-------|--------|------------|
| `admin@example.com` | `123` | ADMIN | Acesso total |
| `user@example.com` | `123` | USER | Notas/Tarefas próprias |

### Recursos Protegidos

| Rota | Roles Permitidos |
|------|------------------|
| `/users/**` | ADMIN |
| `/profiles/**` | ADMIN |
| `/audit/**` | ADMIN |
| `/notes/**` | USER, ADMIN |
| `/tasks/**` | USER, ADMIN |
| `/login` , `/logout` | Público |
| `/` , `/docs` | Público |

---

## 📁 Estrutura do Projeto

```
quarkus-test/
├── src/
│   ├── main/
│   │   ├── java/com/robsonbs/
│   │   │   ├── controller/     # Endpoints HTTP
│   │   │   ├── service/        # Lógica de negócio
│   │   │   ├── dao/            # Acesso a dados
│   │   │   ├── dto/            # Transferência de dados
│   │   │   ├── model/          # Entidades JPA
│   │   │   ├── filter/         # Filtros HTTP
│   │   │   └── view/           # Helpers de view
│   │   └── resources/
│   │       ├── db/migration/   # Scripts Flyway
│   │       └── templates/      # Templates Qute
│   └── test/                   # Testes
├── docs/                       # Esta documentação
├── docker-compose.yml          # Serviços Docker
├── pom.xml                     # Dependências Maven
└── README.md                   # Visão geral
```

---

## 🛠️ Comandos Úteis

### Desenvolvimento

```bash
# Modo desenvolvimento (hot reload)
./mvnw quarkus:dev

# Executar testes
./mvnw test

# Build
./mvnw package
```

### Docker

```bash
# Iniciar banco
docker compose up -d postgres

# Parar tudo
docker compose down

# Ver logs
docker compose logs -f
```

### Banco de Dados

```bash
# Conectar ao PostgreSQL
docker compose exec postgres psql -U postgres -d quarkus-test

# Backup
docker compose exec postgres pg_dump -U postgres quarkus-test > backup.sql
```

---

## 📝 Convenções

### Código

* Controllers: apenas orquestração HTTP
* Services: toda lógica de negócio
* DAOs: apenas persistência
* DTOs: para todas as entradas/saídas

### Templates Qute

* Tags em `templates/tags/`
* Chamadas: `{#tagName /}`
* Loops: `{#for item in list}...{/for}`
* Condicionais: `{#if condition}...{#else}...{/if}`

### Mensagens

* UI em Português
* Mensagens concisas
* Padrão: "Campo é obrigatório"

---

## 🔗 Links Úteis

* [Quarkus Guides](https://quarkus.io/guides/)
* [Qute Reference](https://quarkus.io/guides/qute-reference)
* [Panache Guide](https://quarkus.io/guides/hibernate-orm-panache)
* [Security Guide](https://quarkus.io/guides/security-overview)

---

## 📞 Contato

Para dúvidas ou sugestões, consulte o [Glossário e FAQ](glossario.md#suporte).

---

*Última atualização: Janeiro 2025*
