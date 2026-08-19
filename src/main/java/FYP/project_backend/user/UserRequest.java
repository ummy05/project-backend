package FYP.project_backend.user;

import FYP.project_backend.enums.Role;
import lombok.Data;

@Data
public class UserRequest {

    private String fullName;

    private String email;

    private String phoneNumber;

    private String password;

    private Integer age;

    private String gender;

    private String address;

    private String profileImage;

    private Role role;


    // Tourist
    private String nationality;


    // Business Owner
    private String businessName;

    private String businessType;

    private String businessAddress;

    private String businessRegistrationNumber;


    private boolean enabled;
}