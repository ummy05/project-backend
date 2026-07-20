package FYP.project_backend.inspection;

import FYP.project_backend.enums.InspectionStatus;
import FYP.project_backend.license.License;
import FYP.project_backend.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InspectionRepository
        extends JpaRepository<Inspection, Long> {

    List<Inspection> findByStatus(InspectionStatus status);

    List<Inspection> findByPerformedBy(User performedBy);

    List<Inspection> findByLicense(License license);

    long count();

    long countByStatus(InspectionStatus status);

}