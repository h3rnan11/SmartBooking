package h3rnan11.smartbooking.User;

import h3rnan11.smartbooking.Appointment.AppointmentException;
import h3rnan11.smartbooking.DTO.DtoLogin;
import h3rnan11.smartbooking.DTO.DtoNewUser;
import h3rnan11.smartbooking.Local.Local;
import h3rnan11.smartbooking.Role.Role;
import h3rnan11.smartbooking.Security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtService jwtService;

    UserService service;
    Local local;
    Local localTest;
    User client;
    User employee;
    User newUser;
    User owner;

    @BeforeEach
    void setUp(){
        service = new UserService(userRepository, passwordEncoder, jwtService);

        local = new Local();
        local.setId(1);
        localTest = new Local();
        localTest.setId(2);
        client = user(1, "ana@test.com", Role.CLIENT);
        employee = user(2, "lucia@test.com", Role.EMPLOYEE);
        newUser = user(3, "clietn@test.com", Role.CLIENT);
        owner = user (4, "owner@test.com", Role.OWNER);


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
    void new_emailAlreadyInUse_Conflict(){
        // Given
        when(userRepository.findByEmail("ana@test.com")).thenReturn(Optional.of(any()));
        var dto = new DtoNewUser("ana","test", "ana@test.com", "123456");

        // Then
        UserException ex = assertThrows(UserException.class,
                () -> service.newUser(dto));

        // When
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        verify(userRepository, never()).save(any());
    }

    @Test
    void logIn_emailNotInUse_notFound(){
        // Given
        when(userRepository.findByEmail("ana@test.com")).thenReturn(Optional.ofNullable(any()));
        var dto = new DtoLogin("ana@test.com","123456");

        // Then
        UserException ex = assertThrows(UserException.class,
                () -> service.logIn(dto));

        // When
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        verify(userRepository, never()).save(any());
    }

    @Test
    void logIn_wrongPassword_unauthorized(){
        // Given
        when(userRepository.findByEmail("ana@test.com")).thenReturn(Optional.of(client));
        var dto = new DtoLogin("ana@test.com","123456");
        when(passwordEncoder.matches(dto.password(), passwordEncoder.encode("123456"))).thenReturn(false);

        // Then
        UserException ex = assertThrows(UserException.class,
                () -> service.logIn(dto));

        // When
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
        verify(userRepository, never()).save(any());
    }

    @Test
    void update_emailNotInUse_conflict(){
        // Given
        when(userRepository.findByEmail("ana@test.com")).thenReturn(Optional.ofNullable(any()));
        var dto = new DtoNewUser("ana","test","ana@tets.com", "123456");

        // Then
        UserException ex = assertThrows(UserException.class,
                () -> service.updateUser("ana@test.com",dto));

        // When
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        verify(userRepository, never()).save(any());
    }

    @Test
    void delete_userDoesNotExists_conflict(){
        // Given
        when(userRepository.findByEmail("ana@test.com")).thenReturn(Optional.ofNullable(any()));

        // Then
        UserException ex = assertThrows(UserException.class,
                () -> service.deleteUser("ana@test.com"));

        // When
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        verify(userRepository, never()).save(any());
    }

    @Test
    void setEmployeeToLocal_ownerIsEmpty_notFound(){
        // Given
        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.empty());

        // Then
        UserException ex = assertThrows(UserException.class,
                () -> service.setEmployeeToLocal("owner@test.com", 1));

        // When
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        verify(userRepository, never()).save(any());
    }

    @Test
    void setEmployeeToLocal_userNotAllow_forbidden(){
        // Given
        when(userRepository.findByEmail("client@test.com")).thenReturn(Optional.of(client));

        // Then
        UserException ex = assertThrows(UserException.class,
                () -> service.setEmployeeToLocal("client@test.com", 2));

        // When
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
        verify(userRepository, never()).save(any());
    }

    @Test
    void setEmployeeToLocal_employeeIsEmpty_notFound(){
        // Given
        when(userRepository.findById(2)).thenReturn(Optional.empty());

        // Then
        UserException ex = assertThrows(UserException.class,
                () -> service.setEmployeeToLocal("client@test.com", 2));

        // When
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        verify(userRepository, never()).save(any());
    }

    @Test
    void setEmployeeToLocal_userIsNotAnEmployee_notFound(){
        // Given
        when(userRepository.findById(1)).thenReturn(Optional.of(client));

        // Then
        UserException ex = assertThrows(UserException.class,
                () -> service.setEmployeeToLocal("client@test.com", 1));

        // When
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        verify(userRepository, never()).save(any());
    }

    @Test
    void setEmployeeToLocal_employeeIsAlreadyAssignedToALocal_conflict(){
        // Given
        when(userRepository.findById(2)).thenReturn(Optional.of(employee));

        // Then
        UserException ex = assertThrows(UserException.class,
                () -> service.setEmployeeToLocal("owner@test.com", 2));

        // When
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        verify(userRepository, never()).save(any());
    }
}
