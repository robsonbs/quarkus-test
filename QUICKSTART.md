# Quick Start Guide

This guide will help you quickly set up and run the Quarkus MVC application.

## Prerequisites

- Java 17 or later
- Maven 3.8+
- Docker and Docker Compose

## Quick Start (Development Mode)

### 1. Start PostgreSQL

```bash
docker compose up -d postgres
```

### 2. Run the Application

```bash
./mvnw quarkus:dev
```

The application will be available at http://localhost:8080

### 3. Access the Application

- **Home Page**: http://localhost:8080/
- **User Management**: http://localhost:8080/users
- **API Endpoint**: http://localhost:8080/users/api

## Quick Start (Production with Docker)

### Build and Run Everything

```bash
# Build the application
./mvnw clean package

# Start all services (database + application)
docker compose up
```

The application will be available at http://localhost:8080

## Stopping the Application

### Development Mode
Press `Ctrl+C` in the terminal running `quarkus:dev`

### Docker Compose
```bash
docker compose down
```

## Creating Your First User

1. Navigate to http://localhost:8080/users
2. Click "Add New User"
3. Fill in the form:
   - Name: John Doe
   - Email: john.doe@example.com
4. Click "Create User"

## API Usage

### Get all users (JSON)
```bash
curl http://localhost:8080/users/api
```

### Response
```json
[
  {
    "id": 1,
    "name": "John Doe",
    "email": "john.doe@example.com",
    "createdAt": "2025-11-13T18:40:35.802087"
  }
]
```

## Troubleshooting

### Port 5432 already in use
If PostgreSQL port is already in use, stop existing PostgreSQL instances:
```bash
docker ps
docker stop <container-id>
```

### Port 8080 already in use
Change the port in `application.properties`:
```properties
quarkus.http.port=8090
```

### Database connection issues
Check PostgreSQL is running and healthy:
```bash
docker compose ps
```

## Development Tips

- **Live Reload**: Changes to Java files are automatically reloaded in dev mode
- **Dev UI**: Access Quarkus Dev UI at http://localhost:8080/q/dev-ui/
- **H2 Console**: Can be enabled for quick testing without Docker

## Project Structure

```
src/main/java/com/robsonbs/
├── model/          # Entities (User)
├── dao/            # Data Access Objects
└── controller/     # REST Controllers

src/main/resources/
├── templates/      # HTML templates
└── application.properties
```

## Next Steps

- Customize the User entity by adding more fields
- Create additional entities and DAOs
- Add authentication and authorization
- Implement pagination for large datasets
- Add input validation
- Configure database migrations with Flyway/Liquibase
