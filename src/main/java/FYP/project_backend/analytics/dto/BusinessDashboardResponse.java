package FYP.project_backend.analytics.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class BusinessDashboardResponse {

    // =====================================================
    // LICENSES
    // =====================================================

    private long myLicenses;

    private long approvedLicenses;

    private long pendingLicenses;

    private long rejectedLicenses;


    // =====================================================
    // PERMITS
    // =====================================================

    private long myPermits;

    private long approvedPermits;

    private long pendingPermits;

    private long waitingPaymentPermits;

    private long rejectedPermits;


    // =====================================================
    // PAYMENTS
    // =====================================================

    private long myPayments;

    private long approvedPayments;

    private long pendingPayments;


    // =====================================================
    // REVENUE
    // =====================================================

    private BigDecimal totalPaid;

}