package h3rnan11.smartbooking;

import h3rnan11.smartbooking.DTO.DtoNewUser;
import h3rnan11.smartbooking.User.User;
import h3rnan11.smartbooking.User.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
