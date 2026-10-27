package h3rnan11.smartbooking.User;

import h3rnan11.smartbooking.DTO.DtoLogin;
import h3rnan11.smartbooking.DTO.DtoNewUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/newUser")
    public ResponseEntity<String> newUser(@RequestBody DtoNewUser dtoNewUser){
        userService.newUser(dtoNewUser);
        return ResponseEntity.ok("User created correctly ");
    }

    @PostMapping("/logIn")
    public ResponseEntity<?> logIn(@RequestBody DtoLogin dtoLogin){

        return userService.logIn(dtoLogin);
    }

    @PostMapping("/me/{id}")
    public ResponseEntity<?> setLocalToEmployee(Authentication authentication, @PathVariable Integer employeeId){
        userService.setEmployeeToLocal(authentication.getName(), employeeId);
        return ResponseEntity.ok("");
    }

}
