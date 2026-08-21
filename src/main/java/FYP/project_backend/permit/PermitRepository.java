package FYP.project_backend.permit;

import FYP.project_backend.enums.PermitStatus;
import FYP.project_backend.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PermitRepository
        extends JpaRepository<Permit, Long> {

    Optional<Permit> findByControlNumber(String controlNumber);

    Optional<Permit> findByPermitNumber(String permitNumber);

    List<Permit> findByOwner(User owner);

    List<Permit> findBySheha(User sheha);

    List<Permit> findByStatus(PermitStatus status);

    long countByStatus(PermitStatus status);

}