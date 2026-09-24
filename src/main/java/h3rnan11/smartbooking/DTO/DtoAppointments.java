package h3rnan11.smartbooking.DTO;

import java.sql.Time;
import java.util.Date;

public class DtoAppointments {

    private Date date;
    private Time start_time;
    private String local_name;
    private String local_location;

    public DtoAppointments(Date date, Time start_time, String local_name, String local_location) {
        this.date = date;
        this.start_time = start_time;
        this.local_name = local_name;
        this.local_location = local_location;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Time getStart_time() {
        return start_time;
    }

    public void setStart_time(Time start_time) {
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
