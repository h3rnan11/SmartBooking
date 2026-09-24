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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.*;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    // Thursday 2026-09-24 at 12:00
    private static final LocalDate TODAY = LocalDate.of(2026, 9, 24);
    private static final LocalDate MONDAY = LocalDate.of(2026, 9, 28);
    private static final Clock CLOCK =
            Clock.fixed(TODAY.atTime(12, 0).atZone(ZoneId.of("UTC")).toInstant(), ZoneId.of("UTC"));

    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private EmployeeScheduleRepository scheduleRepository;
    @Mock
    private UserRepository userRepository;

    private AppointmentService service;
    private User client;
    private User employee;

    @BeforeEach
    void setUp() {
        service = new AppointmentService(appointmentRepository, scheduleRepository, userRepository, CLOCK);
        client = user(5, Role.CLIENT);
        employee = user(3, Role.EMPLOYEE);
    }

    // ---------- create ----------

    @Test
    void createSavesPendingAppointmentWhenSlotIsFree() {
        givenUsers();
        givenSchedule(schedule(List.of(1, 2, 3, 4, 5), "09:00", "13:00"));
        when(appointmentRepository.save(any())).thenAnswer(inv -> {
            Appointment a = inv.getArgument(0);
            a.setId(42);
            return a;
        });

        DtoAppointmentResponse res = service.create(request(MONDAY, "10:00", "10:30"));

        assertEquals(42, res.id());
        assertEquals(Status.PENDING, res.status());
        assertEquals(5, res.clientId());
        assertEquals(3, res.employeeId());
        assertEquals(LocalTime.of(10, 0), res.startTime());
    }

    @Test
    void createRejectsOverlapWithEmployeeAppointment() {
        givenUsers();
        givenSchedule(schedule(List.of(1), "09:00", "13:00"));
        when(appointmentRepository.existsEmployeeOverlap(eq(3), eq(MONDAY), any(), any(), isNull())).thenReturn(true);

        AppointmentException e = assertThrows(AppointmentException.class,
                () -> service.create(request(MONDAY, "10:00", "10:30")));

        assertEquals(HttpStatus.CONFLICT, e.getStatus());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void createRejectsOverlapWithClientAppointment() {
        givenUsers();
        givenSchedule(schedule(List.of(1), "09:00", "13:00"));
        when(appointmentRepository.existsClientOverlap(eq(5), eq(MONDAY), any(), any(), isNull())).thenReturn(true);

        AppointmentException e = assertThrows(AppointmentException.class,
                () -> service.create(request(MONDAY, "10:00", "10:30")));

        assertEquals(HttpStatus.CONFLICT, e.getStatus());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void createRejectsTimeOutsideSchedule() {
        givenUsers();
        givenSchedule(schedule(List.of(1), "09:00", "13:00"));

        AppointmentException e = assertThrows(AppointmentException.class,
                () -> service.create(request(MONDAY, "12:45", "13:15")));

        assertEquals(HttpStatus.CONFLICT, e.getStatus());
    }

    @Test
    void createRejectsDayTheEmployeeDoesNotWork() {
        givenUsers();
        givenSchedule(schedule(List.of(2, 3), "09:00", "13:00"));

        assertThrows(AppointmentException.class, () -> service.create(request(MONDAY, "10:00", "10:30")));
    }

    @Test
    void createRejectsPastTimes() {
        givenUsers();

        AppointmentException e = assertThrows(AppointmentException.class,
                () -> service.create(request(TODAY, "11:00", "11:30")));

        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
    }

    @Test
    void createRejectsInvertedRange() {
        AppointmentException e = assertThrows(AppointmentException.class,
                () -> service.create(request(MONDAY, "11:00", "10:30")));

        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
    }

    @Test
    void createRejectsUserThatIsNotAnEmployee() {
        when(userRepository.findById(5)).thenReturn(Optional.of(client));
        when(userRepository.findById(6)).thenReturn(Optional.of(user(6, Role.CLIENT)));

        DtoAppointmentRequest req = new DtoAppointmentRequest(5, 6, MONDAY, LocalTime.of(10, 0), LocalTime.of(10, 30));
        AppointmentException e = assertThrows(AppointmentException.class, () -> service.create(req));

        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
    }

    @Test
    void createReturnsNotFoundForUnknownClient() {
        when(userRepository.findById(5)).thenReturn(Optional.empty());

        AppointmentException e = assertThrows(AppointmentException.class,
                () -> service.create(request(MONDAY, "10:00", "10:30")));

        assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
    }

    // ---------- update ----------

    @Test
    void updateMovesAppointmentAndExcludesItselfFromOverlapCheck() {
        Appointment existing = appointment(7, Status.CONFIRMED, MONDAY, "10:00", "10:30");
        when(appointmentRepository.findById(7)).thenReturn(Optional.of(existing));
        givenSchedule(schedule(List.of(1), "09:00", "13:00"));
        when(appointmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        DtoAppointmentResponse res = service.update(7,
                new DtoAppointmentUpdate(null, MONDAY, LocalTime.of(10, 15), LocalTime.of(10, 45)));

        assertEquals(LocalTime.of(10, 15), res.startTime());
        assertEquals(LocalTime.of(10, 45), res.endTime());
        verify(appointmentRepository).existsEmployeeOverlap(3, MONDAY, LocalTime.of(10, 15), LocalTime.of(10, 45), 7);
        verify(appointmentRepository).existsClientOverlap(5, MONDAY, LocalTime.of(10, 15), LocalTime.of(10, 45), 7);
    }

    @Test
    void updateRejectsOverlap() {
        Appointment existing = appointment(7, Status.PENDING, MONDAY, "10:00", "10:30");
        when(appointmentRepository.findById(7)).thenReturn(Optional.of(existing));
        givenSchedule(schedule(List.of(1), "09:00", "13:00"));
        when(appointmentRepository.existsEmployeeOverlap(any(), any(), any(), any(), eq(7))).thenReturn(true);

        AppointmentException e = assertThrows(AppointmentException.class, () -> service.update(7,
                new DtoAppointmentUpdate(null, MONDAY, LocalTime.of(11, 0), LocalTime.of(11, 30))));

        assertEquals(HttpStatus.CONFLICT, e.getStatus());
        assertEquals(LocalTime.of(10, 0), existing.getStartTime());
    }

    @Test
    void updateRejectsCancelledAppointment() {
        when(appointmentRepository.findById(7))
                .thenReturn(Optional.of(appointment(7, Status.CANCELLED, MONDAY, "10:00", "10:30")));

        AppointmentException e = assertThrows(AppointmentException.class, () -> service.update(7,
                new DtoAppointmentUpdate(null, MONDAY, LocalTime.of(11, 0), LocalTime.of(11, 30))));

        assertEquals(HttpStatus.CONFLICT, e.getStatus());
    }

    @Test
    void updateReturnsNotFoundForUnknownAppointment() {
        when(appointmentRepository.findById(99)).thenReturn(Optional.empty());

        AppointmentException e = assertThrows(AppointmentException.class, () -> service.update(99,
                new DtoAppointmentUpdate(null, MONDAY, LocalTime.of(11, 0), LocalTime.of(11, 30))));

        assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
    }

    // ---------- cancel ----------

    @Test
    void cancelMarksAppointmentCancelled() {
        Appointment existing = appointment(7, Status.CONFIRMED, MONDAY, "10:00", "10:30");
        when(appointmentRepository.findById(7)).thenReturn(Optional.of(existing));
        when(appointmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertEquals(Status.CANCELLED, service.cancel(7).status());
    }

    @Test
    void cancelRejectsCompletedAppointment() {
        when(appointmentRepository.findById(7))
                .thenReturn(Optional.of(appointment(7, Status.COMPLETED, MONDAY, "10:00", "10:30")));

        assertThrows(AppointmentException.class, () -> service.cancel(7));
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void cancelRejectsAlreadyCancelledAppointment() {
        when(appointmentRepository.findById(7))
                .thenReturn(Optional.of(appointment(7, Status.CANCELLED, MONDAY, "10:00", "10:30")));

        assertThrows(AppointmentException.class, () -> service.cancel(7));
    }

    // ---------- available slots ----------

    @Test
    void availableSlotsSkipBookedTimes() {
        when(userRepository.findById(3)).thenReturn(Optional.of(employee));
        givenSchedule(schedule(List.of(1), "09:00", "11:00"));
        when(appointmentRepository.findByEmployeeIdAndDateAndStatusNot(3, MONDAY, Status.CANCELLED))
                .thenReturn(List.of(appointment(1, Status.CONFIRMED, MONDAY, "09:30", "10:15")));

        List<DtoTimeSlot> slots = service.getAvailableSlots(3, MONDAY, 30);

        assertEquals(List.of(slot("09:00", "09:30"), slot("10:30", "11:00")), slots);
    }

    @Test
    void availableSlotsCombineSeveralScheduleBlocks() {
        when(userRepository.findById(3)).thenReturn(Optional.of(employee));
        givenSchedule(
                schedule(List.of(1), "15:00", "16:00"),
                schedule(List.of(1), "09:00", "10:00"),
                schedule(List.of(2), "12:00", "13:00"));

        List<DtoTimeSlot> slots = service.getAvailableSlots(3, MONDAY, 60);

        assertEquals(List.of(slot("09:00", "10:00"), slot("15:00", "16:00")), slots);
    }

    @Test
    void availableSlotsDropPartialSlotAtEndOfBlock() {
        when(userRepository.findById(3)).thenReturn(Optional.of(employee));
        givenSchedule(schedule(List.of(1), "09:00", "10:10"));

        List<DtoTimeSlot> slots = service.getAvailableSlots(3, MONDAY, 30);

        assertEquals(List.of(slot("09:00", "09:30"), slot("09:30", "10:00")), slots);
    }

    @Test
    void availableSlotsTodayOnlyReturnFutureTimes() {
        when(userRepository.findById(3)).thenReturn(Optional.of(employee));
        givenSchedule(schedule(List.of(4), "11:00", "13:00"));

        List<DtoTimeSlot> slots = service.getAvailableSlots(3, TODAY, null);

        assertEquals(List.of(slot("12:00", "12:30"), slot("12:30", "13:00")), slots);
    }

    @Test
    void availableSlotsForPastDateAreEmpty() {
        when(userRepository.findById(3)).thenReturn(Optional.of(employee));

        assertTrue(service.getAvailableSlots(3, TODAY.minusDays(1), 30).isEmpty());
    }

    @Test
    void availableSlotsRejectNonPositiveDuration() {
        assertThrows(AppointmentException.class, () -> service.getAvailableSlots(3, MONDAY, 0));
    }

    // ---------- helpers ----------

    private void givenUsers() {
        when(userRepository.findById(5)).thenReturn(Optional.of(client));
        when(userRepository.findById(3)).thenReturn(Optional.of(employee));
    }

    private void givenSchedule(EmployeeSchedule... schedules) {
        when(scheduleRepository.findByEmployeeId(3)).thenReturn(List.of(schedules));
    }

    private static DtoAppointmentRequest request(LocalDate date, String start, String end) {
        return new DtoAppointmentRequest(5, 3, date, LocalTime.parse(start), LocalTime.parse(end));
    }

    private static DtoTimeSlot slot(String start, String end) {
        return new DtoTimeSlot(LocalTime.parse(start), LocalTime.parse(end));
    }

    private static User user(int id, Role role) {
        User u = new User();
        u.setId(id);
        u.setRole(role);
        return u;
    }

    private EmployeeSchedule schedule(List<Integer> days, String start, String end) {
        EmployeeSchedule s = new EmployeeSchedule();
        s.setEmployee(employee);
        s.setDayOfWeek(days);
        s.setStartTime(LocalTime.parse(start));
        s.setEndTime(LocalTime.parse(end));
        return s;
    }

    private Appointment appointment(int id, Status status, LocalDate date, String start, String end) {
        Appointment a = new Appointment();
        a.setId(id);
        a.setStatus(status);
        a.setDate(date);
        a.setStartTime(LocalTime.parse(start));
        a.setEndTime(LocalTime.parse(end));
        a.setClient(client);
        a.setEmployee(employee);
        return a;
    }
}
