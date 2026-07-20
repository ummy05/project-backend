package FYP.project_backend.auth.dto;

import lombok.Data;

@Data
public class ResetPasswordRequest {

    private String email;

    private String otp;

    private String newPassword;

}