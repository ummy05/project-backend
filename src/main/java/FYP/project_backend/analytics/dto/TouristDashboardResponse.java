package FYP.project_backend.analytics.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TouristDashboardResponse {

    private long myComplaints;

    private long resolvedComplaints;

    private long pendingComplaints;

    private long rejectedComplaints;

}