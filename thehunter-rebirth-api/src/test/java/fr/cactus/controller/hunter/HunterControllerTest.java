package fr.cactus.controller.hunter;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class HunterControllerTest {

    @Test
    void shouldCreateHunter() {
        String token = createUserAndGetToken();
        String handle = uniqueHandle("hunter");

        given()
                .auth()
                .oauth2(token)
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "handle": "%s",
                          "gender": 1,
                          "face": 1000001
                        }
                        """.formatted(handle))
                .when()
                .post("/api/hunters")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("handle", equalTo(handle))
                .body("gender", equalTo(1))
                .body("face", equalTo(1000001))
                .body(
                        "profilePicture",
                        equalTo(
                                "/assets/profile-pictures/1000001.png"
                        )
                )
                .body("bannerId", equalTo(1))
                .body("hunterScore", equalTo(0));
    }

    @Test
    void shouldRejectCreateWithoutAuthentication() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "handle": "TestHunter",
                          "gender": 1,
                          "face": 1000001
                        }
                        """)
                .when()
                .post("/api/hunters")
                .then()
                .statusCode(401);
    }

    @Test
    void shouldRejectSecondHunterForSameUser() {
        String token = createUserAndGetToken();

        createHunter(
                token,
                uniqueHandle("first")
        );

        given()
                .auth()
                .oauth2(token)
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "handle": "%s",
                          "gender": 0,
                          "face": 1000012
                        }
                        """.formatted(uniqueHandle("second")))
                .when()
                .post("/api/hunters")
                .then()
                .statusCode(409);
    }

    @Test
    void shouldRejectDuplicateHandle() {
        String firstToken = createUserAndGetToken();
        String secondToken = createUserAndGetToken();

        String handle = uniqueHandle("duplicate");

        createHunter(
                firstToken,
                handle
        );

        given()
                .auth()
                .oauth2(secondToken)
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "handle": "%s",
                          "gender": 1,
                          "face": 1000001
                        }
                        """.formatted(handle))
                .when()
                .post("/api/hunters")
                .then()
                .statusCode(409);
    }

    @Test
    void shouldRejectCreateWithoutHandle() {
        String token = createUserAndGetToken();

        given()
                .auth()
                .oauth2(token)
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "gender": 1,
                          "face": 1000001
                        }
                        """)
                .when()
                .post("/api/hunters")
                .then()
                .statusCode(400);
    }

    @Test
    void shouldRejectHandleTooShort() {
        String token = createUserAndGetToken();

        given()
                .auth()
                .oauth2(token)
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "handle": "abc",
                          "gender": 1,
                          "face": 1000001
                        }
                        """)
                .when()
                .post("/api/hunters")
                .then()
                .statusCode(400);
    }

    @Test
    void shouldRejectHandleTooLong() {
        String token = createUserAndGetToken();

        given()
                .auth()
                .oauth2(token)
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "handle": "abcdefghijklmnopqrstu",
                          "gender": 1,
                          "face": 1000001
                        }
                        """)
                .when()
                .post("/api/hunters")
                .then()
                .statusCode(400);
    }

    @Test
    void shouldRejectHandleWithSpace() {
        String token = createUserAndGetToken();

        given()
                .auth()
                .oauth2(token)
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "handle": "Lucas Hunter",
                          "gender": 1,
                          "face": 1000001
                        }
                        """)
                .when()
                .post("/api/hunters")
                .then()
                .statusCode(400);
    }

    @Test
    void shouldGetCurrentHunter() {
        String token = createUserAndGetToken();

        String handle = uniqueHandle("hunter");

        createHunter(
                token,
                handle
        );

        given()
                .auth()
                .oauth2(token)
                .when()
                .get("/api/hunters/me")
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("handle", equalTo(handle))
                .body("gender", equalTo(1))
                .body("face", equalTo(1000001))
                .body("hunterScore", equalTo(0));
    }

    @Test
    void shouldUpdateProfile() {
        String token = createUserAndGetToken();

        createHunter(
                token,
                uniqueHandle("old")
        );

        String newHandle = uniqueHandle("new");

        given()
                .auth()
                .oauth2(token)
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "handle": "%s",
                          "gender": 0,
                          "face": 1000012,
                          "bannerId": 2
                        }
                        """.formatted(newHandle))
                .when()
                .put("/api/hunters/me/profile")
                .then()
                .statusCode(200)
                .body("handle", equalTo(newHandle))
                .body("gender", equalTo(0))
                .body("face", equalTo(1000012))
                .body("bannerId", equalTo(2))
                .body("hunterScore", equalTo(0));
    }

    @Test
    void shouldRejectDuplicateHandleOnProfileUpdate() {
        String firstToken = createUserAndGetToken();
        String secondToken = createUserAndGetToken();

        String handle = uniqueHandle("taken");

        createHunter(
                firstToken,
                handle
        );

        createHunter(
                secondToken,
                uniqueHandle("other")
        );

        given()
                .auth()
                .oauth2(secondToken)
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "handle": "%s",
                          "gender": 1,
                          "face": 1000001,
                          "bannerId": 1
                        }
                        """.formatted(handle))
                .when()
                .put("/api/hunters/me/profile")
                .then()
                .statusCode(409);
    }

    @Test
    void shouldUpdateProfilePicture() {
        String token = createUserAndGetToken();

        createHunter(
                token,
                uniqueHandle("hunter")
        );

        given()
                .auth()
                .oauth2(token)
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "profilePicture":
                          "https://avatar.example.com/test.jpg"
                        }
                        """)
                .when()
                .put("/api/hunters/me/profile-picture")
                .then()
                .statusCode(200)
                .body(
                        "profilePicture",
                        equalTo(
                                "https://avatar.example.com/test.jpg"
                        )
                );
    }

    @Test
    void shouldReturn404WhenHunterDoesNotExist() {
        String token = createUserAndGetToken();

        given()
                .auth()
                .oauth2(token)
                .when()
                .get("/api/hunters/me")
                .then()
                .statusCode(404);
    }

    private void createHunter(
            String token,
            String handle
    ) {
        given()
                .auth()
                .oauth2(token)
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "handle": "%s",
                          "gender": 1,
                          "face": 1000001
                        }
                        """.formatted(handle))
                .when()
                .post("/api/hunters")
                .then()
                .statusCode(201);
    }

    private String uniqueHandle(String prefix) {
        String suffix = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 10);

        return prefix + suffix;
    }

    private String createUserAndGetToken() {
        String email =
                "hunter-" + UUID.randomUUID()
                        + "@test.com";

        return given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "email": "%s",
                          "password": "Test1234!"
                        }
                        """.formatted(email))
                .when()
                .post("/api/auth/register")
                .then()
                .extract()
                .path("accessToken");
    }
}