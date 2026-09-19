package fr.cactus.dto.launcher.account;

import jakarta.validation.constraints.NotBlank;

public record DeleteAccountRequest(

        @NotBlank(message = "CURRENT_PASSWORD_REQUIRED")
        String currentPassword

) {
}