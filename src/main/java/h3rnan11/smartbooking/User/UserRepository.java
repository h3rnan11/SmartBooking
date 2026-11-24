package h3rnan11.smartbooking.User;

import h3rnan11.smartbooking.DTO.DtoEmployeeResponse;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);

    User findUserByEmail(String email);

    @Query("""
        select new h3rnan11.smartbooking.DTO.DtoEmployeeResponse(u.id, u.name, u.lastName)
        from User u
        join u.local l
            where u.local.id = :idLocal and u.role = h3rnan11.smartbooking.Role.Role.EMPLOYEE
        """)
    List<DtoEmployeeResponse> getAllEmployeesFromLocal(@Param("idLocal") Integer idLocal);

    User findEmployeeById(Integer id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("Select u from User where u.id = :id")
    Optional<User> findByIdForUpdate(@Param("id") Integer employeeId);
}
