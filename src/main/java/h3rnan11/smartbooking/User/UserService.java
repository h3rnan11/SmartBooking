package h3rnan11.smartbooking.User;

import h3rnan11.smartbooking.DTO.DtoLogin;
import h3rnan11.smartbooking.DTO.DtoLoginResponse;
import h3rnan11.smartbooking.DTO.DtoNewUser;
import h3rnan11.smartbooking.Role.Role;
import h3rnan11.smartbooking.Security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public Boolean newUser(DtoNewUser newUser){
        User u = new User();
        u.setName(newUser.name());
        u.setLastName(newUser.lastName());
        u.setEmail(newUser.email());
        u.setPassword(passwordEncoder.encode(newUser.password()));
        u.setRole(Role.CLIENT);

        try {
            userRepository.save(u);
            return true;
        } catch (DataIntegrityViolationException e) {
            return false;
        }
    }

    public ResponseEntity<?> logIn(DtoLogin dtoLogin){
        Optional<User> u = userRepository.findByEmail(dtoLogin.email());
        if(u.isPresent()){
            if(passwordEncoder.matches(dtoLogin.password(), u.get().getPassword())){
                String token = jwtService.generateToken(u.get().getEmail(), u.get().getRole().name());
                return ResponseEntity.ok(new DtoLoginResponse(token, u.get().getEmail(), u.get().getRole().name()));
            }else
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Wrong password");
        }else
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No user found with that email");
    }
}
