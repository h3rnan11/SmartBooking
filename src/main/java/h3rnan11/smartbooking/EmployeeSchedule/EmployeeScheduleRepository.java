package h3rnan11.smartbooking.EmployeeSchedule;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalTime;

public interface EmployeeScheduleRepository extends JpaRepository<EmployeeSchedule, Integer> {

    @Query("""
    select count(es) > 0 from EmployeeSchedule es
    join es.dayOfWeek d
    where es.employee.id = :employeeId
      and d = :dayOfWeek
      and es.startTime <= :startTime
      and es.endTime >= :endTime
""")
    boolean existsSlotForAppointment(@Param("employeeId") Integer employeeId,
                                     @Param("dayOfWeek") Integer dayOfWeek,
                                     @Param("startTime") LocalTime startTime,
                                     @Param("endTime") LocalTime endTime);
}
