package h3rnan11.smartbooking.Local;

import h3rnan11.smartbooking.Service.Service;
import h3rnan11.smartbooking.User.User;
import h3rnan11.smartbooking.Utils.Category;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "local")
public class Local {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private String location;

    @ManyToOne
    @JoinColumn(name = "id_owner")
    private User owner;

    @OneToMany(mappedBy = "local")
    private List<User> employees = new ArrayList<>();

    @OneToMany(mappedBy = "local", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Service> services = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private Category category;


    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public List<Service> getServices() {
        return services;
    }

    public void addService(Service service) {
        services.add(service);
        service.setLocal(this);
    }

    public void removeService(Service service) {
        services.remove(service);
        service.setLocal(null);
    }
}
