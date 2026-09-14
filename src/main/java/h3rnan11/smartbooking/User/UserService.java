package h3rnan11.smartbooking.User;

import h3rnan11.smartbooking.DTO.DtoNewUser;
import h3rnan11.smartbooking.Role.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
}
