package h3rnan11.smartbooking.User;

import h3rnan11.smartbooking.DTO.DtoLogin;
import h3rnan11.smartbooking.DTO.DtoLoginResponse;
import h3rnan11.smartbooking.DTO.DtoNewUser;
import h3rnan11.smartbooking.Local.Local;
import h3rnan11.smartbooking.Local.LocalRepository;
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
    private final LocalRepository localRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService,
                       LocalRepository localRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.localRepository = localRepository;
    }

    public User newUser(DtoNewUser newUser){
        User u = new User();
        u.setName(newUser.name());
        u.setLastName(newUser.lastName());
        u.setEmail(newUser.email());
        Optional<User> doesUserAlreadyExists = userRepository.findByEmail(newUser.email());
        if(doesUserAlreadyExists.isPresent()){
            throw UserException.conflict("Email already in use");
        }
        u.setPassword(passwordEncoder.encode(newUser.password()));
        u.setRole(Role.CLIENT);
        return userRepository.save(u);
    }

    public ResponseEntity<?> logIn(DtoLogin dtoLogin){
        Optional<User> u = userRepository.findByEmail(dtoLogin.email());
        if(u.isPresent()){
            if(passwordEncoder.matches(dtoLogin.password(), u.get().getPassword())){
                String token = jwtService.generateToken(u.get().getEmail(), u.get().getRole().name());
                return ResponseEntity.ok(new DtoLoginResponse(token, u.get().getEmail(), u.get().getRole().name()));
            }else
                throw UserException.unauthorized("Wrong password");
        }else {
            throw UserException.notFound("No user found with that email");
        }
    }

    // UpdateUser
    // DeleteUser
    // SetEmployeeToLocal (if local = null -> employee is "unemployee")
    public boolean setEmployeeToLocal(Integer employeeId, Integer localId, String ownerEmail){
        Optional<User> employee = userRepository.findById(employeeId);
        Optional <Local> local = localRepository.findById(localId);
        Optional<User> owner = userRepository.findByEmail(ownerEmail);

        if(owner.isEmpty()){
            throw UserException.notFound("No authentication");
        }
        if(local.isEmpty()){
            throw UserException.notFound("No local found with id: " + localId);
        }
        if(!owner.get().getLocal().equals(local)){
            throw UserException.unauthorized("You are not authorized on this local");
        }

        if(employee.isEmpty()){
            throw UserException.notFound("Employee does not exists");
        }
        if(employee.get().getRole() != Role.EMPLOYEE){
            throw UserException.badRequest("The user given is not an employee");
        }




        return true;
    }
}
