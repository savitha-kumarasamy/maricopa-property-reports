package gov.maricopa.reports.schedule.repository;

import gov.maricopa.reports.schedule.entity.ReportScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReportScheduleRepository extends JpaRepository<ReportScheduleEntity, UUID> {
    List<ReportScheduleEntity> findByUserIdOrderByCreatedAtDesc(UUID userId);
    Optional<ReportScheduleEntity> findByIdAndUserId(UUID id, UUID userId);
    Optional<ReportScheduleEntity> findByPropertyIdAndUserIdAndActiveTrue(UUID propertyId, UUID userId);
    List<ReportScheduleEntity> findByActiveTrueAndNextRunAtLessThanEqual(OffsetDateTime now);
}
