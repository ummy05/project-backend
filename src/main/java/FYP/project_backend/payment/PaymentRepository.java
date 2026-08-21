package FYP.project_backend.payment;

import FYP.project_backend.enums.PaymentStatus;
import FYP.project_backend.license.License;
import FYP.project_backend.permit.Permit;
import FYP.project_backend.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository
        extends JpaRepository<Payment, Long> {

    List<Payment> findByOwner(User owner);

    List<Payment> findByStatus(PaymentStatus status);

    List<Payment> findByLicense(License license);

    List<Payment> findByPermit(Permit permit);

    boolean existsByLicense(License license);

    boolean existsByPermit(Permit permit);

    long count();

    long countByStatus(PaymentStatus status);
}