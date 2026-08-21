package FYP.project_backend.user;

import FYP.project_backend.enums.Role;
import FYP.project_backend.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin("*")
public class UserController {

    private final UserRepository repository;
    private final PasswordEncoder encoder;


    // =====================================================
    // MAP USER → RESPONSE
    // =====================================================

    private UserResponse map(User user) {

        return UserResponse.builder()

                .id(user.getId())

                .fullName(user.getFullName())

                .email(user.getEmail())

                .phoneNumber(user.getPhoneNumber())

                .age(user.getAge())

                .profileImage(user.getProfileImage())

                .gender(user.getGender())

                .address(user.getAddress())

                .nationality(user.getNationality())

                .role(user.getRole())

                .enabled(user.isEnabled())

                .businessName(user.getBusinessName())

                .businessType(user.getBusinessType())

                .businessAddress(user.getBusinessAddress())

                .businessRegistrationNumber(
                        user.getBusinessRegistrationNumber()
                )

                // SHEHA
                .shehia(user.getShehia())

                .build();
    }


    // =====================================================
    // GET ALL USERS
    // =====================================================

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getAllUsers() {

        return repository.findAll()

                .stream()

                .map(this::map)

                .toList();
    }


    // =====================================================
    // CREATE USER
    // ADMIN
    // =====================================================

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createUser(
            @RequestBody UserRequest request) {

        // ==========================================
        // ROLE VALIDATION
        // ==========================================

        if (request.getRole() == null) {

            return ResponseEntity
                    .badRequest()
                    .body("User role is required.");
        }


        // ==========================================
        // EMAIL CHECK
        // ==========================================

        if (repository.findByEmail(request.getEmail()).isPresent()) {

            return ResponseEntity
                    .badRequest()
                    .body("Email already exists");
        }


        // ==========================================
        // PHONE CHECK
        // ==========================================

        if (request.getPhoneNumber() != null &&
                repository.existsByPhoneNumber(
                        request.getPhoneNumber())) {

            return ResponseEntity
                    .badRequest()
                    .body("Phone number already exists");
        }


        // ==========================================
        // BUSINESS REGISTRATION CHECK
        // ==========================================

        if (request.getBusinessRegistrationNumber() != null &&
                !request.getBusinessRegistrationNumber().isBlank() &&
                repository.existsByBusinessRegistrationNumber(
                        request.getBusinessRegistrationNumber())) {

            return ResponseEntity
                    .badRequest()
                    .body("Business registration number already exists");
        }


        // ==========================================
        // SHEHA VALIDATION
        // ==========================================

        if (request.getRole() == Role.SHEHA) {

            if (request.getShehia() == null ||
                    request.getShehia().isBlank()) {

                return ResponseEntity
                        .badRequest()
                        .body("Shehia is required for a Sheha.");
            }
        }


        // ==========================================
        // CREATE USER
        // ==========================================

        User user = User.builder()

                .fullName(request.getFullName())

                .email(request.getEmail())

                .phoneNumber(request.getPhoneNumber())

                .password(
                        encoder.encode(
                                request.getPassword()
                        )
                )

                .age(request.getAge())

                .gender(request.getGender())

                .address(request.getAddress())

                .nationality(request.getNationality())

                .profileImage(request.getProfileImage())

                .role(request.getRole())

                .businessName(request.getBusinessName())

                .businessType(request.getBusinessType())

                .businessAddress(request.getBusinessAddress())

                .businessRegistrationNumber(
                        request.getBusinessRegistrationNumber()
                )

                .shehia(request.getShehia())

                .enabled(request.isEnabled())

                .build();


        repository.save(user);


        return ResponseEntity.ok(
                map(user)
        );
    }


    // =====================================================
    // GET USER BY ID
    // =====================================================

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(
            @PathVariable Long id) {

        return repository.findById(id)

                .map(this::map)

                .map(ResponseEntity::ok)

                .orElse(
                        ResponseEntity.notFound().build()
                );
    }


    // =====================================================
    // UPDATE USER
    // =====================================================

    @PutMapping("/{id}")
    @PreAuthorize(
            "hasAnyRole('ADMIN','BUSINESS_OWNER','TOURIST','SHEHA')"
    )
    public ResponseEntity<?> updateUser(

            @PathVariable Long id,

            @RequestBody UserRequest request) {

        User user = repository.findById(id)
                .orElse(null);

        if (user == null) {

            return ResponseEntity
                    .notFound()
                    .build();
        }


        user.setFullName(
                request.getFullName()
        );

        user.setPhoneNumber(
                request.getPhoneNumber()
        );

        user.setAge(
                request.getAge()
        );

        user.setGender(
                request.getGender()
        );

        user.setAddress(
                request.getAddress()
        );

        user.setNationality(
                request.getNationality()
        );

        user.setBusinessName(
                request.getBusinessName()
        );

        user.setBusinessType(
                request.getBusinessType()
        );

        user.setBusinessAddress(
                request.getBusinessAddress()
        );

        user.setBusinessRegistrationNumber(
                request.getBusinessRegistrationNumber()
        );

        user.setProfileImage(
                request.getProfileImage()
        );

        user.setRole(
                request.getRole()
        );

        // SHEHA
        user.setShehia(
                request.getShehia()
        );


        repository.save(user);


        return ResponseEntity.ok(
                map(user)
        );
    }


    // =====================================================
    // CHANGE STATUS
    // =====================================================

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> changeStatus(
            @PathVariable Long id) {

        User user = repository.findById(id)
                .orElse(null);

        if (user == null) {

            return ResponseEntity
                    .notFound()
                    .build();
        }

        user.setEnabled(
                !user.isEnabled()
        );

        repository.save(user);


        return ResponseEntity.ok(
                map(user)
        );
    }


    // =====================================================
    // GET USERS BY ROLE
    // =====================================================

    @GetMapping("/role/{role}")
    public List<UserResponse> getByRole(
            @PathVariable Role role) {

        return repository

                .findByRole(role)

                .stream()

                .map(this::map)

                .toList();
    }


    // =====================================================
    // SEARCH
    // =====================================================

    @GetMapping("/search")
    public List<UserResponse> search(
            @RequestParam String keyword) {

        return repository

                .findByFullNameContainingIgnoreCase(
                        keyword
                )

                .stream()

                .map(this::map)

                .toList();
    }


    // =====================================================
    // DELETE
    // =====================================================

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(
            @PathVariable Long id) {

        if (!repository.existsById(id)) {

            return ResponseEntity
                    .notFound()
                    .build();
        }

        repository.deleteById(id);

        return ResponseEntity.ok(
                "User deleted successfully"
        );
    }
}