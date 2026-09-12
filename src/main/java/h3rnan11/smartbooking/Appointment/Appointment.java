package h3rnan11.smartbooking.Appointment;

import h3rnan11.smartbooking.User.User;
import h3rnan11.smartbooking.Utils.Status;
import jakarta.persistence.*;

import java.sql.Time;
import java.util.Date;

@Entity
@Table(name = "appointments")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Date date;
    private Time start_time;
    private Time end_time;
    private Status status;

    @ManyToOne
    @JoinColumn(name = "id_client")
    private User idClient;

    @ManyToOne
    @JoinColumn(name = "id_employee")
    private User idEmployee;


}
