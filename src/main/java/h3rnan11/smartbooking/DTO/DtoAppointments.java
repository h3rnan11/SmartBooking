package h3rnan11.smartbooking.DTO;

import java.time.LocalDate;
import java.time.LocalTime;

public record DtoAppointments(
        Integer id,
        LocalDate date,
        LocalTime startTime,
        String name,
        String location
) {}
