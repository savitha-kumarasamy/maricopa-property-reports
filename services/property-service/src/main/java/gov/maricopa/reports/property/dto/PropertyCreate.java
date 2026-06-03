package gov.maricopa.reports.property.dto;

import jakarta.validation.constraints.NotBlank;

public record PropertyCreate(
        @NotBlank String apn
) {
}
