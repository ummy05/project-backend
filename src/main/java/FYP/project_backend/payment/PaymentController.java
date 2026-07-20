package FYP.project_backend.payment;

import FYP.project_backend.enums.PaymentStatus;
import FYP.project_backend.license.License;
import FYP.project_backend.license.LicenseRepository;
import FYP.project_backend.enums.LicenseStatus;
import FYP.project_backend.notification.NotificationService;
import FYP.project_backend.notification.NotificationType;
import FYP.project_backend.payment.dto.PaymentActionRequest;
import FYP.project_backend.payment.dto.PaymentRequest;
import FYP.project_backend.user.User;
import FYP.project_backend.payment.dto.PaymentResponse;
import FYP.project_backend.user.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

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

    private final UserRepository userRepository;

    private final NotificationService notificationService;

    private PaymentResponse map(Payment payment){

        return PaymentResponse.builder()

                .id(payment.getId())

                .paymentNumber(payment.getPaymentNumber())

                .licenseNumber(payment.getLicense().getLicenseNumber())

                .amount(payment.getAmount())

                .paymentMethod(payment.getPaymentMethod())

                .transactionNumber(payment.getTransactionNumber())

                .status(payment.getStatus())

                .adminRemarks(payment.getAdminRemarks())

                .paymentDate(payment.getPaymentDate())

                .build();

    }

    //====================================================
    // MAKE PAYMENT
    //====================================================

    @PostMapping
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    public ResponseEntity<?> makePayment(

            @Valid
            @RequestBody PaymentRequest request){

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        User owner = userRepository

                .findByEmail(authentication.getName())

                .orElse(null);

        if(owner == null){

            return ResponseEntity.badRequest()

                    .body("Owner not found");

        }

        License license = licenseRepository
                .findById(request.getLicenseId())
                .orElse(null);

        if (license == null) {

            return ResponseEntity.badRequest()
                    .body("License not found.");

        }

        if (license.getStatus() != LicenseStatus.APPROVED) {

            return ResponseEntity.badRequest()
                    .body("License is not approved yet.");

        }

        if (!license.getOwner().getId().equals(owner.getId())) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("You are not allowed to pay for this license.");

        }

        long next = paymentRepository.count()+1;

        String paymentNumber =

                String.format(

                        "PAY-%d-%06d",

                        Year.now().getValue(),

                        next

                );

        Payment payment = Payment.builder()

                .paymentNumber(paymentNumber)

                .license(license)

                .owner(owner)

                .amount(license.getLicenseFee())

                .paymentMethod(request.getPaymentMethod())

                .transactionNumber(request.getTransactionNumber())

                .receipt(request.getReceipt())

                .status(PaymentStatus.PENDING)

                .paymentDate(LocalDateTime.now())

                .build();
        if (paymentRepository.existsByLicense(license)) {

            return ResponseEntity.badRequest()
                    .body("Payment already submitted for this license.");

        }

        paymentRepository.save(payment);

        return ResponseEntity.ok(

                map(payment)

        );

    }

    //====================================================
    // MY PAYMENTS
    //====================================================

    @GetMapping("/my")
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    public List<PaymentResponse> myPayments(){

        Authentication authentication =

                SecurityContextHolder

                        .getContext()

                        .getAuthentication();

        User owner = userRepository

                .findByEmail(authentication.getName())

                .orElseThrow();

        return paymentRepository

                .findByOwner(owner)

                .stream()

                .map(this::map)

                .toList();

    }

    //====================================================
    // GET ALL
    //====================================================

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<PaymentResponse> getAll(){

        return paymentRepository

                .findAll()

                .stream()

                .map(this::map)

                .toList();

    }

    //====================================================
    // GET PENDING
    //====================================================

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public List<PaymentResponse> pending(){

        return paymentRepository

                .findByStatus(PaymentStatus.PENDING)

                .stream()

                .map(this::map)

                .toList();

    }

    //====================================================
    // GET BY ID
    //====================================================

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getById(
            @PathVariable Long id){

        return paymentRepository

                .findById(id)

                .map(this::map)

                .map(ResponseEntity::ok)

                .orElse(ResponseEntity.notFound().build());

    }

    //====================================================
    // APPROVE PAYMENT
    //====================================================

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> approve(

            @PathVariable Long id,

            @RequestBody PaymentActionRequest request){

        Payment payment = paymentRepository

                .findById(id)

                .orElse(null);

        if(payment == null){

            return ResponseEntity.notFound().build();

        }

        payment.setStatus(PaymentStatus.APPROVED);

        payment.setAdminRemarks(request.getRemarks());

        payment.setVerifiedAt(LocalDateTime.now());

        paymentRepository.save(payment);
        License license = payment.getLicense();

        license.setRemarks("Payment verified successfully");

        licenseRepository.save(license);
        notificationService.notify(

                payment.getOwner(),

                "Payment Approved",

                "Payment Verified Successfully",

                "Your payment has been verified successfully. Your license payment has been received and confirmed by the system.",

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

    //====================================================
    // REJECT PAYMENT
    //====================================================

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> reject(

            @PathVariable Long id,

            @RequestBody PaymentActionRequest request){

        Payment payment = paymentRepository

                .findById(id)

                .orElse(null);

        if(payment == null){

            return ResponseEntity.notFound().build();

        }

        payment.setStatus(PaymentStatus.REJECTED);

        payment.setAdminRemarks(request.getRemarks());

        payment.setVerifiedAt(LocalDateTime.now());

        paymentRepository.save(payment);

        License license = payment.getLicense();

        license.setRemarks(request.getRemarks());

        licenseRepository.save(license);

        notificationService.notify(

                payment.getOwner(),

                "Payment Rejected",

                "Payment Verification Failed",

                "Unfortunately, your payment could not be verified. Please review the administrator remarks and submit a valid payment receipt.",

                NotificationType.PAYMENT,

                "Payment Number",

                payment.getPaymentNumber(),

                "View Payment",

                "http://localhost:4200/owner/payments"

        );

        return ResponseEntity.ok(

                map(payment)

        );

    }

    //====================================================
    // DELETE
    //====================================================

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(
            @PathVariable Long id){

        if(!paymentRepository.existsById(id)){

            return ResponseEntity.notFound().build();

        }

        paymentRepository.deleteById(id);

        return ResponseEntity.ok("Payment deleted successfully");

    }

}