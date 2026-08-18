package FYP.project_backend.license.dto;

import FYP.project_backend.enums.LicenseStatus;
import FYP.project_backend.enums.LicenseType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class LicenseResponse {

    private Long id;

    private String licenseNumber;

    private String businessName;

    private String ownerName;

    private String ownerEmail;

    private String phoneNumber;

    private LicenseType licenseType;

    private String district;

    private String location;

    private LocalDate issueDate;

    private LocalDate expiryDate;

    private BigDecimal licenseFee;

    private LicenseStatus status;

    private Integer durationMonths;

    private BigDecimal paidAmount;

    private boolean renewal;

    private Integer renewalCount;

}