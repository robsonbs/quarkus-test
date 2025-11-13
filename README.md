# Quarkus MVC Application with PostgreSQL

A modern web application built with Quarkus framework, implementing MVC and DAO patterns with PostgreSQL database and Tailwind CSS for styling.

## Tech Stack

- **Backend Framework**: Quarkus 3.6.4
- **Database**: PostgreSQL 15
- **ORM**: Hibernate with Panache
- **View Engine**: Qute Templates
- **CSS Framework**: Tailwind CSS
- **Containerization**: Docker & Docker Compose
- **Build Tool**: Maven

## Architecture

This application follows these design patterns:

- **MVC (Model-View-Controller)**: Separates concerns between data, presentation, and business logic
- **DAO (Data Access Object)**: Abstracts database operations
- **Dependency Injection**: Uses CDI for loose coupling

### Project Structure

```
src/main/java/com/robsonbs/
├── model/          # Entity classes (Model)
│   └── User.java
├── dao/            # Data Access Objects
│   └── UserDao.java
└── controller/     # REST Controllers (Controller)
    └── UserController.java

src/main/resources/
├── templates/      # Qute HTML templates (View)
│   ├── users.html
│   └── userForm.html
└── application.properties
```

## Features

- ✅ Full CRUD operations for User management
- ✅ RESTful API endpoints
- ✅ Responsive UI with Tailwind CSS
- ✅ PostgreSQL database integration
- ✅ Docker Compose orchestration
- ✅ Hibernate ORM with automatic schema generation

## Prerequisites

- Java 17 or later
- Maven 3.8+
- Docker and Docker Compose

## Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/robsonbs/quarkus-test.git
cd quarkus-test
```

### 2. Start PostgreSQL with Docker Compose

```bash
docker-compose up -d postgres
```

This will start PostgreSQL on port 5432 with:
- Database: `quarkusdb`
- Username: `quarkus`
- Password: `quarkus`

### 3. Run the Application in Dev Mode

```bash
./mvnw quarkus:dev
```

The application will be available at http://localhost:8080

### 4. Run with Docker Compose (Full Stack)

To run both the application and database together:

```bash
# Build the application
./mvnw clean package

# Start all services
docker-compose up
```

## Endpoints

### Web Interface

- **Home Page**: http://localhost:8080/
- **User Management**: http://localhost:8080/users
- **Add User**: http://localhost:8080/users/new

### API Endpoints

- **GET /users/api** - List all users (JSON)
- **POST /users** - Create a new user (form data)
- **POST /users/{id}/delete** - Delete a user

## Database Configuration

The application is configured to connect to PostgreSQL. Configuration can be found in `src/main/resources/application.properties`:

```properties
quarkus.datasource.db-kind=postgresql
quarkus.datasource.username=quarkus
quarkus.datasource.password=quarkus
quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/quarkusdb
quarkus.hibernate-orm.database.generation=update
```

## Development

### Running Tests

```bash
./mvnw test
```

### Building for Production

```bash
./mvnw clean package
java -jar target/quarkus-app/quarkus-run.jar
```

### Building Native Image

```bash
./mvnw package -Pnative
```

## Docker

### Build Docker Image

```bash
docker build -f src/main/docker/Dockerfile.jvm -t quarkus-test .
```

### Run with Docker

```bash
docker run -p 8080:8080 \
  -e QUARKUS_DATASOURCE_JDBC_URL=jdbc:postgresql://host.docker.internal:5432/quarkusdb \
  quarkus-test
```

## Project Details

### User Entity (Model)

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

### UserDao (Data Access Layer)

Provides methods for CRUD operations:
- `findAll()` - Get all users
- `findById(Long id)` - Get user by ID
- `save(User user)` - Create or update user
- `delete(Long id)` - Delete user
- `findByEmail(String email)` - Find user by email

### UserController (MVC Controller)

Handles HTTP requests and returns views or JSON responses:
- HTML views for user interface
- JSON API for programmatic access

## Contributing

Feel free to submit issues and enhancement requests!

## License

This project is licensed under the terms included in the LICENSE file.

## Resources

- [Quarkus Documentation](https://quarkus.io/guides/)
- [Hibernate ORM with Panache](https://quarkus.io/guides/hibernate-orm-panache)
- [Qute Templating Engine](https://quarkus.io/guides/qute)
- [Tailwind CSS](https://tailwindcss.com/)
