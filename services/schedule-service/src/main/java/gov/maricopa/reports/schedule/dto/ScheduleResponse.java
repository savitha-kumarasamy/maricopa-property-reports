package gov.maricopa.reports.schedule.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import gov.maricopa.reports.schedule.entity.ReportScheduleEntity;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ScheduleResponse(
        UUID id,
        UUID propertyId,
        String frequency,
        OffsetDateTime nextRunAt,
        @JsonProperty("is_active") boolean active,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static ScheduleResponse from(ReportScheduleEntity s) {
        return new ScheduleResponse(
                s.getId(),
                s.getPropertyId(),
                s.getFrequency(),
                s.getNextRunAt(),
                s.isActive(),
                s.getCreatedAt(),
                s.getUpdatedAt()
        );
    }
}
