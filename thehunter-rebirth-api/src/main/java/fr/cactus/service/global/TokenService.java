package fr.cactus.service.global;

import fr.cactus.config.JwtConfig;
import fr.cactus.model.launcher.User;
import io.smallrye.jwt.build.Jwt;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

@ApplicationScoped
public class TokenService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String EMAIL_VERIFIED_GROUP = "EMAIL_VERIFIED";

    @Inject
    JwtConfig jwtConfig;

    public String generateJwt(User user, Duration duration) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        Set<String> groups = new HashSet<>();

        user.roles.forEach(role ->
                groups.add(role.code.name())
        );

        if (user.emailVerifiedAt != null) {
            groups.add(EMAIL_VERIFIED_GROUP);
        }

        return Jwt.issuer(jwtConfig.issuer())
                .subject(user.id.toString())
                .upn(user.email)
                .claim("email", user.email)
                .groups(groups)
                .issuedAt(now.toInstant())
                .expiresAt(now.plus(duration).toInstant())
                .sign();
    }

    public String generateOpaqueToken() {
        byte[] bytes = new byte[64];

        SECURE_RANDOM.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    public String hashToken(String token) {
        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    token.getBytes(StandardCharsets.UTF_8)
            );

            return bytesToHex(hash);

        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 algorithm is not available",
                    exception
            );
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder builder =
                new StringBuilder(bytes.length * 2);

        for (byte value : bytes) {
            builder.append(
                    String.format("%02x", value)
            );
        }

        return builder.toString();
    }
}