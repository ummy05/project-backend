package FYP.project_backend.auth.dto;

import FYP.project_backend.enums.Role;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email address")
    private String email;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    @NotBlank(message = "Password is required")
    private String password;

    @NotNull(message = "Age is required")
    private Integer age;

    @NotBlank(message = "Gender is required")
    private String gender;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "Nationality is required")
    private String nationality;

    private Role role;


    // ==============================
    // BUSINESS INFORMATION
    // ==============================

    private String businessName;

    private String businessType;

    private String businessAddress;
}