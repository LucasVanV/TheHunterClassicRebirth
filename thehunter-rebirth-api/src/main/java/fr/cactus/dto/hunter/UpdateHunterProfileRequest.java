package fr.cactus.dto.hunter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateHunterProfileRequest(

        @NotBlank
        @Size(max = 64)
        String handle,

        @NotNull
        Integer gender,

        @NotNull
        Integer face,

        @NotNull
        Integer bannerId

) {
}