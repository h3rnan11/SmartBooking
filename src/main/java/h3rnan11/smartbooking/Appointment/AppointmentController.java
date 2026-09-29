package h3rnan11.smartbooking.Appointment;

import h3rnan11.smartbooking.DTO.DtoAppointments;
import h3rnan11.smartbooking.DTO.DtoEmployeeResponse;
import h3rnan11.smartbooking.DTO.DtoUpdateAppointment;
import h3rnan11.smartbooking.Role.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    @PatchMapping("{id}/canceled")
    public ResponseEntity<?> cancelAppointment(Authentication authentication, @PathVariable("id") Integer id){
        String email = authentication.getName();
        if(appointmentService.cancelAppointment(email, id)){
            return ResponseEntity.ok("Appointment with id:"+id+", cancelled correctly");
        }else
            return ResponseEntity.ofNullable("User not allow");
    }

    @PatchMapping("/updated")
    public ResponseEntity<?> updateAppointment(Authentication authentication, @RequestBody DtoUpdateAppointment appointment){
        String email = authentication.getName();
        appointmentService.updateAppointment(email, appointment);
        return ResponseEntity.ok("Appointment with id:"+appointment.id()+", updated correctly");
    }

    @GetMapping("/getEmployees/{id}")
    public ResponseEntity<List<DtoEmployeeResponse>> getAllEmployeesFromLocal(@PathVariable("id") Integer id){
        return ResponseEntity.of(Optional.ofNullable(appointmentService.getAllEmployeesFromLocal(id)));
    }
}
