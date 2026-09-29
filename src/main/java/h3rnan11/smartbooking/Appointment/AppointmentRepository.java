package h3rnan11.smartbooking.Appointment;

import h3rnan11.smartbooking.DTO.DtoAppointments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {


    Appointment getAppointmentById(Integer id);

    @Query("""
    select new h3rnan11.smartbooking.DTO.DtoAppointments(a.id, a.client.id, a.employee.id, a.date, a.startTime, l.name, l.location)
    from Appointment a
    join a.employee.local l
    where a.client.email = :email
        and a.status <> h3rnan11.smartbooking.Appointment.Status.CANCELLED
        and (a.date > :today or (a.date = :today and a.startTime >= :now))
    order by a.date asc, a.startTime asc
""")
    List<DtoAppointments> getAppointmentsFromClient(@Param("email") String email,
                                                     @Param("today") LocalDate today,
                                                     @Param("now") LocalTime now);


    @Query("""
    select new h3rnan11.smartbooking.DTO.DtoAppointments(a.id, a.client.id, a.employee.id, a.date, a.startTime, l.name, l.location)
    from Appointment a
    join a.employee.local l
    where a.employee.email = :email
        and a.status <> h3rnan11.smartbooking.Appointment.Status.CANCELLED
        and (a.date > :today or (a.date = :today and a.startTime >= :now))
    order by a.date asc, a.startTime asc
""")
    List<DtoAppointments> getAppointmentsFromEmployee(@Param("email") String email,
                                                       @Param("today") LocalDate today,
                                                       @Param("now") LocalTime now);


    @Query("""
    select new h3rnan11.smartbooking.DTO.DtoAppointments(a.id, a.client.id, a.employee.id, a.date, a.startTime, l.name, l.location)
    from Appointment a
    join a.employee.local l
    where l.owner.email = :email
        and a.status <> h3rnan11.smartbooking.Appointment.Status.CANCELLED
        and (a.date > :today or (a.date = :today and a.startTime >= :now))
    order by a.date asc, a.startTime asc
""")
    List<DtoAppointments> getAppointmentsFromOwner(@Param("email") String email,
                                                    @Param("today") LocalDate today,
                                                    @Param("now") LocalTime now);

}
