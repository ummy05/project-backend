package FYP.project_backend.complaint.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ComplaintActionRequest {

    @NotBlank
    private String response;

}