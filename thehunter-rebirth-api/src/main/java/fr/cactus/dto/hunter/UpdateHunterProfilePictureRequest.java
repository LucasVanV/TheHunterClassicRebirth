package fr.cactus.dto.hunter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateHunterProfilePictureRequest(

        @NotBlank
        @Size(max = 512)
        String profilePicture

) {
}