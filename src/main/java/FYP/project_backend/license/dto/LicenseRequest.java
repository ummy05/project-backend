package FYP.project_backend.license.dto;
import FYP.project_backend.enums.LicenseType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
@Data
public class LicenseRequest {

    @NotBlank
    private String businessName;

    @NotBlank
    private String phoneNumber;

    @NotNull
    private LicenseType licenseType;

    @NotBlank
    private String district;

    @NotBlank
    private String location;

    @NotNull
    private BigDecimal licenseFee;

}