package gov.maricopa.reports.property.controller;

import gov.maricopa.reports.property.dto.PropertyFullData;
import gov.maricopa.reports.property.dto.PropertyResponse;
import gov.maricopa.reports.property.service.PropertyService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Service-to-service endpoints used by the report-service. Not exposed through the gateway.
 */
@RestController
@RequestMapping("/internal/properties")
public class InternalPropertyController {

    private final PropertyService propertyService;

    public InternalPropertyController(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    @GetMapping("/{id}")
    public PropertyResponse get(@PathVariable UUID id, @RequestParam UUID userId) {
        return PropertyResponse.from(propertyService.get(userId, id));
    }

    @PostMapping("/{id}/refresh")
    public PropertyFullData refresh(@PathVariable UUID id, @RequestParam UUID userId) {
        return propertyService.refreshForReport(userId, id);
    }
}
