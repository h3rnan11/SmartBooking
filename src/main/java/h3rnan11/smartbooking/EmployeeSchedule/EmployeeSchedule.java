package h3rnan11.smartbooking.EmployeeSchedule;

import h3rnan11.smartbooking.User.User;
import jakarta.persistence.*;

import java.sql.Time;
import java.util.List;

@Entity
@Table(name = "employee_schedule")
public class EmployeeSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_employee")
    private User employee;

    @ElementCollection
    private List<Integer> day_of_week;
    private Time start_time;
    private Time end_time;

}
