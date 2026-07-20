package FYP.project_backend.analytics.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class AnalyticsResponse {

    private long approvedLicenses;

    private long pendingLicenses;

    private long rejectedLicenses;

    private long passedInspections;

    private long failedInspections;

    private long pendingInspections;

    private long approvedPayments;

    private long pendingPayments;

    private long rejectedPayments;

    private long resolvedComplaints;

    private long pendingComplaints;

    private long rejectedComplaints;

    private BigDecimal totalRevenue;

}