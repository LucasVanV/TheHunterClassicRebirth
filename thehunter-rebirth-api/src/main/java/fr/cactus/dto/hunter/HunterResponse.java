package fr.cactus.dto.hunter;

import fr.cactus.model.hunter.Hunter;

import java.util.UUID;

public record HunterResponse(

        UUID id,
        String handle,
        Integer gender,
        Integer face,
        String profilePicture,
        Integer bannerId,
        Integer hunterScore

) {

    public static HunterResponse from(Hunter hunter) {
        return new HunterResponse(
                hunter.id,
                hunter.handle,
                hunter.gender,
                hunter.face,
                hunter.profilePicture,
                hunter.bannerId,
                hunter.hunterScore
        );
    }
}