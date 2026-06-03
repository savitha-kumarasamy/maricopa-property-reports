package gov.maricopa.reports.property.dto;

import gov.maricopa.reports.property.entity.PropertyEntity;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

public record PropertyResponse(
        UUID id,
        String apn,
        String propertyAddress,
        String ownerName,
        String parcelType,
        OffsetDateTime lastSyncedAt,
        Map<String, Object> rawPropertyData,
        OffsetDateTime createdAt
) {
    public static PropertyResponse from(PropertyEntity p) {
        return new PropertyResponse(
                p.getId(),
                p.getApn(),
                p.getPropertyAddress(),
                p.getOwnerName(),
                p.getParcelType(),
                p.getLastSyncedAt(),
                p.getRawPropertyData(),
                p.getCreatedAt()
        );
    }
}
