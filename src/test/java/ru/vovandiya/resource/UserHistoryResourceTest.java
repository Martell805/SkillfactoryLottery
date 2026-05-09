package ru.vovandiya.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.vovandiya.model.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class UserHistoryResourceTest {

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

    private void createTicketWithOperation(long drawId, long opId, String picked) {
        given().contentType("application/json")
                .body("{\"drawId\":" + drawId + ",\"operationId\":" + opId + ",\"pickedNumbers\":\"" + picked + "\",\"prize\":0}")
                .post("/admin/tickets").then().statusCode(201);
    }

    @Test
    @TestSecurity(user = "ludoman", roles = "admin")
    void my_operations_returns_only_own() {
        long ludomanId = createUser("ludoman");
        long bobId = createUser("bob");

        createOperation(ludomanId, "2026-05-01T10:00:00");
        createOperation(ludomanId, "2026-05-02T10:00:00");
        createOperation(bobId, "2026-05-03T10:00:00");

        given().when().get("/user/me/operations")
                .then().statusCode(200)
                .body("$", hasSize(2));
    }

    @Test
    @TestSecurity(user = "ludoman", roles = "admin")
    void my_operations_empty_when_none() {
        createUser("ludoman");

        given().when().get("/user/me/operations")
                .then().statusCode(200)
                .body("$", hasSize(0));
    }

    @Test
    @TestSecurity(user = "ludoman", roles = "admin")
    void my_operations_filter_by_from() {
        long ludomanId = createUser("ludoman");
        createOperation(ludomanId, "2026-01-01T10:00:00");
        createOperation(ludomanId, "2026-06-01T10:00:00");
        createOperation(ludomanId, "2026-12-01T10:00:00");

        given().queryParam("from", "2026-05-01T00:00:00")
                .when().get("/user/me/operations")
                .then().statusCode(200)
                .body("$", hasSize(2));
    }

    @Test
    @TestSecurity(user = "ludoman", roles = "admin")
    void my_operations_filter_by_to() {
        long ludomanId = createUser("ludoman");
        createOperation(ludomanId, "2026-01-01T10:00:00");
        createOperation(ludomanId, "2026-06-01T10:00:00");
        createOperation(ludomanId, "2026-12-01T10:00:00");

        given().queryParam("to", "2026-05-31T23:59:59")
                .when().get("/user/me/operations")
                .then().statusCode(200)
                .body("$", hasSize(1));
    }

    @Test
    @TestSecurity(user = "ludoman", roles = "admin")
    void my_operations_filter_by_range() {
        long ludomanId = createUser("ludoman");
        createOperation(ludomanId, "2026-01-01T10:00:00");
        createOperation(ludomanId, "2026-06-15T10:00:00");
        createOperation(ludomanId, "2026-12-01T10:00:00");

        given().queryParam("from", "2026-06-01T00:00:00")
                .queryParam("to", "2026-06-30T23:59:59")
                .when().get("/user/me/operations")
                .then().statusCode(200)
                .body("$", hasSize(1));
    }

    @Test
    @TestSecurity(user = "ludoman", roles = "admin")
    void my_operations_invalid_range_returns_400() {
        createUser("ludoman");

        given().queryParam("from", "2026-12-01T00:00:00")
                .queryParam("to", "2026-01-01T00:00:00")
                .when().get("/user/me/operations")
                .then().statusCode(400);
    }

    @Test
    @TestSecurity(user = "ludoman", roles = "admin")
    void my_operations_includes_timestamp() {
        long ludomanId = createUser("ludoman");
        createOperation(ludomanId, "2026-05-01T10:00:00");

        given().when().get("/user/me/operations")
                .then().statusCode(200)
                .body("[0].id", notNullValue())
                .body("[0].timestamp", notNullValue());
    }

    @Test
    void my_operations_unauthenticated_returns_401() {
        given().when().get("/user/me/operations").then().statusCode(401);
    }

    @Test
    @TestSecurity(user = "ludoman", roles = "admin")
    void my_tickets_returns_only_own() {
        long ludomanId = createUser("ludoman");
        long bobId = createUser("bob");
        long drawId = createDraw("0-15:6", 100);

        long ludomanOp = createOperation(ludomanId, "2026-05-01T10:00:00");
        long bobOp = createOperation(bobId, "2026-05-02T10:00:00");

        createTicketWithOperation(drawId, ludomanOp, "1,2,3");
        createTicketWithOperation(drawId, ludomanOp, "4,5,6");
        createTicketWithOperation(drawId, bobOp, "7,8,9");

        given().when().get("/user/me/tickets")
                .then().statusCode(200)
                .body("$", hasSize(2));
    }

    @Test
    @TestSecurity(user = "ludoman", roles = "admin")
    void my_tickets_empty_when_none() {
        createUser("ludoman");

        given().when().get("/user/me/tickets")
                .then().statusCode(200)
                .body("$", hasSize(0));
    }

    @Test
    @TestSecurity(user = "ludoman", roles = "admin")
    void my_tickets_filter_by_drawId() {
        long ludomanId = createUser("ludoman");
        long draw1 = createDraw("0-15:6", 100);
        long draw2 = createDraw("7/49", 200);

        long op1 = createOperation(ludomanId, "2026-05-01T10:00:00");
        long op2 = createOperation(ludomanId, "2026-05-02T10:00:00");

        createTicketWithOperation(draw1, op1, "1,2,3");
        createTicketWithOperation(draw2, op2, "4,5,6");
        createTicketWithOperation(draw1, op1, "7,8,9");

        given().queryParam("drawId", draw1)
                .when().get("/user/me/tickets")
                .then().statusCode(200)
                .body("$", hasSize(2));
    }

    @Test
    @TestSecurity(user = "ludoman", roles = "admin")
    void my_tickets_filter_by_time_range() {
        long ludomanId = createUser("ludoman");
        long drawId = createDraw("0-15:6", 100);

        long opJan = createOperation(ludomanId, "2026-01-10T10:00:00");
        long opJun = createOperation(ludomanId, "2026-06-15T10:00:00");
        long opDec = createOperation(ludomanId, "2026-12-20T10:00:00");

        createTicketWithOperation(drawId, opJan, "1,2,3");
        createTicketWithOperation(drawId, opJun, "4,5,6");
        createTicketWithOperation(drawId, opDec, "7,8,9");

        given().queryParam("from", "2026-05-01T00:00:00")
                .queryParam("to", "2026-07-31T23:59:59")
                .when().get("/user/me/tickets")
                .then().statusCode(200)
                .body("$", hasSize(1));
    }

    @Test
    @TestSecurity(user = "ludoman", roles = "admin")
    void my_tickets_filter_by_drawId_and_range_combined() {
        long ludomanId = createUser("ludoman");
        long draw1 = createDraw("0-15:6", 100);
        long draw2 = createDraw("7/49", 200);

        long opMay = createOperation(ludomanId, "2026-05-01T10:00:00");
        long opJun = createOperation(ludomanId, "2026-06-01T10:00:00");

        createTicketWithOperation(draw1, opMay, "1,2,3");
        createTicketWithOperation(draw1, opJun, "4,5,6");
        createTicketWithOperation(draw2, opMay, "7,8,9");

        given().queryParam("drawId", draw1)
                .queryParam("from", "2026-05-01T00:00:00")
                .queryParam("to", "2026-05-31T23:59:59")
                .when().get("/user/me/tickets")
                .then().statusCode(200)
                .body("$", hasSize(1))
                .body("[0].pickedNumbers", equalTo("1,2,3"));
    }

    @Test
    @TestSecurity(user = "ludoman", roles = "admin")
    void my_tickets_contains_draw_and_purchase_info() {
        long ludomanId = createUser("ludoman");
        long drawId = createDraw("0-15:6", 500);
        long opId = createOperation(ludomanId, "2026-05-01T10:00:00");
        createTicketWithOperation(drawId, opId, "5,10,15");

        given().when().get("/user/me/tickets")
                .then().statusCode(200)
                .body("[0].drawId", equalTo((int) drawId))
                .body("[0].pickedNumbers", equalTo("5,10,15"))
                .body("[0].purchasedAt", notNullValue());
    }

    @Test
    @TestSecurity(user = "ludoman", roles = "admin")
    void my_tickets_invalid_range_returns_400() {
        createUser("ludoman");

        given().queryParam("from", "2026-12-01T00:00:00")
                .queryParam("to", "2026-01-01T00:00:00")
                .when().get("/user/me/tickets")
                .then().statusCode(400);
    }

    @Test
    void my_tickets_unauthenticated_returns_401() {
        given().when().get("/user/me/tickets").then().statusCode(401);
    }

}
