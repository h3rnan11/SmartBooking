package h3rnan11.smartbooking.Appointment;

import h3rnan11.smartbooking.DTO.DtoAppointmentRequest;
import h3rnan11.smartbooking.DTO.DtoAppointmentResponse;
import h3rnan11.smartbooking.DTO.DtoAppointmentUpdate;
import h3rnan11.smartbooking.DTO.DtoTimeSlot;
import h3rnan11.smartbooking.EmployeeSchedule.EmployeeSchedule;
import h3rnan11.smartbooking.EmployeeSchedule.EmployeeScheduleRepository;
import h3rnan11.smartbooking.Role.Role;
import h3rnan11.smartbooking.User.User;
import h3rnan11.smartbooking.User.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class AppointmentService {

    public static final int DEFAULT_SLOT_MINUTES = 30;

    private final AppointmentRepository appointmentRepository;
    private final EmployeeScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final Clock clock;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              EmployeeScheduleRepository scheduleRepository,
                              UserRepository userRepository,
                              Clock clock) {
        this.appointmentRepository = appointmentRepository;
        this.scheduleRepository = scheduleRepository;
        this.userRepository = userRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public DtoAppointmentResponse getById(Integer id) {
        return DtoAppointmentResponse.from(findAppointment(id));
    }

    @Transactional(readOnly = true)
    public List<DtoAppointmentResponse> getByClient(Integer clientId) {
        return appointmentRepository.findByClientIdOrderByDateAscStartTimeAsc(clientId).stream()
                .map(DtoAppointmentResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DtoAppointmentResponse> getByEmployee(Integer employeeId) {
        return appointmentRepository.findByEmployeeIdOrderByDateAscStartTimeAsc(employeeId).stream()
                .map(DtoAppointmentResponse::from)
                .toList();
    }

    @Transactional
    public DtoAppointmentResponse create(DtoAppointmentRequest request) {
        if (request == null || request.clientId() == null || request.employeeId() == null) {
            throw AppointmentException.badRequest("clientId and employeeId are required");
        }
        validateTimeRange(request.date(), request.startTime(), request.endTime());

        User client = findUser(request.clientId(), "client");
        User employee = findEmployee(request.employeeId());

        checkAvailability(employee.getId(), client.getId(), request.date(),
                request.startTime(), request.endTime(), null);

        Appointment a = new Appointment();
        a.setClient(client);
        a.setEmployee(employee);
        a.setDate(request.date());
        a.setStartTime(request.startTime());
        a.setEndTime(request.endTime());
        a.setStatus(Status.PENDING);

        return DtoAppointmentResponse.from(appointmentRepository.save(a));
    }

    @Transactional
    public DtoAppointmentResponse update(Integer id, DtoAppointmentUpdate request) {
        if (request == null) {
            throw AppointmentException.badRequest("Request body is required");
        }
        Appointment a = findAppointment(id);
        if (a.getStatus() == Status.CANCELLED || a.getStatus() == Status.COMPLETED) {
            throw AppointmentException.conflict("A " + a.getStatus() + " appointment cannot be modified");
        }
        validateTimeRange(request.date(), request.startTime(), request.endTime());

        User employee = request.employeeId() != null ? findEmployee(request.employeeId()) : a.getEmployee();

        checkAvailability(employee.getId(), a.getClient().getId(), request.date(),
                request.startTime(), request.endTime(), a.getId());

        a.setEmployee(employee);
        a.setDate(request.date());
        a.setStartTime(request.startTime());
        a.setEndTime(request.endTime());

        return DtoAppointmentResponse.from(appointmentRepository.save(a));
    }

    @Transactional
    public DtoAppointmentResponse cancel(Integer id) {
        Appointment a = findAppointment(id);
        if (a.getStatus() == Status.CANCELLED) {
            throw AppointmentException.conflict("Appointment is already cancelled");
        }
        if (a.getStatus() == Status.COMPLETED) {
            throw AppointmentException.conflict("A completed appointment cannot be cancelled");
        }
        a.setStatus(Status.CANCELLED);
        return DtoAppointmentResponse.from(appointmentRepository.save(a));
    }

    /**
     * Free slots of {@code durationMinutes} for an employee on a date. Slots are laid out back to back from
     * the start of each schedule block and dropped when they overlap an active appointment or are already past.
     */
    @Transactional(readOnly = true)
    public List<DtoTimeSlot> getAvailableSlots(Integer employeeId, LocalDate date, Integer durationMinutes) {
        if (employeeId == null || date == null) {
            throw AppointmentException.badRequest("employeeId and date are required");
        }
        int duration = durationMinutes != null ? durationMinutes : DEFAULT_SLOT_MINUTES;
        if (duration <= 0) {
            throw AppointmentException.badRequest("duration must be greater than 0");
        }
        findEmployee(employeeId);

        LocalDate today = LocalDate.now(clock);
        if (date.isBefore(today)) {
            return List.of();
        }
        LocalTime now = date.equals(today) ? LocalTime.now(clock) : null;

        List<Appointment> booked =
                appointmentRepository.findByEmployeeIdAndDateAndStatusNot(employeeId, date, Status.CANCELLED);

        List<DtoTimeSlot> slots = new ArrayList<>();
        for (EmployeeSchedule block : schedulesFor(employeeId, date)) {
            LocalTime start = block.getStartTime();
            // Compare in minutes-of-day so a block ending at midnight-ish can't wrap around.
            while (start.toSecondOfDay() + duration * 60 <= block.getEndTime().toSecondOfDay()) {
                LocalTime end = start.plusMinutes(duration);
                LocalTime slotStart = start;
                boolean past = now != null && slotStart.isBefore(now);
                boolean taken = booked.stream().anyMatch(b -> overlaps(slotStart, end, b.getStartTime(), b.getEndTime()));
                if (!past && !taken) {
                    slots.add(new DtoTimeSlot(slotStart, end));
                }
                start = end;
            }
        }
        slots.sort(Comparator.comparing(DtoTimeSlot::startTime));
        return slots;
    }

    private void checkAvailability(Integer employeeId, Integer clientId, LocalDate date,
                                   LocalTime start, LocalTime end, Integer excludeId) {
        if (LocalDateTime.of(date, start).isBefore(LocalDateTime.now(clock))) {
            throw AppointmentException.badRequest("Appointments cannot be booked in the past");
        }
        boolean withinSchedule = schedulesFor(employeeId, date).stream()
                .anyMatch(s -> !start.isBefore(s.getStartTime()) && !end.isAfter(s.getEndTime()));
        if (!withinSchedule) {
            throw AppointmentException.conflict("The employee does not work at that time");
        }
        if (appointmentRepository.existsEmployeeOverlap(employeeId, date, start, end, excludeId)) {
            throw AppointmentException.conflict("The employee already has an appointment at that time");
        }
        if (appointmentRepository.existsClientOverlap(clientId, date, start, end, excludeId)) {
            throw AppointmentException.conflict("The client already has an appointment at that time");
        }
    }

    private List<EmployeeSchedule> schedulesFor(Integer employeeId, LocalDate date) {
        int day = date.getDayOfWeek().getValue();
        return scheduleRepository.findByEmployeeId(employeeId).stream()
                .filter(s -> s.getDayOfWeek() != null && s.getDayOfWeek().contains(day))
                .filter(s -> s.getStartTime() != null && s.getEndTime() != null)
                .toList();
    }

    private static void validateTimeRange(LocalDate date, LocalTime start, LocalTime end) {
        if (date == null || start == null || end == null) {
            throw AppointmentException.badRequest("date, startTime and endTime are required");
        }
        if (!start.isBefore(end)) {
            throw AppointmentException.badRequest("startTime must be before endTime");
        }
    }

    private static boolean overlaps(LocalTime aStart, LocalTime aEnd, LocalTime bStart, LocalTime bEnd) {
        return aStart.isBefore(bEnd) && bStart.isBefore(aEnd);
    }

    private Appointment findAppointment(Integer id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> AppointmentException.notFound("No appointment found with id " + id));
    }

    private User findUser(Integer id, String label) {
        return userRepository.findById(id)
                .orElseThrow(() -> AppointmentException.notFound("No " + label + " found with id " + id));
    }

    private User findEmployee(Integer id) {
        User employee = findUser(id, "employee");
        if (employee.getRole() != Role.EMPLOYEE) {
            throw AppointmentException.badRequest("User " + id + " is not an employee");
        }
        return employee;
    }
}
