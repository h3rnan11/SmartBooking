package h3rnan11.smartbooking.Local;

import org.springframework.http.HttpStatus;

public class LocalException extends RuntimeException {
    public final HttpStatus status;

    public LocalException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public static LocalException empty(String message){
        return new LocalException(HttpStatus.FOUND, message);
    }

}
