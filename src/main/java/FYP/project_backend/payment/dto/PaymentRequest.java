package FYP.project_backend.payment.dto;

import FYP.project_backend.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentRequest {

    @NotNull
    private Long licenseId;

    @NotNull
    private PaymentMethod paymentMethod;

    private String transactionNumber;

    private String receipt;

}