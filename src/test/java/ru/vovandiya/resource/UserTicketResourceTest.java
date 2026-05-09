package ru.vovandiya.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.vovandiya.model.*;

import static io.restassured.RestAssured.given;

@QuarkusTest
class UserTicketResourceTest {

    @BeforeEach
    @Transactional
    void cleanup() {
        Ticket.deleteAll();
        DrawResult.deleteAll();
        Operation.deleteAll();
        Draw.deleteAll();
        User.deleteAll();
    }

    private long createDraw(String format, Integer prisePool) {
        return given().contentType("application/json")
                .body("{\"format\":\"" + format + "\",\"isInstantaneous\":true,\"isScheduled\":false,\"prisePool\":" + prisePool + "}")
                .post("/admin/draws").jsonPath().getLong("id");
    }

    private long createUser(String username) {
        return given().contentType("application/json")
                .body("{\"username\":\"" + username + "\",\"password\":\"secret\",\"role\":\"user\"}")
                .post("/admin/users").jsonPath().getLong("id");
    }

    @Test
    @TestSecurity(user = "ludoman", roles = "admin")
    void buy_nonexistent_ticket_returns_404() {
        createUser("ludoman");

        given().contentType("application/json")
                .when().post("/user/tickets/99999/buy")
                .then().statusCode(404);
    }

    @Test
    void buy_existing_ticket_unauthenticated_returns_401() {
        given().contentType("application/json")
                .when().post("/user/tickets/" + 123 + "/buy")
                .then().statusCode(401);
    }

    @Test
    @TestSecurity(user = "ludoman", roles = "admin")
    void buy_new_ticket_missing_pickedNumbers_returns_400() {
        createUser("ludoman");
        long drawId = createDraw("6/45", 1000);

        given().contentType("application/json")
                .body("{}")
                .when().post("/user/draws/" + drawId + "/tickets/buy")
                .then().statusCode(400);
    }

    @Test
    @TestSecurity(user = "ludoman", roles = "admin")
    void buy_new_ticket_blank_pickedNumbers_returns_400() {
        createUser("ludoman");
        long drawId = createDraw("6/45", 1000);

        given().contentType("application/json")
                .body("{\"pickedNumbers\":\"   \"}")
                .when().post("/user/draws/" + drawId + "/tickets/buy")
                .then().statusCode(400);
    }

    @Test
    @TestSecurity(user = "ludoman", roles = "admin")
    void buy_new_ticket_nonexistent_draw_returns_404() {
        createUser("ludoman");

        given().contentType("application/json")
                .body("{\"pickedNumbers\":\"1,2,3\"}")
                .when().post("/user/draws/99999/tickets/buy")
                .then().statusCode(404);
    }

    @Test
    void buy_new_ticket_unauthenticated_returns_401() {
        given().contentType("application/json")
                .body("{\"pickedNumbers\":\"1,2,3\"}")
                .when().post("/user/draws/1/tickets/buy")
                .then().statusCode(401);
    }
}
