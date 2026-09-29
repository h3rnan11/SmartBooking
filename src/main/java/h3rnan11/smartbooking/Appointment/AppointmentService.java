package h3rnan11.smartbooking.Appointment;

import h3rnan11.smartbooking.DTO.DtoAppointments;
import h3rnan11.smartbooking.DTO.DtoEmployeeResponse;
import h3rnan11.smartbooking.DTO.DtoUpdateAppointment;
import h3rnan11.smartbooking.Role.Role;
import h3rnan11.smartbooking.User.User;
import h3rnan11.smartbooking.User.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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

    public List<DtoEmployeeResponse> getAllEmployeesFromLocal(Integer id){
        List<DtoEmployeeResponse> employees = userRepository.getAllEmployeesFromLocal(id);
        if(employees.isEmpty())
            throw AppointmentException.notFound("Not employees found for the selected local");
        else
            return employees;
    }

    public boolean cancelAppointment (String email, Integer id){
        Appointment apt = appointmentRepository.getAppointmentById(id);
        if(Objects.equals(apt.getEmployee().getEmail(), email)
                || Objects.equals(apt.getClient().getEmail(), email)){
            apt.setStatus(Status.CANCELLED);
            appointmentRepository.save(apt);
            return true;
        }
        throw AppointmentException.forbidden("User not allow");
    }

    public void updateAppointment (String email, DtoUpdateAppointment appointment){
        Appointment apt = appointmentRepository.findById(appointment.id())
                .orElseThrow(() -> AppointmentException.notFound("No appointment found with id " + appointment.id()));
        User employee = userRepository.findById(appointment.employeeId())
                .orElseThrow(() -> AppointmentException.notFound("No employee found with id " + appointment.employeeId()));

        if(!Objects.equals(apt.getClient().getEmail(), email) && !Objects.equals(apt.getEmployee().getEmail(), email)){
            throw AppointmentException.forbidden("User not allow to update foreign appointments");
        }

        apt.setEmployee(employee);
        apt.setDate(appointment.date());
        apt.setStartTime(appointment.startTime());
        appointmentRepository.save(apt);
    }
}
