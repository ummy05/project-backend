package FYP.project_backend.license;

import FYP.project_backend.enums.LicenseStatus;
import FYP.project_backend.license.dto.LicenseActionRequest;
import FYP.project_backend.license.dto.LicenseRequest;
import FYP.project_backend.notification.NotificationService;
import FYP.project_backend.notification.NotificationType;
import FYP.project_backend.user.User;
import FYP.project_backend.user.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import FYP.project_backend.enums.LicenseType;
import java.math.BigDecimal;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.pdf.PdfWriter;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.ByteArrayOutputStream;

@RestController
@RequestMapping("/api/licenses")
@RequiredArgsConstructor
@CrossOrigin("*")
public class LicenseController {

    private final LicenseRepository repository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    // ===========================
    // APPLY LICENSE
    // ===========================
    @PostMapping("/apply")
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    public ResponseEntity<?> applyLicense(
            @Valid @RequestBody LicenseRequest request) {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        User owner = userRepository
                .findByEmail(authentication.getName())
                .orElse(null);

        if (owner == null) {

            return ResponseEntity.badRequest()
                    .body("Business owner not found.");
        }

        long next = repository.count() + 1;

        String licenseNumber =
                String.format(
                        "LIC-%d-%06d",
                        Year.now().getValue(),
                        next
                );

        String controlNumber =
                String.format(
                        "CTL-LIC-%d-%08d",
                        Year.now().getValue(),
                        next
                );

        BigDecimal amount =
                calculateFee(
                        request.getLicenseType(),
                        request.getDurationMonths()
                );

        License license =
                License.builder()

                        .licenseNumber(licenseNumber)

                        .controlNumber(controlNumber)

                        .businessName(request.getBusinessName())

                        .ownerName(owner.getFullName())

                        .ownerEmail(owner.getEmail())

                        .phoneNumber(request.getPhoneNumber())

                        .licenseType(request.getLicenseType())

                        .district(request.getDistrict())

                        .location(request.getLocation())

                        .durationMonths(request.getDurationMonths())

                        .licenseFee(amount)

                        .paidAmount(BigDecimal.ZERO)

                        .status(LicenseStatus.PENDING)

                        .renewal(false)

                        .renewalCount(0)

                        .issueDate(null)

                        .expiryDate(null)

                        .createdAt(LocalDateTime.now())

                        .owner(owner)

                        .build();

        repository.save(license);

        // ==========================================
        // EMAIL PAYMENT INSTRUCTIONS
        // ==========================================

        notificationService.notify(

                owner,

                "License Payment Instructions",

                "License Application Created",

                "Dear " + owner.getFullName()
                        + ", your license application has been created successfully. "
                        + "Please make payment using the control number below. "
                        + "Required payment amount is TZS "
                        + amount
                        + ".",

                FYP.project_backend.notification.NotificationType.PAYMENT,

                "Control Number",

                controlNumber,

                "Make Payment",

                "http://localhost:4200/business-owner/payments"

        );

        return ResponseEntity.ok(license);
    }

    // ===========================
    // ADMIN GET ALL
    // ===========================

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<License> getAllLicenses() {

        return repository.findAll();

    }

    // ===========================
    // GET BY ID
    // ===========================

    @GetMapping("/{id}")
    public ResponseEntity<?> getLicense(
            @PathVariable Long id){

        return repository.findById(id)

                .map(ResponseEntity::ok)

                .orElse(ResponseEntity.notFound().build());

    }

    // ===========================
    // GET PENDING
    // ===========================

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public List<License> pendingLicenses(){

        return repository.findByStatus(
                LicenseStatus.PENDING
        );

    }
    @GetMapping("/calculate-fee")
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    public BigDecimal calculateLicenseFee(

            @RequestParam LicenseType type,

            @RequestParam Integer durationMonths){

        return calculateFee(

                type,

                durationMonths

        );

    }

    // ===========================
    // MY LICENSES
    // ===========================

    @GetMapping("/my")
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    public List<License> myLicenses() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        User owner = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        return repository.findByOwner(owner);

    }

    // ===========================
    // APPROVE
    // ===========================

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> approve(
            @PathVariable Long id){

        License license=

                repository.findById(id)

                        .orElse(null);

        if(license==null){

            return ResponseEntity.notFound().build();

        }

        license.setStatus(LicenseStatus.APPROVED);

        license.setIssueDate(LocalDate.now());

        license.setExpiryDate(

                LocalDate.now()

                        .plusMonths(

                                license.getDurationMonths()

                        )

        );

        repository.save(license);

        return ResponseEntity.ok(license);

    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> reject(

            @PathVariable Long id,

            @RequestBody LicenseActionRequest request){

        License license=

                repository.findById(id)

                        .orElse(null);

        if(license==null){

            return ResponseEntity.notFound().build();

        }

        license.setStatus(LicenseStatus.REJECTED);

        license.setRemarks(request.getReason());

        repository.save(license);

        return ResponseEntity.ok(license);

    }

    // ===========================
    // DELETE
    // ===========================

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(
            @PathVariable Long id){

        if(!repository.existsById(id)){

            return ResponseEntity.notFound().build();

        }

        repository.deleteById(id);

        return ResponseEntity.ok("License deleted successfully");

    }



    @PostMapping("/{id}/renew")
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    public ResponseEntity<?> renewLicense(

            @PathVariable Long id,

            @RequestParam Integer durationMonths){

        License license=

                repository.findById(id)

                        .orElse(null);

        if(license==null){

            return ResponseEntity.notFound().build();

        }

        BigDecimal fee=

                calculateFee(

                        license.getLicenseType(),

                        durationMonths

                );

        license.setRenewal(true);

        license.setDurationMonths(durationMonths);

        license.setLicenseFee(fee);

        license.setStatus(LicenseStatus.PENDING);

        repository.save(license);

        return ResponseEntity.ok(license);

    }

    private BigDecimal calculateFee(
            LicenseType type,
            Integer months){

        BigDecimal yearlyFee;

        switch(type){

            case BEACH_HOTEL ->
                    yearlyFee = new BigDecimal("600000");

            case TOUR_OPERATOR ->
                    yearlyFee = new BigDecimal("500000");

            case BOAT_OPERATOR ->
                    yearlyFee = new BigDecimal("450000");

            case FISHING ->
                    yearlyFee = new BigDecimal("300000");

            case BEACH_RESTAURANT ->
                    yearlyFee = new BigDecimal("400000");

            case WATER_SPORTS ->
                    yearlyFee = new BigDecimal("550000");

            case BEACH_EVENT ->
                    yearlyFee = new BigDecimal("350000");

            default ->
                    yearlyFee = new BigDecimal("250000");

        }

        return yearlyFee

                .multiply(BigDecimal.valueOf(months))

                .divide(
                        BigDecimal.valueOf(12),
                        2,
                        java.math.RoundingMode.HALF_UP
                );

    }

    // =====================================================
// BUSINESS OWNER - DOWNLOAD LICENSE PDF
// =====================================================

    @GetMapping("/{id}/pdf")
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    public ResponseEntity<?> downloadLicensePdf(
            @PathVariable Long id) {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        User owner =
                userRepository
                        .findByEmail(authentication.getName())
                        .orElse(null);

        if (owner == null) {

            return ResponseEntity
                    .badRequest()
                    .body("Business owner not found.");
        }


        License license =
                repository
                        .findById(id)
                        .orElse(null);

        if (license == null) {

            return ResponseEntity
                    .notFound()
                    .build();
        }


        // =====================================================
        // SECURITY - ONLY OWNER CAN DOWNLOAD
        // =====================================================

        if (license.getOwner() == null ||
                !license.getOwner()
                        .getId()
                        .equals(owner.getId())) {

            return ResponseEntity
                    .status(403)
                    .body(
                            "You cannot download this license."
                    );
        }


        // =====================================================
        // ONLY APPROVED LICENSE
        // =====================================================

        if (license.getStatus() !=
                LicenseStatus.APPROVED) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            "Only approved licenses can be downloaded."
                    );
        }


        // =====================================================
        // PAYMENT MUST BE COMPLETED
        // =====================================================

        BigDecimal paid =
                license.getPaidAmount() == null
                        ? BigDecimal.ZERO
                        : license.getPaidAmount();


        BigDecimal fee =
                license.getLicenseFee() == null
                        ? BigDecimal.ZERO
                        : license.getLicenseFee();


        if (paid.compareTo(fee) < 0) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            "License payment must be completed before downloading."
                    );
        }


        try {

            ByteArrayOutputStream output =
                    new ByteArrayOutputStream();


            Document document =
                    new Document();


            PdfWriter.getInstance(
                    document,
                    output
            );


            document.open();


            Font titleFont =
                    FontFactory.getFont(
                            FontFactory.HELVETICA_BOLD,
                            20
                    );


            Font normalFont =
                    FontFactory.getFont(
                            FontFactory.HELVETICA,
                            11
                    );


            document.add(
                    new Paragraph(
                            "COASTAL CONSERVATION AND REVENUE MONITORING SYSTEM",
                            titleFont
                    )
            );


            document.add(
                    new Paragraph(
                            "BUSINESS LICENSE",
                            titleFont
                    )
            );


            document.add(
                    new Paragraph(
                            " "
                    )
            );


            document.add(
                    new Paragraph(
                            "License Number: "
                                    + license.getLicenseNumber(),
                            normalFont
                    )
            );


            document.add(
                    new Paragraph(
                            "Control Number: "
                                    + license.getControlNumber(),
                            normalFont
                    )
            );


            document.add(
                    new Paragraph(
                            "Business Name: "
                                    + license.getBusinessName(),
                            normalFont
                    )
            );


            document.add(
                    new Paragraph(
                            "Owner Name: "
                                    + license.getOwnerName(),
                            normalFont
                    )
            );


            document.add(
                    new Paragraph(
                            "Owner Email: "
                                    + license.getOwnerEmail(),
                            normalFont
                    )
            );


            document.add(
                    new Paragraph(
                            "Phone Number: "
                                    + license.getPhoneNumber(),
                            normalFont
                    )
            );


            document.add(
                    new Paragraph(
                            "License Type: "
                                    + license.getLicenseType(),
                            normalFont
                    )
            );


            document.add(
                    new Paragraph(
                            "District: "
                                    + license.getDistrict(),
                            normalFont
                    )
            );


            document.add(
                    new Paragraph(
                            "Location: "
                                    + license.getLocation(),
                            normalFont
                    )
            );


            document.add(
                    new Paragraph(
                            "Duration: "
                                    + license.getDurationMonths()
                                    + " Months",
                            normalFont
                    )
            );


            document.add(
                    new Paragraph(
                            "License Fee: TZS "
                                    + license.getLicenseFee(),
                            normalFont
                    )
            );


            document.add(
                    new Paragraph(
                            "Paid Amount: TZS "
                                    + license.getPaidAmount(),
                            normalFont
                    )
            );


            document.add(
                    new Paragraph(
                            "Issue Date: "
                                    + license.getIssueDate(),
                            normalFont
                    )
            );


            document.add(
                    new Paragraph(
                            "Expiry Date: "
                                    + license.getExpiryDate(),
                            normalFont
                    )
            );


            document.add(
                    new Paragraph(
                            "Status: "
                                    + license.getStatus(),
                            normalFont
                    )
            );


            document.add(
                    new Paragraph(
                            " "
                    )
            );


            document.add(
                    new Paragraph(
                            "This license has been officially issued through the Coastal Conservation and Revenue Monitoring System.",
                            normalFont
                    )
            );


            document.close();


            byte[] pdf =
                    output.toByteArray();


            return ResponseEntity.ok()

                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\""
                                    + license.getLicenseNumber()
                                    + ".pdf\""
                    )

                    .contentType(
                            MediaType.APPLICATION_PDF
                    )

                    .contentLength(
                            pdf.length
                    )

                    .body(pdf);


        } catch (DocumentException e) {

            return ResponseEntity
                    .internalServerError()
                    .body(
                            "Failed to generate license PDF."
                    );

        }

    }
}