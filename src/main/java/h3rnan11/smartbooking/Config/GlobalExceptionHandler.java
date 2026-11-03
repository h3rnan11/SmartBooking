package h3rnan11.smartbooking.Config;

import h3rnan11.smartbooking.Appointment.AppointmentException;
import h3rnan11.smartbooking.User.UserException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppointmentException.class)
    public ResponseEntity<String>
    handleAppointmentException(AppointmentException ex) {
        return ResponseEntity.status(ex.getStatus()).body(ex.getMessage());
    }

    @ExceptionHandler(UserException.class)
    public ResponseEntity<String>
    handleUserException(UserException ex){
        return ResponseEntity.status(ex.getStatus()).body(ex.getMessage());
    }
}
