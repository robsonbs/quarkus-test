# 📋 Resumo Executivo do Projeto

## Sistema de Gestão de Notas e Tarefas

**Disciplina:** Programação para Web III  
**Instituição:** IFG – Campus Luziânia  
**Data:** Dezembro de 2025

---

## 1. Objetivo do Projeto

Desenvolver uma aplicação web completa demonstrando domínio de arquitetura **MVC** com tecnologias Java modernas, implementando todos os requisitos funcionais e não-funcionais especificados.

---

## 2. Escopo Implementado

### Módulos do Sistema

| Módulo | Descrição | Perfis de Acesso |
|--------|-----------|------------------|
| 🔐 Autenticação | Login/logout com sessão segura | Público |
| 👥 Usuários | CRUD completo de usuários | ADMIN |
| 🏷️ Perfis | Gerenciamento de perfis de acesso | ADMIN |
| 📝 Notas | Anotações pessoais por usuário | USER, ADMIN |
| ✅ Tarefas | Gestão com workflow de status | USER, ADMIN |
| 📊 Auditoria | Logs de todas as ações | ADMIN |

### Casos de Uso de Domínio

1. **Gestão de Notas Pessoais**
   - CRUD completo com ownership por usuário
   - Validação de campos obrigatórios

2. **Gestão de Tarefas com Prazos**
   - Workflow: Pendente → Em Andamento → Concluída
   - Validação de data limite (não permite passadas)
   - Auditoria completa de mudanças

---

## 3. Arquitetura Técnica

### Stack Tecnológica

| Camada | Tecnologia | Versão |
|--------|------------|--------|
| Linguagem | Java | 17 LTS |
| Framework | Quarkus | 3.6.4 |
| Web | JAX-RS (RESTEasy) | 3.x |
| Templates | Qute | - |
| ORM | Hibernate + Panache | 6.x |
| Segurança | Elytron JDBC | - |
| Banco | PostgreSQL | 15 |
| Migrações | Flyway | - |
| Frontend | Tailwind CSS | CDN |

### Padrões de Projeto

| Padrão | Implementação |
|--------|---------------|
| MVC | Controller → Service → DAO |
| DAO | Panache Repositories |
| DTO | Request/Response separados |
| BO | Service Layer |
| Entity | JPA/Hibernate |

---

## 4. Métricas do Projeto

### Código Fonte

| Categoria | Quantidade |
|-----------|------------|
| Classes Java | 44 |
| Controllers | 9 |
| Services (BO) | 6 |
| DAOs | 5 |
| Entities | 6 |
| DTOs | 12 |
| Templates HTML | 15 |

### Testes Automatizados

| Tipo | Quantidade |
|------|------------|
| Testes de Service | 114 |
| Testes de DTO | 26 |
| Testes de Integração | 35 |
| Testes de Segurança | 19 |
| **TOTAL** | **185** |

---

## 5. Conformidade com Requisitos

### Requisitos Funcionais

| RF | Descrição | Status |
|----|-----------|--------|
| RF1 | Autenticar Usuário | ✅ |
| RF2 | Manter Usuário | ✅ |
| RF3 | Manter Perfil de Usuário | ✅ |
| RF4 | Exibir Opções de Navegação | ✅ |
| RF5 | Dois Casos de Uso de Domínio | ✅ |
| RF6 | Auditoria e Rastreabilidade | ✅ |

### Requisitos Não-Funcionais

| RNF | Implementação | Status |
|-----|---------------|--------|
| Java EE | Quarkus 3.x (Jakarta EE) | ✅ |
| MVC | Controller/Service/DAO | ✅ |
| JAX-RS | RESTEasy Reactive | ✅ |
| DAO | Panache Repositories | ✅ |
| Entity | JPA/Hibernate | ✅ |
| BO | Service Layer | ✅ |
| DTO | Request/Response DTOs | ✅ |

---

## 6. Funcionalidades de Segurança

* **Autenticação:** Form-based via Elytron JDBC
* **Autorização:** @RolesAllowed por endpoint
* **Senhas:** Hash BCrypt
* **Ownership:** Verificação no Service Layer
* **Auditoria:** Registro de todas as ações sensíveis

---

## 7. Como Executar

### Pré-requisitos

* Java 17+
* Docker e Docker Compose

### Comandos

```bash
# Iniciar banco de dados
docker compose up -d postgres

# Executar aplicação
./mvnw quarkus:dev

# Executar testes
./mvnw test
```

### Acesso

* **URL:** http://localhost:8080
* **Admin:** admin@example.com / 123
* **User:** user@example.com / 123

---

## 8. Estrutura de Arquivos

```
src/main/java/com/robsonbs/
├── controller/     # 9 JAX-RS Controllers
├── service/        # 6 Business Services
├── dao/            # 5 Panache Repositories
├── model/          # 6 JPA Entities
├── dto/            # 12 Data Transfer Objects
├── filter/         # Security Filters
└── view/           # View Helpers

src/main/resources/
├── templates/      # 15 Qute Templates
│   ├── tags/       # Componentes reutilizáveis
│   └── errors/     # Páginas de erro
└── db/migration/   # Flyway Scripts

src/test/java/
└── com/robsonbs/   # 185 Testes Automatizados
```

---

## 9. Documentação Disponível

| Documento | Localização |
|-----------|-------------|
| README | `/README.md` |
| Análise de Requisitos | `/docs/analise-requisitos.md` |
| Plano de Alterações | `/docs/plano-alteracoes.md` |
| Slides da Apresentação | `/docs/apresentacao/SLIDES.md` |
| Diagramas | `/docs/apresentacao/DIAGRAMAS.md` |
| Guia de Demonstração | `/docs/apresentacao/GUIA-DEMONSTRACAO.md` |

---

## 10. Conclusão

O projeto **Sistema de Gestão de Notas e Tarefas** atende integralmente aos requisitos funcionais e não-funcionais especificados, demonstrando:

✅ Domínio completo da arquitetura MVC  
✅ Implementação correta dos padrões DAO, DTO e BO  
✅ Segurança robusta com autenticação e autorização  
✅ Dois casos de uso de domínio funcionais  
✅ Sistema de auditoria completo  
✅ Alta cobertura de testes (185 testes)  
✅ Interface responsiva e funcional  

---

**Projeto desenvolvido com ❤️ usando Quarkus, Java 17 e PostgreSQL**
