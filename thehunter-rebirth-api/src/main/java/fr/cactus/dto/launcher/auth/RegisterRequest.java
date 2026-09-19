package fr.cactus.dto.launcher.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank(message = "EMAIL_REQUIRED")
        @Email(message = "EMAIL_INVALID")
        @Size(
                max = 320,
                message = "EMAIL_TOO_LONG"
        )
        String email,

        @NotBlank(message = "PASSWORD_REQUIRED")
        @Pattern(
                regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&._-])[A-Za-z\\d@$!%*?&._-]{8,128}$",
                message = "PASSWORD_INVALID"
        )
        String password

) {
}