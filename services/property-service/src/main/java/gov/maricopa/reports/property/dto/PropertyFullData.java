package gov.maricopa.reports.property.dto;

import java.util.Map;

/** Internal payload: the refreshed property plus the raw aggregated Maricopa data. */
public record PropertyFullData(
        PropertyResponse property,
        Map<String, Object> data
) {
}
