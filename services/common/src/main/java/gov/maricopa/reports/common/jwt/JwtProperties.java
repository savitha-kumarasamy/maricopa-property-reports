package gov.maricopa.reports.common.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    /** HMAC secret shared by all services. Must be at least 32 characters for HS256. */
    private String secret = "change-me-in-production-use-a-real-secret-key";

    private long accessTokenExpireMinutes = 30;

    private long refreshTokenExpireDays = 7;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getAccessTokenExpireMinutes() {
        return accessTokenExpireMinutes;
    }

    public void setAccessTokenExpireMinutes(long accessTokenExpireMinutes) {
        this.accessTokenExpireMinutes = accessTokenExpireMinutes;
    }

    public long getRefreshTokenExpireDays() {
        return refreshTokenExpireDays;
    }

    public void setRefreshTokenExpireDays(long refreshTokenExpireDays) {
        this.refreshTokenExpireDays = refreshTokenExpireDays;
    }
}
