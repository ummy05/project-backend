package FYP.project_backend.permit.dto;

import FYP.project_backend.enums.PermitType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PermitRequest {

    @NotNull
    private PermitType permitType;

    @NotBlank
    private String eventName;

    @NotBlank
    private String description;

    @NotNull
    private LocalDate eventDate;

    @NotBlank
    private String eventTime;

    @NotBlank
    private String location;

    @NotBlank
    private String shehia;
}