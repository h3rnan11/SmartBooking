package h3rnan11.smartbooking.EmployeeSchedule;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmployeeScheduleRepository extends JpaRepository<EmployeeSchedule, Integer> {

    List<EmployeeSchedule> findByEmployeeId(Integer employeeId);
}
