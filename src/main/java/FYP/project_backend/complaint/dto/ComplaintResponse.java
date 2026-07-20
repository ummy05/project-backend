package FYP.project_backend.complaint.dto;
import FYP.project_backend.enums.ComplaintCategory;
import FYP.project_backend.enums.ComplaintStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ComplaintResponse {

    private Long id;

    private String complaintNumber;

    private String title;

    private String description;

    private ComplaintCategory category;

    private String location;

    private String imageUrl;

    private ComplaintStatus status;

    private String adminResponse;

    private LocalDateTime reportedAt;

    private LocalDateTime resolvedAt;

}