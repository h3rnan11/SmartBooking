package h3rnan11.smartbooking.EmployeeSchedule;

import h3rnan11.smartbooking.User.User;
import jakarta.persistence.*;

import java.time.LocalTime;
import java.util.ArrayList;
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

    // ISO day numbers: 1 = Monday ... 7 = Sunday (java.time.DayOfWeek#getValue)
    @ElementCollection(fetch = FetchType.EAGER)
    private List<Integer> dayOfWeek = new ArrayList<>();
    private LocalTime startTime;
    private LocalTime endTime;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public User getEmployee() {
        return employee;
    }

    public void setEmployee(User employee) {
        this.employee = employee;
    }

    public List<Integer> getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(List<Integer> dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }
}
