# Aplicação Quarkus MVC com PostgreSQL

Uma aplicação web moderna construída com o framework Quarkus, implementando os padrões MVC e DAO com banco de dados PostgreSQL e Tailwind CSS para estilização.

## Stack Tecnológica

- **Framework Backend**: Quarkus 3.6.4
- **Banco de Dados**: PostgreSQL 15
- **ORM**: Hibernate com Panache
- **Motor de Templates**: Qute Templates
- **Framework CSS**: Tailwind CSS
- **Containerização**: Docker & Docker Compose
- **Ferramenta de Build**: Maven

## Arquitetura

Esta aplicação segue os seguintes padrões de design:

- **MVC (Model-View-Controller)**: Separa as preocupações entre dados, apresentação e lógica de negócios
- **DAO (Data Access Object)**: Abstrai operações de banco de dados
- **Injeção de Dependência**: Usa CDI para baixo acoplamento

### Estrutura do Projeto

```
src/main/java/com/robsonbs/
├── model/          # Classes de entidade (Model)
│   └── User.java
├── dao/            # Data Access Objects
│   └── UserDao.java
└── controller/     # Controladores REST (Controller)
    └── UserController.java

src/main/resources/
├── templates/      # Templates HTML Qute (View)
│   ├── users.html
│   └── userForm.html
└── application.properties
```

## Funcionalidades

- ✅ Operações CRUD completas para gerenciamento de Usuários
- ✅ Endpoints de API RESTful
- ✅ Interface responsiva com Tailwind CSS
- ✅ Integração com banco de dados PostgreSQL
- ✅ Orquestração com Docker Compose
- ✅ Hibernate ORM com geração automática de schema

## Pré-requisitos

- Java 17 ou superior
- Maven 3.8+
- Docker e Docker Compose

## Começando

### 1. Clone o Repositório

```bash
git clone https://github.com/robsonbs/quarkus-test.git
cd quarkus-test
```

### 2. Inicie o PostgreSQL com Docker Compose

```bash
docker compose up -d postgres
```

Isso iniciará o PostgreSQL na porta 5432 com:
- Banco de dados: `quarkusdb`
- Usuário: `quarkus`
- Senha: `quarkus`

### 3. Execute a Aplicação no Modo Dev

```bash
./mvnw quarkus:dev
```

A aplicação estará disponível em http://localhost:8080

### 4. Execute com Docker Compose (Stack Completo)

Para executar tanto a aplicação quanto o banco de dados juntos:

```bash
# Compile a aplicação
./mvnw clean package

# Inicie todos os serviços
docker compose up
```

## Endpoints

### Interface Web

- **Página Inicial**: http://localhost:8080/
- **Gerenciamento de Usuários**: http://localhost:8080/users
- **Adicionar Usuário**: http://localhost:8080/users/new

### Endpoints da API

- **GET /users/api** - Listar todos os usuários (JSON)
- **POST /users** - Criar um novo usuário (dados de formulário)
- **POST /users/{id}/delete** - Deletar um usuário

## Configuração do Banco de Dados

A aplicação está configurada para conectar ao PostgreSQL. A configuração pode ser encontrada em `src/main/resources/application.properties`:

```properties
quarkus.datasource.db-kind=postgresql
quarkus.datasource.username=quarkus
quarkus.datasource.******
quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/quarkusdb
quarkus.hibernate-orm.database.generation=update
```

## Desenvolvimento

### Executando Testes

```bash
./mvnw test
```

### Compilando para Produção

```bash
./mvnw clean package
java -jar target/quarkus-app/quarkus-run.jar
```

### Compilando Imagem Nativa

```bash
./mvnw package -Pnative
```

## Docker

### Compilar Imagem Docker

```bash
docker build -f src/main/docker/Dockerfile.jvm -t quarkus-test .
```

### Executar com Docker

```bash
docker run -p 8080:8080 \
  -e QUARKUS_DATASOURCE_JDBC_URL=jdbc:postgresql://host.docker.internal:5432/quarkusdb \
  quarkus-test
```

## Detalhes do Projeto

### Entidade User (Model)

```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;
    private LocalDateTime createdAt;
}
```

### UserDao (Camada de Acesso a Dados)

Fornece métodos para operações CRUD:
- `findAll()` - Buscar todos os usuários
- `findById(Long id)` - Buscar usuário por ID
- `save(User user)` - Criar ou atualizar usuário
- `delete(Long id)` - Deletar usuário
- `findByEmail(String email)` - Buscar usuário por email

### UserController (Controlador MVC)

Trata requisições HTTP e retorna views ou respostas JSON:
- Views HTML para interface do usuário
- API JSON para acesso programático

## Contribuindo

Sinta-se livre para enviar issues e solicitações de melhorias!

## Licença

Este projeto está licenciado sob os termos incluídos no arquivo LICENSE.

## Recursos

- [Documentação Quarkus](https://quarkus.io/guides/)
- [Hibernate ORM with Panache](https://quarkus.io/guides/hibernate-orm-panache)
- [Motor de Templates Qute](https://quarkus.io/guides/qute)
- [Tailwind CSS](https://tailwindcss.com/)
