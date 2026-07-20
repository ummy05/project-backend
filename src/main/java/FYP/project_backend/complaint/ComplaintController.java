package FYP.project_backend.complaint;

import FYP.project_backend.complaint.dto.ComplaintActionRequest;
import FYP.project_backend.complaint.dto.ComplaintRequest;
import FYP.project_backend.complaint.dto.ComplaintResponse;
import FYP.project_backend.enums.ComplaintStatus;
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
@RequestMapping("/api/complaints")
@RequiredArgsConstructor
@CrossOrigin("*")
public class ComplaintController {

    private final ComplaintRepository repository;

    private final UserRepository userRepository;

    private final NotificationService notificationService;

    private ComplaintResponse map(Complaint complaint){

        return ComplaintResponse.builder()

                .id(complaint.getId())

                .complaintNumber(complaint.getComplaintNumber())

                .title(complaint.getTitle())

                .description(complaint.getDescription())

                .category(complaint.getCategory())

                .location(complaint.getLocation())

                .imageUrl(complaint.getImageUrl())

                .status(complaint.getStatus())

                .adminResponse(complaint.getAdminResponse())

                .reportedAt(complaint.getReportedAt())

                .resolvedAt(complaint.getResolvedAt())

                .build();

    }

    //====================================================
    // REPORT COMPLAINT
    //====================================================

    @PostMapping
    @PreAuthorize("hasRole('TOURIST')")
    public ResponseEntity<?> reportComplaint(

            @Valid
            @RequestBody ComplaintRequest request){

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        User tourist = userRepository
                .findByEmail(authentication.getName())
                .orElse(null);

        if(tourist == null){

            return ResponseEntity.badRequest()
                    .body("Tourist not found.");

        }

        long next = repository.count()+1;

        String complaintNumber = String.format(

                "CMP-%d-%06d",

                Year.now().getValue(),

                next

        );

        Complaint complaint = Complaint.builder()

                .complaintNumber(complaintNumber)

                .title(request.getTitle())

                .description(request.getDescription())

                .category(request.getCategory())

                .location(request.getLocation())

                .imageUrl(request.getImageUrl())

                .status(ComplaintStatus.PENDING)

                .reportedAt(LocalDateTime.now())

                .reportedBy(tourist)

                .build();

        repository.save(complaint);

        return ResponseEntity.ok(map(complaint));

    }

    //====================================================
    // MY COMPLAINTS
    //====================================================

    @GetMapping("/my")
    @PreAuthorize("hasRole('TOURIST')")
    public List<ComplaintResponse> myComplaints(){

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        User tourist = userRepository
                .findByEmail(authentication.getName())
                .orElseThrow();

        return repository

                .findByReportedBy(tourist)

                .stream()

                .map(this::map)

                .toList();

    }

    //====================================================
    // GET ALL
    //====================================================

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<ComplaintResponse> getAll(){

        return repository

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
    public List<ComplaintResponse> pending(){

        return repository

                .findByStatus(ComplaintStatus.PENDING)

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

        return repository

                .findById(id)

                .map(this::map)

                .map(ResponseEntity::ok)

                .orElse(ResponseEntity.notFound().build());

    }

    //====================================================
    // MARK IN PROGRESS
    //====================================================

    @PatchMapping("/{id}/progress")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> progress(
            @PathVariable Long id){

        Complaint complaint =
                repository.findById(id)
                        .orElse(null);

        if(complaint == null){

            return ResponseEntity.notFound().build();

        }

        complaint.setStatus(
                ComplaintStatus.IN_PROGRESS
        );

        repository.save(complaint);
        notificationService.notify(

                complaint.getReportedBy(),

                "Complaint Under Review",

                "Complaint Received",

                "Your complaint has been received successfully and is currently under review by our environmental officers.",

                NotificationType.COMPLAINT,

                "Complaint Number",

                complaint.getComplaintNumber(),

                "View Complaint",

                "http://localhost:4200/tourist/my-reports"

        );

        return ResponseEntity.ok(

                map(complaint)

        );

    }

    //====================================================
    // RESOLVE
    //====================================================

    @PatchMapping("/{id}/resolve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> resolve(

            @PathVariable Long id,

            @RequestBody ComplaintActionRequest request){

        Complaint complaint =
                repository.findById(id)
                        .orElse(null);

        if(complaint == null){

            return ResponseEntity.notFound().build();

        }

        complaint.setStatus(
                ComplaintStatus.RESOLVED
        );

        complaint.setAdminResponse(
                request.getResponse()
        );

        complaint.setResolvedAt(
                LocalDateTime.now()
        );

        repository.save(complaint);

        notificationService.notify(

                complaint.getReportedBy(),

                "Complaint Resolved",

                "Complaint Resolved Successfully",

                request.getResponse(),

                NotificationType.COMPLAINT,

                "Complaint Number",

                complaint.getComplaintNumber(),

                "View Complaint",

                "http://localhost:4200/tourist/my-reports"

        );

        return ResponseEntity.ok(

                map(complaint)

        );

    }
    //====================================================
    // REJECT
    //====================================================

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> reject(

            @PathVariable Long id,

            @RequestBody ComplaintActionRequest request){

        Complaint complaint =
                repository.findById(id)
                        .orElse(null);

        if(complaint == null){

            return ResponseEntity.notFound().build();

        }

        complaint.setStatus(
                ComplaintStatus.REJECTED
        );

        complaint.setAdminResponse(
                request.getResponse()
        );

        complaint.setResolvedAt(
                LocalDateTime.now()
        );

        repository.save(complaint);

        notificationService.notify(

                complaint.getReportedBy(),

                "Complaint Closed",

                "Complaint Rejected",

                request.getResponse(),

                NotificationType.COMPLAINT,

                "Complaint Number",

                complaint.getComplaintNumber(),

                "View Complaint",

                "http://localhost:4200/tourist/my-reports"

        );

        return ResponseEntity.ok(

                map(complaint)

        );

    }

    //====================================================
    // DELETE
    //====================================================

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(
            @PathVariable Long id){

        if(!repository.existsById(id)){

            return ResponseEntity.notFound().build();

        }

        repository.deleteById(id);

        return ResponseEntity.ok(
                "Complaint deleted successfully"
        );

    }

}