package gov.maricopa.reports.common.security;

import gov.maricopa.reports.common.jwt.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Extracts a bearer access token, validates it, and exposes the user id as a request
 * attribute. Does not reject unauthenticated requests itself — authorization is enforced
 * by {@link CurrentUserId} on endpoints that require it.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    public static final String USER_ID_ATTRIBUTE = "authenticatedUserId";

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            Claims claims = jwtService.parse(token);
            if (claims != null && JwtService.TYPE_ACCESS.equals(claims.get("type", String.class))) {
                try {
                    request.setAttribute(USER_ID_ATTRIBUTE, UUID.fromString(claims.getSubject()));
                } catch (IllegalArgumentException ignored) {
                    // malformed subject — leave unauthenticated
                }
            }
        }
        chain.doFilter(request, response);
    }
}
