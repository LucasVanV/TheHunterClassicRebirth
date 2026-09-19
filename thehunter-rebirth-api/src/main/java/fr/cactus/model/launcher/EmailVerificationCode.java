package fr.cactus.model.launcher;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@Table(name = "email_verification_codes")
public class EmailVerificationCode extends PanacheEntityBase {

    @Id
    @Column(name = "user_id")
    public UUID userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    public User user;

    @Column(name = "code_hash", nullable = false, length = 255)
    public String codeHash;

    @Column(name = "expires_at", nullable = false)
    public OffsetDateTime expiresAt;

    public boolean isExpired() {
        return OffsetDateTime.now(ZoneOffset.UTC).isAfter(expiresAt);
    }
}