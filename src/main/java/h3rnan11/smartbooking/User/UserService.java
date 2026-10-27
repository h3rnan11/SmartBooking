package h3rnan11.smartbooking.User;

import h3rnan11.smartbooking.DTO.DtoLogin;
import h3rnan11.smartbooking.DTO.DtoLoginResponse;
import h3rnan11.smartbooking.DTO.DtoNewUser;
import h3rnan11.smartbooking.Local.Local;
import h3rnan11.smartbooking.Local.LocalRepository;
import h3rnan11.smartbooking.Role.Role;
import h3rnan11.smartbooking.Security.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;
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
    public boolean setEmployeeToLocal(String ownerEmail, Integer employeeId){
        Optional<User> employee = userRepository.findById(employeeId);
        Optional<User> owner = userRepository.findByEmail(ownerEmail);

        if(owner.isEmpty()){
            throw UserException.notFound("No authentication");
        }

        if(!owner.get().getRole().equals(Role.OWNER)){
            throw UserException.forbidden("User is not allow to modify employees");
        }
        Local localFromOwner = owner.get().getLocal();

        if(employee.isEmpty()){
            throw UserException.notFound("Employee with id {"+employeeId+"} does not exists");
        }

        if(employee.get().getRole() != Role.EMPLOYEE){
            throw UserException.badRequest("The user given is not an employee");
        }

        if(employee.get().getLocal() != null){
            throw UserException.conflict("Current employee is assigned already to a local");
        }
        User newEmployee = employee.get();
        newEmployee.setLocal(localFromOwner);
        userRepository.save(newEmployee);

        return true;
    }
}
