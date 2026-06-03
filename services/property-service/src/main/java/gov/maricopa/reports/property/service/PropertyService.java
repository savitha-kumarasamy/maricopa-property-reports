package gov.maricopa.reports.property.service;

import gov.maricopa.reports.common.error.BadRequestException;
import gov.maricopa.reports.common.error.NotFoundException;
import gov.maricopa.reports.property.dto.PropertyFullData;
import gov.maricopa.reports.property.dto.PropertyResponse;
import gov.maricopa.reports.property.dto.PropertySearchResponse;
import gov.maricopa.reports.property.dto.PropertySearchResult;
import gov.maricopa.reports.property.entity.PropertyEntity;
import gov.maricopa.reports.property.maricopa.MaricopaApiClient;
import gov.maricopa.reports.property.repository.PropertyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class PropertyService {

    private final PropertyRepository repository;
    private final MaricopaApiClient maricopa;

    public PropertyService(PropertyRepository repository, MaricopaApiClient maricopa) {
        this.repository = repository;
        this.maricopa = maricopa;
    }

    @Transactional(readOnly = true)
    public List<PropertyEntity> list(UUID userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public PropertyEntity get(UUID userId, UUID id) {
        return repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NotFoundException("Property not found"));
    }

    @Transactional
    public PropertyEntity add(UUID userId, String apn) {
        if (repository.existsByUserIdAndApn(userId, apn)) {
            throw new BadRequestException("Property already tracked");
        }
        Map<String, Object> apiData = maricopa.fetchFullPropertyData(apn);

        PropertyEntity prop = new PropertyEntity();
        prop.setUserId(userId);
        prop.setApn(apn);
        prop.setPropertyAddress(deriveAddress(apiData));
        prop.setOwnerName(deriveOwner(apiData));
        prop.setParcelType(deriveParcelType(apiData));
        prop.setRawPropertyData(apiData);
        prop.setLastSyncedAt(OffsetDateTime.now(ZoneOffset.UTC));
        return repository.save(prop);
    }

    @Transactional
    public PropertyEntity sync(UUID userId, UUID id) {
        PropertyEntity prop = get(userId, id);
        Map<String, Object> apiData = maricopa.fetchFullPropertyData(prop.getApn());
        prop.setRawPropertyData(apiData);
        prop.setLastSyncedAt(OffsetDateTime.now(ZoneOffset.UTC));
        String owner = deriveOwner(apiData);
        if (owner != null) {
            prop.setOwnerName(owner);
        }
        String address = deriveAddress(apiData);
        if (address != null) {
            prop.setPropertyAddress(address);
        }
        return repository.save(prop);
    }

    @Transactional
    public void delete(UUID userId, UUID id) {
        PropertyEntity prop = get(userId, id);
        repository.delete(prop);
    }

    /**
     * Re-fetches Maricopa data and updates the property, returning both the property and the
     * raw aggregated data so the report-service can build a report snapshot.
     */
    @Transactional
    public PropertyFullData refreshForReport(UUID userId, UUID id) {
        PropertyEntity prop = get(userId, id);
        Map<String, Object> apiData = maricopa.fetchFullPropertyData(prop.getApn());

        Object parcel = apiData.get("parcel");
        boolean parcelOk = !(parcel instanceof Map<?, ?> m && m.containsKey("error"));
        if (parcelOk) {
            prop.setRawPropertyData(apiData);
            prop.setLastSyncedAt(OffsetDateTime.now(ZoneOffset.UTC));
            String owner = deriveOwner(apiData);
            if (owner != null) {
                prop.setOwnerName(owner);
            }
            String address = deriveAddress(apiData);
            if (address != null) {
                prop.setPropertyAddress(address);
            }
            repository.save(prop);
        }
        return new PropertyFullData(PropertyResponse.from(prop), apiData);
    }

    public PropertySearchResponse search(String query, int page) {
        Map<String, Object> data = maricopa.searchProperty(query, page);
        List<PropertySearchResult> results = new ArrayList<>();

        Object rawResults = data.getOrDefault("results", data.get("RealProperty"));
        if (rawResults instanceof List<?> list) {
            for (Object item : list) {
                if (item instanceof Map<?, ?> map) {
                    results.add(new PropertySearchResult(
                            stringValue(map, "APN", "apn"),
                            stringValue(map, "Address", "address"),
                            stringValue(map, "OwnerName", "owner_name"),
                            stringValue(map, "ParcelType", "parcel_type")
                    ));
                }
            }
        }

        int total = data.get("total") instanceof Number n ? n.intValue() : results.size();
        return new PropertySearchResponse(results, total, page);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> section(Map<String, Object> apiData, String key) {
        Object value = apiData.get(key);
        if (value instanceof Map<?, ?> map && !map.containsKey("error")) {
            return (Map<String, Object>) map;
        }
        return null;
    }

    private String deriveAddress(Map<String, Object> apiData) {
        Map<String, Object> address = section(apiData, "address");
        if (address == null) {
            return null;
        }
        List<String> parts = new ArrayList<>();
        for (String key : List.of("street_address", "city", "zip")) {
            Object part = address.get(key);
            if (part != null && !part.toString().isBlank()) {
                parts.add(part.toString());
            }
        }
        return parts.isEmpty() ? null : String.join(", ", parts);
    }

    private String deriveOwner(Map<String, Object> apiData) {
        Map<String, Object> owner = section(apiData, "owner");
        if (owner == null) {
            return null;
        }
        Object name = owner.getOrDefault("name", owner.get("owner_name"));
        return name != null ? name.toString() : null;
    }

    private String deriveParcelType(Map<String, Object> apiData) {
        Map<String, Object> parcel = section(apiData, "parcel");
        if (parcel == null) {
            return null;
        }
        Object type = parcel.getOrDefault("parcel_type", parcel.get("ParcelType"));
        return type != null ? type.toString() : null;
    }

    private String stringValue(Map<?, ?> map, String primary, String fallback) {
        Object value = map.get(primary);
        if (value == null) {
            value = map.get(fallback);
        }
        return value != null ? value.toString() : null;
    }
}
