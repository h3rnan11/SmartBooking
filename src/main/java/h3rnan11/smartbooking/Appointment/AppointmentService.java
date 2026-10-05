package h3rnan11.smartbooking.Appointment;

import h3rnan11.smartbooking.DTO.DtoAppointments;
import h3rnan11.smartbooking.DTO.DtoNewAppointment;
import h3rnan11.smartbooking.DTO.DtoUpdateAppointment;
import h3rnan11.smartbooking.EmployeeSchedule.EmployeeScheduleRepository;
import h3rnan11.smartbooking.Local.Local;
import h3rnan11.smartbooking.Role.Role;
import h3rnan11.smartbooking.Service.ServiceRepository;
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
    private final ServiceRepository serviceRepository;
    private final EmployeeScheduleRepository employeeScheduleRepository;
    private final Clock clock;

    public AppointmentService(AppointmentRepository appointmentRepository, UserRepository userRepository,
                              Clock clock, ServiceRepository serviceRepository, EmployeeScheduleRepository employeeScheduleRepository) {
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
        this.serviceRepository = serviceRepository;
        this.employeeScheduleRepository = employeeScheduleRepository;
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

        checkAppointmentDate(appointment.date(), appointment.startTime());

        apt.setEmployee(employee);
        apt.setDate(appointment.date());
        apt.setStartTime(appointment.startTime());
        apt.deriveEndTime();

        if(appointmentRepository.existsOverlap(
                apt.getEmployee().getId(),
                apt.getDate(),
                apt.getStartTime(),
                apt.getEndTime(),
                id))
            throw AppointmentException.conflict("The spot for this appointment overlap another one");

        appointmentRepository.save(apt);
    }

    @Transactional
    public Appointment newAppointment(String email, DtoNewAppointment appointment){
        User client = userRepository.findByEmail(email)
                .orElseThrow(() -> AppointmentException.notFound("No client found"));
        if(!client.getRole().equals(Role.CLIENT)){
            throw AppointmentException.forbidden("Only Clients can create new appointments");
        }

        User employee = userRepository.findById(appointment.employeeId())
                .orElseThrow(() -> AppointmentException.notFound("No employee found with id " + appointment.employeeId()));
        h3rnan11.smartbooking.Service.Service serviceType = serviceRepository.findById(appointment.serviceId())
                .orElseThrow(() -> AppointmentException.notFound("No Service found with id "+ appointment.serviceId()));
        if(!employee.getRole().equals(Role.EMPLOYEE)
                || employee.getLocal() == null){
            throw AppointmentException.badRequest("The given employee is not an Employee or doesn't work in this local");
        }

        Local local = employee.getLocal();

        if(!Objects.equals(serviceType.getLocal(), local)){
            throw AppointmentException.badRequest("The given service does not exists in the current local");
        }

        checkAppointmentDate(appointment.appointmentDate(), appointment.startTime());

        Appointment newApt = new Appointment();
        newApt.setClient(client);
        newApt.setEmployee(employee);
        newApt.setService(serviceType);
        newApt.setDate(appointment.appointmentDate());
        newApt.setStartTime(appointment.startTime());
        newApt.deriveEndTime();
        newApt.setStatus(Status.CONFIRMED);

        if(!employeeScheduleRepository.existsSlotForAppointment(employee.getId(), newApt.getDate().getDayOfWeek().getValue(),
                newApt.getStartTime(), newApt.getEndTime())){
            throw AppointmentException.badRequest("The employee does not work on that day/time");
        }
        if(appointmentRepository.existsOverlap(
                newApt.getEmployee().getId(),
                newApt.getDate(),
                newApt.getStartTime(),
                newApt.getEndTime(),
                null)){
            throw AppointmentException.conflict("The spot for this appointment overlap another one");
        }
        return appointmentRepository.save(newApt);
    }

    private void checkAppointmentDate(LocalDate aptDate, LocalTime aptStartTime){
        if (aptDate == null || aptStartTime == null) {
            throw AppointmentException.badRequest("Appointment date/startTime cant be null");
        }
        LocalDateTime newDateTime = LocalDateTime.of(aptDate, aptStartTime);
        if(!newDateTime.isAfter(LocalDateTime.now(clock))){
            throw AppointmentException.badRequest("You cannot reserve on a previous date or time");
        }
    }
}
