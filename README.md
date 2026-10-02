# SmartBooking

SmartBooking is a web application that lets clients book appointments at service businesses (hairdressers, physiotherapists, dentists...) without making a phone call.
Clients, employees and owners each see the appointments that concern them, and clients and employees can reschedule or cancel them in a few clicks.

## Table of Contents

1. [Features](#features)
2. [Roles](#roles)
3. [Tech Stack](#tech-stack)
4. [Getting Started](#getting-started)
5. [Test Users](#test-users)
6. [Project Structure](#project-structure)
7. [Documentation](#documentation)
8. [Roadmap](#roadmap)

## Features

| Feature | Status |
| --- | --- |
| Log in with JWT | API + UI |
| Register as a new client | API only |
| View your upcoming appointments | API + UI |
| Reschedule an appointment (date, time, employee) | API only (UI pending) |
| Cancel an appointment | API only (UI pending) |

## Roles

Permissions are enforced by the API, regardless of what the UI shows.

| Role | Permissions |
| --- | --- |
| `CLIENT` | View, reschedule and cancel their own appointments. New users register as clients. |
| `EMPLOYEE` | View, reschedule and cancel the appointments assigned to them. |
| `OWNER` | View every appointment of their business. |
| `ADMIN` | System administrator. No appointment features yet. |

## Tech Stack

- **Backend:** Java 21, Spring Boot 4.1, Spring Security (stateless JWT with jjwt, BCrypt passwords), Spring Data JPA / Hibernate.
- **Frontend:** Angular 21 (signals, route guards, HTTP interceptor).
- **Database:** MySQL 8, with sample data loaded from `src/main/resources/data.sql` on startup.
- **Testing:** JUnit 5, Mockito and AssertJ.
- **Environment:** Docker Compose.

## Getting Started

### Requirements

- Docker Desktop
- Java 21 (only to run the tests or the backend outside Docker)

### 1. Configure the JWT secret

The secret used to sign JWT tokens is not stored in the repository. The backend reads it from the `SMARTBOOKING_JWT_SECRET` environment variable and won't start without it.

Copy [.env.example](.env.example) to `.env` and fill in `SMARTBOOKING_JWT_SECRET` with at least 32 characters. You can generate one with:

```bash
openssl rand -base64 32
```

`.env` is git-ignored; never commit it. Changing the secret invalidates every token issued before.

To run the backend from your IDE or with `./mvnw spring-boot:run`, set `SMARTBOOKING_JWT_SECRET` in your shell or run configuration instead.

### 2. Start everything

```bash
docker compose up -d --build
```

On Windows you can run [start-project.ps1](start-project.ps1) instead: it waits for Docker, checks that the ports are free and creates `.env` with a random secret if it doesn't exist.

| Service | URL |
| --- | --- |
| Frontend | http://localhost:4200 |
| API | http://localhost:8080/smartBooking/smart-booking |
| MySQL | localhost:3306 |

### 3. Run the tests

```bash
./mvnw test
```

## Test Users

`data.sql` creates these users on startup. All of them use the password `password123`.

| Email | Role |
| --- | --- |
| `ana.client@smartbooking.com` | Client |
| `pedro.client@smartbooking.com` | Client |
| `lucia.employee@smartbooking.com` | Employee |
| `marcos.employee@smartbooking.com` | Employee |
| `carlos.owner@smartbooking.com` | Owner of *Bella Hair Studio* |

## Project Structure

```
SmartBooking/
├── src/main/java/h3rnan11/smartbooking/
│   ├── Appointment/        # Appointments: entity, repository, service, controller
│   ├── User/               # Users, registration and login
│   ├── Local/              # Businesses and their employees
│   ├── Service/            # Services offered by a business (entity only for now)
│   ├── EmployeeSchedule/   # Employees' working hours
│   ├── Security/           # JWT generation and validation
│   ├── Config/             # Spring Security, JWT filter, Clock, admin seeder
│   ├── DTO/  Role/  Utils/ # Records, role enum, Status and Category enums
├── src/main/resources/     # application.properties, data.sql
├── src/test/java/...       # Unit tests (AppointmentServiceTest)
├── frontend/               # Angular app
├── docs/                   # Documentation per domain
└── docker-compose.yml
```

## Documentation

- [Appointment](docs/appointment.md)
- More domains coming soon.

## Roadmap

- [ ] UI to reschedule and cancel appointments
- [ ] Pessimistic lock on the employee when booking or rescheduling
- [ ] Book new appointments based on each employee's free slots
- [ ] Calendar view of your appointments
- [ ] Admin metrics
- [ ] Documentation for every domain in `docs/`
- [ ] CI with GitHub Actions and Swagger (springdoc)