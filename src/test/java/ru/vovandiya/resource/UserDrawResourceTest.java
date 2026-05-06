package ru.vovandiya.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.vovandiya.model.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

@QuarkusTest
class UserDrawResourceTest {

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

    private long createScheduledDraw(String format, String drawDate, Integer prisePool) {
        return given().contentType("application/json")
                .body("{\"format\":\"" + format + "\",\"isInstantaneous\":false,\"isScheduled\":true,\"drawDate\":\"" + drawDate + "\",\"prisePool\":" + prisePool + "}")
                .post("/admin/draws").jsonPath().getLong("id");
    }

    private void createDrawResult(long drawId, String numbers, String status) {
        given().contentType("application/json")
                .body("{\"drawId\":" + drawId + ",\"drawnNumbers\":\"" + numbers + "\",\"status\":\"" + status + "\"}")
                .post("/admin/draw-results").then().statusCode(201);
    }

    @Test
    @TestSecurity(user = "ludoman", roles = "admin")
    void list_draws_returns_all_when_no_filters() {
        createDraw("6/45", 100);
        createDraw("7/49", 200);

        given().when().get("/user/draws")
                .then().statusCode(200)
                .body("$", hasSize(2));
    }

    @Test
    @TestSecurity(user = "ludoman", roles = "admin")
    void list_draws_empty_when_none() {
        given().when().get("/user/draws")
                .then().statusCode(200)
                .body("$", hasSize(0));
    }

    @Test
    @TestSecurity(user = "ludoman", roles = "admin")
    void list_draws_filter_by_format() {
        createDraw("6/45", 100);
        createDraw("6/45", 200);
        createDraw("7/49", 300);

        given().queryParam("format", "6/45").when().get("/user/draws")
                .then().statusCode(200)
                .body("$", hasSize(2));
    }

    @Test
    @TestSecurity(user = "ludoman", roles = "admin")
    void list_draws_filter_by_status() {
        long d1 = createDraw("6/45", 100);
        long d2 = createDraw("6/45", 200);
        createDrawResult(d1, "1,2,3,4,5,6", "COMPLETE");
        createDrawResult(d2, "", "NEW");

        given().queryParam("status", "COMPLETE").when().get("/user/draws")
                .then().statusCode(200)
                .body("$", hasSize(1))
                .body("[0].status", equalTo("COMPLETE"));
    }

    @Test
    @TestSecurity(user = "ludoman", roles = "admin")
    void list_draws_filter_by_status_new() {
        long d1 = createDraw("6/45", 100);
        long d2 = createDraw("6/45", 200);
        createDrawResult(d1, "1,2,3", "COMPLETE");
        createDrawResult(d2, "", "NEW");

        given().queryParam("status", "NEW").when().get("/user/draws")
                .then().statusCode(200)
                .body("$", hasSize(1))
                .body("[0].status", equalTo("NEW"));
    }

    @Test
    @TestSecurity(user = "ludoman", roles = "admin")
    void list_draws_filter_by_time_range() {
        createScheduledDraw("6/45", "2025-01-01T10:00:00", 100);
        createScheduledDraw("6/45", "2026-06-01T10:00:00", 200);
        createScheduledDraw("6/45", "2027-01-01T10:00:00", 300);

        given().queryParam("from", "2026-01-01T00:00:00")
                .queryParam("to", "2026-12-31T23:59:59")
                .when().get("/user/draws")
                .then().statusCode(200)
                .body("$", hasSize(1));
    }

    @Test
    @TestSecurity(user = "ludoman", roles = "admin")
    void list_draws_invalid_range_returns_400() {
        given().queryParam("from", "2027-01-01T00:00:00")
                .queryParam("to", "2026-01-01T00:00:00")
                .when().get("/user/draws")
                .then().statusCode(400);
    }

    @Test
    @TestSecurity(user = "ludoman", roles = "admin")
    void list_draws_format_and_status_combined() {
        long d1 = createDraw("6/45", 100);
        long d2 = createDraw("7/49", 200);
        createDrawResult(d1, "1,2,3", "COMPLETE");
        createDrawResult(d2, "4,5,6", "COMPLETE");

        given().queryParam("format", "6/45")
                .queryParam("status", "COMPLETE")
                .when().get("/user/draws")
                .then().statusCode(200)
                .body("$", hasSize(1))
                .body("[0].format", equalTo("6/45"));
    }

    @Test
    void list_draws_unauthenticated_returns_401() {
        given().when().get("/user/draws").then().statusCode(401);
    }

    @Test
    @TestSecurity(user = "ludoman", roles = "admin")
    void get_draw_result_returns_data() {
        long drawId = createDraw("6/45", 1000);
        createDrawResult(drawId, "7,14,21,35,42,45", "COMPLETE");

        given().when().get("/user/draws/" + drawId + "/result")
                .then().statusCode(200)
                .body("drawId", equalTo((int) drawId))
                .body("status", equalTo("COMPLETE"))
                .body("drawnNumbers", equalTo("7,14,21,35,42,45"));
    }

    @Test
    @TestSecurity(user = "ludoman", roles = "admin")
    void get_draw_result_no_result_returns_404() {
        long drawId = createDraw("6/45", 100);

        given().when().get("/user/draws/" + drawId + "/result")
                .then().statusCode(404);
    }

    @Test
    @TestSecurity(user = "ludoman", roles = "admin")
    void get_draw_result_nonexistent_draw_returns_404() {
        given().when().get("/user/draws/99999/result")
                .then().statusCode(404);
    }

    @Test
    @TestSecurity(user = "ludoman", roles = "admin")
    void get_draw_result_includes_draw_metadata() {
        long drawId = createDraw("6/45", 5000);
        createDrawResult(drawId, "1,2,3", "COMPLETE");

        given().when().get("/user/draws/" + drawId + "/result")
                .then().statusCode(200)
                .body("format", equalTo("6/45"))
                .body("prisePool", equalTo(5000));
    }

    @Test
    @TestSecurity(user = "ludoman", roles = "admin")
    void get_last_barrel_returns_last_number() {
        long drawId = createDraw("6/45", 100);
        createDrawResult(drawId, "3,17,22,41,5,9", "STARTED");

        given().when().get("/user/draws/" + drawId + "/last-barrel")
                .then().statusCode(200)
                .body("lastBarrel", equalTo("9"))
                .body("totalDrawn", equalTo(6))
                .body("drawId", equalTo((int) drawId));
    }

    @Test
    @TestSecurity(user = "ludoman", roles = "admin")
    void get_last_barrel_single_number() {
        long drawId = createDraw("6/45", 100);
        createDrawResult(drawId, "42", "STARTED");

        given().when().get("/user/draws/" + drawId + "/last-barrel")
                .then().statusCode(200)
                .body("lastBarrel", equalTo("42"))
                .body("totalDrawn", equalTo(1));
    }

    @Test
    @TestSecurity(user = "ludoman", roles = "admin")
    void get_last_barrel_no_result_returns_404() {
        long drawId = createDraw("6/45", 100);

        given().when().get("/user/draws/" + drawId + "/last-barrel")
                .then().statusCode(404);
    }

    @Test
    @TestSecurity(user = "ludoman", roles = "admin")
    void get_last_barrel_empty_numbers_returns_404() {
        long drawId = createDraw("6/45", 100);
        createDrawResult(drawId, "", "NEW");

        given().when().get("/user/draws/" + drawId + "/last-barrel")
                .then().statusCode(404);
    }

    @Test
    @TestSecurity(user = "ludoman", roles = "admin")
    void get_last_barrel_nonexistent_draw_returns_404() {
        given().when().get("/user/draws/99999/last-barrel")
                .then().statusCode(404);
    }

    @Test
    @TestSecurity(user = "ludoman", roles = "admin")
    void available_tickets_returns_only_unpurchased() {
        long userId = createAdminUser("admin_tmp");
        long drawId = createDraw("6/45", 100);

        createBareTicket(drawId, "1,2,3");
        createBareTicket(drawId, "4,5,6");

        long opId = createOperation(userId, "2026-05-01T10:00:00");
        createTicketWithOperation(drawId, opId, "7,8,9");

        given().when().get("/user/draws/" + drawId + "/tickets/available")
                .then().statusCode(200)
                .body("$", hasSize(2));
    }

    @Test
    @TestSecurity(user = "ludoman", roles = "admin")
    void available_tickets_empty_when_all_purchased() {
        long userId = createAdminUser("admin_tmp2");
        long drawId = createDraw("6/45", 100);
        long opId = createOperation(userId, "2026-05-01T10:00:00");
        createTicketWithOperation(drawId, opId, "1,2,3");

        given().when().get("/user/draws/" + drawId + "/tickets/available")
                .then().statusCode(200)
                .body("$", hasSize(0));
    }

    @Test
    @TestSecurity(user = "ludoman", roles = "admin")
    void available_tickets_nonexistent_draw_returns_404() {
        given().when().get("/user/draws/99999/tickets/available")
                .then().statusCode(404);
    }

    private long createAdminUser(String username) {
        return given().contentType("application/json")
                .body("{\"username\":\"" + username + "\",\"password\":\"x\",\"role\":\"admin\"}")
                .post("/admin/users").jsonPath().getLong("id");
    }

    private long createOperation(long userId, String timestamp) {
        return given().contentType("application/json")
                .body("{\"userId\":" + userId + ",\"timestamp\":\"" + timestamp + "\"}")
                .post("/admin/operations").jsonPath().getLong("id");
    }

    private void createBareTicket(long drawId, String picked) {
        given().contentType("application/json")
                .body("{\"drawId\":" + drawId + ",\"pickedNumbers\":\"" + picked + "\",\"prize\":0}")
                .post("/admin/tickets").then().statusCode(201);
    }

    private void createTicketWithOperation(long drawId, long opId, String picked) {
        given().contentType("application/json")
                .body("{\"drawId\":" + drawId + ",\"operationId\":" + opId + ",\"pickedNumbers\":\"" + picked + "\",\"prize\":0}")
                .post("/admin/tickets").then().statusCode(201);
    }
}
