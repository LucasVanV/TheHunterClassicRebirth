package fr.cactus.service.launcher;

import fr.cactus.config.LauncherAuthConfig;
import fr.cactus.dto.launcher.auth.AuthResponse;
import fr.cactus.dto.launcher.auth.LoginRequest;
import fr.cactus.dto.launcher.auth.RegisterRequest;
import fr.cactus.exception.launcher.ApiException;
import fr.cactus.exception.launcher.ErrorCode;
import fr.cactus.model.enums.RoleCode;
import fr.cactus.model.launcher.AuthRefreshToken;
import fr.cactus.model.launcher.Role;
import fr.cactus.model.launcher.User;
import fr.cactus.repository.launcher.AuthRefreshTokenRepository;
import fr.cactus.repository.launcher.RoleRepository;
import fr.cactus.repository.launcher.UserRepository;
import fr.cactus.service.global.NormalizationService;
import fr.cactus.service.global.TokenService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    AuthRefreshTokenRepository refreshTokenRepository;

    @Mock
    RoleRepository roleRepository;

    @Mock
    TokenService tokenService;

    @Mock
    NormalizationService normalizationService;

    @Mock
    LauncherAuthConfig launcherAuthConfig;

    AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService();

        authService.userRepository = userRepository;
        authService.refreshTokenRepository = refreshTokenRepository;
        authService.roleRepository = roleRepository;
        authService.tokenService = tokenService;
        authService.normalizationService = normalizationService;
        authService.launcherAuthConfig = launcherAuthConfig;

        lenient()
                .when(launcherAuthConfig.accessTokenDuration())
                .thenReturn(Duration.ofMinutes(15));

        lenient()
                .when(launcherAuthConfig.refreshTokenDuration())
                .thenReturn(Duration.ofDays(30));
    }

    @Test
    void registerShouldCreateHunterUser() {
        RegisterRequest request = new RegisterRequest(
                "Hunter@Test.com",
                "Password1!"
        );

        Role hunterRole = new Role();
        hunterRole.id = UUID.randomUUID();
        hunterRole.code = RoleCode.HUNTER;

        when(normalizationService.normalizeEmail(request.email()))
                .thenReturn("hunter@test.com");

        when(userRepository.existsByEmail("hunter@test.com"))
                .thenReturn(false);

        when(roleRepository.findByCode(RoleCode.HUNTER))
                .thenReturn(Optional.of(hunterRole));

        when(tokenService.generateJwt(any(User.class), any(Duration.class)))
                .thenReturn("access-token");

        when(tokenService.generateOpaqueToken())
                .thenReturn("refresh-token");

        when(tokenService.hashToken("refresh-token"))
                .thenReturn("refresh-token-hash");

        AuthResponse response = authService.register(request);

        ArgumentCaptor<User> userCaptor =
                ArgumentCaptor.forClass(User.class);

        verify(userRepository).persist(userCaptor.capture());

        User persistedUser = userCaptor.getValue();

        assertEquals("hunter@test.com", persistedUser.email);
        assertNotNull(persistedUser.passwordHash);
        assertNotEquals(request.password(), persistedUser.passwordHash);
        assertTrue(persistedUser.roles.contains(hunterRole));

        assertEquals("access-token", response.accessToken());
        assertEquals("refresh-token", response.refreshToken());
    }

    @Test
    void registerShouldRejectExistingEmail() {
        RegisterRequest request = new RegisterRequest(
                "hunter@test.com",
                "Password1!"
        );

        when(normalizationService.normalizeEmail(request.email()))
                .thenReturn("hunter@test.com");

        when(userRepository.existsByEmail("hunter@test.com"))
                .thenReturn(true);

        ApiException exception = assertThrows(
                ApiException.class,
                () -> authService.register(request)
        );

        assertEquals(
                ErrorCode.EMAIL_ALREADY_IN_USE,
                exception.getCode()
        );

        verify(userRepository, never()).persist(any(User.class));
    }

    @Test
    void loginShouldReturnTokensForValidCredentials() {
        String password = "Password1!";

        LoginRequest request = new LoginRequest(
                "Hunter@Test.com",
                password
        );

        User user = createUser(
                "hunter@test.com",
                password
        );

        when(normalizationService.normalizeEmail(request.email()))
                .thenReturn("hunter@test.com");

        when(userRepository.findByEmail("hunter@test.com"))
                .thenReturn(Optional.of(user));

        when(tokenService.generateJwt(user, Duration.ofMinutes(15)))
                .thenReturn("access-token");

        when(tokenService.generateOpaqueToken())
                .thenReturn("refresh-token");

        when(tokenService.hashToken("refresh-token"))
                .thenReturn("refresh-token-hash");

        AuthResponse response = authService.login(request);

        assertEquals("access-token", response.accessToken());
        assertEquals("refresh-token", response.refreshToken());
        assertEquals(user.id, response.user().id());
    }

    @Test
    void loginShouldRejectUnknownEmail() {
        LoginRequest request = new LoginRequest(
                "unknown@test.com",
                "Password1!"
        );

        when(normalizationService.normalizeEmail(request.email()))
                .thenReturn("unknown@test.com");

        when(userRepository.findByEmail("unknown@test.com"))
                .thenReturn(Optional.empty());

        ApiException exception = assertThrows(
                ApiException.class,
                () -> authService.login(request)
        );

        assertEquals(
                ErrorCode.EMAIL_NOT_FOUND,
                exception.getCode()
        );
    }

    @Test
    void loginShouldRejectInvalidPassword() {
        LoginRequest request = new LoginRequest(
                "hunter@test.com",
                "WrongPassword1!"
        );

        User user = createUser(
                "hunter@test.com",
                "CorrectPassword1!"
        );

        when(normalizationService.normalizeEmail(request.email()))
                .thenReturn("hunter@test.com");

        when(userRepository.findByEmail("hunter@test.com"))
                .thenReturn(Optional.of(user));

        ApiException exception = assertThrows(
                ApiException.class,
                () -> authService.login(request)
        );

        assertEquals(
                ErrorCode.INVALID_PASSWORD,
                exception.getCode()
        );
    }

    @Test
    void refreshShouldRotateRefreshToken() {
        User user = createUser(
                "hunter@test.com",
                "Password1!"
        );

        AuthRefreshToken storedRefreshToken =
                new AuthRefreshToken();

        storedRefreshToken.user = user;
        storedRefreshToken.tokenHash = "old-hash";
        storedRefreshToken.expiresAt =
                OffsetDateTime.now(ZoneOffset.UTC).plusDays(1);

        when(tokenService.hashToken("old-refresh-token"))
                .thenReturn("old-hash");

        when(refreshTokenRepository.findByTokenHash("old-hash"))
                .thenReturn(Optional.of(storedRefreshToken));

        when(tokenService.generateJwt(user, Duration.ofMinutes(15)))
                .thenReturn("new-access-token");

        when(tokenService.generateOpaqueToken())
                .thenReturn("new-refresh-token");

        when(tokenService.hashToken("new-refresh-token"))
                .thenReturn("new-hash");

        AuthResponse response =
                authService.refresh("old-refresh-token");

        assertNotNull(storedRefreshToken.revokedAt);

        assertEquals(
                "new-access-token",
                response.accessToken()
        );

        assertEquals(
                "new-refresh-token",
                response.refreshToken()
        );

        verify(refreshTokenRepository)
                .persist(any(AuthRefreshToken.class));
    }

    @Test
    void refreshShouldRejectInvalidToken() {
        when(tokenService.hashToken("invalid-token"))
                .thenReturn("invalid-hash");

        when(refreshTokenRepository.findByTokenHash("invalid-hash"))
                .thenReturn(Optional.empty());

        ApiException exception = assertThrows(
                ApiException.class,
                () -> authService.refresh("invalid-token")
        );

        assertEquals(
                ErrorCode.INVALID_REFRESH_TOKEN,
                exception.getCode()
        );
    }

    @Test
    void logoutShouldRevokeRefreshToken() {
        AuthRefreshToken refreshToken =
                new AuthRefreshToken();

        refreshToken.tokenHash = "token-hash";
        refreshToken.expiresAt =
                OffsetDateTime.now(ZoneOffset.UTC).plusDays(1);

        when(tokenService.hashToken("refresh-token"))
                .thenReturn("token-hash");

        when(refreshTokenRepository.findByTokenHash("token-hash"))
                .thenReturn(Optional.of(refreshToken));

        authService.logout("refresh-token");

        assertTrue(refreshToken.isRevoked());
        assertNotNull(refreshToken.revokedAt);
    }

    private User createUser(
            String email,
            String password
    ) {
        Role hunterRole = new Role();
        hunterRole.id = UUID.randomUUID();
        hunterRole.code = RoleCode.HUNTER;

        User user = new User();
        user.id = UUID.randomUUID();
        user.email = email;
        user.passwordHash =
                io.quarkus.elytron.security.common.BcryptUtil
                        .bcryptHash(password);

        user.roles.add(hunterRole);

        return user;
    }
}