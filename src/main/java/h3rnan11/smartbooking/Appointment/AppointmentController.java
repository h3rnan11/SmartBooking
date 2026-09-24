package h3rnan11.smartbooking.Appointment;

import h3rnan11.smartbooking.DTO.DtoAppointmentRequest;
import h3rnan11.smartbooking.DTO.DtoAppointmentResponse;
import h3rnan11.smartbooking.DTO.DtoAppointmentUpdate;
import h3rnan11.smartbooking.DTO.DtoTimeSlot;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/smart-booking/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping("/{id}")
    public DtoAppointmentResponse getById(@PathVariable Integer id) {
        return appointmentService.getById(id);
    }

    @GetMapping(params = "clientId")
    public List<DtoAppointmentResponse> getByClient(@RequestParam Integer clientId) {
        return appointmentService.getByClient(clientId);
    }

    @GetMapping(params = "employeeId")
    public List<DtoAppointmentResponse> getByEmployee(@RequestParam Integer employeeId) {
        return appointmentService.getByEmployee(employeeId);
    }

    @GetMapping("/available-slots")
    public List<DtoTimeSlot> getAvailableSlots(@RequestParam Integer employeeId,
                                               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                               @RequestParam(required = false) Integer duration) {
        return appointmentService.getAvailableSlots(employeeId, date, duration);
    }

    @PostMapping
    public ResponseEntity<DtoAppointmentResponse> create(@RequestBody DtoAppointmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.create(request));
    }

    @PutMapping("/{id}")
    public DtoAppointmentResponse update(@PathVariable Integer id, @RequestBody DtoAppointmentUpdate request) {
        return appointmentService.update(id, request);
    }

    @PatchMapping("/{id}/cancel")
    public DtoAppointmentResponse cancel(@PathVariable Integer id) {
        return appointmentService.cancel(id);
    }

    @ExceptionHandler(AppointmentException.class)
    public ResponseEntity<Map<String, String>> handleAppointmentException(AppointmentException e) {
        return ResponseEntity.status(e.getStatus()).body(Map.of("error", e.getMessage()));
    }
}
