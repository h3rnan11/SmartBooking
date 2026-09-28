package h3rnan11.smartbooking.Appointment;

import h3rnan11.smartbooking.DTO.DtoAppointments;
import h3rnan11.smartbooking.Role.Role;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class AppointmentService {


    private final AppointmentRepository appointmentRepository;
    private final Clock clock;

    public AppointmentService(AppointmentRepository appointmentRepository, Clock clock) {
        this.appointmentRepository = appointmentRepository;
        this.clock = clock;
    }

    public List<DtoAppointments> getAppointments(String email, Role role){
        LocalDate today = LocalDate.now(clock);
        LocalTime now = LocalTime.now(clock);

        return switch(role){
            case Role.OWNER -> appointmentRepository.getAppointmentsFromOwner(email, today, now);
            case Role.CLIENT -> appointmentRepository.getAppointmentsFromClient(email, today, now);
            case Role.EMPLOYEE -> appointmentRepository.getAppointmentsFromEmployee(email, today, now);
            case Role.ADMIN -> throw AppointmentException.forbidden("Admins do not have appointments");
        };
    }
}
