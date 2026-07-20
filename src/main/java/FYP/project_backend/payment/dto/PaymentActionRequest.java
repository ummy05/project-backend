package FYP.project_backend.payment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PaymentActionRequest {

    @NotBlank
    private String remarks;

}