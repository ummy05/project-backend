package FYP.project_backend.payment;

import FYP.project_backend.enums.LicenseStatus;
import FYP.project_backend.enums.PaymentStatus;
import FYP.project_backend.enums.PermitStatus;
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

        // =================================================
        // GET CURRENT USER
        // =================================================

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        User applicant =
                userRepository
                        .findByEmail(authentication.getName())
                        .orElse(null);

        if (applicant == null) {

            return ResponseEntity
                    .badRequest()
                    .body("Applicant not found.");
        }


        // =================================================
        // FIND LICENSE
        // =================================================

        License license =
                licenseRepository
                        .findByControlNumber(
                                request.getControlNumber()
                        )
                        .orElse(null);


        // =================================================
        // IF NOT LICENSE -> FIND PERMIT
        // =================================================

        Permit permit = null;

        if (license == null) {

            permit =
                    permitRepository
                            .findByControlNumber(
                                    request.getControlNumber()
                            )
                            .orElse(null);
        }


        // =================================================
        // INVALID CONTROL NUMBER
        // =================================================

        if (license == null && permit == null) {

            return ResponseEntity
                    .badRequest()
                    .body("Invalid control number.");
        }


        // =================================================
        // DETERMINE REQUIRED AMOUNT
        // =================================================

        BigDecimal requiredAmount;


        // =================================================
        // LICENSE PAYMENT
        // =================================================

        if (license != null) {

            // ---------------------------------------------
            // CHECK OWNER
            // ---------------------------------------------

            User paymentOwner =
                    license.getOwner();

            if (paymentOwner == null ||
                    !paymentOwner.getId()
                            .equals(applicant.getId())) {

                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(
                                "This control number does not belong to you."
                        );
            }


            // ---------------------------------------------
            // LICENSE MUST BE APPROVED
            // ---------------------------------------------

            if (license.getStatus() !=
                    LicenseStatus.APPROVED) {

                return ResponseEntity
                        .badRequest()
                        .body(
                                "License must be approved before payment."
                        );
            }


            // ---------------------------------------------
            // PREVENT DUPLICATE PAYMENT
            // ---------------------------------------------

            if (paymentRepository
                    .existsByLicense(license)) {

                return ResponseEntity
                        .badRequest()
                        .body(
                                "Payment has already been submitted for this license."
                        );
            }


            // ---------------------------------------------
            // LICENSE FEE
            // ---------------------------------------------

            requiredAmount =
                    license.getLicenseFee();

        }


        // =================================================
        // PERMIT PAYMENT
        // =================================================

        else {

            User paymentOwner =
                    permit.getOwner();

            // ---------------------------------------------
            // CHECK OWNER
            // ---------------------------------------------

            if (paymentOwner == null ||
                    !paymentOwner.getId()
                            .equals(applicant.getId())) {

                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(
                                "This control number does not belong to you."
                        );
            }


            // ---------------------------------------------
            // PREVENT DUPLICATE PAYMENT
            // ---------------------------------------------

            if (paymentRepository
                    .existsByPermit(permit)) {

                return ResponseEntity
                        .badRequest()
                        .body(
                                "Payment has already been submitted for this permit."
                        );
            }


            // ---------------------------------------------
            // PERMIT FEE
            // ---------------------------------------------

            requiredAmount =
                    permit.getPermitFee();
        }


        // =================================================
        // EXACT AMOUNT CHECK
        // =================================================

        if (request.getAmount() == null) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            "Payment amount is required."
                    );
        }


        if (request.getAmount()
                .compareTo(requiredAmount) != 0) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            "Invalid payment amount. Required amount is TZS "
                                    + requiredAmount
                    );
        }


        // =================================================
        // GENERATE PAYMENT NUMBER
        // =================================================

        long next =
                paymentRepository.count() + 1;

        String paymentNumber =
                String.format(
                        "PAY-%d-%06d",
                        Year.now().getValue(),
                        next
                );


        // =================================================
        // CREATE PAYMENT
        // =================================================

        Payment payment =
                Payment.builder()

                        .paymentNumber(
                                paymentNumber
                        )

                        .license(
                                license
                        )

                        .permit(
                                permit
                        )

                        .owner(
                                applicant
                        )

                        .amount(
                                request.getAmount()
                        )

                        .paymentMethod(
                                request.getPaymentMethod()
                        )

                        .transactionNumber(
                                request.getControlNumber()
                        )

                        .status(
                                PaymentStatus.PENDING
                        )

                        .paymentDate(
                                LocalDateTime.now()
                        )

                        .verifiedAt(
                                null
                        )

                        .createdAt(
                                LocalDateTime.now()
                        )

                        .build();


        paymentRepository.save(payment);


        // =================================================
        // UPDATE LICENSE
        // =================================================

        if (license != null) {

            license.setPaidAmount(
                    request.getAmount()
            );

            license.setRemarks(
                    "Payment submitted successfully. Awaiting verification."
            );

            licenseRepository.save(license);
        }


        // =================================================
        // UPDATE PERMIT
        // =================================================

        if (permit != null) {

            permit.setPaidAmount(
                    request.getAmount()
            );

            permit.setStatus(
                    PermitStatus.PENDING
            );

            permit.setRemarks(
                    "Payment submitted successfully. Awaiting verification."
            );

            permitRepository.save(permit);
        }


        // =================================================
        // NOTIFICATION
        // =================================================

        notificationService.notify(

                applicant,

                "Payment Submitted",

                "Payment Submitted Successfully",

                "Your payment of TZS "
                        + request.getAmount()
                        + " has been submitted successfully "
                        + "for control number "
                        + request.getControlNumber()
                        + ". It is awaiting verification.",

                NotificationType.PAYMENT,

                "Payment Number",

                paymentNumber,

                "View Payments",

                "http://localhost:4200/business-owner/payments"
        );


        // =================================================
        // RESPONSE
        // =================================================

        return ResponseEntity
                .ok(
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
                        .findByEmail(
                                authentication.getName()
                        )
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
                        ResponseEntity
                                .notFound()
                                .build()
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


        // =================================================
        // ONLY PENDING
        // =================================================

        if (
                payment.getStatus()
                        != PaymentStatus.PENDING
        ) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            "Only pending payments can be approved."
                    );

        }


        // =================================================
        // APPROVE
        // =================================================

        payment.setStatus(
                PaymentStatus.APPROVED
        );

        payment.setAdminRemarks(
                request.getRemarks()
        );

        payment.setVerifiedAt(
                LocalDateTime.now()
        );


        Payment savedPayment =
                paymentRepository.save(
                        payment
                );


        // =================================================
        // UPDATE LICENSE
        // =================================================

        License license =
                savedPayment.getLicense();


        if (license != null) {

            license.setPaidAmount(
                    savedPayment.getAmount()
            );

            license.setRemarks(
                    "Payment verified successfully."
            );

            licenseRepository.save(
                    license
            );

        }


        // =================================================
        // NOTIFICATION
        // =================================================

        try {

            notificationService.notify(

                    savedPayment.getOwner(),

                    "Payment Approved",

                    "Payment Verified Successfully",

                    "Your payment "
                            + savedPayment.getPaymentNumber()
                            + " has been verified and approved successfully. "
                            + "Amount received: TZS "
                            + savedPayment.getAmount()
                            + ".",

                    NotificationType.PAYMENT,

                    "Payment Number",

                    savedPayment.getPaymentNumber(),

                    "View Payments",

                    "http://localhost:4200/business-owner/payments"

            );

        } catch (Exception e) {

            // Payment is already approved.
            // Notification failure must not
            // undo the payment approval.

            System.err.println(
                    "Payment notification failed: "
                            + e.getMessage()
            );

        }


        // =================================================
        // RESPONSE
        // =================================================

        return ResponseEntity.ok(
                map(savedPayment)
        );

    }
    // =====================================================
    // REJECT PAYMENT
    // =====================================================

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> reject(

            @PathVariable Long id,

            @Valid
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


        // =================================================
        // ONLY PENDING PAYMENT
        // =================================================

        if (payment.getStatus() !=
                PaymentStatus.PENDING) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            "Only pending payments can be rejected."
                    );
        }


        // =================================================
        // REASON REQUIRED
        // =================================================

        if (request.getRemarks() == null ||
                request.getRemarks()
                        .trim()
                        .isEmpty()) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            "Rejection reason is required."
                    );
        }


        // =================================================
        // REJECT PAYMENT
        // =================================================

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


        // =================================================
        // UPDATE LICENSE
        // =================================================

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


        // =================================================
        // NOTIFICATION
        // =================================================

        notificationService.notify(

                payment.getOwner(),

                "Payment Rejected",

                "Payment Verification Failed",

                "Your payment "
                        + payment.getPaymentNumber()
                        + " has been rejected. "
                        + "Reason: "
                        + request.getRemarks(),

                NotificationType.PAYMENT,

                "Payment Number",

                payment.getPaymentNumber(),

                "View Payments",

                "http://localhost:4200/business-owner/payments"
        );


        return ResponseEntity
                .ok(
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

        if (!paymentRepository
                .existsById(id)) {

            return ResponseEntity
                    .notFound()
                    .build();
        }


        paymentRepository.deleteById(id);


        return ResponseEntity
                .ok(
                        "Payment deleted successfully."
                );
    }
}