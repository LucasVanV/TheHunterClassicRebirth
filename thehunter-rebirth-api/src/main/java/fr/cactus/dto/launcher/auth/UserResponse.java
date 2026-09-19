package fr.cactus.dto.launcher.auth;

import fr.cactus.model.enums.RoleCode;
import fr.cactus.model.launcher.User;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public record UserResponse(
        UUID id,
        String email,
        OffsetDateTime emailVerifiedAt,
        OffsetDateTime createdAt,
        Set<RoleCode> roles
) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.id,
                user.email,
                user.emailVerifiedAt,
                user.createdAt,
                user.roles.stream()
                        .map(role -> role.code)
                        .collect(Collectors.toSet())
        );
    }
}