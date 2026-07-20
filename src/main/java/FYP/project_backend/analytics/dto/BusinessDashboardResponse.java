package FYP.project_backend.analytics.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class BusinessDashboardResponse {

    private long myLicenses;

    private long approvedLicenses;

    private long pendingLicenses;

    private long rejectedLicenses;

    private long myPayments;

    private long approvedPayments;

    private long pendingPayments;

    private BigDecimal totalPaid;

}