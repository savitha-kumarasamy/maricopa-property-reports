package gov.maricopa.reports.report.client;

import gov.maricopa.reports.common.error.NotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.UUID;

@Component
public class PropertyClient {

    private final RestClient restClient;

    public PropertyClient(@Value("${app.services.property-base-url}") String propertyBaseUrl) {
        this.restClient = RestClient.builder().baseUrl(propertyBaseUrl).build();
    }

    /** Refreshes Maricopa data for a property and returns it along with the raw aggregated data. */
    public PropertyFullDataDto refreshForReport(UUID propertyId, UUID userId) {
        try {
            return restClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/internal/properties/{id}/refresh")
                            .queryParam("userId", userId)
                            .build(propertyId))
                    .retrieve()
                    .body(PropertyFullDataDto.class);
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                throw new NotFoundException("Property not found");
            }
            throw ex;
        }
    }
}
