package fr.cactus.service.global;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NormalizationServiceTest {

    private final NormalizationService normalizationService =
            new NormalizationService();

    @Test
    void normalizeEmailShouldTrimAndLowercase() {
        String result = normalizationService.normalizeEmail(
                "  Hunter@Test.COM  "
        );

        assertEquals(
                "hunter@test.com",
                result
        );
    }

    @Test
    void normalizeEmailShouldReturnNullForNullValue() {
        assertNull(
                normalizationService.normalizeEmail(null)
        );
    }
}