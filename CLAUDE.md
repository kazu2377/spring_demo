# CLAUDE.md

##日本語で記載して

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Spring Boot MVC web application with authentication and authorization using Spring Security. Uses H2 in-memory database for development and supports role-based access control (ADMIN/USER roles).

## Common Commands

```bash
# Build the project
./mvnw clean compile

# Run tests
./mvnw test

# Run the application
./mvnw spring-boot:run

# Package the application
./mvnw clean package

# Run with specific profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

## Architecture Overview

- **MVC Pattern**: Controllers handle web requests, Services contain business logic, Repositories handle data access
- **Security Layer**: Spring Security with form-based authentication and role-based authorization
- **Database**: H2 in-memory database with JPA/Hibernate for ORM
- **View Layer**: Thymeleaf templates with role-based conditional rendering

## Key Components Structure

```
src/main/java/com/example/demo/
├── config/SecurityConfig.java          # Spring Security configuration
├── controller/                         # Web controllers
│   ├── AuthController.java            # Authentication endpoints
│   ├── DashboardController.java       # Main dashboard
│   └── AdminController.java           # Admin-only endpoints
├── entity/User.java                   # User entity with roles
├── repository/UserRepository.java     # User data access
├── service/UserService.java          # User business logic
└── DemoApplication.java              # Main application class

src/main/resources/
├── templates/                         # Thymeleaf templates
│   ├── login.html                    # Login page
│   ├── dashboard.html                # Main dashboard
│   └── admin/                        # Admin-only pages
├── static/                           # CSS, JS, images
└── application.properties            # App configuration
```

## Security Configuration

- Custom login page with form-based authentication
- Role-based access control: ADMIN users see all features, regular users have limited access
- Dashboard shows different content based on user role
- Admin pages require ADMIN role access

## Database Setup

- H2 in-memory database for development
- User entity stores username, password (encoded), and roles
- Default admin user should be created on startup for testing

## Development Notes

- Use `@PreAuthorize` annotations for method-level security
- Thymeleaf `sec:authorize` for conditional rendering based on roles
- BCrypt password encoding for security
- Session-based authentication with proper logout handling
