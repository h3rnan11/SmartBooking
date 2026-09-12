package h3rnan11.smartbooking.User;

import h3rnan11.smartbooking.Appointment.Appointment;
import h3rnan11.smartbooking.EmployeeSchedule.EmployeeSchedule;
import h3rnan11.smartbooking.Local.Local;
import h3rnan11.smartbooking.Role.Role;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name;
    private String lastName;
    private String email;
    private String password;

    @ManyToOne
    @JoinColumn(name = "id_role")
    private Role role;

    @ManyToOne
    @JoinColumn(name = "id_local")
    private Local local;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL)
    private List<EmployeeSchedule> schedules = new ArrayList<>();

    @OneToMany(mappedBy = "idClient", cascade = CascadeType.ALL)
    private List<Appointment> clientAppointments = new ArrayList<>();

    @OneToMany(mappedBy = "idEmployee", cascade = CascadeType.ALL)
    private List<Appointment> employeeAppointments = new ArrayList<>();

}
