package fr.cactus.repository.launcher;

import fr.cactus.model.enums.RoleCode;
import fr.cactus.model.launcher.Role;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class RoleRepository implements PanacheRepositoryBase<Role, UUID> {

    public Optional<Role> findByCode(RoleCode code) {
        return find("code", code).firstResultOptional();
    }
}