package gov.maricopa.reports.property.controller;

import gov.maricopa.reports.common.security.CurrentUserId;
import gov.maricopa.reports.property.dto.PropertyCreate;
import gov.maricopa.reports.property.dto.PropertyResponse;
import gov.maricopa.reports.property.dto.PropertySearchResponse;
import gov.maricopa.reports.property.service.PropertyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/properties")
public class PropertyController {

    private final PropertyService propertyService;

    public PropertyController(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    @GetMapping("/search")
    public PropertySearchResponse search(
            @CurrentUserId UUID userId,
            @RequestParam("q") String q,
            @RequestParam(value = "page", defaultValue = "1") int page) {
        return propertyService.search(q, page);
    }

    @GetMapping
    public List<PropertyResponse> list(@CurrentUserId UUID userId) {
        return propertyService.list(userId).stream().map(PropertyResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PropertyResponse add(@CurrentUserId UUID userId, @Valid @RequestBody PropertyCreate request) {
        return PropertyResponse.from(propertyService.add(userId, request.apn()));
    }

    @GetMapping("/{id}")
    public PropertyResponse get(@CurrentUserId UUID userId, @PathVariable UUID id) {
        return PropertyResponse.from(propertyService.get(userId, id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@CurrentUserId UUID userId, @PathVariable UUID id) {
        propertyService.delete(userId, id);
    }

    @PostMapping("/{id}/sync")
    public PropertyResponse sync(@CurrentUserId UUID userId, @PathVariable UUID id) {
        return PropertyResponse.from(propertyService.sync(userId, id));
    }
}
