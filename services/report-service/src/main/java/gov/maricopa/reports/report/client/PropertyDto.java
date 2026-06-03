package gov.maricopa.reports.report.client;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

public record PropertyDto(
        UUID id,
        String apn,
        String propertyAddress,
        String ownerName,
        String parcelType,
        OffsetDateTime lastSyncedAt,
        Map<String, Object> rawPropertyData,
        OffsetDateTime createdAt
) {
}
