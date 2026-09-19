package fr.cactus.service.global;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.UUID;

@ApplicationScoped
public class CurrentUserService {

    @Inject
    JsonWebToken jwt;

    public UUID getUserId() {
        return UUID.fromString(
                jwt.getSubject()
        );
    }
}