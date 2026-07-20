package FYP.project_backend.license;

import FYP.project_backend.enums.LicenseStatus;
import FYP.project_backend.license.dto.LicenseRequest;
import FYP.project_backend.notification.NotificationService;
import FYP.project_backend.user.User;
import FYP.project_backend.user.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;

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
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        User owner = userRepository.findByEmail(email)
                .orElse(null);

        if (owner == null) {
            return ResponseEntity.badRequest()
                    .body("Business owner not found.");
        }

        long next = repository.count() + 1;

        String licenseNumber = String.format(
                "LIC-%d-%06d",
                Year.now().getValue(),
                next
        );

        License license = License.builder()

                .licenseNumber(licenseNumber)

                .businessName(request.getBusinessName())

                .ownerName(owner.getFullName())

                .ownerEmail(owner.getEmail())

                .phoneNumber(request.getPhoneNumber())

                .licenseType(request.getLicenseType())

                .district(request.getDistrict())

                .location(request.getLocation())

                .licenseFee(request.getLicenseFee())

                .status(LicenseStatus.PENDING)

                .issueDate(null)

                .expiryDate(null)

                .createdAt(LocalDateTime.now())

                .owner(owner)

                .build();

        repository.save(license);

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

        License license = repository.findById(id)
                .orElse(null);

        if(license == null){

            return ResponseEntity.notFound().build();

        }

        license.setStatus(LicenseStatus.APPROVED);

        license.setIssueDate(LocalDate.now());

        license.setExpiryDate(LocalDate.now().plusYears(1));

        repository.save(license);


        return ResponseEntity.ok(license);

    }

    // ===========================
    // REJECT
    // ===========================

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> reject(
            @PathVariable Long id){

        License license = repository.findById(id)
                .orElse(null);

        if(license == null){

            return ResponseEntity.notFound().build();

        }

        license.setStatus(LicenseStatus.REJECTED);

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

}