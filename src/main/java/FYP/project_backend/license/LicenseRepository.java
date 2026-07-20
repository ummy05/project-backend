package FYP.project_backend.license;

import FYP.project_backend.enums.LicenseStatus;
import FYP.project_backend.enums.LicenseType;
import FYP.project_backend.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LicenseRepository
        extends JpaRepository<License, Long> {

    Optional<License> findByLicenseNumber(String licenseNumber);

    List<License> findByOwner(User owner);

    List<License> findByStatus(LicenseStatus status);

    List<License> findByLicenseType(LicenseType licenseType);
    List<License> findAll();

    long count();

    long countByStatus(LicenseStatus status);

}