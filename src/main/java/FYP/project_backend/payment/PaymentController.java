package FYP.project_backend.payment;

import FYP.project_backend.enums.PaymentStatus;
import FYP.project_backend.enums.LicenseStatus;
import FYP.project_backend.license.License;
import FYP.project_backend.license.LicenseRepository;
import FYP.project_backend.notification.NotificationService;
import FYP.project_backend.notification.NotificationType;
import FYP.project_backend.payment.dto.PaymentActionRequest;
import FYP.project_backend.payment.dto.PaymentRequest;
import FYP.project_backend.payment.dto.PaymentResponse;
import FYP.project_backend.permit.Permit;
import FYP.project_backend.permit.PermitRepository;
import FYP.project_backend.user.User;
import FYP.project_backend.user.UserRepository;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@CrossOrigin("*")
public class PaymentController {

    private final PaymentRepository paymentRepository;

    private final LicenseRepository licenseRepository;

    private final PermitRepository permitRepository;

    private final UserRepository userRepository;

    private final NotificationService notificationService;


    // =====================================================
    // MAP PAYMENT RESPONSE
    // =====================================================

    private PaymentResponse map(Payment payment) {

        return PaymentResponse.builder()

                .id(payment.getId())

                .paymentNumber(
                        payment.getPaymentNumber()
                )

                .licenseNumber(
                        payment.getLicense() != null
                                ? payment.getLicense().getLicenseNumber()
                                : null
                )

                .permitNumber(
                        payment.getPermit() != null
                                ? payment.getPermit().getPermitNumber()
                                : null
                )

                .amount(
                        payment.getAmount()
                )

                .paymentMethod(
                        payment.getPaymentMethod()
                )

                .transactionNumber(
                        payment.getTransactionNumber()
                )

                .status(
                        payment.getStatus()
                )

                .adminRemarks(
                        payment.getAdminRemarks()
                )

                .paymentDate(
                        payment.getPaymentDate()
                )

                .build();
    }


    // =====================================================
    // MAKE PAYMENT
    // =====================================================

    @PostMapping
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER','TOURIST')")
    public ResponseEntity<?> makePayment(
            @Valid @RequestBody PaymentRequest request) {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        User applicant = userRepository
                .findByEmail(authentication.getName())
                .orElse(null);

        if (applicant == null) {

            return ResponseEntity.badRequest()
                    .body("Applicant not found.");
        }

        // =====================================================
        // 1. FIND LICENSE BY CONTROL NUMBER
        // =====================================================

        License license =
                licenseRepository
                        .findByControlNumber(
                                request.getControlNumber()
                        )
                        .orElse(null);

        // =====================================================
        // 2. IF NOT LICENSE -> FIND PERMIT
        // =====================================================

        Permit permit = null;

        if (license == null) {

            permit =
                    permitRepository
                            .findByControlNumber(
                                    request.getControlNumber()
                            )
                            .orElse(null);
        }

        // =====================================================
        // 3. INVALID CONTROL NUMBER
        // =====================================================

        if (license == null && permit == null) {

            return ResponseEntity.badRequest()
                    .body("Invalid control number.");
        }

        // =====================================================
        // 4. DETERMINE PAYMENT
        // =====================================================

        BigDecimal requiredAmount;

        User paymentOwner;

        if (license != null) {

            paymentOwner = license.getOwner();

            if (paymentOwner == null ||
                    !paymentOwner.getId().equals(applicant.getId())) {

                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(
                                "This control number does not belong to you."
                        );
            }

            if (paymentRepository.existsByLicense(license)) {

                return ResponseEntity.badRequest()
                        .body(
                                "Payment has already been submitted for this license."
                        );
            }

            requiredAmount = license.getLicenseFee();

        } else {

            paymentOwner = permit.getOwner();

            if (paymentOwner == null ||
                    !paymentOwner.getId().equals(applicant.getId())) {

                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(
                                "This control number does not belong to you."
                        );
            }

            if (paymentRepository.existsByPermit(permit)) {

                return ResponseEntity.badRequest()
                        .body(
                                "Payment has already been submitted for this permit."
                        );
            }

            requiredAmount = permit.getPermitFee();
        }

        // =====================================================
        // 5. EXACT AMOUNT CHECK
        // =====================================================

        if (request.getAmount()
                .compareTo(requiredAmount) != 0) {

            return ResponseEntity.badRequest()
                    .body(
                            "Invalid payment amount. Required amount is TZS "
                                    + requiredAmount
                    );
        }

        // =====================================================
        // 6. GENERATE PAYMENT NUMBER
        // =====================================================

        long next = paymentRepository.count() + 1;

        String paymentNumber =
                String.format(
                        "PAY-%d-%06d",
                        Year.now().getValue(),
                        next
                );

        // =====================================================
        // 7. CREATE PAYMENT
        // =====================================================

        Payment payment =
                Payment.builder()

                        .paymentNumber(paymentNumber)

                        .license(license)

                        .permit(permit)

                        .owner(applicant)

                        .amount(request.getAmount())

                        .paymentMethod(
                                request.getPaymentMethod()
                        )

                        .transactionNumber(
                                request.getControlNumber()
                        )

                        // EXACT CONTROL + AMOUNT = SUCCESS
                        .status(
                                PaymentStatus.APPROVED
                        )

                        .paymentDate(
                                LocalDateTime.now()
                        )

                        .verifiedAt(
                                LocalDateTime.now()
                        )

                        .createdAt(
                                LocalDateTime.now()
                        )

                        .build();

        paymentRepository.save(payment);

        // =====================================================
        // 8. UPDATE LICENSE
        // =====================================================

        if (license != null) {

            license.setPaidAmount(
                    request.getAmount()
            );

            license.setRemarks(
                    "Payment completed successfully."
            );

            licenseRepository.save(license);
        }

        // =====================================================
        // 9. UPDATE PERMIT
        // =====================================================

        if (permit != null) {

            permit.setPaidAmount(
                    request.getAmount()
            );

            permit.setStatus(
                    FYP.project_backend.enums.PermitStatus.PENDING
            );

            permit.setRemarks(
                    "Payment completed successfully."
            );

            permitRepository.save(permit);
        }

        // =====================================================
        // 10. NOTIFICATION
        // =====================================================

        notificationService.notify(

                applicant,

                "Payment Successful",

                "Payment Successful",

                "Your payment has been completed successfully. "
                        + "The amount of TZS "
                        + request.getAmount()
                        + " has been received for control number "
                        + request.getControlNumber()
                        + ".",

                NotificationType.PAYMENT,

                "Payment Number",

                paymentNumber,

                "View Payments",

                "http://localhost:4200/business-owner/payments"
        );

        return ResponseEntity.ok(
                map(payment)
        );
    }

    // =====================================================
    // MY PAYMENTS
    // =====================================================

    @GetMapping("/my")
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    public List<PaymentResponse> myPayments() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();


        User owner =
                userRepository
                        .findByEmail(authentication.getName())
                        .orElseThrow();


        return paymentRepository

                .findByOwner(owner)

                .stream()

                .map(this::map)

                .toList();
    }


    // =====================================================
    // GET ALL PAYMENTS
    // =====================================================

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<PaymentResponse> getAll() {

        return paymentRepository

                .findAll()

                .stream()

                .map(this::map)

                .toList();
    }


    // =====================================================
    // GET PENDING PAYMENTS
    // =====================================================

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public List<PaymentResponse> pending() {

        return paymentRepository

                .findByStatus(
                        PaymentStatus.PENDING
                )

                .stream()

                .map(this::map)

                .toList();
    }


    // =====================================================
    // GET PAYMENT BY ID
    // =====================================================

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getById(
            @PathVariable Long id) {

        return paymentRepository

                .findById(id)

                .map(this::map)

                .map(ResponseEntity::ok)

                .orElse(
                        ResponseEntity.notFound().build()
                );
    }


    // =====================================================
    // APPROVE PAYMENT
    // =====================================================

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> approve(

            @PathVariable Long id,

            @RequestBody PaymentActionRequest request) {


        Payment payment =
                paymentRepository

                        .findById(id)

                        .orElse(null);


        if (payment == null) {

            return ResponseEntity
                    .notFound()
                    .build();

        }


        // =========================
        // APPROVE PAYMENT
        // =========================

        payment.setStatus(
                PaymentStatus.APPROVED
        );

        payment.setAdminRemarks(
                request.getRemarks()
        );

        payment.setVerifiedAt(
                LocalDateTime.now()
        );


        paymentRepository.save(payment);


        // =========================
        // UPDATE LICENSE
        // =========================

        License license =
                payment.getLicense();


        if (license != null) {

            license.setPaidAmount(
                    payment.getAmount()
            );

            license.setRemarks(
                    "Payment verified successfully."
            );

            licenseRepository.save(license);
        }


        // =========================
        // NOTIFICATION
        // =========================

        notificationService.notify(

                payment.getOwner(),

                "Payment Approved",

                "Payment Verified Successfully",

                "Your payment has been verified successfully. Your payment has been received and confirmed by the system.",

                NotificationType.PAYMENT,

                "Payment Number",

                payment.getPaymentNumber(),

                "View Payments",

                "http://localhost:4200/owner/payments"

        );


        return ResponseEntity.ok(
                map(payment)
        );
    }


    // =====================================================
    // REJECT PAYMENT
    // =====================================================

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> reject(

            @PathVariable Long id,

            @RequestBody PaymentActionRequest request) {


        Payment payment =
                paymentRepository

                        .findById(id)

                        .orElse(null);


        if (payment == null) {

            return ResponseEntity
                    .notFound()
                    .build();

        }


        // =========================
        // REJECT PAYMENT
        // =========================

        payment.setStatus(
                PaymentStatus.REJECTED
        );

        payment.setAdminRemarks(
                request.getRemarks()
        );

        payment.setVerifiedAt(
                LocalDateTime.now()
        );


        paymentRepository.save(payment);


        // =========================
        // UPDATE LICENSE
        // =========================

        License license =
                payment.getLicense();


        if (license != null) {

            license.setPaidAmount(
                    BigDecimal.ZERO
            );

            license.setRemarks(
                    request.getRemarks()
            );

            licenseRepository.save(license);
        }


        // =========================
        // NOTIFICATION
        // =========================

        notificationService.notify(

                payment.getOwner(),

                "Payment Rejected",

                "Payment Verification Failed",

                "Unfortunately, your payment could not be verified. Please review the administrator remarks and submit a valid payment.",

                NotificationType.PAYMENT,

                "Payment Number",

                payment.getPaymentNumber(),

                "View Payments",

                "http://localhost:4200/owner/payments"

        );


        return ResponseEntity.ok(
                map(payment)
        );
    }


    // =====================================================
    // DELETE PAYMENT
    // =====================================================

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(
            @PathVariable Long id) {


        if (!paymentRepository.existsById(id)) {

            return ResponseEntity
                    .notFound()
                    .build();

        }


        paymentRepository.deleteById(id);


        return ResponseEntity.ok(
                "Payment deleted successfully."
        );
    }

}