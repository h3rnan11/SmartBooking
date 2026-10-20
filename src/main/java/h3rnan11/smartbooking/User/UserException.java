package h3rnan11.smartbooking.User;

import org.springframework.http.HttpStatus;

public class UserException extends RuntimeException {


    private final HttpStatus status;

    public UserException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus(){
        return status;
    }

    public static UserException notFound(String message){
        return new UserException(HttpStatus.NOT_FOUND, message);
    }

    public static UserException forbidden(String message){
        return new UserException(HttpStatus.FORBIDDEN, message);
    }

    public static UserException badRequest(String message){
        return new UserException(HttpStatus.BAD_REQUEST, message);
    }

    public static UserException conflict(String message){
        return new UserException(HttpStatus.CONFLICT, message);
    }

    public static UserException unauthorized(String message){
        return new UserException(HttpStatus.UNAUTHORIZED, message);
    }
}
