# Guia de Início Rápido

Este guia ajudará você a configurar e executar rapidamente a aplicação Quarkus MVC.

## Pré-requisitos

- Java 17 ou superior
- Maven 3.8+
- Docker e Docker Compose

## Início Rápido (Modo de Desenvolvimento)

### 1. Iniciar PostgreSQL

```bash
docker compose up -d postgres
```

### 2. Executar a Aplicação

```bash
./mvnw quarkus:dev
```

A aplicação estará disponível em http://localhost:8080

### 3. Acessar a Aplicação

- **Página Inicial**: http://localhost:8080/
- **Gerenciamento de Usuários**: http://localhost:8080/users
- **Endpoint da API**: http://localhost:8080/users/api

## Início Rápido (Produção com Docker)

### Compilar e Executar Tudo

```bash
# Compilar a aplicação
./mvnw clean package

# Iniciar todos os serviços (banco de dados + aplicação)
docker compose up
```

A aplicação estará disponível em http://localhost:8080

## Parando a Aplicação

### Modo de Desenvolvimento
Pressione `Ctrl+C` no terminal executando `quarkus:dev`

### Docker Compose
```bash
docker compose down
```

## Criando Seu Primeiro Usuário

1. Navegue para http://localhost:8080/users
2. Clique em "Adicionar Novo Usuário"
3. Preencha o formulário:
   - Nome: João Silva
   - Email: joao.silva@exemplo.com
4. Clique em "Criar Usuário"

## Uso da API

### Buscar todos os usuários (JSON)
```bash
curl http://localhost:8080/users/api
```

### Resposta
```json
[
  {
    "id": 1,
    "name": "João Silva",
    "email": "joao.silva@exemplo.com",
    "createdAt": "2025-11-13T18:40:35.802087"
  }
]
```

## Solução de Problemas

### Porta 5432 já está em uso
Se a porta do PostgreSQL já estiver em uso, pare as instâncias existentes do PostgreSQL:
```bash
docker ps
docker stop <container-id>
```

### Porta 8080 já está em uso
Altere a porta no `application.properties`:
```properties
quarkus.http.port=8090
```

### Problemas de conexão com o banco de dados
Verifique se o PostgreSQL está em execução e saudável:
```bash
docker compose ps
```

## Dicas de Desenvolvimento

- **Recarga Automática**: Alterações em arquivos Java são automaticamente recarregadas no modo dev
- **Dev UI**: Acesse a Interface de Dev do Quarkus em http://localhost:8080/q/dev-ui/
- **Console H2**: Pode ser habilitado para testes rápidos sem Docker

## Estrutura do Projeto

```
src/main/java/com/robsonbs/
├── model/          # Entidades (User)
├── dao/            # Data Access Objects
└── controller/     # Controladores REST

src/main/resources/
├── templates/      # Templates HTML
└── application.properties
```

## Próximos Passos

- Personalizar a entidade User adicionando mais campos
- Criar entidades e DAOs adicionais
- Adicionar autenticação e autorização
- Implementar paginação para grandes conjuntos de dados
- Adicionar validação de entrada
- Configurar migrações de banco de dados com Flyway/Liquibase
