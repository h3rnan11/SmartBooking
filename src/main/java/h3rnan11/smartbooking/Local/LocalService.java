package h3rnan11.smartbooking.Local;

import h3rnan11.smartbooking.Appointment.AppointmentException;
import h3rnan11.smartbooking.DTO.DtoEmployeeResponse;
import h3rnan11.smartbooking.User.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LocalService {

    private final UserRepository userRepository;

    public LocalService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    public List<DtoEmployeeResponse> getAllEmployeesFromLocal(Integer id){
        List<DtoEmployeeResponse> employees = userRepository.getAllEmployeesFromLocal(id);
        if(employees.isEmpty())
            throw AppointmentException.notFound("Not employees found for the selected local");
        else
            return employees;
    }
}
