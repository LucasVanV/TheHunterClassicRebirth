package fr.cactus.dto.hunter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateHunterRequest(

        @NotBlank(message = "HUNTER_HANDLE_REQUIRED")
        @Size(
                min = 4,
                max = 20,
                message = "HUNTER_HANDLE_INVALID"
        )
        @Pattern(
                regexp = "^[A-Za-z0-9_-]+$",
                message = "HUNTER_HANDLE_INVALID"
        )
        String handle,

        @NotNull
        Integer gender,

        @NotNull
        Integer face

) {
}