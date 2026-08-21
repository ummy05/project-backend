package FYP.project_backend.analytics.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TouristDashboardResponse {

    // =====================================================
    // COMPLAINTS
    // =====================================================

    private long myComplaints;

    private long resolvedComplaints;

    private long pendingComplaints;

    private long rejectedComplaints;


    // =====================================================
    // PERMITS
    // =====================================================

    private long myPermits;

    private long approvedPermits;

    private long pendingPermits;

    private long waitingPaymentPermits;

    private long rejectedPermits;

}