package FYP.project_backend.user.dto;

import FYP.project_backend.enums.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {

    private Long id;

    private String fullName;

    private String email;

    private String phoneNumber;

    private Integer age;

    private String profileImage;

    private String gender;

    private String address;

    private Role role;

    private boolean enabled;


    // Tourist
    private String nationality;


    // Business Owner
    private String businessName;

    private String businessType;

    private String businessAddress;

    private String businessRegistrationNumber;
}