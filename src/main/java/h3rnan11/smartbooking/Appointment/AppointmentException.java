package h3rnan11.smartbooking.Appointment;

import org.springframework.http.HttpStatus;

public class AppointmentException extends RuntimeException {

    private final HttpStatus status;

    public AppointmentException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public static AppointmentException notFound(String message) {
        return new AppointmentException(HttpStatus.NOT_FOUND, message);
    }

    public static AppointmentException badRequest(String message) {
        return new AppointmentException(HttpStatus.BAD_REQUEST, message);
    }

    public static AppointmentException conflict(String message) {
        return new AppointmentException(HttpStatus.CONFLICT, message);
    }

    public static AppointmentException forbidden(String message) {
        return new AppointmentException(HttpStatus.FORBIDDEN, message);
    }
}
