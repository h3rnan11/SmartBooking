package h3rnan11.smartbooking.DTO;

import java.time.LocalDate;
import java.time.LocalTime;

// employeeId is optional: when null the appointment keeps its current employee.
public record DtoAppointmentUpdate(
        Integer employeeId,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime
) {}
