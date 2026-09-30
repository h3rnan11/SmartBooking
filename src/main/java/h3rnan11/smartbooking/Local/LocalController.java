package h3rnan11.smartbooking.Local;

import h3rnan11.smartbooking.DTO.DtoEmployeeResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/smart-booking/locals")
public class LocalController {

    private final LocalService localService;

    public LocalController(LocalService localService){
        this.localService = localService;
    }

    @GetMapping("/{id}/employees")
    public ResponseEntity<List<DtoEmployeeResponse>> getAllEmployeesFromLocal(@PathVariable("id") Integer id){
        return ResponseEntity.of(Optional.ofNullable(localService.getAllEmployeesFromLocal(id)));
    }
}
