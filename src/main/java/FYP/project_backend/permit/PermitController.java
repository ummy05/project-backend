package FYP.project_backend.permit;

import FYP.project_backend.enums.PaymentStatus;
import FYP.project_backend.enums.PermitStatus;
import FYP.project_backend.enums.PermitType;
import FYP.project_backend.enums.Role;
import FYP.project_backend.notification.NotificationService;
import FYP.project_backend.notification.NotificationType;
import FYP.project_backend.payment.Payment;
import FYP.project_backend.payment.PaymentRepository;
import FYP.project_backend.permit.dto.PermitActionRequest;
import FYP.project_backend.permit.dto.PermitPaymentRequest;
import FYP.project_backend.permit.dto.PermitRequest;
import FYP.project_backend.user.User;
import FYP.project_backend.user.UserRepository;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;

@RestController
@RequestMapping("/api/permits")
@RequiredArgsConstructor
@CrossOrigin("*")
public class PermitController {

    private final PermitRepository repository;

    private final UserRepository userRepository;

    private final PaymentRepository paymentRepository;

    private final NotificationService notificationService;


    // =========================================================
    // APPLY FOR PERMIT
    // =========================================================

    @PostMapping("/apply")
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'TOURIST')")
    public ResponseEntity<?> applyPermit(
            @Valid @RequestBody PermitRequest request) {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        User owner = userRepository
                .findByEmail(authentication.getName())
                .orElse(null);

        if (owner == null) {

            return ResponseEntity.badRequest()
                    .body("User not found.");
        }


        // =====================================================
        // FIND SHEHA
        // =====================================================

        User sheha = userRepository
                .findFirstByRoleAndShehiaIgnoreCase(
                        Role.SHEHA,
                        request.getShehia()
                )
                .orElse(null);

        if (sheha == null) {

            return ResponseEntity.badRequest()
                    .body(
                            "No Sheha is registered for the selected Shehia."
                    );
        }


        // =====================================================
        // GENERATE NUMBERS
        // =====================================================

        long next = repository.count() + 1;

        String permitNumber =
                String.format(
                        "PRM-%d-%06d",
                        Year.now().getValue(),
                        next
                );

        String controlNumber =
                String.format(
                        "CTL-%d-%08d",
                        Year.now().getValue(),
                        next
                );


        // =====================================================
        // CALCULATE FEE
        // =====================================================

        BigDecimal fee =
                calculatePermitFee(
                        request.getPermitType()
                );


        // =====================================================
        // CREATE PERMIT
        // =====================================================

        Permit permit =
                Permit.builder()

                        .permitNumber(permitNumber)

                        .controlNumber(controlNumber)

                        .permitType(request.getPermitType())

                        .eventName(request.getEventName())

                        .description(request.getDescription())

                        .eventDate(request.getEventDate())

                        .eventTime(request.getEventTime())

                        .location(request.getLocation())

                        .shehia(request.getShehia())

                        .sheha(sheha)

                        .permitFee(fee)

                        .paidAmount(BigDecimal.ZERO)

                        .status(PermitStatus.WAITING_PAYMENT)

                        .businessName(
                                owner.getBusinessName() != null
                                        ? owner.getBusinessName()
                                        : "Individual / Tourist"
                        )

                        .ownerName(owner.getFullName())

                        .ownerEmail(owner.getEmail())

                        .phoneNumber(owner.getPhoneNumber())

                        .owner(owner)

                        .createdAt(LocalDateTime.now())

                        .build();


        repository.save(permit);


        // =====================================================
        // NOTIFICATION
        // =====================================================

        notificationService.notify(

                owner,

                "Permit Payment Instructions",

                "Permit Application Created",

                "Your permit application has been received successfully. "
                        + "Please make payment using the control number below. "
                        + "The required payment amount is TZS "
                        + fee
                        + ".",

                NotificationType.PAYMENT,

                "Control Number",

                controlNumber,

                "View Permit",

                "http://localhost:4200/business-owner/permits"
        );


        return ResponseEntity.ok(permit);
    }


    // =========================================================
    // MY PERMITS
    // =========================================================

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'TOURIST')")
    public ResponseEntity<?> myPermits() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        User owner = userRepository
                .findByEmail(authentication.getName())
                .orElse(null);

        if (owner == null) {

            return ResponseEntity.badRequest()
                    .body("User not found.");
        }

        return ResponseEntity.ok(
                repository.findByOwner(owner)
        );
    }


    // =========================================================
    // GET ONE PERMIT
    // =========================================================

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getPermit(
            @PathVariable Long id) {

        Permit permit = repository
                .findById(id)
                .orElse(null);

        if (permit == null) {

            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(permit);
    }


    // =========================================================
    // PAY PERMIT
    // =========================================================

    @PostMapping("/pay")
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'TOURIST')")
    public ResponseEntity<?> payPermit(
            @Valid @RequestBody PermitPaymentRequest request) {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();


        User owner = userRepository
                .findByEmail(authentication.getName())
                .orElse(null);

        if (owner == null) {

            return ResponseEntity.badRequest()
                    .body("User not found.");
        }


        // =====================================================
        // FIND PERMIT
        // =====================================================

        Permit permit =
                repository
                        .findByControlNumber(
                                request.getControlNumber().trim()
                        )
                        .orElse(null);

        if (permit == null) {

            return ResponseEntity.badRequest()
                    .body("Invalid control number.");
        }


        // =====================================================
        // OWNER CHECK
        // =====================================================

        if (permit.getOwner() == null ||
                !permit.getOwner()
                        .getId()
                        .equals(owner.getId())) {

            return ResponseEntity.status(403)
                    .body(
                            "This control number does not belong to you."
                    );
        }


        // =====================================================
        // STATUS CHECK
        // =====================================================

        if (permit.getStatus()
                != PermitStatus.WAITING_PAYMENT) {

            return ResponseEntity.badRequest()
                    .body(
                            "This permit is not waiting for payment."
                    );
        }


        // =====================================================
        // AMOUNT CHECK
        // =====================================================

        if (request.getAmount() == null) {

            return ResponseEntity.badRequest()
                    .body("Payment amount is required.");
        }


        if (request.getAmount()
                .compareTo(permit.getPermitFee()) != 0) {

            return ResponseEntity.badRequest()
                    .body(
                            "Invalid payment amount. Expected "
                                    + permit.getPermitFee()
                    );
        }


        // =====================================================
        // CHECK DUPLICATE PAYMENT
        // =====================================================

        boolean alreadyPaid =
                paymentRepository
                        .findByPermit(permit)
                        .stream()
                        .anyMatch(
                                payment ->
                                        payment.getStatus()
                                                == PaymentStatus.APPROVED
                        );

        if (alreadyPaid) {

            return ResponseEntity.badRequest()
                    .body(
                            "Payment has already been completed for this permit."
                    );
        }


        // =====================================================
        // GENERATE PAYMENT NUMBER
        // =====================================================

        String paymentNumber =
                String.format(
                        "PAY-%d-%06d",
                        Year.now().getValue(),
                        paymentRepository.count() + 1
                );


        // =====================================================
        // CREATE PAYMENT
        // =====================================================

        Payment payment =
                Payment.builder()

                        .paymentNumber(paymentNumber)

                        .permit(permit)

                        .owner(owner)

                        .amount(request.getAmount())

                        .transactionNumber(
                                request.getControlNumber()
                        )

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
        // UPDATE PERMIT
        // =====================================================

        permit.setPaidAmount(
                request.getAmount()
        );

        permit.setStatus(
                PermitStatus.PENDING
        );

        permit.setRemarks(
                "Payment completed successfully. "
                        + "Waiting for Sheha/Admin approval."
        );

        repository.save(permit);


        // =====================================================
        // NOTIFICATION
        // =====================================================

        notificationService.notify(

                owner,

                "Permit Payment Successful",

                "Payment Successful",

                "Your permit payment has been verified successfully. "
                        + "Your application is now waiting for approval.",

                NotificationType.PAYMENT,

                "Amount Paid",

                request.getAmount().toString(),

                "View Permit",

                "/permits/my"
        );


        return ResponseEntity.ok(permit);
    }


    // =========================================================
    // SHEHA PERMITS
    // =========================================================

    @GetMapping("/sheha")
    @PreAuthorize("hasRole('SHEHA')")
    public ResponseEntity<?> shehaPermits() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        User sheha = userRepository
                .findByEmail(authentication.getName())
                .orElse(null);

        if (sheha == null) {

            return ResponseEntity.badRequest()
                    .body("Sheha not found.");
        }

        return ResponseEntity.ok(
                repository.findBySheha(sheha)
        );
    }


    // =========================================================
    // APPROVE PERMIT
    // =========================================================

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'SHEHA')")
    public ResponseEntity<?> approvePermit(
            @PathVariable Long id) {

        Permit permit = repository
                .findById(id)
                .orElse(null);

        if (permit == null) {

            return ResponseEntity.notFound().build();
        }


        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();


        User approver = userRepository
                .findByEmail(authentication.getName())
                .orElse(null);

        if (approver == null) {

            return ResponseEntity.badRequest()
                    .body("Approver not found.");
        }


        // =====================================================
        // SHEHA OWNERSHIP CHECK
        // =====================================================

        if (approver.getRole() == Role.SHEHA) {

            if (permit.getSheha() == null ||
                    !permit.getSheha()
                            .getId()
                            .equals(approver.getId())) {

                return ResponseEntity.status(403)
                        .body(
                                "You are not assigned to this permit."
                        );
            }
        }


        // =====================================================
        // STATUS CHECK
        // =====================================================

        if (permit.getStatus()
                != PermitStatus.PENDING) {

            return ResponseEntity.badRequest()
                    .body(
                            "Permit is not ready for approval."
                    );
        }


        // =====================================================
        // APPROVE
        // =====================================================

        permit.setStatus(
                PermitStatus.APPROVED
        );

        permit.setIssueDate(
                LocalDate.now()
        );

        permit.setExpiryDate(
                permit.getEventDate()
        );

        permit.setRemarks(
                "Permit approved successfully."
        );

        repository.save(permit);


        // =====================================================
        // NOTIFICATION
        // =====================================================

        notificationService.notify(

                permit.getOwner(),

                "Permit Approved",

                "Permit Approved",

                "Your permit application has been approved successfully.",

                NotificationType.LICENSE,

                "Permit Number",

                permit.getPermitNumber(),

                "View Permit",

                "/permits/my"
        );


        return ResponseEntity.ok(permit);
    }


    // =========================================================
    // REJECT PERMIT
    // =========================================================

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'SHEHA')")
    public ResponseEntity<?> rejectPermit(

            @PathVariable Long id,

            @RequestBody PermitActionRequest request) {

        Permit permit = repository
                .findById(id)
                .orElse(null);

        if (permit == null) {

            return ResponseEntity.notFound().build();
        }


        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();


        User approver = userRepository
                .findByEmail(authentication.getName())
                .orElse(null);

        if (approver == null) {

            return ResponseEntity.badRequest()
                    .body("Approver not found.");
        }


        // =====================================================
        // SHEHA OWNERSHIP CHECK
        // =====================================================

        if (approver.getRole() == Role.SHEHA) {

            if (permit.getSheha() == null ||
                    !permit.getSheha()
                            .getId()
                            .equals(approver.getId())) {

                return ResponseEntity.status(403)
                        .body(
                                "You are not assigned to this permit."
                        );
            }
        }


        // =====================================================
        // STATUS CHECK
        // =====================================================

        if (permit.getStatus()
                != PermitStatus.PENDING) {

            return ResponseEntity.badRequest()
                    .body(
                            "Only pending permits can be rejected."
                    );
        }


        permit.setStatus(
                PermitStatus.REJECTED
        );

        permit.setRemarks(
                request.getRemarks() == null ||
                        request.getRemarks().isBlank()
                        ? "Permit application rejected."
                        : request.getRemarks()
        );

        repository.save(permit);


        // =====================================================
        // NOTIFICATION
        // =====================================================

        notificationService.notify(

                permit.getOwner(),

                "Permit Application Rejected",

                "Permit Rejected",

                "Your permit application has been rejected.",

                NotificationType.LICENSE,

                "Reason",

                permit.getRemarks(),

                "View Permit",

                "/permits/my"
        );


        return ResponseEntity.ok(permit);
    }


    // =========================================================
    // ADMIN GET ALL
    // =========================================================

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAll() {

        return ResponseEntity.ok(
                repository.findAll()
        );
    }


    // =========================================================
    // CALCULATE PERMIT FEE
    // =========================================================

    private BigDecimal calculatePermitFee(
            PermitType type) {

        return switch (type) {

            case WEDDING_EVENT ->
                    new BigDecimal("150000");

            case MUSIC_EVENT ->
                    new BigDecimal("100000");

            case BEACH_EVENT ->
                    new BigDecimal("100000");

            case CULTURAL_EVENT ->
                    new BigDecimal("100000");

            case PRIVATE_EVENT ->
                    new BigDecimal("100000");

            case SPORTS_EVENT ->
                    new BigDecimal("100000");

            case OTHER_EVENT ->
                    new BigDecimal("100000");
        };
    }
}