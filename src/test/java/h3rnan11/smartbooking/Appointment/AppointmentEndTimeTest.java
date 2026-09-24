package h3rnan11.smartbooking.Appointment;

import h3rnan11.smartbooking.Service.Service;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

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
        appointment.setStartTime(LocalTime.parse("10:00:00"));
        appointment.setService(serviceOf(45));

        assertEquals(LocalTime.parse("10:45:00"), appointment.getEndTime());
    }

    @Test
    void changingStartTimeRecomputesEndTime() {
        Appointment appointment = new Appointment();
        appointment.setService(serviceOf(30));
        appointment.setStartTime(LocalTime.parse("09:15:00"));
        assertEquals(LocalTime.parse("09:45:00"), appointment.getEndTime());

        appointment.setStartTime(LocalTime.parse("16:00:00"));
        assertEquals(LocalTime.parse("16:30:00"), appointment.getEndTime());
    }

    @Test
    void serviceDurationOverridesManualEndTime() {
        Appointment appointment = new Appointment();
        appointment.setStartTime(LocalTime.parse("11:00:00"));
        appointment.setService(serviceOf(60));
        appointment.setEndTime(LocalTime.parse("11:10:00"));

        appointment.deriveEndTime();

        assertEquals(LocalTime.parse("12:00:00"), appointment.getEndTime());
    }

    @Test
    void appointmentWithoutServiceKeepsItsEndTime() {
        Appointment appointment = new Appointment();
        appointment.setStartTime(LocalTime.parse("10:00:00"));
        appointment.setEndTime(LocalTime.parse("10:30:00"));

        appointment.deriveEndTime();

        assertEquals(LocalTime.parse("10:30:00"), appointment.getEndTime());
        assertNull(appointment.getService());
    }
}
