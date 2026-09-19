package fr.cactus.service.hunter;

import fr.cactus.model.hunter.Hunter;
import fr.cactus.model.launcher.User;
import fr.cactus.repository.hunter.HunterRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class HunterService {

    @Inject
    HunterRepository hunterRepository;

    public Optional<Hunter> findById(UUID id) {
        return hunterRepository.findByIdOptional(id);
    }

    public Optional<Hunter> findByUserId(UUID userId) {
        return hunterRepository.findByUserId(userId);
    }

    public Optional<Hunter> findByHandle(String handle) {
        return hunterRepository.findByHandle(handle);
    }

    public boolean handleExists(String handle) {
        return hunterRepository.existsByHandle(handle);
    }

    @Transactional
    public Hunter create(
            User user,
            String handle,
            Integer gender,
            Integer face
    ) {
        Hunter hunter = new Hunter();

        hunter.user = user;
        hunter.handle = handle;
        hunter.gender = gender;
        hunter.face = face;

        hunter.profilePicture =
                "/assets/profile-pictures/" + face + ".png";

        hunter.bannerId = 1;
        hunter.hunterScore = 0;

        hunterRepository.persist(hunter);

        return hunter;
    }

    @Transactional
    public Hunter updateProfile(
            Hunter hunter,
            String handle,
            Integer gender,
            Integer face,
            Integer bannerId
    ) {
        hunter.handle = handle;
        hunter.gender = gender;
        hunter.face = face;
        hunter.bannerId = bannerId;

        return hunter;
    }

    @Transactional
    public Hunter updateProfilePicture(
            Hunter hunter,
            String profilePicture
    ) {
        hunter.profilePicture = profilePicture;

        return hunter;
    }

    @Transactional
    public Hunter updateHunterScore(
            Hunter hunter,
            Integer hunterScore
    ) {
        hunter.hunterScore = hunterScore;

        return hunter;
    }
}