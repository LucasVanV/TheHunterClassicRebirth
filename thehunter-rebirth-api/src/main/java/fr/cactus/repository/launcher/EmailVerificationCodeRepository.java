package fr.cactus.repository.launcher;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

import fr.cactus.model.launcher.EmailVerificationCode;

@ApplicationScoped
public class EmailVerificationCodeRepository
        implements PanacheRepositoryBase<EmailVerificationCode, UUID> {
}