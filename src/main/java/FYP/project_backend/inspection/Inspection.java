package FYP.project_backend.inspection;

import FYP.project_backend.enums.InspectionStatus;
import FYP.project_backend.license.License;
import FYP.project_backend.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "inspections")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inspection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String inspectionNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "license_id")
    @JsonIgnore
    private License license;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_by")
    @JsonIgnore
    private User performedBy;

    private LocalDate inspectionDate;

    @Enumerated(EnumType.STRING)
    private InspectionStatus status;

    @Column(length = 1000)
    private String remarks;

    private LocalDateTime createdAt;

}