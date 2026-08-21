package FYP.project_backend.permit.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PermitPaymentRequest {

    @NotBlank
    private String controlNumber;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;
}