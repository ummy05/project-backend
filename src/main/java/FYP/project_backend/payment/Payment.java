package FYP.project_backend.payment;

import FYP.project_backend.enums.PaymentMethod;
import FYP.project_backend.enums.PaymentStatus;
import FYP.project_backend.license.License;
import FYP.project_backend.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String paymentNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "license_id")
    @JsonIgnore
    private License license;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    @JsonIgnore
    private User owner;

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    /**
     * Control Number / Transaction ID
     */
    private String transactionNumber;

    /**
     * Receipt image path (optional)
     */
    private String receipt;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @Column(length = 1000)
    private String adminRemarks;

    private LocalDateTime paymentDate;

    private LocalDateTime verifiedAt;

    private LocalDateTime createdAt;

}