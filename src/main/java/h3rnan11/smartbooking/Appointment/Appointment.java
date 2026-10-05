package h3rnan11.smartbooking.Appointment;

import h3rnan11.smartbooking.Service.Service;
import h3rnan11.smartbooking.User.User;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "appointments")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToOne
    @JoinColumn(name = "id_client")
    private User client;

    @ManyToOne
    @JoinColumn(name = "id_employee")
    private User employee;

    // Nullable so existing rows keep working after ddl-auto adds the column.
    @ManyToOne
    @JoinColumn(name = "id_service")
    private Service service;

    /**
     * When a service is set, endTime is always startTime + service duration.
     * Appointments without a service (legacy rows) keep their stored endTime.
     */
    @PrePersist
    @PreUpdate
    void deriveEndTime() {
        if (service == null || startTime == null || service.getDurationMinutes() == null) {
            return;
        }
        endTime = startTime.plusMinutes(service.getDurationMinutes());
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
        deriveEndTime();
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public User getClient() {
        return client;
    }

    public void setClient(User client) {
        this.client = client;
    }

    public User getEmployee() {
        return employee;
    }

    public void setEmployee(User employee) {
        this.employee = employee;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
        deriveEndTime();
    }

    @Override
    public String toString() {
        return "Appointment{" +
                ", date=" + date +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", status=" + status +
                ", employee=" + employee.getName() + employee.getLastName() +
                ", service=" + service.getName() + service.getDurationMinutes() +
                '}';
    }
}
