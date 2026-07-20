package FYP.project_backend.inspection.dto;
import FYP.project_backend.enums.InspectionStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class InspectionResponse {

    private Long id;

    private String inspectionNumber;

    private String licenseNumber;

    private String businessName;

    private String inspector;

    private LocalDate inspectionDate;

    private InspectionStatus status;

    private String remarks;

}