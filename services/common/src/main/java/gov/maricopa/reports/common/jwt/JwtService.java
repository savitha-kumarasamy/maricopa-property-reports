package gov.maricopa.reports.common.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

/**
 * Issues and validates JWTs. Token layout (claims: sub, type, exp) is identical to the
 * original FastAPI backend so tokens remain interchangeable across the migration.
 */
@Service
public class JwtService {

    public static final String TYPE_ACCESS = "access";
    public static final String TYPE_REFRESH = "refresh";

    private final SecretKey key;
    private final JwtProperties properties;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        this.key = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(UUID userId) {
        Instant expiry = Instant.now().plus(properties.getAccessTokenExpireMinutes(), ChronoUnit.MINUTES);
        return build(userId, TYPE_ACCESS, expiry);
    }

    public String createRefreshToken(UUID userId) {
        Instant expiry = Instant.now().plus(properties.getRefreshTokenExpireDays(), ChronoUnit.DAYS);
        return build(userId, TYPE_REFRESH, expiry);
    }

    private String build(UUID userId, String type, Instant expiry) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("type", type)
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(expiry))
                .signWith(key)
                .compact();
    }

    /** Parses and verifies a token, returning its claims, or {@code null} if invalid/expired. */
    public Claims parse(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException ex) {
            return null;
        }
    }
}
