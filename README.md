# MovieFlix — Movie Management REST API

A Spring Boot REST API for managing movies, handling user authentication, and securing API access with JWT-based authentication. The project includes refresh-token sessions, role-based authorization, movie pagination, file uploads, and an email-based password recovery workflow.

Built with Java 21, Spring Boot, Spring Security, and MySQL.

## Key Features

### Movie Management
- RESTful endpoints for movie management.
- Retrieve movies with pagination.
- Retrieve movies with pagination and sorting.
- Upload and retrieve movie poster files.
- DTO-based request and response handling.

### Authentication & Authorization
- User registration and login.
- JWT-based stateless authentication.
- Access tokens with a 15-minute expiration.
- Refresh tokens with a 7-day expiration.
- Refresh-token rotation.
- Independent refresh-token sessions for multiple logins.
- Session-specific logout.
- Role-based endpoint authorization using Spring Security.
- Method-level authorization with `@PreAuthorize`.

### Password Recovery & Email Integration
A complete password recovery workflow using Gmail SMTP:

1. **Request OTP:** The user submits their email address to request a password reset.
2. **Email Delivery:** The application generates a secure six-digit OTP and sends it to the registered email address.
3. **OTP Verification:** The submitted OTP is validated against its stored hash and expiration time.
4. **Reset Token:** After successful verification, the application issues a temporary reset token.
5. **Password Reset:** The user submits the reset token and a new password.
6. **Session Invalidation:** Existing refresh-token sessions are deleted after a successful password reset.

Security measures include:
- Secure random OTP generation.
- Hashed OTP and reset-token storage.
- OTP expiration.
- Limited OTP verification attempts.
- One-time OTP verification.
- Short-lived password reset tokens.
- Generic responses to avoid exposing whether an email address is registered.

### File Management
- File upload and retrieval endpoints.
- Configurable file storage directory.

## Technologies

| Category | Technologies |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.2.1 |
| Web | Spring Web |
| Security | Spring Security 6 |
| Authentication | JSON Web Tokens (JJWT 0.11.5) |
| Database | MySQL |
| ORM | Spring Data JPA / Hibernate |
| Validation | Jakarta Bean Validation |
| Email | Spring Boot Starter Mail / JavaMailSender |
| Build Tool | Maven |
| Utilities | Lombok |
| API Testing | Postman |
| IDE | IntelliJ IDEA |

## Architecture

The application follows a layered architecture to separate responsibilities:

- **Controllers:** Handle HTTP requests and responses.
- **Services:** Contain application business logic.
- **Repositories:** Handle database operations through Spring Data JPA.
- **Entities:** Represent persistent database models.
- **DTOs:** Separate API contracts from database entities.
- **Security Configuration:** Defines authentication, authorization, and request security rules.

## Getting Started

### Prerequisites

Make sure you have installed:

- JDK 21
- Maven
- MySQL Server
- Git

### 1. Clone the Repository

```bash
git clone YOUR_REPOSITORY_URL
cd movieApi
```

### 2. Create the Database

Create a MySQL database:

```sql
CREATE DATABASE movies;
```

### 3. Configure Environment Variables

Configure your database, JWT, and email credentials as environment variables.

Example:

```text
DB_URL=jdbc:mysql://localhost:3306/movies
DB_USERNAME=your_database_username
DB_PASSWORD=your_database_password

MAIL_USERNAME=your_gmail_address
MAIL_APP_PASSWORD=your_gmail_app_password
```

Do not commit real credentials or secrets to the repository.

### 4. Configure application.yml

Use environment-variable placeholders in your Spring configuration.

Example Gmail SMTP configuration:

```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${MAIL_USERNAME}
    password: ${MAIL_APP_PASSWORD}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
```

Configure the datasource and JWT properties using the actual property names expected by the application.

**Gmail App Password:** Gmail SMTP authentication requires an App Password. Enable 2-Step Verification on your Google account, then generate an App Password from your Google Account security settings. Do not use your regular Gmail password.

### 5. Run the Application

Using Maven:

```bash
mvn spring-boot:run
```

Or run the main Spring Boot application class from your IDE.

The application will start at:

```text
http://localhost:8080
```

## Password Recovery API

The password recovery endpoints are:

| Method | Endpoint | Description |
|---|---|---|
| POST | `/forgotPassword/request` | Request a password reset OTP |
| POST | `/forgotPassword/verify-otp` | Verify the OTP and receive a reset token |
| POST | `/forgotPassword/reset` | Reset the account password |

### Request an OTP

```http
POST /forgotPassword/request
Content-Type: application/json
```

```json
{
  "email": "user@example.com"
}
```

### Verify the OTP

```http
POST /forgotPassword/verify-otp
Content-Type: application/json
```

```json
{
  "email": "user@example.com",
  "otp": "123456"
}
```

Successful verification returns a temporary reset token.

### Reset the Password

```http
POST /forgotPassword/reset
Content-Type: application/json
```

```json
{
  "resetToken": "YOUR_RESET_TOKEN",
  "password": "NewPassword123",
  "repeatPassword": "NewPassword123"
}
```

## API Testing

The API can be tested using Postman.

For protected endpoints, include the access token in the request:

```http
Authorization: Bearer YOUR_ACCESS_TOKEN
```

Public authentication and password recovery endpoints do not require an access token.

## Security Notes

- Never commit database passwords, Gmail App Passwords, or JWT secrets.
- Use environment variables for sensitive configuration.
- Use HTTPS when deploying the application publicly.
- Configure production database migrations instead of relying on automatic schema updates.
- Apply rate limiting to authentication and password recovery endpoints.
- Validate uploaded files and generate server-controlled filenames.

## Project Goals

This project was built to practice and demonstrate backend engineering concepts, including REST API development, relational database modeling, authentication, authorization, session management, email integration, and secure password recovery.
