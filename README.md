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
[Role.java](src/main/java/h3rnan11/smartbooking/Role/Role.java) - Defines the permission level of a user: `ADMIN`, `CLIENT`, `EMPLOYEE` or `OWNER`.

### Appointment
[Appointment.java](src/main/java/h3rnan11/smartbooking/Appointment/Appointment.java) - A booking made by a client with an employee, with a date, time range and status.

### Local
[Local.java](src/main/java/h3rnan11/smartbooking/Local/Local.java) - A business premises owned by a user, with its employees and a category.

### EmployeeSchedule
[EmployeeSchedule.java](src/main/java/h3rnan11/smartbooking/EmployeeSchedule/EmployeeSchedule.java) - Defines the working days and hours of an employee.

## Other Classes
1. [Category](#category)
2. [Status](#status)

### Category
[Category.java](src/main/java/h3rnan11/smartbooking/Utils/Category.java) - Enum with the type of business a `Local` offers (hairdresser, nail salon, massage, etc.).

### Status
[Status.java](src/main/java/h3rnan11/smartbooking/Utils/Status.java) - Enum with the possible states of an `Appointment` (pending, confirmed, canceled, completed).

<!-- TODO add the rest utils when they are added to the software -->