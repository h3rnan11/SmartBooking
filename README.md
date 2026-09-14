# SMARTBOOKING

## Markdown
1. [Description](#Description)
2. [Entities](#Entities)
3. [Other Classes](#other-classes)


## Description
SmartBooking is a software developed to simplify the task of making an appointment without any call. 
It gives the client the possibility of create, modify or cancel any appointment with a few clicks.
<!-- TODO finish the description -->

## Entities
1. [User](#user)
2. [Role](#role)
3. [Appointment](#appointment)
4. [Local](#local)
5. [EmployeeSchedule](#employeeschedule)

### User
[User.java](src/main/java/h3rnan11/smartbooking/User/User.java) - Represents any person using the platform (client, employee or owner). Holds personal data and links to their role, local and appointments.

### Role
[Role.java](src/main/java/h3rnan11/smartbooking/Role/Role.java) - Enum with the permission level of a user: `ADMIN`, `CLIENT`, `EMPLOYEE` or `OWNER`.

### Appointment
[Appointment.java](src/main/java/h3rnan11/smartbooking/Appointment/Appointment.java) - A booking made by a client with an employee, with a date, time range and status.

### Local
[Local.java](src/main/java/h3rnan11/smartbooking/Local/Local.java) - A business premises owned by a user, with its employees and a category.

### EmployeeSchedule
[EmployeeSchedule.java](src/main/java/h3rnan11/smartbooking/EmployeeSchedule/EmployeeSchedule.java) - Defines the working days and hours of an employee.

## Other Classes
1. [Category](#category)
2. [Status](#status) 
3. [AdminSeeder](#AdminSeeder)
4. [SecurityConfig](#SecurityConfig)
5. [UserDetailsServiceImpl](#UserDetailsServiceImpl)

### Category
[Category.java](src/main/java/h3rnan11/smartbooking/Utils/Category.java) - Enum with the type of business a `Local` offers (hairdresser, nail salon, massage, etc.).

### Status
[Status.java](src/main/java/h3rnan11/smartbooking/Utils/Status.java) - Enum with the possible states of an `Appointment` (pending, confirmed, canceled, completed).

### AdminSeeder
[AdminSeeder.java](src/main/java/h3rnan11/smartbooking/Config/AdminSeeder.java) - Runs on startup and creates the initial `ADMIN` user (from credentials in `application.properties`) if one doesn't already exist.

### SecurityConfig
[SecurityConfig.java](src/main/java/h3rnan11/smartbooking/Config/SecurityConfig.java) - Configures Spring Security: defines the `PasswordEncoder` (BCrypt) and the `SecurityFilterChain` (stateless sessions, public registration endpoint, HTTP Basic auth for the rest).

### UserDetailsServiceImpl
[UserDetailsServiceImpl](src/main/java/h3rnan11/smartbooking/Config/UserDetailsServiceImpl.java) - Bridges Spring Security with the database: loads a `User` by email and adapts it into Spring's `UserDetails` so the framework can authenticate requests and read the user's role.
<!-- TODO add the rest utils when they are added to the software -->