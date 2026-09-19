package fr.cactus.dto.launcher.auth;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(

        @NotBlank(message = "REFRESH_TOKEN_REQUIRED")
        String refreshToken

) {
}