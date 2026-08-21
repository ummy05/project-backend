package FYP.project_backend.permit;

import FYP.project_backend.enums.PermitStatus;
import FYP.project_backend.enums.PermitType;
import FYP.project_backend.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "permits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String permitNumber;

    @Column(unique = true)
    private String controlNumber;

    // ==============================
    // BUSINESS OWNER
    // ==============================

    private String businessName;

    private String ownerName;

    private String ownerEmail;

    private String phoneNumber;

    // ==============================
    // EVENT INFORMATION
    // ==============================

    @Enumerated(EnumType.STRING)
    private PermitType permitType;

    private String eventName;

    @Column(length = 1000)
    private String description;

    private LocalDate eventDate;

    private String eventTime;

    private String location;

    // ==============================
    // SHEHIA / SHEHA
    // ==============================

    private String shehia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sheha_id")
    @JsonIgnore
    private User sheha;

    // ==============================
    // PAYMENT
    // ==============================

    private BigDecimal permitFee;

    private BigDecimal paidAmount;

    // ==============================
    // STATUS
    // ==============================

    @Enumerated(EnumType.STRING)
    private PermitStatus status;

    @Column(length = 1000)
    private String remarks;

    // ==============================
    // DATES
    // ==============================

    private LocalDate issueDate;

    private LocalDate expiryDate;

    private LocalDateTime createdAt;

    // ==============================
    // OWNER
    // ==============================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    @JsonIgnore
    private User owner;
}