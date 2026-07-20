package FYP.project_backend.license.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LicenseActionRequest {

    @NotBlank
    private String reason;

}