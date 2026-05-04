package ru.vovandiya.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.vovandiya.model.Operation;
import ru.vovandiya.model.User;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.hasKey;

@QuarkusTest
class AdminUserResourceTest {

    @BeforeEach
    @Transactional
    void cleanup() {
        Operation.deleteAll();
        User.deleteAll();
    }

    @Test
    @TestSecurity(user = "admin", roles = "admin")
    void create_user_and_password_not_in_response() {
        given().contentType("application/json")
                .body("{\"username\":\"alice\",\"password\":\"secret\",\"role\":\"user\"}")
                .when().post("/admin/users")
                .then().statusCode(201)
                .body("username", equalTo("alice"))
                .body("$", not(hasKey("password")));
    }

    @Test
    @TestSecurity(user = "admin", roles = "admin")
    void list_users_no_password() {
        given().contentType("application/json")
                .body("{\"username\":\"alice\",\"password\":\"secret\",\"role\":\"user\"}")
                .post("/admin/users").then().statusCode(201);

        given().when().get("/admin/users")
                .then().statusCode(200)
                .body("[0]", not(hasKey("password")));
    }

    @Test
    @TestSecurity(user = "admin", roles = "admin")
    void duplicate_username_returns_409() {
        given().contentType("application/json")
                .body("{\"username\":\"alice\",\"password\":\"x\",\"role\":\"user\"}")
                .post("/admin/users").then().statusCode(201);

        given().contentType("application/json")
                .body("{\"username\":\"alice\",\"password\":\"y\",\"role\":\"user\"}")
                .when().post("/admin/users")
                .then().statusCode(409);
    }

    @Test
    @TestSecurity(user = "admin", roles = "admin")
    void update_keeps_username_unique_for_self() {
        Long id = given().contentType("application/json")
                .body("{\"username\":\"alice\",\"password\":\"x\",\"role\":\"user\"}")
                .post("/admin/users").jsonPath().getLong("id");

        given().contentType("application/json")
                .body("{\"username\":\"alice\",\"password\":\"\",\"role\":\"admin\"}")
                .when().put("/admin/users/" + id)
                .then().statusCode(200)
                .body("role", equalTo("admin"));
    }

    @Test
    @TestSecurity(user = "admin", roles = "admin")
    void delete_user() {
        Long id = given().contentType("application/json")
                .body("{\"username\":\"alice\",\"password\":\"x\",\"role\":\"user\"}")
                .post("/admin/users").jsonPath().getLong("id");

        given().when().delete("/admin/users/" + id).then().statusCode(204);
        given().when().get("/admin/users/" + id).then().statusCode(404);
    }
}
