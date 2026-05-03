package ru.vovandiya.resource;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.vovandiya.model.User;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.is;

@QuarkusTest
class AuthResourceTest {

    @BeforeEach
    @Transactional
    void cleanup() {
        User.deleteAll();
    }

    @Test
    void register_creates_user() {
        given()
                .contentType("application/json")
                .body("{\"username\":\"john\",\"password\":\"secret\"}")
                .when().post("/auth/register")
                .then().statusCode(201);
    }

    @Test
    void register_duplicate_returns_409() {
        given().contentType("application/json")
                .body("{\"username\":\"john\",\"password\":\"secret\"}")
                .post("/auth/register")
                .then().statusCode(201);

        given().contentType("application/json")
                .body("{\"username\":\"john\",\"password\":\"another\"}")
                .when().post("/auth/register")
                .then().statusCode(409);
    }

    @Test
    void login_returns_token() {
        given().contentType("application/json")
                .body("{\"username\":\"john\",\"password\":\"secret\"}")
                .post("/auth/register")
                .then().statusCode(201);

        given().contentType("application/json")
                .body("{\"username\":\"john\",\"password\":\"secret\"}")
                .when().post("/auth/login")
                .then().statusCode(200)
                .body("token", notNullValue());
    }

    @Test
    void login_wrong_password_returns_401() {
        given().contentType("application/json")
                .body("{\"username\":\"john\",\"password\":\"secret\"}")
                .post("/auth/register")
                .then().statusCode(201);

        given().contentType("application/json")
                .body("{\"username\":\"john\",\"password\":\"wrong\"}")
                .when().post("/auth/login")
                .then().statusCode(401);
    }

    @Test
    void login_unknown_user_returns_401() {
        given().contentType("application/json")
                .body("{\"username\":\"ghost\",\"password\":\"x\"}")
                .when().post("/auth/login")
                .then().statusCode(401);
    }
}
