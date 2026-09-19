package fr.cactus.service.launcher;

import fr.cactus.dto.launcher.account.ChangeEmailRequest;
import fr.cactus.dto.launcher.account.ChangePasswordRequest;
import fr.cactus.dto.launcher.account.DeleteAccountRequest;
import fr.cactus.exception.launcher.ApiException;
import fr.cactus.exception.launcher.ErrorCode;
import fr.cactus.model.launcher.User;
import fr.cactus.repository.launcher.AuthRefreshTokenRepository;
import fr.cactus.repository.launcher.EmailVerificationCodeRepository;
import fr.cactus.repository.launcher.UserRepository;
import fr.cactus.service.global.NormalizationService;

import io.quarkus.elytron.security.common.BcryptUtil;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.UUID;

@ApplicationScoped
public class AccountService {

    @Inject
    UserRepository userRepository;

    @Inject
    AuthRefreshTokenRepository authRefreshTokenRepository;

    @Inject
    EmailVerificationCodeRepository emailVerificationCodeRepository;

    @Inject
    NormalizationService normalizationService;

    @Transactional
    public void changeEmail(
            UUID userId,
            ChangeEmailRequest request
    ) {
        User user = getUser(userId);

        verifyCurrentPassword(
                user,
                request.currentPassword()
        );

        String email =
                normalizationService.normalizeEmail(
                        request.email()
                );

        if (email.equals(user.email)) {
            return;
        }

        if (userRepository.existsByEmail(email)) {
            throw new ApiException(
                    ErrorCode.EMAIL_ALREADY_IN_USE
            );
        }

        user.email = email;

        /*
         * A new email address must be verified again.
         */
        user.emailVerifiedAt = null;

        /*
         * A verification code created for the previous
         * email address must never remain valid.
         */
        emailVerificationCodeRepository.deleteById(
                user.id
        );

        /*
         * Invalidate every existing session.
         */
        authRefreshTokenRepository.revokeAllByUserId(
                user.id
        );
    }

    @Transactional
    public void changePassword(
            UUID userId,
            ChangePasswordRequest request
    ) {
        User user = getUser(userId);

        verifyCurrentPassword(
                user,
                request.currentPassword()
        );

        if (BcryptUtil.matches(
                request.newPassword(),
                user.passwordHash
        )) {
            throw new ApiException(
                    ErrorCode.NEW_PASSWORD_SAME_AS_CURRENT
            );
        }

        user.passwordHash =
                BcryptUtil.bcryptHash(
                        request.newPassword()
                );

        authRefreshTokenRepository.revokeAllByUserId(
                user.id
        );
    }

    @Transactional
    public void deleteAccount(
            UUID userId,
            DeleteAccountRequest request
    ) {
        User user = getUser(userId);

        verifyCurrentPassword(
                user,
                request.currentPassword()
        );

        userRepository.delete(user);
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

    private void verifyCurrentPassword(
            User user,
            String password
    ) {
        if (!BcryptUtil.matches(
                password,
                user.passwordHash
        )) {
            throw new ApiException(
                    ErrorCode.INVALID_CURRENT_PASSWORD
            );
        }
    }
}