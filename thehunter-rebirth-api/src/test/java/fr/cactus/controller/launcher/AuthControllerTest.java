package fr.cactus.controller.launcher;

import fr.cactus.exception.launcher.ErrorCode;
import fr.cactus.model.enums.RoleCode;
import fr.cactus.model.launcher.AuthRefreshToken;
import fr.cactus.model.launcher.User;
import fr.cactus.repository.launcher.AuthRefreshTokenRepository;
import fr.cactus.repository.launcher.UserRepository;
import fr.cactus.service.global.TokenService;

import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.test.junit.QuarkusTest;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class AuthControllerTest {

    private static final String EMAIL = "hunter@test.com";
    private static final String PASSWORD = "Password1!";

    @Inject
    UserRepository userRepository;

    @Inject
    AuthRefreshTokenRepository refreshTokenRepository;

    @Inject
    TokenService tokenService;

    @BeforeEach
    @Transactional
    void cleanDatabase() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void registerShouldCreateHunterAccount() {
        given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "email", EMAIL,
                        "password", PASSWORD
                ))
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(201)
                .body("accessToken", not(emptyOrNullString()))
                .body("refreshToken", not(emptyOrNullString()))
                .body("user.id", not(emptyOrNullString()))
                .body("user.email", equalTo(EMAIL))
                .body("user.emailVerifiedAt", nullValue())
                .body("user.createdAt", not(emptyOrNullString()))
                .body("user.roles", hasItem(RoleCode.HUNTER.name()));

        User user = userRepository.findByEmail(EMAIL)
                .orElseThrow();

        assertNotNull(user.id);
        assertEquals(EMAIL, user.email);

        assertNotEquals(
                PASSWORD,
                user.passwordHash
        );

        assertTrue(
                BcryptUtil.matches(
                        PASSWORD,
                        user.passwordHash
                )
        );

        assertTrue(
                user.roles.stream()
                        .anyMatch(role ->
                                role.code == RoleCode.HUNTER
                        )
        );

        assertFalse(
                user.roles.stream()
                        .anyMatch(role ->
                                role.code == RoleCode.ADMIN
                        )
        );
    }

    @Test
    void registerShouldNormalizeEmail() {
        given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "email", "Hunter@Test.COM",
                        "password", PASSWORD
                ))
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(201)
                .body("user.email", equalTo(EMAIL));

        assertTrue(
                userRepository.findByEmail(EMAIL).isPresent()
        );
    }

    @Test
    void registerShouldRejectEmailWithLeadingOrTrailingSpaces() {
        given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "email", "  Hunter@Test.COM  ",
                        "password", PASSWORD
                ))
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(400)
                .body(
                        "code",
                        equalTo(ErrorCode.VALIDATION_ERROR.name())
                )
                .body(
                        "errors.find { it.field == 'email' }.code",
                        equalTo(ErrorCode.EMAIL_INVALID.name())
                );
    }

    @Test
    void registerShouldRejectExistingEmail() {
        register();

        given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "email", EMAIL,
                        "password", PASSWORD
                ))
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(409)
                .body(
                        "code",
                        equalTo(
                                ErrorCode.EMAIL_ALREADY_IN_USE.name()
                        )
                );
    }

    @Test
    void registerShouldRejectInvalidData() {
        given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "email", "invalid-email",
                        "password", "abc"
                ))
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(400)
                .body(
                        "code",
                        equalTo(
                                ErrorCode.VALIDATION_ERROR.name()
                        )
                )
                .body(
                        "errors.find { it.field == 'email' }.code",
                        equalTo(
                                ErrorCode.EMAIL_INVALID.name()
                        )
                )
                .body(
                        "errors.find { it.field == 'password' }.code",
                        equalTo(
                                ErrorCode.PASSWORD_INVALID.name()
                        )
                );
    }

    @Test
    void loginShouldAuthenticateExistingUser() {
        register();

        given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "email", EMAIL,
                        "password", PASSWORD
                ))
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .body("accessToken", not(emptyOrNullString()))
                .body("refreshToken", not(emptyOrNullString()))
                .body("user.email", equalTo(EMAIL))
                .body(
                        "user.roles",
                        hasItem(RoleCode.HUNTER.name())
                );
    }

    @Test
    void loginShouldRejectInvalidPassword() {
        register();

        given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "email", EMAIL,
                        "password", "WrongPassword1!"
                ))
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(401)
                .body(
                        "code",
                        equalTo(
                                ErrorCode.INVALID_PASSWORD.name()
                        )
                );
    }

    @Test
    void loginShouldRejectUnknownAccountWithSameError() {
        given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "email", "unknown@test.com",
                        "password", PASSWORD
                ))
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(401)
                .body(
                        "code",
                        equalTo(
                                ErrorCode.EMAIL_NOT_FOUND.name()
                        )
                );
    }

    @Test
    void refreshTokenShouldBeStoredHashed() {
        String rawRefreshToken = register()
                .jsonPath()
                .getString("refreshToken");

        String hash = tokenService.hashToken(
                rawRefreshToken
        );

        AuthRefreshToken storedToken =
                refreshTokenRepository
                        .findByTokenHash(hash)
                        .orElseThrow();

        assertEquals(
                hash,
                storedToken.tokenHash
        );

        assertNotEquals(
                rawRefreshToken,
                storedToken.tokenHash
        );

        assertTrue(storedToken.isValid());
        assertFalse(storedToken.isRevoked());
    }

    @Test
    void refreshShouldRotateRefreshToken() {
        String oldRefreshToken = register()
                .jsonPath()
                .getString("refreshToken");

        Response refreshResponse =
                given()
                        .contentType(ContentType.JSON)
                        .body(Map.of(
                                "refreshToken",
                                oldRefreshToken
                        ))
                        .when()
                        .post("/api/auth/refresh");

        refreshResponse
                .then()
                .statusCode(200)
                .body(
                        "accessToken",
                        not(emptyOrNullString())
                )
                .body(
                        "refreshToken",
                        not(emptyOrNullString())
                );

        String newRefreshToken =
                refreshResponse
                        .jsonPath()
                        .getString("refreshToken");

        assertNotEquals(
                oldRefreshToken,
                newRefreshToken
        );

        AuthRefreshToken oldStoredToken =
                refreshTokenRepository
                        .findByTokenHash(
                                tokenService.hashToken(
                                        oldRefreshToken
                                )
                        )
                        .orElseThrow();

        AuthRefreshToken newStoredToken =
                refreshTokenRepository
                        .findByTokenHash(
                                tokenService.hashToken(
                                        newRefreshToken
                                )
                        )
                        .orElseThrow();

        assertTrue(oldStoredToken.isRevoked());
        assertNotNull(oldStoredToken.revokedAt);

        assertTrue(newStoredToken.isValid());
        assertFalse(newStoredToken.isRevoked());
    }

    @Test
    void refreshShouldRejectReusedRefreshToken() {
        String oldRefreshToken = register()
                .jsonPath()
                .getString("refreshToken");

        given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "refreshToken",
                        oldRefreshToken
                ))
                .when()
                .post("/api/auth/refresh")
                .then()
                .statusCode(200);

        given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "refreshToken",
                        oldRefreshToken
                ))
                .when()
                .post("/api/auth/refresh")
                .then()
                .statusCode(401)
                .body(
                        "code",
                        equalTo(
                                ErrorCode.INVALID_REFRESH_TOKEN.name()
                        )
                );
    }

    @Test
    void refreshShouldRejectUnknownToken() {
        given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "refreshToken",
                        "this-token-does-not-exist"
                ))
                .when()
                .post("/api/auth/refresh")
                .then()
                .statusCode(401)
                .body(
                        "code",
                        equalTo(
                                ErrorCode.INVALID_REFRESH_TOKEN.name()
                        )
                );
    }

    @Test
    void logoutShouldRevokeRefreshToken() {
        String refreshToken = register()
                .jsonPath()
                .getString("refreshToken");

        given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "refreshToken",
                        refreshToken
                ))
                .when()
                .post("/api/auth/logout")
                .then()
                .statusCode(204);

        AuthRefreshToken storedToken =
                refreshTokenRepository
                        .findByTokenHash(
                                tokenService.hashToken(
                                        refreshToken
                                )
                        )
                        .orElseThrow();

        assertTrue(storedToken.isRevoked());
        assertNotNull(storedToken.revokedAt);
    }

    @Test
    void logoutShouldPreventFutureRefresh() {
        String refreshToken = register()
                .jsonPath()
                .getString("refreshToken");

        given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "refreshToken",
                        refreshToken
                ))
                .when()
                .post("/api/auth/logout")
                .then()
                .statusCode(204);

        given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "refreshToken",
                        refreshToken
                ))
                .when()
                .post("/api/auth/refresh")
                .then()
                .statusCode(401)
                .body(
                        "code",
                        equalTo(
                                ErrorCode.INVALID_REFRESH_TOKEN.name()
                        )
                );
    }

    @Test
    void logoutShouldBeIdempotentForUnknownToken() {
        given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "refreshToken",
                        "unknown-refresh-token"
                ))
                .when()
                .post("/api/auth/logout")
                .then()
                .statusCode(204);
    }

    private Response register() {
        return given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "email", EMAIL,
                        "password", PASSWORD
                ))
                .when()
                .post("/api/auth/register");
    }
}