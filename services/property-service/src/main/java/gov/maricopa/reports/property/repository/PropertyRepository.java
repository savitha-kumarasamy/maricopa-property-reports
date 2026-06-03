package gov.maricopa.reports.property.repository;

import gov.maricopa.reports.property.entity.PropertyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PropertyRepository extends JpaRepository<PropertyEntity, UUID> {
    List<PropertyEntity> findByUserIdOrderByCreatedAtDesc(UUID userId);
    Optional<PropertyEntity> findByIdAndUserId(UUID id, UUID userId);
    boolean existsByUserIdAndApn(UUID userId, String apn);
}
