package fr.cactus.controller.launcher;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.cactus.model.launcher.EmailVerificationCode;
import fr.cactus.model.launcher.User;
import fr.cactus.repository.launcher.AuthRefreshTokenRepository;
import fr.cactus.repository.launcher.EmailVerificationCodeRepository;
import fr.cactus.repository.launcher.UserRepository;

import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.mailer.MockMailbox;
import io.quarkus.test.junit.QuarkusTest;

import io.vertx.ext.mail.MailMessage;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class AccountControllerTest {

    private static final String PASSWORD =
            "Password1!";

    private static final String NEW_PASSWORD =
            "NewPassword2!";

    private static final Pattern VERIFICATION_CODE_PATTERN =
            Pattern.compile("\\b\\d{6}\\b");

    @Inject
    UserRepository userRepository;

    @Inject
    AuthRefreshTokenRepository authRefreshTokenRepository;

    @Inject
    EmailVerificationCodeRepository emailVerificationCodeRepository;

    @Inject
    MockMailbox mailbox;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    EntityManager entityManager;

    @BeforeEach
    @Transactional
    void cleanDatabase() {
        emailVerificationCodeRepository.deleteAll();
        authRefreshTokenRepository.deleteAll();
        userRepository.deleteAll();

        mailbox.clear();
    }

    @Test
    void shouldSendEmailVerificationCode() {
        String email =
                "send-code@example.com";

        TestSession session =
                register(
                        email,
                        PASSWORD
                );

        given()
                .contentType(
                        "application/json"
                )
                .auth()
                .oauth2(
                        session.accessToken()
                )
                .when()
                .post(
                        "/api/account/email-verification/send"
                )
                .then()
                .statusCode(204);

        List<MailMessage> mails =
                mailbox.getMailMessagesSentTo(
                        email
                );

        assertEquals(
                1,
                mails.size()
        );

        assertEquals(
                1,
                mailbox.getTotalMessagesSent()
        );

        String code =
                extractVerificationCode(
                        mails.get(0)
                );

        assertEquals(
                6,
                code.length()
        );

        User user =
                userRepository
                        .findByEmail(email)
                        .orElseThrow();

        EmailVerificationCode storedCode =
                emailVerificationCodeRepository
                        .findById(user.id);

        assertNotNull(
                storedCode
        );

        /*
         * The raw verification code must never
         * be stored directly in the database.
         */
        assertNotEquals(
                code,
                storedCode.codeHash
        );

        assertTrue(
                BcryptUtil.matches(
                        code,
                        storedCode.codeHash
                )
        );

        assertFalse(
                storedCode.isExpired()
        );
    }

    @Test
    void shouldVerifyEmailAndAddEmailVerifiedGroupToNextJwt()
            throws Exception {

        String email =
                "verify-email@example.com";

        TestSession session =
                register(
                        email,
                        PASSWORD
                );

        sendVerificationCode(
                session.accessToken()
        );

        String code =
                getLatestVerificationCode(
                        email
                );

        given()
                .contentType(
                        "application/json"
                )
                .auth()
                .oauth2(
                        session.accessToken()
                )
                .body(
                        Map.of(
                                "code",
                                code
                        )
                )
                .when()
                .post(
                        "/api/account/email-verification/verify"
                )
                .then()
                .statusCode(204);

        User user =
                userRepository
                        .findByEmail(email)
                        .orElseThrow();

        assertNotNull(
                user.emailVerifiedAt
        );

        assertNull(
                emailVerificationCodeRepository
                        .findById(user.id)
        );

        String newAccessToken =
                given()
                        .contentType(
                                "application/json"
                        )
                        .body(
                                Map.of(
                                        "refreshToken",
                                        session.refreshToken()
                                )
                        )
                        .when()
                        .post(
                                "/api/auth/refresh"
                        )
                        .then()
                        .statusCode(200)
                        .extract()
                        .path(
                                "accessToken"
                        );

        List<String> groups =
                getJwtGroups(
                        newAccessToken
                );

        assertTrue(
                groups.contains(
                        "HUNTER"
                )
        );

        assertTrue(
                groups.contains(
                        "EMAIL_VERIFIED"
                )
        );
    }

    @Test
    void shouldRejectInvalidVerificationCode() {
        String email =
                "invalid-code@example.com";

        TestSession session =
                register(
                        email,
                        PASSWORD
                );

        sendVerificationCode(
                session.accessToken()
        );

        String validCode =
                getLatestVerificationCode(
                        email
                );

        String invalidCode =
                validCode.equals("000000")
                        ? "000001"
                        : "000000";

        given()
                .contentType(
                        "application/json"
                )
                .auth()
                .oauth2(
                        session.accessToken()
                )
                .body(
                        Map.of(
                                "code",
                                invalidCode
                        )
                )
                .when()
                .post(
                        "/api/account/email-verification/verify"
                )
                .then()
                .statusCode(400)
                .body(
                        "code",
                        equalTo(
                                "VERIFICATION_CODE_INVALID"
                        )
                );

        User user =
                userRepository
                        .findByEmail(email)
                        .orElseThrow();

        assertNull(
                user.emailVerifiedAt
        );

        assertNotNull(
                emailVerificationCodeRepository
                        .findById(user.id)
        );
    }

    @Test
    void shouldChangeEmailResetVerificationAndRevokeRefreshTokens() {
        String oldEmail =
                "old-email@example.com";

        String newEmail =
                "new-email@example.com";

        TestSession session =
                register(
                        oldEmail,
                        PASSWORD
                );

        /*
         * Verify the original address first so that
         * we can ensure the verification status is
         * reset when the email address changes.
         */
        sendVerificationCode(
                session.accessToken()
        );

        String code =
                getLatestVerificationCode(
                        oldEmail
                );

        given()
                .contentType(
                        "application/json"
                )
                .auth()
                .oauth2(
                        session.accessToken()
                )
                .body(
                        Map.of(
                                "code",
                                code
                        )
                )
                .when()
                .post(
                        "/api/account/email-verification/verify"
                )
                .then()
                .statusCode(204);

        User verifiedUser =
                userRepository
                        .findByEmail(oldEmail)
                        .orElseThrow();

        assertNotNull(
                verifiedUser.emailVerifiedAt
        );

        /*
         * The User above is now managed by the
         * test persistence context. The following
         * HTTP request uses another persistence
         * context, so clear this one afterwards
         * before reading the updated entity.
         */
        mailbox.clear();

        given()
                .contentType(
                        "application/json"
                )
                .auth()
                .oauth2(
                        session.accessToken()
                )
                .body(
                        Map.of(
                                "email",
                                newEmail,
                                "currentPassword",
                                PASSWORD
                        )
                )
                .when()
                .patch(
                        "/api/account/email"
                )
                .then()
                .statusCode(204);

        /*
         * Force the next repository queries to read
         * the current database state instead of
         * returning the User instance previously
         * cached by Hibernate in this test.
         */
        entityManager.clear();

        assertTrue(
                userRepository
                        .findByEmail(oldEmail)
                        .isEmpty()
        );

        User user =
                userRepository
                        .findByEmail(newEmail)
                        .orElseThrow();

        assertEquals(
                newEmail,
                user.email
        );

        /*
         * The new address must be verified again.
         */
        assertNull(
                user.emailVerifiedAt
        );

        /*
         * Refresh tokens created before the email
         * change must be revoked.
         */
        given()
                .contentType(
                        "application/json"
                )
                .body(
                        Map.of(
                                "refreshToken",
                                session.refreshToken()
                        )
                )
                .when()
                .post(
                        "/api/auth/refresh"
                )
                .then()
                .statusCode(401)
                .body(
                        "code",
                        equalTo(
                                "INVALID_REFRESH_TOKEN"
                        )
                );

        /*
         * The old access token remains valid until
         * its expiration and identifies the account
         * using the User UUID.
         *
         * It can therefore request a verification
         * code for the new email address.
         */
        sendVerificationCode(
                session.accessToken()
        );

        assertEquals(
                1,
                mailbox
                        .getMailMessagesSentTo(
                                newEmail
                        )
                        .size()
        );

        assertEquals(
                0,
                mailbox
                        .getMailMessagesSentTo(
                                oldEmail
                        )
                        .size()
        );

        /*
         * The previous email address can no longer
         * be used to authenticate.
         */
        given()
                .contentType(
                        "application/json"
                )
                .body(
                        Map.of(
                                "email",
                                oldEmail,
                                "password",
                                PASSWORD
                        )
                )
                .when()
                .post(
                        "/api/auth/login"
                )
                .then()
                .statusCode(401)
                .body(
                        "code",
                        equalTo(
                                "EMAIL_NOT_FOUND"
                        )
                );
    }

    @Test
    void shouldChangePasswordAndRevokeRefreshTokens() {
        String email =
                "change-password@example.com";

        TestSession session =
                register(
                        email,
                        PASSWORD
                );

        given()
                .contentType(
                        "application/json"
                )
                .auth()
                .oauth2(
                        session.accessToken()
                )
                .body(
                        Map.of(
                                "currentPassword",
                                PASSWORD,
                                "newPassword",
                                NEW_PASSWORD
                        )
                )
                .when()
                .patch(
                        "/api/account/password"
                )
                .then()
                .statusCode(204);

        /*
         * Refresh tokens issued before the password
         * change must no longer be valid.
         */
        given()
                .contentType(
                        "application/json"
                )
                .body(
                        Map.of(
                                "refreshToken",
                                session.refreshToken()
                        )
                )
                .when()
                .post(
                        "/api/auth/refresh"
                )
                .then()
                .statusCode(401)
                .body(
                        "code",
                        equalTo(
                                "INVALID_REFRESH_TOKEN"
                        )
                );

        /*
         * The old password must fail.
         */
        given()
                .contentType(
                        "application/json"
                )
                .body(
                        Map.of(
                                "email",
                                email,
                                "password",
                                PASSWORD
                        )
                )
                .when()
                .post(
                        "/api/auth/login"
                )
                .then()
                .statusCode(401)
                .body(
                        "code",
                        equalTo(
                                "INVALID_PASSWORD"
                        )
                );

        /*
         * The new password must work.
         */
        given()
                .contentType(
                        "application/json"
                )
                .body(
                        Map.of(
                                "email",
                                email,
                                "password",
                                NEW_PASSWORD
                        )
                )
                .when()
                .post(
                        "/api/auth/login"
                )
                .then()
                .statusCode(200);
    }

    @Test
    void shouldDeleteAccount() {
        String email =
                "delete-account@example.com";

        TestSession session =
                register(
                        email,
                        PASSWORD
                );

        given()
                .contentType(
                        "application/json"
                )
                .auth()
                .oauth2(
                        session.accessToken()
                )
                .body(
                        Map.of(
                                "currentPassword",
                                PASSWORD
                        )
                )
                .when()
                .delete(
                        "/api/account"
                )
                .then()
                .statusCode(204);

        entityManager.clear();

        assertTrue(
                userRepository
                        .findByEmail(email)
                        .isEmpty()
        );

        /*
         * Authentication is impossible after the
         * account has been deleted.
         */
        given()
                .contentType(
                        "application/json"
                )
                .body(
                        Map.of(
                                "email",
                                email,
                                "password",
                                PASSWORD
                        )
                )
                .when()
                .post(
                        "/api/auth/login"
                )
                .then()
                .statusCode(401)
                .body(
                        "code",
                        equalTo(
                                "EMAIL_NOT_FOUND"
                        )
                );

        /*
         * Refresh tokens are removed by the database
         * ON DELETE CASCADE constraint.
         */
        given()
                .contentType(
                        "application/json"
                )
                .body(
                        Map.of(
                                "refreshToken",
                                session.refreshToken()
                        )
                )
                .when()
                .post(
                        "/api/auth/refresh"
                )
                .then()
                .statusCode(401)
                .body(
                        "code",
                        equalTo(
                                "INVALID_REFRESH_TOKEN"
                        )
                );
    }

    @Test
    void shouldRequireAuthenticationForAccountEndpoints() {
        given()
                .contentType(
                        "application/json"
                )
                .when()
                .post(
                        "/api/account/email-verification/send"
                )
                .then()
                .statusCode(401);
    }

    private TestSession register(
            String email,
            String password
    ) {
        var response =
                given()
                        .contentType(
                                "application/json"
                        )
                        .body(
                                Map.of(
                                        "email",
                                        email,
                                        "password",
                                        password
                                )
                        )
                        .when()
                        .post(
                                "/api/auth/register"
                        )
                        .then()
                        .statusCode(201)
                        .extract()
                        .response();

        return new TestSession(
                response.path(
                        "accessToken"
                ),
                response.path(
                        "refreshToken"
                )
        );
    }

    private void sendVerificationCode(
            String accessToken
    ) {
        given()
                .contentType(
                        "application/json"
                )
                .auth()
                .oauth2(
                        accessToken
                )
                .when()
                .post(
                        "/api/account/email-verification/send"
                )
                .then()
                .statusCode(204);
    }

    private String getLatestVerificationCode(
            String email
    ) {
        List<MailMessage> mails =
                mailbox.getMailMessagesSentTo(
                        email
                );

        assertFalse(
                mails.isEmpty(),
                "No verification email was sent to "
                        + email
        );

        return extractVerificationCode(
                mails.get(
                        mails.size() - 1
                )
        );
    }

    private String extractVerificationCode(
            MailMessage mail
    ) {
        Matcher matcher =
                VERIFICATION_CODE_PATTERN.matcher(
                        mail.getText()
                );

        assertTrue(
                matcher.find(),
                "No six-digit verification code found in email"
        );

        return matcher.group();
    }

    private List<String> getJwtGroups(
            String token
    ) throws Exception {

        String[] parts =
                token.split("\\.");

        assertEquals(
                3,
                parts.length
        );

        byte[] decodedPayload =
                Base64
                        .getUrlDecoder()
                        .decode(
                                parts[1]
                        );

        JsonNode payload =
                objectMapper.readTree(
                        new String(
                                decodedPayload,
                                StandardCharsets.UTF_8
                        )
                );

        JsonNode groupsNode =
                payload.get(
                        "groups"
                );

        assertNotNull(
                groupsNode
        );

        List<String> groups =
                new ArrayList<>();

        groupsNode.forEach(
                group ->
                        groups.add(
                                group.asText()
                        )
        );

        return groups;
    }

    private record TestSession(
            String accessToken,
            String refreshToken
    ) {
    }
}