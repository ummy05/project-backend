package FYP.project_backend.user;

import FYP.project_backend.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository
        extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    long countByRole(Role role);

    boolean existsByPhoneNumber(String phoneNumber);

    List<User> findByRole(Role role);

    List<User> findByFullNameContainingIgnoreCase(
            String keyword
    );

    List<User> findByEmailContainingIgnoreCase(
            String keyword
    );

    boolean existsByBusinessRegistrationNumber(
            String businessRegistrationNumber
    );


    // =====================================================
    // SHEHA
    // =====================================================

    Optional<User> findFirstByRoleAndShehiaIgnoreCase(
            Role role,
            String shehia
    );
}