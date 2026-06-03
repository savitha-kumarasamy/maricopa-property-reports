package gov.maricopa.reports.auth.controller;

import gov.maricopa.reports.auth.dto.UserResponse;
import gov.maricopa.reports.auth.service.AuthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Service-to-service endpoints, not exposed through the API gateway (paths are not under /api).
 */
@RestController
@RequestMapping("/internal/users")
public class InternalUserController {

    private final AuthService authService;

    public InternalUserController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/{id}")
    public UserResponse getUser(@PathVariable UUID id) {
        return UserResponse.from(authService.getById(id));
    }
}
