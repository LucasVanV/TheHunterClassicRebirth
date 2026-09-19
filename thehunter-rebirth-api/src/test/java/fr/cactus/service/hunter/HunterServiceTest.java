package fr.cactus.service.hunter;

import fr.cactus.model.hunter.Hunter;
import fr.cactus.model.launcher.User;
import fr.cactus.repository.hunter.HunterRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HunterServiceTest {

    HunterRepository hunterRepository;
    HunterService hunterService;

    @BeforeEach
    void setUp() {
        hunterRepository = mock(HunterRepository.class);

        hunterService = new HunterService();
        hunterService.hunterRepository = hunterRepository;
    }

    @Test
    void shouldFindHunterById() {
        UUID hunterId = UUID.randomUUID();

        Hunter hunter = new Hunter();
        hunter.id = hunterId;

        when(hunterRepository.findByIdOptional(hunterId))
                .thenReturn(Optional.of(hunter));

        Optional<Hunter> result =
                hunterService.findById(hunterId);

        assertTrue(result.isPresent());
        assertSame(hunter, result.get());

        verify(hunterRepository)
                .findByIdOptional(hunterId);
    }

    @Test
    void shouldFindHunterByUserId() {
        UUID userId = UUID.randomUUID();

        Hunter hunter = new Hunter();

        when(hunterRepository.findByUserId(userId))
                .thenReturn(Optional.of(hunter));

        Optional<Hunter> result =
                hunterService.findByUserId(userId);

        assertTrue(result.isPresent());
        assertSame(hunter, result.get());

        verify(hunterRepository)
                .findByUserId(userId);
    }

    @Test
    void shouldFindHunterByHandle() {
        String handle = "impulsionbleu";

        Hunter hunter = new Hunter();
        hunter.handle = handle;

        when(hunterRepository.findByHandle(handle))
                .thenReturn(Optional.of(hunter));

        Optional<Hunter> result =
                hunterService.findByHandle(handle);

        assertTrue(result.isPresent());
        assertSame(hunter, result.get());

        verify(hunterRepository)
                .findByHandle(handle);
    }

    @Test
    void shouldCheckIfHandleExists() {
        String handle = "impulsionbleu";

        when(hunterRepository.existsByHandle(handle))
                .thenReturn(true);

        boolean result =
                hunterService.handleExists(handle);

        assertTrue(result);

        verify(hunterRepository)
                .existsByHandle(handle);
    }

    @Test
    void shouldCreateHunter() {
        User user = new User();

        String handle = "impulsionbleu";
        Integer gender = 1;
        Integer face = 1000001;

        Hunter hunter = hunterService.create(
                user,
                handle,
                gender,
                face
        );

        assertNotNull(hunter);

        assertSame(user, hunter.user);
        assertEquals(handle, hunter.handle);
        assertEquals(gender, hunter.gender);
        assertEquals(face, hunter.face);

        assertEquals(
                "/assets/profile-pictures/1000001.png",
                hunter.profilePicture
        );

        assertEquals(1, hunter.bannerId);
        assertEquals(0, hunter.hunterScore);

        verify(hunterRepository)
                .persist(hunter);
    }

    @Test
    void shouldUpdateProfile() {
        Hunter hunter = new Hunter();

        hunter.handle = "oldHandle";
        hunter.gender = 0;
        hunter.face = 1000012;
        hunter.bannerId = 1;

        Hunter result = hunterService.updateProfile(
                hunter,
                "newHandle",
                1,
                1000001,
                2
        );

        assertSame(hunter, result);

        assertEquals("newHandle", hunter.handle);
        assertEquals(1, hunter.gender);
        assertEquals(1000001, hunter.face);
        assertEquals(2, hunter.bannerId);
    }

    @Test
    void shouldUpdateProfilePicture() {
        Hunter hunter = new Hunter();

        hunter.profilePicture =
                "/assets/profile-pictures/1000001.png";

        Hunter result =
                hunterService.updateProfilePicture(
                        hunter,
                        "https://avatar.example.com/avatar.jpg"
                );

        assertSame(hunter, result);

        assertEquals(
                "https://avatar.example.com/avatar.jpg",
                hunter.profilePicture
        );
    }

    @Test
    void shouldUpdateHunterScore() {
        Hunter hunter = new Hunter();
        hunter.hunterScore = 0;

        Hunter result =
                hunterService.updateHunterScore(
                        hunter,
                        9460
                );

        assertSame(hunter, result);
        assertEquals(9460, hunter.hunterScore);
    }
}