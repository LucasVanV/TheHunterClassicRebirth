package fr.cactus.service.launcher;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

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
import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class AuthService {

    @Inject
    UserRepository userRepository;

    @Inject
    AuthRefreshTokenRepository refreshTokenRepository;

    @Inject
    TokenService tokenService;

    @Inject
    NormalizationService normalizationService;

    @Inject
    RoleRepository roleRepository;

    @Inject
    LauncherAuthConfig launcherAuthConfig;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalizationService.normalizeEmail(request.email());

        if (userRepository.existsByEmail(email)) {
            throw new ApiException(
                    ErrorCode.EMAIL_ALREADY_IN_USE
            );
        }

        Role hunterRole = roleRepository
                .findByCode(RoleCode.HUNTER)
                .orElseThrow(() ->
                        new ApiException(
                                ErrorCode.INTERNAL_ERROR
                        )
                );

        User user = new User();
        user.email = email;
        user.passwordHash = BcryptUtil.bcryptHash(request.password());
        user.roles.add(hunterRole);

        userRepository.persist(user);

        return createAuthResponse(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        String email = normalizationService.normalizeEmail(
                request.email()
        );

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new ApiException(
                                ErrorCode.EMAIL_NOT_FOUND
                        )
                );

        if (!BcryptUtil.matches(
                request.password(),
                user.passwordHash
        )) {
                throw new ApiException(
                        ErrorCode.INVALID_PASSWORD
                );
        }

        return createAuthResponse(user);
    }

    @Transactional
    public AuthResponse refresh(String rawRefreshToken) {
        String tokenHash = tokenService.hashToken(rawRefreshToken);

        AuthRefreshToken refreshToken = refreshTokenRepository
                .findByTokenHash(tokenHash)
                .orElseThrow(() ->
                        new ApiException(
                                ErrorCode.INVALID_REFRESH_TOKEN
                        )
                );

        if (!refreshToken.isValid()) {
            throw new ApiException(
                    ErrorCode.INVALID_REFRESH_TOKEN
            );
        }

        refreshToken.revokedAt = OffsetDateTime.now(ZoneOffset.UTC);

        return createAuthResponse(refreshToken.user);
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        String tokenHash = tokenService.hashToken(rawRefreshToken);

        refreshTokenRepository
                .findByTokenHash(tokenHash)
                .ifPresent(refreshToken -> {
                    if (!refreshToken.isRevoked()) {
                        refreshToken.revokedAt =
                                OffsetDateTime.now(ZoneOffset.UTC);
                    }
                });
    }

    private AuthResponse createAuthResponse(User user) {
        String accessToken = tokenService.generateJwt(
                user,
                launcherAuthConfig.accessTokenDuration()
        );

        String refreshToken = createRefreshToken(user);

        return AuthResponse.from(
                accessToken,
                refreshToken,
                user
        );
    }

    private String createRefreshToken(User user) {
        String rawToken = tokenService.generateOpaqueToken();

        AuthRefreshToken refreshToken = new AuthRefreshToken();

        refreshToken.user = user;
        refreshToken.tokenHash = tokenService.hashToken(rawToken);
        refreshToken.expiresAt = OffsetDateTime.now(ZoneOffset.UTC)
                .plus(launcherAuthConfig.refreshTokenDuration());

        refreshTokenRepository.persist(refreshToken);

        return rawToken;
    }
}