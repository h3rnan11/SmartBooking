package h3rnan11.smartbooking.Appointment;

import h3rnan11.smartbooking.DTO.DtoAppointments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {

    @Query("""
        select new h3rnan11.smartbooking.DTO.DtoAppointments(
        a.date,
        a.start_time,
        l.name,
        l.location
        ) from Appointment a
        inner join a.idEmployee u
        inner join u.local l
        where a.date > :appointment_date and a.start_time > :start_hour and a.idClient.id = :id

""")
    public List<DtoAppointments> AppointmetsFroClient
            (@Param("appointment_date") LocalDate date,
             @Param("start_hour") LocalTime startHour,
             @Param("id") Integer id);

    @Query("""
        select new h3rnan11.smartbooking.DTO.DtoAppointmets(
        a.date,
        a.start_time,
        l.name,
        l.location
        ) from Appointment a
        inner join a.idEmployee u
        inner join u.local l
        where a.date > :appointment_date and a.start_time > :start_hour and a.idEmployee.id = :id

""")
    public List<DtoAppointments> AppointmetsFroEmployee
            (@Param("appointment_date") LocalDate date,
             @Param("start_hour") LocalTime startHour,
             @Param("id") Integer id);
}
