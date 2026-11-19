# Multi-Tenant Task Management System

A backend application built using Java 21 and Spring Boot that supports multiple tenants using a single shared database with tenant-level data isolation.

## Technology Stack

- Java 21
- Spring Boot
- Spring Data JPA
- Spring Security
- JWT Authentication
- H2 Database
- Maven
- JUnit and Mockito

## Features

- Tenant registration with initial ADMIN user.
- JWT-based authentication.
- Role-based authorization.
- Tenant isolation using JWT claims.
- Secure task creation and assignment.
- Task status updates.
- Repository-level tenant filtering.
- Password encryption using BCrypt.

## User Roles

| Role | Permissions |
|---|---|
| ADMIN | Create users and view all tenant tasks |
| MANAGER | Create tasks, assign tasks and view tenant tasks |
| USER | View assigned tasks and update task status |

## API Endpoints

| Method | Endpoint | Access |
|---|---|---|
| POST | `/tenant/register` | Public |
| POST | `/auth/login` | Public |
| POST | `/users` | ADMIN |
| POST | `/tasks` | MANAGER |
| PUT | `/tasks/{id}/assign` | MANAGER |
| PUT | `/tasks/{id}/status` | USER |
| GET | `/tasks` | ADMIN, MANAGER, USER |

## Tenant Isolation and Security

- Every tenant has its own users and tasks.
- Tenant ID is included in the JWT.
- Tenant ID is extracted from the authenticated JWT.
- Clients cannot specify tenant ID through request parameters or request bodies.
- Repository queries filter records using tenant ID.
- Users can only access data belonging to their tenant.
- Regular users can only view and update their assigned tasks.

## JWT Authentication Flow

1. User registers a tenant or logs in.
2. Backend validates credentials.
3. Backend generates a JWT containing user ID, tenant ID and role.
4. JWT authentication filter validates incoming requests.
5. Authenticated user details are made available to the services.
6. Services use tenant ID from the authenticated principal to enforce tenant isolation.

## Database Design

The application uses a single H2 database containing:

- `tenants`
- `users`
- `tasks`

Each user and task is associated with a tenant.

## Run Locally

### Prerequisites

- Java 21
- Maven

### Configure JWT Secret

Set the `JWT_SECRET` environment variable before starting the application.

### Start Application

```bash
./mvnw spring-boot:run
```

On Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Application URL:

```text
http://localhost:8080
```

H2 Console:

```text
http://localhost:8080/h2-console
```

## Run Tests

Windows:

```powershell
.\mvnw.cmd clean test
```

## Project Structure

```text
src/main/java/com/techpulse/multitenant
├── config
├── controller
├── dto
├── entity
├── enums
├── exception
├── repository
├── security
└── service
```

## Author

Developed as a Java Spring Boot backend assignment.