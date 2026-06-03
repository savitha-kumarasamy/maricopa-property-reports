package gov.maricopa.reports.property.dto;

public record PropertySearchResult(
        String apn,
        String address,
        String ownerName,
        String parcelType
) {
}
