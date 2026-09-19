package fr.cactus.repository.launcher;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import fr.cactus.model.launcher.AuthRefreshToken;

@ApplicationScoped
public class AuthRefreshTokenRepository
        implements PanacheRepositoryBase<AuthRefreshToken, UUID> {

    public Optional<AuthRefreshToken> findByTokenHash(String tokenHash) {
        return find("tokenHash", tokenHash)
                .firstResultOptional();
    }

    public void deleteByUserId(UUID userId) {
        delete("user.id", userId);
    }

    public long revokeAllByUserId(UUID userId) {
        return update(
                "revokedAt = ?1 WHERE user.id = ?2 AND revokedAt IS NULL",
                OffsetDateTime.now(ZoneOffset.UTC),
                userId
        );
    }
}