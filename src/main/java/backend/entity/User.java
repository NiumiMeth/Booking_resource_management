package backend.entity;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

    @Id
    private UUID id;

    private String name;

    private String email;

    private String department;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;

}