package FYP.project_backend.license;

import FYP.project_backend.enums.LicenseStatus;
import FYP.project_backend.enums.LicenseType;
import FYP.project_backend.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "licenses")

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class License {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String licenseNumber;

    private String businessName;

    private String ownerName;

    private String ownerEmail;

    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private LicenseType licenseType;

    private String district;

    private String location;

    private LocalDate issueDate;

    private LocalDate expiryDate;

    private BigDecimal licenseFee;

    @Enumerated(EnumType.STRING)
    private LicenseStatus status;

    @Column(length = 1000)
    private String remarks;

    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="owner_id")
    @JsonIgnore
    private User owner;

}