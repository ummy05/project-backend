package FYP.project_backend.user;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import FYP.project_backend.enums.Role;
import FYP.project_backend.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin("*")
public class UserController {

    private final UserRepository repository;
    private final PasswordEncoder encoder;


    // =====================================================
    // PROFILE IMAGE DIRECTORY
    // =====================================================

    private final Path profileImageDirectory =
            Paths.get("uploads/profile-images");


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
    // =====================================================

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createUser(
            @RequestBody UserRequest request) {

        if (request.getRole() == null) {

            return ResponseEntity
                    .badRequest()
                    .body("User role is required.");
        }

        if (repository.findByEmail(request.getEmail()).isPresent()) {

            return ResponseEntity
                    .badRequest()
                    .body("Email already exists");
        }

        if (request.getPhoneNumber() != null &&
                repository.existsByPhoneNumber(
                        request.getPhoneNumber())) {

            return ResponseEntity
                    .badRequest()
                    .body("Phone number already exists");
        }

        if (request.getBusinessRegistrationNumber() != null &&
                !request.getBusinessRegistrationNumber().isBlank() &&
                repository.existsByBusinessRegistrationNumber(
                        request.getBusinessRegistrationNumber())) {

            return ResponseEntity
                    .badRequest()
                    .body("Business registration number already exists");
        }

        if (request.getRole() == Role.SHEHA) {

            if (request.getShehia() == null ||
                    request.getShehia().isBlank()) {

                return ResponseEntity
                        .badRequest()
                        .body("Shehia is required for a Sheha.");
            }
        }

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
    // =====================================================
// UPLOAD CURRENT USER PROFILE IMAGE
// =====================================================

    @PostMapping(
            value = "/me/profile-image",
            consumes = "multipart/form-data"
    )
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> uploadMyProfileImage(
            @RequestParam("file") MultipartFile file) {

        try {

            // ==========================================
            // VALIDATE FILE
            // ==========================================

            if (file == null || file.isEmpty()) {

                return ResponseEntity
                        .badRequest()
                        .body("Profile image is required.");
            }


            // ==========================================
            // VALIDATE IMAGE TYPE
            // ==========================================

            String contentType =
                    file.getContentType();

            if (
                    contentType == null ||
                            !contentType.startsWith("image/")
            ) {

                return ResponseEntity
                        .badRequest()
                        .body("Only image files are allowed.");
            }


            // ==========================================
            // MAX 5MB
            // ==========================================

            if (file.getSize() > 5 * 1024 * 1024) {

                return ResponseEntity
                        .badRequest()
                        .body("Profile image must not exceed 5MB.");
            }


            // ==========================================
            // GET CURRENT LOGGED-IN USER
            // ==========================================

            Authentication authentication =
                    SecurityContextHolder
                            .getContext()
                            .getAuthentication();


            if (
                    authentication == null ||
                            !authentication.isAuthenticated()
            ) {

                return ResponseEntity
                        .status(401)
                        .body("Authentication required.");
            }


            String email =
                    authentication.getName();


            User user =
                    repository
                            .findByEmail(email)
                            .orElse(null);


            if (user == null) {

                return ResponseEntity
                        .notFound()
                        .build();
            }


            // ==========================================
            // CREATE UPLOAD DIRECTORY
            // ==========================================

            Path uploadDirectory =
                    Paths.get(
                            "uploads",
                            "profile-images"
                    );


            Files.createDirectories(
                    uploadDirectory
            );


            // ==========================================
            // GET FILE EXTENSION
            // ==========================================

            String originalName =
                    file.getOriginalFilename();


            String extension = ".jpg";


            if (
                    originalName != null &&
                            originalName.contains(".")
            ) {

                extension =
                        originalName.substring(
                                originalName.lastIndexOf(".")
                        );
            }


            // ==========================================
            // GENERATE UNIQUE FILE NAME
            // ==========================================

            String fileName =
                    UUID.randomUUID()
                            .toString()
                            .replace("-", "")
                            + extension;


            // ==========================================
            // FILE PATH
            // ==========================================

            Path filePath =
                    uploadDirectory.resolve(
                            fileName
                    );


            // ==========================================
            // SAVE FILE
            // ==========================================

            Files.copy(

                    file.getInputStream(),

                    filePath,

                    StandardCopyOption.REPLACE_EXISTING

            );


            // ==========================================
            // DELETE OLD IMAGE
            // ==========================================

            String oldImage =
                    user.getProfileImage();


            if (
                    oldImage != null &&
                            !oldImage.isBlank() &&
                            oldImage.startsWith(
                                    "/uploads/profile-images/"
                            )
            ) {

                try {

                    Path oldPath =
                            Paths.get(
                                    oldImage.substring(1)
                            );


                    Files.deleteIfExists(
                            oldPath
                    );

                }
                catch (Exception ignored) {

                    // Do not fail upload
                    // because old image
                    // could not be deleted.

                }

            }


            // ==========================================
            // SAVE IMAGE PATH TO DATABASE
            // ==========================================

            String imageUrl =
                    "/uploads/profile-images/"
                            + fileName;


            user.setProfileImage(
                    imageUrl
            );


            repository.save(user);


            // ==========================================
            // RESPONSE
            // ==========================================

            return ResponseEntity.ok(
                    map(user)
            );

        }
        catch (IOException ex) {

            ex.printStackTrace();

            return ResponseEntity
                    .status(500)
                    .body(
                            "Failed to save profile image."
                    );

        }
    }

}