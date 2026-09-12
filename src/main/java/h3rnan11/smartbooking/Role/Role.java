package h3rnan11.smartbooking.Role;

import jakarta.persistence.*;

@Entity
@Table(name = "role")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    private RoleName name;

    public enum RoleName {
        ADMIN,
        CLIENT,
        EMPLOYEE,
        OWNER
    }

}
