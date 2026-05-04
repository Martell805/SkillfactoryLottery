package ru.vovandiya.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.vovandiya.model.Draw;
import ru.vovandiya.model.DrawResult;
import ru.vovandiya.model.Operation;
import ru.vovandiya.model.Ticket;
import ru.vovandiya.model.User;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class AdminDrawResourceTest {

    @BeforeEach
    @Transactional
    void cleanup() {
        Ticket.deleteAll();
        DrawResult.deleteAll();
        Operation.deleteAll();
        Draw.deleteAll();
        User.deleteAll();
    }

    @Test
    void unauthenticated_returns_401() {
        given().when().get("/admin/draws").then().statusCode(401);
    }

    @Test
    @TestSecurity(user = "bob", roles = "user")
    void user_role_returns_403() {
        given().when().get("/admin/draws").then().statusCode(403);
    }

    @Test
    @TestSecurity(user = "admin", roles = "admin")
    void create_and_get_draw() {
        Long id = given().contentType("application/json")
                .body("{\"format\":\"6/45\",\"isInstantaneous\":true,\"isScheduled\":false,\"prisePool\":1000}")
                .when().post("/admin/draws")
                .then().statusCode(201)
                .body("id", notNullValue())
                .body("format", equalTo("6/45"))
                .extract().jsonPath().getLong("id");

        given().when().get("/admin/draws/" + id)
                .then().statusCode(200)
                .body("format", equalTo("6/45"))
                .body("prisePool", equalTo(1000));
    }

    @Test
    @TestSecurity(user = "admin", roles = "admin")
    void update_draw() {
        Long id = given().contentType("application/json")
                .body("{\"format\":\"6/45\",\"isInstantaneous\":true,\"isScheduled\":false,\"prisePool\":100}")
                .post("/admin/draws").jsonPath().getLong("id");

        given().contentType("application/json")
                .body("{\"format\":\"7/49\",\"isInstantaneous\":false,\"isScheduled\":true,\"drawDate\":\"2026-12-01T10:00:00\",\"prisePool\":500}")
                .when().put("/admin/draws/" + id)
                .then().statusCode(200)
                .body("format", equalTo("7/49"))
                .body("prisePool", equalTo(500));
    }

    @Test
    @TestSecurity(user = "admin", roles = "admin")
    void delete_draw_then_404() {
        Long id = given().contentType("application/json")
                .body("{\"format\":\"6/45\",\"isInstantaneous\":true,\"isScheduled\":false,\"prisePool\":100}")
                .post("/admin/draws").jsonPath().getLong("id");

        given().when().delete("/admin/draws/" + id).then().statusCode(204);
        given().when().get("/admin/draws/" + id).then().statusCode(404);
    }

    @Test
    @TestSecurity(user = "admin", roles = "admin")
    void list_returns_all() {
        for (int i = 0; i < 3; i++) {
            given().contentType("application/json")
                    .body("{\"format\":\"6/45\",\"isInstantaneous\":true,\"isScheduled\":false,\"prisePool\":" + i + "}")
                    .post("/admin/draws").then().statusCode(201);
        }

        given().when().get("/admin/draws")
                .then().statusCode(200)
                .body("$", hasSize(3));
    }

    @Test
    @TestSecurity(user = "admin", roles = "admin")
    void scheduled_without_drawDate_returns_400() {
        given().contentType("application/json")
                .body("{\"format\":\"6/45\",\"isInstantaneous\":false,\"isScheduled\":true,\"prisePool\":100}")
                .when().post("/admin/draws")
                .then().statusCode(400)
                .body("error", notNullValue());
    }

    @Test
    @TestSecurity(user = "admin", roles = "admin")
    void missing_format_returns_400() {
        given().contentType("application/json")
                .body("{\"isInstantaneous\":true,\"isScheduled\":false,\"prisePool\":100}")
                .when().post("/admin/draws")
                .then().statusCode(400);
    }

    @Test
    @TestSecurity(user = "admin", roles = "admin")
    void negative_prisePool_returns_400() {
        given().contentType("application/json")
                .body("{\"format\":\"6/45\",\"isInstantaneous\":true,\"isScheduled\":false,\"prisePool\":-1}")
                .when().post("/admin/draws")
                .then().statusCode(400);
    }

    @Test
    @TestSecurity(user = "admin", roles = "admin")
    void get_nonexistent_returns_404() {
        given().when().get("/admin/draws/999").then().statusCode(404);
    }

    @Test
    @TestSecurity(user = "admin", roles = "admin")
    void zero_id_returns_400() {
        given().when().get("/admin/draws/0").then().statusCode(400);
    }
}
