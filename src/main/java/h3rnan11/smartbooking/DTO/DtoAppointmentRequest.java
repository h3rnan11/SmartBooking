package h3rnan11.smartbooking.DTO;

import java.time.LocalDate;
import java.time.LocalTime;

public record DtoAppointmentRequest(
        Integer clientId,
        Integer employeeId,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime
) {}
