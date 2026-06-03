package gov.maricopa.reports.schedule.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import gov.maricopa.reports.common.error.NotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Map;
import java.util.UUID;

/**
 * Outbound calls to the property, report, and auth services for schedule processing.
 */
@Component
public class ServiceClients {

    private final RestClient propertyClient;
    private final RestClient reportClient;
    private final RestClient authClient;

    public ServiceClients(
            @Value("${app.services.property-base-url}") String propertyBaseUrl,
            @Value("${app.services.report-base-url}") String reportBaseUrl,
            @Value("${app.services.auth-base-url}") String authBaseUrl) {
        this.propertyClient = RestClient.builder().baseUrl(propertyBaseUrl).build();
        this.reportClient = RestClient.builder().baseUrl(reportBaseUrl).build();
        this.authClient = RestClient.builder().baseUrl(authBaseUrl).build();
    }

    /** Verifies a property exists and belongs to the user; throws NotFound otherwise. */
    public void verifyProperty(UUID propertyId, UUID userId) {
        try {
            propertyClient.get()
                    .uri(b -> b.path("/internal/properties/{id}").queryParam("userId", userId).build(propertyId))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                throw new NotFoundException("Property not found");
            }
            throw ex;
        }
    }

    public GeneratedReport generateReport(UUID propertyId, UUID userId) {
        return reportClient.post()
                .uri(b -> b.path("/internal/reports/generate")
                        .queryParam("propertyId", propertyId)
                        .queryParam("userId", userId)
                        .build())
                .retrieve()
                .body(GeneratedReport.class);
    }

    public void markReportSent(UUID reportId, String email) {
        reportClient.patch()
                .uri(b -> b.path("/internal/reports/{id}/mark-sent").queryParam("email", email).build(reportId))
                .retrieve()
                .toBodilessEntity();
    }

    public UserInfo getUser(UUID userId) {
        return authClient.get()
                .uri("/internal/users/{id}", userId)
                .retrieve()
                .body(UserInfo.class);
    }

    public record GeneratedReport(
            UUID id,
            UUID propertyId,
            String status,
            Map<String, Object> reportData
    ) {
    }

    public record UserInfo(
            UUID id,
            String email,
            String fullName,
            @JsonProperty("is_active") boolean active
    ) {
    }
}
