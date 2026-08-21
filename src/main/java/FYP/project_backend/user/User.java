package FYP.project_backend.user;

import FYP.project_backend.enums.Role;
import FYP.project_backend.notification.Notification;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String phoneNumber;

    private String password;

    private Integer age;

    private String gender;

    private String address;

    private boolean enabled = true;

    @Enumerated(EnumType.STRING)
    private Role role;

    private String profileImage;


    // ==============================
    // BUSINESS INFORMATION
    // ==============================

    private String businessName;

    private String businessType;

    private String businessAddress;

    @Column(unique = true)
    private String businessRegistrationNumber;

    //sheha
    private String shehia;
    // ==============================
    // TOURIST INFORMATION
    // ==============================

    private String nationality;


    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Notification> notifications = new ArrayList<>();
}