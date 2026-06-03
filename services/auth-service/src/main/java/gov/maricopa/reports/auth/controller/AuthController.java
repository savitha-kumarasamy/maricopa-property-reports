package gov.maricopa.reports.auth.controller;

import gov.maricopa.reports.auth.dto.LoginRequest;
import gov.maricopa.reports.auth.dto.RefreshRequest;
import gov.maricopa.reports.auth.dto.RegisterRequest;
import gov.maricopa.reports.auth.dto.TokenResponse;
import gov.maricopa.reports.auth.dto.UserResponse;
import gov.maricopa.reports.auth.entity.UserEntity;
import gov.maricopa.reports.auth.service.AuthService;
import gov.maricopa.reports.common.security.CurrentUserId;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        UserEntity user = authService.register(request.email(), request.password(), request.fullName());
        return UserResponse.from(user);
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        UserEntity user = authService.authenticate(request.email(), request.password());
        return TokenResponse.of(authService.accessToken(user.getId()), authService.refreshToken(user.getId()));
    }

    @PostMapping("/refresh")
    public TokenResponse refresh(@Valid @RequestBody RefreshRequest request) {
        UserEntity user = authService.refresh(request.refreshToken());
        return TokenResponse.of(authService.accessToken(user.getId()), authService.refreshToken(user.getId()));
    }

    @GetMapping("/me")
    public UserResponse me(@CurrentUserId UUID userId) {
        return UserResponse.from(authService.getById(userId));
    }

}
