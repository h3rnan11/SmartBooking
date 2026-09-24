package h3rnan11.smartbooking.Service;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceRepository extends JpaRepository<Service, Integer> {

    List<Service> findByLocalId(Integer localId);
}
