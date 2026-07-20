package FYP.project_backend.inspection.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class InspectionRequest {

    private Long licenseId;

    private Long inspectorId;

    private LocalDate inspectionDate;

    private String remarks;

}