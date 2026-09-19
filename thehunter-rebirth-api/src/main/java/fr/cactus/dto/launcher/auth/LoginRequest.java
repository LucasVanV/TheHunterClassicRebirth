package fr.cactus.dto.launcher.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @NotBlank(message = "EMAIL_REQUIRED")
        @Email(message = "EMAIL_INVALID")
        String email,

        @NotBlank(message = "PASSWORD_REQUIRED")
        String password

) {
}