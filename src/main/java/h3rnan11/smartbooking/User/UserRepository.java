package h3rnan11.smartbooking.User;

import h3rnan11.smartbooking.DTO.DtoEmployeeResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);

    User findUserByEmail(String email);

    @Query("""
        select new h3rnan11.smartbooking.DTO.DtoEmployeeResponse.java(u.id, u.name, u.lastName)
        from User u
        join u.local l
            where l.id = :idLocal
        """)
    List<DtoEmployeeResponse> getAllEmployeesFromLocal(@Param("idLocal") Integer idLocal);
}
