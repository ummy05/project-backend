package FYP.project_backend.auth.dto;
import FYP.project_backend.enums.Role;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank
    private String fullName;

    @Email
    private String email;

    @NotBlank
    private String phoneNumber;

    @NotBlank
    private String password;

    private Integer age;

    private String gender;

    private String address;

    private Role role;

}
