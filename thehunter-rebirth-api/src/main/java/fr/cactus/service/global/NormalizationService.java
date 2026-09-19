package fr.cactus.service.global;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.Locale;

@ApplicationScoped
public class NormalizationService {

    public String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }

        return email
                .trim()
                .toLowerCase(Locale.ROOT);
    }
}