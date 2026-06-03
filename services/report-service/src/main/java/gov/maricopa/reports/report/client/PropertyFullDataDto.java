package gov.maricopa.reports.report.client;

import java.util.Map;

public record PropertyFullDataDto(
        PropertyDto property,
        Map<String, Object> data
) {
}
