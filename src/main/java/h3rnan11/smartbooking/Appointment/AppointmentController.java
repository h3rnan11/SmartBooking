package h3rnan11.smartbooking.Appointment;

import h3rnan11.smartbooking.DTO.DtoAppointments;
import h3rnan11.smartbooking.Role.Role;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/smart-booking/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping("/me")
    public List<DtoAppointments> getAppointmentsFromUser(Authentication authentication) {
        String email = authentication.getName();
        Role role = authentication.getAuthorities().stream()
                .findFirst()
                .map(a -> Role.valueOf(a.getAuthority().replace("ROLE_", "")))
                .orElseThrow();

        return appointmentService.getAppointments(email, role);
    }

    @ExceptionHandler(AppointmentException.class)
    public ResponseEntity<Map<String, String>> handleAppointmentException(AppointmentException e) {
        return ResponseEntity.status(e.getStatus()).body(Map.of("error", e.getMessage()));
    }
}
