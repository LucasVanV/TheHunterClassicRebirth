package fr.cactus.repository.hunter;

import fr.cactus.model.hunter.Hunter;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class HunterRepository implements PanacheRepositoryBase<Hunter, UUID> {

    public Optional<Hunter> findByUserId(UUID userId) {
        return find("user.id", userId).firstResultOptional();
    }

    public Optional<Hunter> findByHandle(String handle) {
        return find("handle", handle).firstResultOptional();
    }

    public boolean existsByHandle(String handle) {
        return count("handle", handle) > 0;
    }
}