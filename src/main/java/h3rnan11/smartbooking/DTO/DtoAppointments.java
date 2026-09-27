package h3rnan11.smartbooking.DTO;

import java.time.LocalDate;
import java.time.LocalTime;

public class DtoAppointments {

    private Integer id;
    private LocalDate date;
    private LocalTime start_time;
    private String local_name;
    private String local_location;

    public DtoAppointments(Integer id, LocalDate date, LocalTime start_time, String local_name, String local_location) {
        this.id = id;
        this.date = date;
        this.start_time = start_time;
        this.local_name = local_name;
        this.local_location = local_location;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDate getDate() { return date; }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getStart_time() {
        return start_time;
    }

    public void setStart_time(LocalTime start_time) {
        this.start_time = start_time;
    }

    public String getLocal_name() {
        return local_name;
    }

    public void setLocal_name(String local_name) {
        this.local_name = local_name;
    }

    public String getLocal_location() {
        return local_location;
    }

    public void setLocal_location(String local_location) {
        this.local_location = local_location;
    }
}
