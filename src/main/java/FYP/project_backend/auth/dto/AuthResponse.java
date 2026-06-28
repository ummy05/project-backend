package FYP.project_backend.auth.dto;
import FYP.project_backend.enums.Role;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {

    private String token;

    private String fullName;

    private String email;

    private Role role;

}
