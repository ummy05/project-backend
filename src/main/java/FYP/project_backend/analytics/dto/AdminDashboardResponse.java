package FYP.project_backend.analytics.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class AdminDashboardResponse {

    private long totalUsers;

    private long totalBusinessOwners;

    private long totalTourists;

    private long totalLicenses;

    private long approvedLicenses;

    private long pendingLicenses;

    private long rejectedLicenses;

    private long totalInspections;

    private long passedInspections;

    private long failedInspections;

    private long pendingInspections;

    private long totalComplaints;

    private long resolvedComplaints;

    private long pendingComplaints;

    private long rejectedComplaints;

    private long totalPayments;

    private long approvedPayments;

    private long pendingPayments;

    private long rejectedPayments;

    private BigDecimal totalRevenue;

}