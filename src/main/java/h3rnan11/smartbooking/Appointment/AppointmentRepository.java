package h3rnan11.smartbooking.Appointment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {

    List<Appointment> findByEmployeeIdAndDateAndStatusNot(Integer employeeId, LocalDate date, Status status);

    List<Appointment> findByClientIdOrderByDateAscStartTimeAsc(Integer clientId);

    List<Appointment> findByEmployeeIdOrderByDateAscStartTimeAsc(Integer employeeId);

    // Two ranges overlap when each one starts before the other ends. Cancelled appointments free their slot.
    @Query("""
            select count(a) > 0 from Appointment a
            where a.employee.id = :employeeId
              and a.date = :date
              and a.status <> h3rnan11.smartbooking.Appointment.Status.CANCELLED
              and a.startTime < :endTime
              and a.endTime > :startTime
              and (:excludeId is null or a.id <> :excludeId)
            """)
    boolean existsEmployeeOverlap(@Param("employeeId") Integer employeeId,
                                  @Param("date") LocalDate date,
                                  @Param("startTime") LocalTime startTime,
                                  @Param("endTime") LocalTime endTime,
                                  @Param("excludeId") Integer excludeId);

    @Query("""
            select count(a) > 0 from Appointment a
            where a.client.id = :clientId
              and a.date = :date
              and a.status <> h3rnan11.smartbooking.Appointment.Status.CANCELLED
              and a.startTime < :endTime
              and a.endTime > :startTime
              and (:excludeId is null or a.id <> :excludeId)
            """)
    boolean existsClientOverlap(@Param("clientId") Integer clientId,
                                @Param("date") LocalDate date,
                                @Param("startTime") LocalTime startTime,
                                @Param("endTime") LocalTime endTime,
                                @Param("excludeId") Integer excludeId);
}
