package FYP.project_backend.auth.dto;

import lombok.Data;

@Data
public class VerifyOtpRequest {

    private String email;

    private String otp;

}