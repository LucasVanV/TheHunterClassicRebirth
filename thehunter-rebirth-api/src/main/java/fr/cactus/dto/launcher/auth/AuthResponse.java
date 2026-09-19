package fr.cactus.dto.launcher.auth;

import fr.cactus.model.launcher.User;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        UserResponse user
) {

    public static AuthResponse from(
            String accessToken,
            String refreshToken,
            User user
    ) {
        return new AuthResponse(
                accessToken,
                refreshToken,
                UserResponse.from(user)
        );
    }
}