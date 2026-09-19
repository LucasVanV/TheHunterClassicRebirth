package fr.cactus.dto.launcher.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VerifyEmailRequest(

        @NotBlank(message = "VERIFICATION_CODE_REQUIRED")
        @Pattern(
                regexp = "^\\d{6}$",
                message = "VERIFICATION_CODE_INVALID"
        )
        String code

) {
}