package fr.cactus.model.hunter;

import fr.cactus.model.launcher.User;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@Table(name = "hunters")
public class Hunter extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    public User user;

    @Column(nullable = false, unique = true, length = 20)
    public String handle;

    @Column(nullable = false)
    public Integer gender;

    @Column(nullable = false)
    public Integer face;

    @Column(name = "profile_picture", length = 512)
    public String profilePicture;

    @Column(name = "banner_id")
    public Integer bannerId;

    @Column(name = "hunter_score", nullable = false)
    public Integer hunterScore = 0;

    @Column(name = "created_at", nullable = false)
    public OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    public OffsetDateTime updatedAt;

    @PrePersist
    void prePersist() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        if (createdAt == null) {
            createdAt = now;
        }

        if (hunterScore == null) {
            hunterScore = 0;
        }

        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }
}