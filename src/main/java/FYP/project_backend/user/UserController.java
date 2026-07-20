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

    private UserResponse map(User user){

        return UserResponse.builder()

                .id(user.getId())

                .fullName(user.getFullName())

                .email(user.getEmail())

                .phoneNumber(user.getPhoneNumber())

                .age(user.getAge())

                .profileImage(user.getProfileImage())

                .gender(user.getGender())

                .address(user.getAddress())

                .role(user.getRole())

                .enabled(user.isEnabled())

                .build();

    }



    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getAllUsers(){

        return repository.findAll()

                .stream()

                .map(this::map)

                .toList();

    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createUser(
            @RequestBody UserRequest request){

        if(repository.findByEmail(request.getEmail()).isPresent()){

            return ResponseEntity
                    .badRequest()
                    .body("Email already exists");

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

                .profileImage(request.getProfileImage())

                .role(request.getRole())

                .enabled(request.isEnabled())

                .build();

        repository.save(user);

        return ResponseEntity.ok(
                map(user)
        );

    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(

            @PathVariable Long id){

        return repository.findById(id)

                .map(this::map)

                .map(ResponseEntity::ok)

                .orElse(ResponseEntity.notFound().build());

    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','BUSINESS_OWNER','TOURIST')")
    public ResponseEntity<?> updateUser(

            @PathVariable Long id,

            @RequestBody UserRequest request){

        User user = repository.findById(id)
                .orElse(null);

        if(user == null){

            return ResponseEntity.notFound().build();

        }

        user.setFullName(request.getFullName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setAge(request.getAge());
        user.setGender(request.getGender());
        user.setAddress(request.getAddress());
        user.setProfileImage(
                request.getProfileImage()
        );
        user.setRole(request.getRole());

        repository.save(user);

        return ResponseEntity.ok(map(user));

    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> changeStatus(
            @PathVariable Long id){

        User user = repository.findById(id)
                .orElse(null);

        if(user == null){

            return ResponseEntity.notFound().build();

        }

        user.setEnabled(!user.isEnabled());

        repository.save(user);

        return ResponseEntity.ok(map(user));

    }

    @GetMapping("/role/{role}")
    public List<UserResponse> getByRole(
            @PathVariable Role role){

        return repository

                .findByRole(role)

                .stream()

                .map(this::map)

                .toList();

    }

    @GetMapping("/search")
    public List<UserResponse>search(
            @RequestParam String keyword){

        return repository

                .findByFullNameContainingIgnoreCase(keyword)

                .stream()

                .map(this::map)

                .toList();

    }



    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(
            @PathVariable Long id){

        if(!repository.existsById(id)){

            return ResponseEntity.notFound().build();

        }

        repository.deleteById(id);

        return ResponseEntity.ok("User deleted successfully");

    }

}
