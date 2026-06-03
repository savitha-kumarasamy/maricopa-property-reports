package gov.maricopa.reports.property.dto;

import java.util.List;

public record PropertySearchResponse(
        List<PropertySearchResult> results,
        int total,
        int page
) {
}
