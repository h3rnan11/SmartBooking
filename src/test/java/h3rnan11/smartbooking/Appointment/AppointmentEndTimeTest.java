package h3rnan11.smartbooking.Appointment;

import h3rnan11.smartbooking.Service.Service;
import org.junit.jupiter.api.Test;

import java.sql.Time;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AppointmentEndTimeTest {

    private static Service serviceOf(int minutes) {
        Service service = new Service();
        service.setName("Corte");
        service.setDurationMinutes(minutes);
        return service;
    }

    @Test
    void endTimeIsStartPlusServiceDuration() {
        Appointment appointment = new Appointment();
        appointment.setStartTime(Time.valueOf("10:00:00"));
        appointment.setService(serviceOf(45));

        assertEquals(Time.valueOf("10:45:00"), appointment.getEndTime());
    }

    @Test
    void changingStartTimeRecomputesEndTime() {
        Appointment appointment = new Appointment();
        appointment.setService(serviceOf(30));
        appointment.setStartTime(Time.valueOf("09:15:00"));
        assertEquals(Time.valueOf("09:45:00"), appointment.getEndTime());

        appointment.setStartTime(Time.valueOf("16:00:00"));
        assertEquals(Time.valueOf("16:30:00"), appointment.getEndTime());
    }

    @Test
    void serviceDurationOverridesManualEndTime() {
        Appointment appointment = new Appointment();
        appointment.setStartTime(Time.valueOf("11:00:00"));
        appointment.setService(serviceOf(60));
        appointment.setEndTime(Time.valueOf("11:10:00"));

        appointment.deriveEndTime();

        assertEquals(Time.valueOf("12:00:00"), appointment.getEndTime());
    }

    @Test
    void appointmentWithoutServiceKeepsItsEndTime() {
        Appointment appointment = new Appointment();
        appointment.setStartTime(Time.valueOf("10:00:00"));
        appointment.setEndTime(Time.valueOf("10:30:00"));

        appointment.deriveEndTime();

        assertEquals(Time.valueOf("10:30:00"), appointment.getEndTime());
        assertNull(appointment.getService());
    }
}
