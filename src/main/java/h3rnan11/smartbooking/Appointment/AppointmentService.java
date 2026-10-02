package h3rnan11.smartbooking.Appointment;

import h3rnan11.smartbooking.DTO.DtoAppointments;
import h3rnan11.smartbooking.DTO.DtoUpdateAppointment;
import h3rnan11.smartbooking.Local.Local;
import h3rnan11.smartbooking.Role.Role;
import h3rnan11.smartbooking.User.User;
import h3rnan11.smartbooking.User.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

@Service
public class AppointmentService {


    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final Clock clock;

    public AppointmentService(AppointmentRepository appointmentRepository, UserRepository userRepository, Clock clock) {
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
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

    @Transactional
    public void cancelAppointment (String email, Integer id){
        Appointment apt = appointmentRepository.findById(id)
                .orElseThrow(() -> AppointmentException.notFound("No appointment found with id " + id));

        if(Objects.equals(apt.getEmployee().getEmail(), email)
                || Objects.equals(apt.getClient().getEmail(), email)){
            if(apt.getStatus().equals(Status.CONFIRMED)
                || apt.getStatus().equals(Status.PENDING)){
                apt.setStatus(Status.CANCELLED);
                appointmentRepository.save(apt);
                return;
            }
            throw AppointmentException.conflict("Appointment cant be canceled");
        }
        throw AppointmentException.forbidden("User not allow");
    }

    @Transactional
    public void updateAppointment (String email, DtoUpdateAppointment appointment, Integer id) {
        Appointment apt = appointmentRepository.findById(id)
                .orElseThrow(() -> AppointmentException.notFound("No appointment found with id " + id));

        // USER confirmation
        if (!Objects.equals(apt.getClient().getEmail(), email) && !Objects.equals(apt.getEmployee().getEmail(), email)) {
            throw AppointmentException.forbidden("User not allow to update foreign appointments");
        }

        //If employeeId is null we left the previous employee
        User employee = (appointment.employeeId() == null)
                ? apt.getEmployee()
                : userRepository.findById(appointment.employeeId())
                .orElseThrow(() -> AppointmentException.notFound(
                        "No employee found with id " + appointment.employeeId()));


        Local newLocal = employee.getLocal();
        Local aptLocal = apt.getEmployee().getLocal();
        if (employee.getRole() != Role.EMPLOYEE
                || newLocal == null
                || !Objects.equals(newLocal.getId(), aptLocal.getId())) {
            throw AppointmentException.badRequest("employeeId does not match with an employee from the local selected");
        }

        // STATUS confirmation
        if (apt.getStatus() == Status.CANCELLED || apt.getStatus() == Status.COMPLETED) {
            throw AppointmentException.conflict("You cant update an appointment that is CANCELED or COMPLETED");
        }

        // DATE confirmation
        if (appointment.date() == null || appointment.startTime() == null) {
            throw AppointmentException.badRequest("Appointment date/startTime cant be null");
        }
        LocalDateTime newDateTime = LocalDateTime.of(appointment.date(), appointment.startTime());
        if(!newDateTime.isAfter(LocalDateTime.now(clock))){
            throw AppointmentException.badRequest("You cannot reserve on a previous date or time");
        }
        apt.setEmployee(employee);
        apt.setDate(appointment.date());
        apt.setStartTime(appointment.startTime());

        if(appointmentRepository.existsOverlap(
                apt.getEmployee().getId(),
                apt.getDate(),
                apt.getStartTime(),
                apt.getEndTime(),
                id))
            throw AppointmentException.conflict("The spot for this appointment overlap another one");

        appointmentRepository.save(apt);
    }
}
