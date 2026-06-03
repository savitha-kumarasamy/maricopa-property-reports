package gov.maricopa.reports.report.repository;

import gov.maricopa.reports.report.entity.ReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReportRepository extends JpaRepository<ReportEntity, UUID> {
    List<ReportEntity> findByUserIdOrderByGeneratedAtDesc(UUID userId);
    Optional<ReportEntity> findByIdAndUserId(UUID id, UUID userId);
    long countByUserId(UUID userId);
}
