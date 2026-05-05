package ru.vovandiya.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.vovandiya.model.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

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

    private long createOperation(long userId, String timestamp) {
        return given().contentType("application/json")
                .body("{\"userId\":" + userId + ",\"timestamp\":\"" + timestamp + "\"}")
                .post("/admin/operations").jsonPath().getLong("id");
    }

    private long createBareTicket(long drawId, String picked) {
        return given().contentType("application/json")
                .body("{\"drawId\":" + drawId + ",\"pickedNumbers\":\"" + picked + "\",\"prize\":0}")
                .post("/admin/tickets").jsonPath().getLong("id");
    }

    private long createPurchasedTicket(long drawId, long opId, String picked) {
        return given().contentType("application/json")
                .body("{\"drawId\":" + drawId + ",\"operationId\":" + opId + ",\"pickedNumbers\":\"" + picked + "\",\"prize\":0}")
                .post("/admin/tickets").jsonPath().getLong("id");
    }

    @Test
    @TestSecurity(user = "ludoman", roles = "admin")
    void buy_existing_ticket_succeeds() {
        createUser("ludoman");
        long drawId = createDraw("6/45", 500);
        long ticketId = createBareTicket(drawId, "5,10,15,20,25,30");

        given().contentType("application/json")
                .when().post("/user/tickets/" + ticketId + "/buy")
                .then().statusCode(200)
                .body("id", equalTo((int) ticketId))
                .body("operation", notNullValue());
    }

    @Test
    @TestSecurity(user = "ludoman", roles = "admin")
    void buy_existing_ticket_twice_returns_409() {
        createUser("ludoman");
        long drawId = createDraw("6/45", 500);
        long ticketId = createBareTicket(drawId, "5,10,15,20,25,30");

        given().contentType("application/json").when().post("/user/tickets/" + ticketId + "/buy").then().statusCode(200);
        given().contentType("application/json").when().post("/user/tickets/" + ticketId + "/buy").then().statusCode(409);
    }

    @Test
    @TestSecurity(user = "ludoman", roles = "admin")
    void buy_already_purchased_ticket_returns_409() {
        createUser("ludoman");
        long userId2 = createUser("bob");
        long drawId = createDraw("6/45", 500);
        long opId = createOperation(userId2, "2026-05-01T09:00:00");
        long ticketId = createPurchasedTicket(drawId, opId, "1,2,3");

        given().contentType("application/json")
                .when().post("/user/tickets/" + ticketId + "/buy")
                .then().statusCode(409);
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
    void buy_new_ticket_creates_ticket_and_operation() {
        createUser("ludoman");
        long drawId = createDraw("6/45", 1000);

        given().contentType("application/json")
                .body("{\"pickedNumbers\":\"3,9,17,22,38,45\"}")
                .when().post("/user/draws/" + drawId + "/tickets/buy")
                .then().statusCode(201)
                .body("id", notNullValue())
                .body("pickedNumbers", equalTo("3,9,17,22,38,45"))
                .body("operation", notNullValue());
    }

    @Test
    @TestSecurity(user = "ludoman", roles = "admin")
    void buy_new_ticket_each_call_creates_separate_ticket() {
        createUser("ludoman");
        long drawId = createDraw("6/45", 1000);

        long ticket1 = given().contentType("application/json")
                .body("{\"pickedNumbers\":\"1,2,3\"}")
                .post("/user/draws/" + drawId + "/tickets/buy")
                .jsonPath().getLong("id");

        long ticket2 = given().contentType("application/json")
                .body("{\"pickedNumbers\":\"4,5,6\"}")
                .post("/user/draws/" + drawId + "/tickets/buy")
                .jsonPath().getLong("id");

        assert ticket1 != ticket2;
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
