package h3rnan11.smartbooking.Appointment;

import h3rnan11.smartbooking.Local.Local;
import h3rnan11.smartbooking.Service.Service;
import h3rnan11.smartbooking.Service.ServiceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.sql.Time;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@DataJpaTest
@TestPropertySource(properties = "spring.sql.init.mode=never")
class AppointmentServicePersistenceTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    private Service persistService(String name, int minutes) {
        Local local = new Local();
        local.setName("Bella Hair Studio");
        Service service = new Service();
        service.setName(name);
        service.setDurationMinutes(minutes);
        service.setPrice(new BigDecimal("25.00"));
        local.addService(service);
        em.persist(local);
        return service;
    }

    @Test
    void servicesAreSavedWithTheirLocal() {
        Service service = persistService("Corte y peinado", 45);
        em.flush();
        em.clear();

        List<Service> found = serviceRepository.findByLocalId(service.getLocal().getId());

        assertEquals(1, found.size());
        assertEquals(45, found.get(0).getDurationMinutes());
        assertEquals(0, new BigDecimal("25.00").compareTo(found.get(0).getPrice()));
    }

    @Test
    void endTimeIsDerivedOnSave() {
        Service service = persistService("Corte de pelo", 30);
        Appointment appointment = new Appointment();
        appointment.setStatus(Status.CONFIRMED);
        appointment.setService(service);
        appointment.setStartTime(Time.valueOf("10:00:00"));
        appointmentRepository.saveAndFlush(appointment);
        em.clear();

        Appointment reloaded = appointmentRepository.findById(appointment.getId()).orElseThrow();
        assertEquals(Time.valueOf("10:30:00"), reloaded.getEndTime());
        assertEquals(service.getId(), reloaded.getService().getId());
    }

    @Test
    void appointmentsWithoutServiceStillPersist() {
        Appointment appointment = new Appointment();
        appointment.setStartTime(Time.valueOf("09:00:00"));
        appointment.setEndTime(Time.valueOf("09:30:00"));
        appointmentRepository.saveAndFlush(appointment);
        em.clear();

        Appointment reloaded = appointmentRepository.findById(appointment.getId()).orElseThrow();
        assertNull(reloaded.getService());
        assertEquals(Time.valueOf("09:30:00"), reloaded.getEndTime());
    }
}
