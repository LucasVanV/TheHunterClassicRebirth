package fr.cactus.service.launcher;

import fr.cactus.config.EmailVerificationConfig;
import fr.cactus.exception.launcher.ApiException;
import fr.cactus.exception.launcher.ErrorCode;
import fr.cactus.model.launcher.EmailVerificationCode;
import fr.cactus.model.launcher.User;
import fr.cactus.repository.launcher.EmailVerificationCodeRepository;
import fr.cactus.repository.launcher.UserRepository;
import fr.cactus.service.global.EmailService;

import io.quarkus.elytron.security.common.BcryptUtil;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@ApplicationScoped
public class EmailVerificationService {

    private static final SecureRandom SECURE_RANDOM =
            new SecureRandom();

    @Inject
    UserRepository userRepository;

    @Inject
    EmailVerificationCodeRepository verificationCodeRepository;

    @Inject
    EmailVerificationConfig emailVerificationConfig;

    @Inject
    EmailService emailService;

    @Transactional
    public void sendCode(UUID userId) {
        User user = getUser(userId);

        if (user.emailVerifiedAt != null) {
            throw new ApiException(
                    ErrorCode.EMAIL_ALREADY_VERIFIED
            );
        }

        String code = generateSixDigitCode();

        EmailVerificationCode verificationCode =
                verificationCodeRepository
                        .findByIdOptional(userId)
                        .orElseGet(() -> {
                            EmailVerificationCode created =
                                    new EmailVerificationCode();

                            created.user = user;
                            created.userId = user.id;

                            return created;
                        });

        verificationCode.codeHash =
                BcryptUtil.bcryptHash(code);

        verificationCode.expiresAt =
                OffsetDateTime.now(ZoneOffset.UTC)
                        .plus(
                                emailVerificationConfig
                                        .codeDuration()
                        );

        if (!verificationCodeRepository
                .isPersistent(verificationCode)) {

            verificationCodeRepository.persist(
                    verificationCode
            );
        }

        emailService.sendText(
                user.email,
                "Verify your TheHunterClassicRebirth account",
                """
                Your TheHunterClassicRebirth verification code is:

                %s

                This code is valid for 15 minutes.

                If you did not request this code, you can ignore this email.
                """.formatted(code)
        );
    }

    @Transactional
    public void verify(UUID userId, String code) {
        User user = getUser(userId);

        if (user.emailVerifiedAt != null) {
            throw new ApiException(
                    ErrorCode.EMAIL_ALREADY_VERIFIED
            );
        }

        EmailVerificationCode verificationCode =
                verificationCodeRepository
                        .findByIdOptional(userId)
                        .orElseThrow(() ->
                                new ApiException(
                                        ErrorCode.VERIFICATION_CODE_INVALID
                                )
                        );

        if (verificationCode.isExpired()) {
            verificationCodeRepository.delete(
                    verificationCode
            );

            throw new ApiException(
                    ErrorCode.VERIFICATION_CODE_EXPIRED
            );
        }

        if (!BcryptUtil.matches(
                code,
                verificationCode.codeHash
        )) {
            throw new ApiException(
                    ErrorCode.VERIFICATION_CODE_INVALID
            );
        }

        user.emailVerifiedAt =
                OffsetDateTime.now(ZoneOffset.UTC);

        verificationCodeRepository.delete(
                verificationCode
        );
    }

    private User getUser(UUID userId) {
        return userRepository
                .findByIdOptional(userId)
                .orElseThrow(() ->
                        new ApiException(
                                ErrorCode.UNAUTHORIZED
                        )
                );
    }

    private String generateSixDigitCode() {
        return "%06d".formatted(
                SECURE_RANDOM.nextInt(1_000_000)
        );
    }
}