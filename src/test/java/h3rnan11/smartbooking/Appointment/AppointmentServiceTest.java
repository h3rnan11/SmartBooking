package h3rnan11.smartbooking.Appointment;

import com.nimbusds.oauth2.sdk.http.HTTPRequestSender;
import h3rnan11.smartbooking.DTO.DtoUpdateAppointment;
import h3rnan11.smartbooking.Local.Local;
import h3rnan11.smartbooking.Role.Role;
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

import java.time.*;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class AppointmentServiceTest {

    @Mock AppointmentRepository appointmentRepository;
    @Mock UserRepository userRepository;

    Clock clock = Clock.fixed(Instant.parse("2026-10-01T10:00:00Z"), ZoneOffset.UTC);

    AppointmentService service;

    Local local;
    User client;
    User employee;
    Appointment apt;

    @BeforeEach
    void setUp(){
        service = new AppointmentService(appointmentRepository, userRepository, clock);

        local = new Local();
        client = user(1, "ana@test.com", Role.CLIENT);
        employee = user(2, "lucia@test.com", Role.EMPLOYEE);

        apt = new Appointment();
        apt.setId(10);
        apt.setClient(client);
        apt.setEmployee(employee);
        apt.setStatus(Status.CONFIRMED);
        apt.setDate(LocalDate.of(2026, 10, 5));
        apt.setStartTime(LocalTime.of(10, 0));
        apt.setEndTime(LocalTime.of(10, 30));
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
    void update_employeeCanUpdateAppointments_throwsConflict(){
        // Given
        when(appointmentRepository.existsOverlap(any(), any(), any(), any(), any())).thenReturn(false);
        when(appointmentRepository.findById(10)).thenReturn(Optional.of(apt));
        var dto = new DtoUpdateAppointment(null, LocalDate.of(2026, 10, 6), LocalTime.of(10, 0));

        // When
        service.updateAppointment("lucia@test.com", dto, 10);

        // Then
        assertEquals(apt.getDate(), dto.date());
        assertEquals(apt.getStartTime(), dto.startTime());
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

}
