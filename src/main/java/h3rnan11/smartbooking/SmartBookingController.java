package h3rnan11.smartbooking;

import h3rnan11.smartbooking.DTO.DtoLogin;
import h3rnan11.smartbooking.DTO.DtoNewUser;
import h3rnan11.smartbooking.User.User;
import h3rnan11.smartbooking.User.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/smart-booking")
public class SmartBookingController {

    @Autowired
    private UserService userService;

    @PostMapping("/newUser")
    public ResponseEntity<String> newUser(@RequestBody DtoNewUser dtoNewUser){
        if(userService.newUser(dtoNewUser)){
            return ResponseEntity.ok("User saved correctly");
        }
        return ResponseEntity.ofNullable("Body does not match requirements " + dtoNewUser);
    }

    @PostMapping("/logIn")
    public ResponseEntity<?> logIn(@RequestBody DtoLogin dtoLogin){
        return userService.logIn(dtoLogin);
    }
}
