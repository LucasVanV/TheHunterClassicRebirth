package fr.cactus.dto.launcher.account;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangeEmailRequest(

        @NotBlank(message = "EMAIL_REQUIRED")
        @Email(message = "EMAIL_INVALID")
        @Size(max = 320, message = "EMAIL_TOO_LONG")
        String email,

        @NotBlank(message = "CURRENT_PASSWORD_REQUIRED")
        String currentPassword

) {
}