package FYP.project_backend.complaint;

import FYP.project_backend.enums.ComplaintCategory;
import FYP.project_backend.enums.ComplaintStatus;
import FYP.project_backend.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComplaintRepository
        extends JpaRepository<Complaint, Long> {

    List<Complaint> findByStatus(ComplaintStatus status);

    List<Complaint> findByReportedBy(User user);

    List<Complaint> findByCategory(ComplaintCategory category);

    long countByStatus(ComplaintStatus status);
}