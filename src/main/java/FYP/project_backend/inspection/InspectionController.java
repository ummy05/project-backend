package FYP.project_backend.inspection;

import FYP.project_backend.enums.InspectionStatus;
import FYP.project_backend.enums.LicenseStatus;
import FYP.project_backend.inspection.dto.InspectionRequest;
import FYP.project_backend.license.License;
import FYP.project_backend.license.LicenseRepository;
import FYP.project_backend.notification.NotificationService;
import FYP.project_backend.notification.NotificationType;
import FYP.project_backend.user.User;
import FYP.project_backend.user.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;

@RestController
@RequestMapping("/api/inspections")
@RequiredArgsConstructor
@CrossOrigin("*")
public class InspectionController {

    private final InspectionRepository inspectionRepository;

    private final LicenseRepository licenseRepository;

    private final UserRepository userRepository;

    private final NotificationService notificationService;

    //====================================================
    // CREATE INSPECTION
    //====================================================

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createInspection(

            @Valid
            @RequestBody InspectionRequest request){

        License license = licenseRepository

                .findById(request.getLicenseId())

                .orElse(null);

        if(license == null){

            return ResponseEntity.badRequest()

                    .body("License not found");

        }

        Authentication authentication =

                SecurityContextHolder

                        .getContext()

                        .getAuthentication();

        User admin = userRepository

                .findByEmail(authentication.getName())

                .orElse(null);

        long next = inspectionRepository.count()+1;

        String inspectionNumber =

                String.format(

                        "INSP-%d-%06d",

                        Year.now().getValue(),

                        next

                );

        Inspection inspection = Inspection.builder()

                .inspectionNumber(inspectionNumber)

                .license(license)

                .performedBy(admin)

                .inspectionDate(request.getInspectionDate())

                .remarks(request.getRemarks())

                .status(InspectionStatus.PENDING)

                .createdAt(LocalDateTime.now())

                .build();

        inspectionRepository.save(inspection);

        return ResponseEntity.ok(inspection);

    }

    //====================================================
    // GET ALL
    //====================================================

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<Inspection> getAll(){

        return inspectionRepository.findAll();

    }

    //====================================================
    // GET BY ID
    //====================================================

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getById(

            @PathVariable Long id){

        return inspectionRepository.findById(id)

                .map(ResponseEntity::ok)

                .orElse(ResponseEntity.notFound().build());

    }

    //====================================================
    // GET PENDING
    //====================================================

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Inspection> pending(){

        return inspectionRepository

                .findByStatus(

                        InspectionStatus.PENDING

                );

    }

    //====================================================
    // PASS INSPECTION
    //====================================================

    @PatchMapping("/{id}/pass")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> passInspection(

            @PathVariable Long id){

        Inspection inspection =

                inspectionRepository

                        .findById(id)

                        .orElse(null);

        if(inspection == null){

            return ResponseEntity.notFound().build();

        }

        inspection.setStatus(

                InspectionStatus.PASSED

        );

        inspectionRepository.save(inspection);

        License license = inspection.getLicense();

        license.setStatus(LicenseStatus.APPROVED);

        licenseRepository.save(license);

        notificationService.notify(

                license.getOwner(),

                "Inspection Passed",

                "Inspection Completed Successfully",

                "Congratulations! Your business inspection has been completed successfully and your license has been approved.",

                NotificationType.INSPECTION,

                "Inspection Number",

                inspection.getInspectionNumber(),

                "View License",

                "http://localhost:4200/owner/licenses"

        );

        return ResponseEntity.ok(inspection);

    }

    //====================================================
    // FAIL INSPECTION
    //====================================================

    @PatchMapping("/{id}/fail")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> failInspection(

            @PathVariable Long id){

        Inspection inspection =

                inspectionRepository

                        .findById(id)

                        .orElse(null);

        if(inspection == null){

            return ResponseEntity.notFound().build();

        }

        inspection.setStatus(

                InspectionStatus.FAILED

        );

        inspectionRepository.save(inspection);

        License license = inspection.getLicense();

        license.setStatus(

                LicenseStatus.REJECTED

        );

        licenseRepository.save(license);
        notificationService.notify(

                license.getOwner(),

                "Inspection Failed",

                "Inspection Failed",

                "Your business inspection did not meet the required coastal conservation standards. Please review the remarks and submit a new application after making the necessary improvements.",

                NotificationType.INSPECTION,

                "Inspection Number",

                inspection.getInspectionNumber(),

                "View Details",

                "http://localhost:4200/owner/licenses"

        );

        return ResponseEntity.ok(inspection);

    }

    //====================================================
    // DELETE
    //====================================================

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(

            @PathVariable Long id){

        if(!inspectionRepository.existsById(id)){

            return ResponseEntity.notFound().build();

        }

        inspectionRepository.deleteById(id);

        return ResponseEntity.ok(

                "Inspection deleted successfully"

        );

    }

}