package h3rnan11.smartbooking.DTO;

import java.time.LocalDate;
import java.time.LocalTime;

public record DtoUpdateAppointment(
        Integer id,
        Integer employeeId,
        LocalDate date,
        LocalTime startTime
) {
}
