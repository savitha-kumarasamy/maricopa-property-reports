package gov.maricopa.reports.auth.service;

import gov.maricopa.reports.auth.entity.UserEntity;
import gov.maricopa.reports.auth.repository.UserRepository;
import gov.maricopa.reports.common.error.BadRequestException;
import gov.maricopa.reports.common.error.UnauthorizedException;
import gov.maricopa.reports.common.jwt.JwtService;
import io.jsonwebtoken.Claims;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public UserEntity register(String email, String password, String fullName) {
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email already registered");
        }
        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setHashedPassword(passwordEncoder.encode(password));
        user.setFullName(fullName);
        user.setActive(true);
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public UserEntity authenticate(String email, String password) {
        UserEntity user = userRepository.findByEmail(email).orElse(null);
        if (user == null || !passwordEncoder.matches(password, user.getHashedPassword())) {
            throw new UnauthorizedException("Invalid email or password");
        }
        return user;
    }

    @Transactional(readOnly = true)
    public UserEntity getById(UUID id) {
        return userRepository.findById(id)
                .filter(UserEntity::isActive)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
    }

    public UserEntity refresh(String refreshToken) {
        Claims claims = jwtService.parse(refreshToken);
        if (claims == null || !JwtService.TYPE_REFRESH.equals(claims.get("type", String.class))) {
            throw new UnauthorizedException("Invalid refresh token");
        }
        UUID userId = UUID.fromString(claims.getSubject());
        return getById(userId);
    }

    public String accessToken(UUID userId) {
        return jwtService.createAccessToken(userId);
    }

    public String refreshToken(UUID userId) {
        return jwtService.createRefreshToken(userId);
    }
}
