package gov.maricopa.reports.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import gov.maricopa.reports.auth.entity.UserEntity;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String fullName,
        @JsonProperty("is_active") boolean active,
        OffsetDateTime createdAt
) {
    public static UserResponse from(UserEntity user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.isActive(),
                user.getCreatedAt()
        );
    }
}
