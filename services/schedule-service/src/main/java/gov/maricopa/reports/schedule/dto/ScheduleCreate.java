package gov.maricopa.reports.schedule.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ScheduleCreate(
        @NotNull UUID propertyId,
        @NotBlank String frequency
) {
}
