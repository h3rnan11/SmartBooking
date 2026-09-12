package h3rnan11.smartbooking.Local;

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

    @Enumerated(EnumType.STRING)
    private Category category;


}
