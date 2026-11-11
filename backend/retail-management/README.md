# Retail Management System

A backend RESTful API for managing retail business operations, built with Java 21 and Spring Boot.

The system provides product, order, customer, supplier, inventory and reporting management, with JWT authentication and role-based access control (RBAC).

## 1. Technology Stack

| Category | Technologies |
|---|---|
| Language | Java 21 |
| Backend Framework | Spring Boot 4.1.2 |
| ORM | Spring Data JPA, Hibernate |
| Database | Microsoft SQL Server 2025 |
| Security | Spring Security, JWT |
| Password Encryption | BCrypt |
| API Documentation | Swagger / OpenAPI |
| Build Tool | Maven |
| Testing | JUnit 5, Mockito, Spring Boot Test, MockMvc |
| Containerization | Docker, Docker Compose |
| Version Control | Git |

## 2. Main Features

### Product Management
- Create, update, delete and retrieve products.
- Validate product information.
- Prevent duplicate product SKUs.
- Optimistic locking using `@Version`.
- Role-based access control.

### Order Management
- Create and manage customer orders.
- Support order statuses: PENDING, CONFIRMED, CANCELLED and COMPLETED.
- Validate available product stock.
- Restore stock when pending orders are cancelled.
- Manage transactions to maintain data consistency.

### Customer Management
- Create, update, delete and retrieve customers.
- Validate customer information.
- Prevent duplicate customer emails.

### Supplier Management
- Manage supplier information.
- Support supplier CRUD operations.
- Prevent duplicate supplier emails.

### Inventory Management
- Manage stock-in, stock-out and stock adjustments.
- Track inventory transactions.
- Record stock quantities before and after each transaction.
- Prevent invalid inventory operations.

### Dashboard & Reporting
- Dashboard overview.
- Order statistics.
- Inventory summary.
- Low-stock reports.
- Daily sales reports.

### Authentication & Authorization
- User registration and login.
- JWT-based authentication.
- BCrypt password hashing.
- Role-based authorization using Spring Security.
- Three roles: ADMIN, MANAGER and STAFF.
- RBAC demonstrated in the Product Management module.

## 3. Architecture

The application follows a layered architecture to separate responsibilities.

```text
Client / Swagger UI
        |
        v
REST Controllers
        |
        v
Service Layer
        |
        v
Repository Layer
        |
        v
SQL Server Database
```

**Responsibilities:**

- **Controller:** Handles HTTP requests and responses.
- **Service:** Implements business logic and transaction management.
- **Repository:** Handles database operations through Spring Data JPA.
- **Entity:** Represents database entities.
- **DTO:** Separates API request and response models.
- **Security:** Handles JWT authentication and role-based authorization.
- **Exception Handling:** Centralizes application error responses.

## 4. Security & RBAC

| Operation | ADMIN | MANAGER | STAFF |
|---|:---:|:---:|:---:|
| View products | ✓ | ✓ | ✓ |
| Create products | ✓ | ✓ | ✗ |
| Update products | ✓ | ✓ | ✗ |
| Delete products | ✓ | ✗ | ✗ |

Public registration assigns the STAFF role by default. Users cannot select ADMIN or MANAGER through the public registration API.

## 5. Project Structure

```text
RetailManagement/
├── src/
│   ├── main/
│   │   ├── java/com/retail/management/
│   │   │   ├── config/
│   │   │   ├── controller/
│   │   │   ├── dto/
│   │   │   ├── entity/
│   │   │   ├── exception/
│   │   │   ├── repository/
│   │   │   ├── security/
│   │   │   └── service/
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/com/retail/management/
├── Dockerfile
├── .dockerignore
├── docker-compose.yml
├── .env
├── .gitignore
├── pom.xml
└── README.md
```

## 6. Running Locally

### Prerequisites

- Java 21
- Maven
- SQL Server 2025
- Git

### Database Configuration

Create a database named:

```sql
CREATE DATABASE RetailManagementDb;
```

Configure database connection and environment variables.

```properties
DB_URL=jdbc:sqlserver://localhost:14330;databaseName=RetailManagementDb;encrypt=true;trustServerCertificate=true
DB_USERNAME=your_sql_username
DB_PASSWORD=your_sql_password
JWT_SECRET=your_base64_encoded_secret
```

Do not commit real credentials or JWT secrets to source control.

### Build the Application

```bash
mvn clean package
```

### Run the Application

```bash
mvn spring-boot:run
```

Application URL:

```text
http://localhost:8080
```

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

## 7. Running with Docker Compose

### Prerequisites

- Docker Desktop
- Existing SQL Server container running and accessible on port 14330.

Create a `.env` file in the project root:

```properties
DB_USERNAME=your_sql_username
DB_PASSWORD=your_sql_password
JWT_SECRET=your_base64_encoded_secret
```

The `.env` file must not be committed to Git.

### Build and Start

```bash
docker compose up -d --build
```

### Check Container Status

```bash
docker compose ps
```

### View Application Logs

```bash
docker compose logs -f api
```

### Stop Containers

```bash
docker compose stop
```

### Start Containers

```bash
docker compose start
```

### Stop and Remove Compose Containers

```bash
docker compose down
```

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

**Note:** The current Docker Compose configuration reuses the existing SQL Server container through `host.docker.internal:14330`. It does not create a second SQL Server instance.

## 8. Testing

Run all tests:

```bash
mvn test
```

The project includes unit tests and integration tests.

Key testing areas include:

- Product business logic.
- Order creation and cancellation.
- Inventory stock operations.
- Authentication and authorization.
- Product RBAC permissions.
- Exception handling and validation.

## 9. Key Technical Concepts Demonstrated

- Object-Oriented Programming (OOP).
- SOLID principles.
- Layered Architecture.
- RESTful API design.
- Dependency Injection.
- DTO pattern.
- Repository pattern.
- Transaction management.
- Optimistic locking with `@Version`.
- Pessimistic locking.
- JWT authentication.
- Role-Based Access Control (RBAC).
- Global exception handling.
- Unit testing and integration testing.
- Docker and Docker Compose.

## 10. Project Goals

This project demonstrates practical backend development skills using Java and Spring Boot, including API development, database management, authentication, authorization, transaction handling, testing and containerization.