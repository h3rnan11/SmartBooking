package h3rnan11.smartbooking.DTO;

import h3rnan11.smartbooking.Appointment.Appointment;
import h3rnan11.smartbooking.Appointment.Status;

import java.time.LocalDate;
import java.time.LocalTime;

public record DtoAppointmentResponse(
        Integer id,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        Status status,
        Integer clientId,
        Integer employeeId
) {
    public static DtoAppointmentResponse from(Appointment a) {
        return new DtoAppointmentResponse(
                a.getId(),
                a.getDate(),
                a.getStartTime(),
                a.getEndTime(),
                a.getStatus(),
                a.getClient() != null ? a.getClient().getId() : null,
                a.getEmployee() != null ? a.getEmployee().getId() : null
        );
    }
}
