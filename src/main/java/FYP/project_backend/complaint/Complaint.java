package FYP.project_backend.complaint;

import FYP.project_backend.enums.ComplaintCategory;
import FYP.project_backend.enums.ComplaintStatus;
import FYP.project_backend.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "complaints")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Complaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String complaintNumber;

    private String title;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    private ComplaintCategory category;

    private String location;

    /**
     * Kwa sasa tunaweka URL/String.
     * Baadaye tunaweza kuongeza image upload.
     */
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    private ComplaintStatus status;

    @Column(length = 2000)
    private String adminResponse;

    private LocalDateTime reportedAt;

    private LocalDateTime resolvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_by")
    @JsonIgnore
    private User reportedBy;

}