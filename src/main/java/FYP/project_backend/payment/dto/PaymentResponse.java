package FYP.project_backend.payment.dto;
import FYP.project_backend.enums.PaymentMethod;
import FYP.project_backend.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PaymentResponse {

    private Long id;

    private String paymentNumber;

    private String licenseNumber;

    private BigDecimal amount;

    private PaymentMethod paymentMethod;

    private String transactionNumber;

    private PaymentStatus status;

    private String adminRemarks;

    private LocalDateTime paymentDate;

}