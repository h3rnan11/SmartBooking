package h3rnan11.smartbooking.Appointment;

import h3rnan11.smartbooking.DTO.DtoNewAppointment;
import h3rnan11.smartbooking.DTO.DtoUpdateAppointment;
import h3rnan11.smartbooking.EmployeeSchedule.EmployeeScheduleRepository;
import h3rnan11.smartbooking.Local.Local;
import h3rnan11.smartbooking.Role.Role;
import h3rnan11.smartbooking.Service.Service;
import h3rnan11.smartbooking.Service.ServiceRepository;
import h3rnan11.smartbooking.User.User;
import h3rnan11.smartbooking.User.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.*;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class AppointmentServiceTest {

    @Mock AppointmentRepository appointmentRepository;
    @Mock UserRepository userRepository;
    @Mock ServiceRepository serviceRepository;
    @Mock EmployeeScheduleRepository employeeScheduleRepository;

    Clock clock = Clock.fixed(Instant.parse("2026-10-01T10:00:00Z"), ZoneOffset.UTC);

    AppointmentService service;

    Local local;
    User client;
    User employee;
    Appointment apt;
    Service serviceType;
    Service notService;
    User caller;
    User notEmployee;
    Local localTest;

    @BeforeEach
    void setUp(){
        service = new AppointmentService(appointmentRepository, userRepository,
                clock, serviceRepository, employeeScheduleRepository);

        local = new Local();
        localTest = new Local();
        client = user(1, "ana@test.com", Role.CLIENT);
        employee = user(2, "lucia@test.com", Role.EMPLOYEE);
        caller = user(3, "role@test.com", Role.EMPLOYEE);
        notEmployee = user(4, "notEmployee@gmail.com", Role.OWNER);

        serviceType = new Service();
        serviceType.setLocal(local);
        serviceType.setId(1);
        serviceType.setDurationMinutes(30);
        serviceType.setName("Hair cut");
        serviceType.setPrice(BigDecimal.valueOf(14.5));


        notService = new Service();
        notService.setLocal(local);
        notService.setId(2);
        notService.setDurationMinutes(30);
        notService.setName("Hair cut");
        notService.setPrice(BigDecimal.valueOf(14.5));

        apt = new Appointment();
        apt.setId(10);
        apt.setClient(client);
        apt.setEmployee(employee);
        apt.setStatus(Status.CONFIRMED);
        apt.setDate(LocalDate.of(2026, 10, 5));
        apt.setStartTime(LocalTime.of(10, 0));
        apt.setEndTime(LocalTime.of(10, 30));
        apt.setService(serviceType);
    }


    private User user(Integer id, String email, Role role){
        User u = new User();
        u.setId(id);
        u.setEmail(email);
        u.setRole(role);
        u.setLocal(local);
        return u;
    }

    @Test
    void update_userNotInvolved_throwsForbidden() {
        // Given
        when(appointmentRepository.findById(10)).thenReturn(Optional.of(apt));
        var dto = new DtoUpdateAppointment(null, LocalDate.of(2026, 10, 6), LocalTime.of(11, 0));

        // When
        AppointmentException ex = assertThrows(AppointmentException.class,
                () -> service.updateAppointment("intruso@test.com", dto, 10));

        // Then
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void update_validData_savesNewDateAndTime() {
        // Given
        when(appointmentRepository.findById(10)).thenReturn(Optional.of(apt));
        when(appointmentRepository.existsOverlap(any(), any(), any(), any(), any())).thenReturn(false);
        var dto = new DtoUpdateAppointment(null, LocalDate.of(2026, 10, 6), LocalTime.of(11, 0));

        // When
        service.updateAppointment("ana@test.com", dto, 10);

        // Then
        assertEquals(LocalDate.of(2026, 10, 6), apt.getDate());
        assertEquals(LocalTime.of(11, 0), apt.getStartTime());
        verify(appointmentRepository).save(apt);
    }

    @Test
    void update_appointmentNotFound_throwsNotFound(){
        // Given
        when(appointmentRepository.findById(99)).thenReturn(Optional.empty());
        var dto = new DtoUpdateAppointment(null, LocalDate.of(2026, 10, 6), LocalTime.of(11, 0));

        // When
        AppointmentException ex = assertThrows(AppointmentException.class,
                () -> service.updateAppointment("ana@test.com", dto, 99));

        // Then
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void update_employeeNotFound_throwsNotFound(){
        // Given
        when(userRepository.findById(99)).thenReturn(Optional.empty());
        when(appointmentRepository.findById(10)).thenReturn(Optional.of(apt));
        var dto = new DtoUpdateAppointment(99, LocalDate.of(2026, 10, 6), LocalTime.of(11, 0));

        // When
        AppointmentException ex = assertThrows(AppointmentException.class,
                () -> service.updateAppointment("ana@test.com", dto, 10));

        // Then
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void update_employeeIdIsNotFromAnEmployee_throwsBadRequest(){
        // Given
        User notEmployee = user(99, "notemployee@test.com", Role.CLIENT);
        when(appointmentRepository.findById(10)).thenReturn(Optional.of(apt));
        when(userRepository.findById(99)).thenReturn(Optional.of(notEmployee));
        var dto = new DtoUpdateAppointment(99, LocalDate.of(2026, 10, 6), LocalTime.of(11, 0));

        // When
        AppointmentException ex = assertThrows(AppointmentException.class,
                () -> service.updateAppointment("ana@test.com", dto, 10));

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        verify(appointmentRepository, never()).save(any());
    }

    @ParameterizedTest
    @EnumSource(value = Status.class, names = {"CANCELLED", "COMPLETED"})
    void update_appointmentNotModifiable_throwsConflict(Status status){
        // Given
        apt.setStatus(status);
        when(appointmentRepository.findById(10)).thenReturn(Optional.of(apt));
        var dto = new DtoUpdateAppointment(null,LocalDate.of(2026, 10, 6), LocalTime.of(11, 0));

        // When
        AppointmentException ex = assertThrows(AppointmentException.class,
                ()-> service.updateAppointment("ana@test.com", dto, 10));

        // Then
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void update_dateIsNull_throwsBadRequest(){
        // Given
        when(appointmentRepository.findById(10)).thenReturn(Optional.of(apt));
        var dto = new DtoUpdateAppointment(null, null, LocalTime.of(11, 0));

        // When
        AppointmentException ex = assertThrows(AppointmentException.class,
                () -> service.updateAppointment("ana@test.com", dto, 10));

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void update_startTimeIsNull_throwsBadRequest(){
        // Given
        when(appointmentRepository.findById(10)).thenReturn(Optional.of(apt));
        var dto = new DtoUpdateAppointment(null, LocalDate.of(2026, 10, 6), null);

        // When
        AppointmentException ex = assertThrows(AppointmentException.class,
                () -> service.updateAppointment("ana@test.com", dto, 10));

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void update_newDateTimeIsBeforeNow_throwsBadRequest(){
        // Given
        when(appointmentRepository.findById(10)).thenReturn(Optional.of(apt));
        var dto = new DtoUpdateAppointment(null, LocalDate.of(2026, 10, 1), LocalTime.of(10, 0));

        // When
        AppointmentException ex = assertThrows(AppointmentException.class,
                () -> service.updateAppointment("ana@test.com", dto, 10));

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void update_ExistsOverlap_throwsConflict(){
        // Given
        when(appointmentRepository.existsOverlap(any(), any(), any(), any(), any())).thenReturn(true);
        when(appointmentRepository.findById(10)).thenReturn(Optional.of(apt));
        var dto = new DtoUpdateAppointment(null, LocalDate.of(2026, 10, 6), LocalTime.of(10, 0));

        // When
        AppointmentException ex = assertThrows(AppointmentException.class,
                () -> service.updateAppointment("ana@test.com", dto, 10));

        // Then
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void update_newStartTime_checksOverlapWithRecalculatedEndTime(){
        // Given
        when(appointmentRepository.existsOverlap
                (2, LocalDate.of(2026,10,6),
                        LocalTime.of(12,0), LocalTime.of(12,30), 10)).thenReturn(false);
        when(appointmentRepository.findById(10)).thenReturn(Optional.of(apt));
        var dto = new DtoUpdateAppointment(null, LocalDate.of(2026, 10, 6), LocalTime.of(12, 0));


        // When
        service.updateAppointment("lucia@test.com", dto, 10);

        // Then
        assertEquals(apt.getDate(), dto.date());
        assertEquals(apt.getStartTime(), dto.startTime());
        assertEquals(apt.getEndTime(), dto.startTime().plusMinutes(30));
        verify(appointmentRepository).save(apt);
    }

    @Test
    void update_employeeCanUpdateAppointments_(){
        // Given
        when(appointmentRepository.existsOverlap(any(), any(), any(), any(), any())).thenReturn(false);
        when(appointmentRepository.findById(10)).thenReturn(Optional.of(apt));
        var dto = new DtoUpdateAppointment(null, LocalDate.of(2026, 10, 6), LocalTime.of(10, 0));

        // When
        service.updateAppointment("lucia@test.com", dto, 10);

        // Then
        assertEquals(dto.date(), apt.getDate());
        assertEquals(dto.startTime(), apt.getStartTime());
        verify(appointmentRepository).save(apt);
    }

    @Test
    void cancel_idNotFound_throwsNotFound(){
        // Given
        when(appointmentRepository.findById(10)).thenReturn(Optional.empty());

        // When
        AppointmentException ex = assertThrows(AppointmentException.class,
                () -> service.cancelAppointment("ana@test.com",10));

        // Then
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void cancel_notInvolvedUser_throwsForbidden(){
        // Given
        when(appointmentRepository.findById(10)).thenReturn(Optional.of(apt));

        // When
        AppointmentException ex = assertThrows(AppointmentException.class,
                () -> service.cancelAppointment("intruso@test.com",10));

        // Then
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
        verify(appointmentRepository, never()).save(any());
    }

    @ParameterizedTest
    @EnumSource(value = Status.class, names = {"CONFIRMED", "PENDING"})
    void cancel_clientCanCancelAppointment_setsStatusCancelled(Status status){
        // Given
        apt.setStatus(status);
        when(appointmentRepository.findById(10)).thenReturn(Optional.of(apt));

        // When
        service.cancelAppointment("ana@test.com", 10);

        // Then
        assertEquals(Status.CANCELLED, apt.getStatus());
        verify(appointmentRepository).save(apt);
    }

    @Test
    void cancel_EmployeeCanCancelAppointment_setsStatusCancelled(){
        // Given
        when(appointmentRepository.findById(10)).thenReturn(Optional.of(apt));

        // When
        service.cancelAppointment("lucia@test.com", 10);

        // Then
        assertEquals(Status.CANCELLED, apt.getStatus());
        verify(appointmentRepository).save(apt);
    }

    @ParameterizedTest
    @EnumSource(value = Status.class, names = {"COMPLETED", "CANCELLED"})
    void cancel_notValidStatus_throwsConflict(Status status){
        // Given
        apt.setStatus(status);
        when(appointmentRepository.findById(10)).thenReturn(Optional.of(apt));

        // When
        AppointmentException ex = assertThrows(AppointmentException.class,
                () -> service.cancelAppointment("lucia@test.com", 10));


        // Then
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void new_clientNotFound_throwsNotFound(){
        // Given
        when(userRepository.findByEmail("ana@test.com")).thenReturn(Optional.empty());
        DtoNewAppointment apt = new DtoNewAppointment(2,1, LocalDate.of(2026,10,6), LocalTime.of(10,30));

        // When
        AppointmentException ex = assertThrows(AppointmentException.class,
                () -> service.newAppointment("ana@test.com",apt));

        // Then
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        assertTrue(ex.getMessage().contains("client"));
        verify(appointmentRepository, never()).save(any());
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"EMPLOYEE", "OWNER", "ADMIN"})
    void new_userNotAllow_throwsForbidden(Role role){
        // Given
        caller.setRole(role);
        when(userRepository.findByEmail("role@test.com")).thenReturn(Optional.of(caller));
        DtoNewAppointment apt = new DtoNewAppointment(2,1, LocalDate.of(2026,10,6), LocalTime.of(10,30));

        // When
        AppointmentException ex = assertThrows(AppointmentException.class,
                () -> service.newAppointment("role@test.com",apt));

        // Then
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void new_employeeNotFound_throwsNotFound(){
        // Given
        when(userRepository.findByEmail("ana@test.com")).thenReturn(Optional.of(client));
        when(userRepository.findById(2)).thenReturn(Optional.empty());
        DtoNewAppointment apt = new DtoNewAppointment(2,1, LocalDate.of(2026,10,6), LocalTime.of(10,30));

        // When
        AppointmentException ex = assertThrows(AppointmentException.class,
                () -> service.newAppointment("ana@test.com",apt));

        // Then
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        assertTrue(ex.getMessage().contains("employee"));
        verify(appointmentRepository, never()).save(any());
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"OWNER", "ADMIN", "CLIENT"})
    void new_employeeIsNotAnEmployee_throwsBadRequest(Role role){
        // Given
        notEmployee.setRole(role);
        when(userRepository.findByEmail("ana@test.com")).thenReturn(Optional.of(client));
        when(userRepository.findById(2)).thenReturn(Optional.of(notEmployee));
        DtoNewAppointment apt = new DtoNewAppointment(2,1, LocalDate.of(2026,10,6), LocalTime.of(10,30));

        // When
        AppointmentException ex = assertThrows(AppointmentException.class,
                () -> service.newAppointment("ana@test.com",apt));

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void new_employeeIsNotFromThisLocal_throwsBadRequest(){
        // Given
        employee.setLocal(null);
        when(userRepository.findByEmail("ana@test.com")).thenReturn(Optional.of(client));
        when(userRepository.findById(2)).thenReturn(Optional.of(employee));
        DtoNewAppointment apt = new DtoNewAppointment(2,1, LocalDate.of(2026,10,6), LocalTime.of(10,30));

        // When
        AppointmentException ex = assertThrows(AppointmentException.class,
                () -> service.newAppointment("ana@test.com",apt));

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void new_serviceDoesNotExist_throwNotFound(){
        // Given
        employee.setLocal(local);
        when(userRepository.findByEmail("ana@test.com")).thenReturn(Optional.of(client));
        when(userRepository.findById(2)).thenReturn(Optional.of(employee));
        when(serviceRepository.findById(2)).thenReturn(Optional.empty());
        DtoNewAppointment apt = new DtoNewAppointment(2,2, LocalDate.of(2026,10,6), LocalTime.of(10,30));

        // When
        AppointmentException ex = assertThrows(AppointmentException.class,
                () -> service.newAppointment("ana@test.com", apt));

        // Then
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void new_serviceDoesNotExistInCurrentLocal_throwBadRequest(){
        // Given
        employee.setLocal(local);
        notService.setLocal(null);
        when(userRepository.findByEmail("ana@test.com")).thenReturn(Optional.of(client));
        when(userRepository.findById(2)).thenReturn(Optional.of(employee));
        when(serviceRepository.findById(2)).thenReturn(Optional.of(notService));
        DtoNewAppointment apt = new DtoNewAppointment(2,2, LocalDate.of(2026,10,6), LocalTime.of(10,30));

        // When
        AppointmentException ex = assertThrows(AppointmentException.class,
                () -> service.newAppointment("ana@test.com", apt));

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void new_employeeScheduleDoesNotMatch_throwBadRequest(){
        LocalDate aptDate = LocalDate.of(2026,10,6);
        int weekDay = aptDate.getDayOfWeek().getValue();
        LocalTime aptStartTime = LocalTime.of(10,30);
        LocalTime aptEndTime = aptStartTime.plusMinutes(serviceType.getDurationMinutes());

        // Given
        employee.setLocal(local);
        when(userRepository.findByEmail("ana@test.com")).thenReturn(Optional.of(client));
        when(userRepository.findById(2)).thenReturn(Optional.of(employee));
        when(serviceRepository.findById(1)).thenReturn(Optional.of(serviceType));
        when(employeeScheduleRepository.existsSlotForAppointment(2, weekDay, aptStartTime, aptEndTime )).thenReturn(false);
        DtoNewAppointment apt = new DtoNewAppointment(2,1, aptDate, aptStartTime);

        // When
        AppointmentException ex = assertThrows(AppointmentException.class,
                () -> service.newAppointment("ana@test.com", apt));

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void new_existsOverlap_throwConflict(){
        LocalDate aptDate = LocalDate.of(2026,10,6);
        int weekDay = aptDate.getDayOfWeek().getValue();
        LocalTime aptStartTime = LocalTime.of(10,30);
        LocalTime aptEndTime = aptStartTime.plusMinutes(serviceType.getDurationMinutes());

        // Given
        employee.setLocal(local);
        when(userRepository.findByEmail("ana@test.com")).thenReturn(Optional.of(client));
        when(userRepository.findById(2)).thenReturn(Optional.of(employee));
        when(serviceRepository.findById(1)).thenReturn(Optional.of(serviceType));
        when(employeeScheduleRepository.existsSlotForAppointment(2, weekDay, aptStartTime, aptEndTime )).thenReturn(true);
        when(appointmentRepository.existsOverlap(2, aptDate, aptStartTime, aptEndTime, null)).thenReturn(true);
        DtoNewAppointment apt = new DtoNewAppointment(2,1, aptDate, aptStartTime);

        // When
        AppointmentException ex = assertThrows(AppointmentException.class,
                () -> service.newAppointment("ana@test.com", apt));

        // Then
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void new_clientCanCreateNewAppointment_(){
        LocalDate aptDate = LocalDate.of(2026,10,6);
        int weekDay = aptDate.getDayOfWeek().getValue();
        LocalTime aptStartTime = LocalTime.of(10,30);
        LocalTime aptEndTime = aptStartTime.plusMinutes(serviceType.getDurationMinutes());

        // Given
        employee.setLocal(local);
        when(userRepository.findByEmail("ana@test.com")).thenReturn(Optional.of(client));
        when(userRepository.findById(2)).thenReturn(Optional.of(employee));
        when(serviceRepository.findById(1)).thenReturn(Optional.of(serviceType));
        when(employeeScheduleRepository.existsSlotForAppointment(2, weekDay, aptStartTime, aptEndTime )).thenReturn(true);
        when(appointmentRepository.existsOverlap(2, aptDate, aptStartTime, aptEndTime, null)).thenReturn(false);
        DtoNewAppointment apt = new DtoNewAppointment(2,1, aptDate, aptStartTime);

        // When
        Appointment newApt = service.newAppointment("ana@test.com", apt);

        // Then

        verify(appointmentRepository).save(newApt);
    }
}
