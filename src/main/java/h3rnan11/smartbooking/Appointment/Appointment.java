package h3rnan11.smartbooking.Appointment;

import h3rnan11.smartbooking.Service.Service;
import h3rnan11.smartbooking.User.User;
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

    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToOne
    @JoinColumn(name = "id_client")
    private User idClient;

    @ManyToOne
    @JoinColumn(name = "id_employee")
    private User idEmployee;

    // Nullable so existing rows keep working after ddl-auto adds the column.
    @ManyToOne
    @JoinColumn(name = "id_service")
    private Service service;


    /**
     * When a service is set, end_time is always start_time + service duration.
     * Appointments without a service (legacy rows) keep their stored end_time.
     */
    @PrePersist
    @PreUpdate
    void deriveEndTime() {
        if (service == null || start_time == null || service.getDurationMinutes() == null) {
            return;
        }
        end_time = Time.valueOf(start_time.toLocalTime().plusMinutes(service.getDurationMinutes()));
    }

    public Integer getId() {
        return id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Time getStartTime() {
        return start_time;
    }

    public void setStartTime(Time startTime) {
        this.start_time = startTime;
        deriveEndTime();
    }

    public Time getEndTime() {
        return end_time;
    }

    public void setEndTime(Time endTime) {
        this.end_time = endTime;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public User getIdClient() {
        return idClient;
    }

    public void setIdClient(User idClient) {
        this.idClient = idClient;
    }

    public User getIdEmployee() {
        return idEmployee;
    }

    public void setIdEmployee(User idEmployee) {
        this.idEmployee = idEmployee;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
        deriveEndTime();
    }
}
