package fr.cactus.dto.launcher.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ChangePasswordRequest(

        @NotBlank(message = "CURRENT_PASSWORD_REQUIRED")
        String currentPassword,

        @NotBlank(message = "NEW_PASSWORD_REQUIRED")
        @Pattern(
                regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&._-])[A-Za-z\\d@$!%*?&._-]{8,128}$",
                message = "NEW_PASSWORD_INVALID"
        )
        String newPassword

) {
}