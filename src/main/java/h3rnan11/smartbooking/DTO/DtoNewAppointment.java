package h3rnan11.smartbooking.DTO;

import java.time.LocalDate;
import java.time.LocalTime;

public record DtoNewAppointment(
        Integer employeeId,
        Integer serviceId,
        LocalDate appointmentDate,
        LocalTime startTime
) {
}
