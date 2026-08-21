package FYP.project_backend.analytics.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class AnalyticsResponse {

    // =====================================================
    // LICENSES
    // =====================================================

    private long totalLicenses;

    private long approvedLicenses;

    private long pendingLicenses;

    private long rejectedLicenses;


    // =====================================================
    // PERMITS
    // =====================================================

    private long totalPermits;

    private long issuedPermits;

    private long approvedPermits;

    private long pendingPermits;

    private long waitingPaymentPermits;

    private long rejectedPermits;


    // =====================================================
    // INSPECTIONS
    // =====================================================

    private long totalInspections;

    private long passedInspections;

    private long failedInspections;

    private long pendingInspections;


    // =====================================================
    // PAYMENTS
    // =====================================================

    private long totalPayments;

    private long approvedPayments;

    private long pendingPayments;

    private long rejectedPayments;


    // =====================================================
    // COMPLAINTS
    // =====================================================

    private long totalComplaints;

    private long resolvedComplaints;

    private long pendingComplaints;

    private long rejectedComplaints;


    // =====================================================
    // SHEHA / SHEHIA
    // =====================================================

    private long totalShehas;

    private long totalShehias;


    // =====================================================
    // REVENUE
    // =====================================================

    private BigDecimal totalRevenue;

    private BigDecimal permitRevenue;

    private BigDecimal licenseRevenue;

}