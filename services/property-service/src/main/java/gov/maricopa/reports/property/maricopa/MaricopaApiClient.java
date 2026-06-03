package gov.maricopa.reports.property.maricopa;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * Thin client over the Maricopa County Assessor REST API. Each call returns the raw JSON
 * payload as a Map, or {@code {"error": ...}} on failure, mirroring the original FastAPI service.
 */
@Component
public class MaricopaApiClient {

    private static final Logger log = LoggerFactory.getLogger(MaricopaApiClient.class);
    private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE =
            new ParameterizedTypeReference<>() {
            };

    private final RestClient restClient;
    private final MaricopaProperties properties;

    public MaricopaApiClient(MaricopaProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .build();
    }

    private Map<String, Object> get(String uri) {
        try {
            Map<String, Object> body = restClient.get()
                    .uri(uri)
                    .headers(this::applyHeaders)
                    .retrieve()
                    .body(MAP_TYPE);
            return body != null ? body : Map.of();
        } catch (Exception ex) {
            log.error("Maricopa request failed for {}: {}", uri, ex.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("error", ex.getMessage());
            return error;
        }
    }

    private void applyHeaders(HttpHeaders headers) {
        headers.set(HttpHeaders.USER_AGENT, "");
        if (properties.getToken() != null && !properties.getToken().isBlank()) {
            headers.set("AUTHORIZATION", properties.getToken());
        }
    }

    public Map<String, Object> searchProperty(String query, int page) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/search/property/").queryParam("q", query);
        if (page > 1) {
            builder.queryParam("page", page);
        }
        Map<String, Object> result = get(builder.build().toUriString());
        if (result.containsKey("error")) {
            Map<String, Object> fallback = new HashMap<>(result);
            fallback.put("results", java.util.List.of());
            fallback.put("total", 0);
            return fallback;
        }
        return result;
    }

    public Map<String, Object> getParcelDetails(String apn) {
        return get("/parcel/" + apn);
    }

    public Map<String, Object> getPropertyInfo(String apn) {
        return get("/parcel/" + apn + "/propertyinfo");
    }

    public Map<String, Object> getPropertyAddress(String apn) {
        return get("/parcel/" + apn + "/address");
    }

    public Map<String, Object> getValuations(String apn) {
        return get("/parcel/" + apn + "/valuations");
    }

    public Map<String, Object> getOwnerDetails(String apn) {
        return get("/parcel/" + apn + "/owner-details");
    }

    public Map<String, Object> getResidentialDetails(String apn) {
        return get("/parcel/" + apn + "/residential-details");
    }

    /** Aggregates the per-section calls into a single structure used by reports. */
    public Map<String, Object> fetchFullPropertyData(String apn) {
        Map<String, Object> data = new HashMap<>();
        data.put("parcel", getParcelDetails(apn));
        data.put("property_info", getPropertyInfo(apn));
        data.put("address", getPropertyAddress(apn));
        data.put("valuations", getValuations(apn));
        data.put("owner", getOwnerDetails(apn));
        return data;
    }
}
