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
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;

@QuarkusTest
class AdminReportResourceTest {

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
                .body("{\"username\":\"" + username + "\",\"password\":\"x\",\"role\":\"user\"}")
                .post("/admin/users").jsonPath().getLong("id");
    }

    private long createOperation(long userId, String timestamp) {
        return given().contentType("application/json")
                .body("{\"userId\":" + userId + ",\"timestamp\":\"" + timestamp + "\"}")
                .post("/admin/operations").jsonPath().getLong("id");
    }

    private long createTicket(long drawId, long operationId, String picked, int prize) {
        return given().contentType("application/json")
                .body("{\"drawId\":" + drawId + ",\"operationId\":" + operationId + ",\"pickedNumbers\":\"" + picked + "\",\"prize\":" + prize + "}")
                .post("/admin/tickets").jsonPath().getLong("id");
    }

    private void createDrawResult(long drawId, String numbers, String status) {
        given().contentType("application/json")
                .body("{\"drawId\":" + drawId + ",\"drawnNumbers\":\"" + numbers + "\",\"status\":\"" + status + "\"}")
                .post("/admin/draw-results").then().statusCode(201);
    }

    @Test
    @TestSecurity(user = "admin", roles = "admin")
    void operations_json_lists_all() {
        long uid = createUser("alice");
        createOperation(uid, "2026-04-01T10:00:00");
        createOperation(uid, "2026-04-15T15:00:00");

        given().when().get("/admin/reports/operations/json")
                .then().statusCode(200)
                .body("$", hasSize(2));
    }

    @Test
    @TestSecurity(user = "admin", roles = "admin")
    void operations_csv_has_header_and_rows() {
        long uid = createUser("alice");
        createOperation(uid, "2026-04-01T10:00:00");

        given().when().get("/admin/reports/operations/csv")
                .then().statusCode(200)
                .body(startsWith("id,userId,username,timestamp"))
                .body(containsString("alice"));
    }

    @Test
    @TestSecurity(user = "admin", roles = "admin")
    void operations_filtered_by_userId() {
        long alice = createUser("alice");
        long bob = createUser("bob");
        createOperation(alice, "2026-04-01T10:00:00");
        createOperation(bob, "2026-04-02T10:00:00");

        given().queryParam("userId", alice).when().get("/admin/reports/operations/json")
                .then().statusCode(200)
                .body("$", hasSize(1))
                .body("[0].username", org.hamcrest.Matchers.equalTo("alice"));  // ← без .user
    }

    @Test
    @TestSecurity(user = "admin", roles = "admin")
    void operations_filtered_by_drawId() {
        long uid = createUser("alice");
        long draw1 = createDraw("6/45", 100);
        long draw2 = createDraw("7/49", 200);
        long op1 = createOperation(uid, "2026-04-01T10:00:00");
        long op2 = createOperation(uid, "2026-04-02T10:00:00");
        createTicket(draw1, op1, "1,2,3", 0);
        createTicket(draw2, op2, "4,5,6", 0);

        given().queryParam("drawId", draw1).when().get("/admin/reports/operations/json")
                .then().statusCode(200)
                .body("$", hasSize(1));
    }

    @Test
    @TestSecurity(user = "admin", roles = "admin")
    void tickets_csv_escapes_commas() {
        long uid = createUser("alice");
        long draw = createDraw("6/45", 100);
        long op = createOperation(uid, "2026-04-01T10:00:00");
        createTicket(draw, op, "1,2,3,4,5,6", 100);

        given().when().get("/admin/reports/tickets/csv")
                .then().statusCode(200)
                .body(containsString("\"1,2,3,4,5,6\""));
    }

    @Test
    @TestSecurity(user = "admin", roles = "admin")
    void draws_filtered_by_format() {
        createDraw("6/45", 100);
        createDraw("7/49", 200);
        createDraw("6/45", 300);

        given().queryParam("format", "6/45").when().get("/admin/reports/draws/json")
                .then().statusCode(200)
                .body("$", hasSize(2));
    }

    @Test
    @TestSecurity(user = "admin", roles = "admin")
    void draws_filtered_by_status() {
        long draw1 = createDraw("6/45", 100);
        long draw2 = createDraw("6/45", 200);
        createDrawResult(draw1, "1,2,3", "COMPLETE");
        createDrawResult(draw2, "", "NEW");

        given().queryParam("status", "COMPLETE").when().get("/admin/reports/draws/json")
                .then().statusCode(200)
                .body("$", hasSize(1));
    }

    @Test
    @TestSecurity(user = "admin", roles = "admin")
    void invalid_date_range_returns_400() {
        given().queryParam("from", "2026-12-01T00:00:00")
                .queryParam("to", "2026-01-01T00:00:00")
                .when().get("/admin/reports/draws/json")
                .then().statusCode(400);
    }

    @Test
    void unauthenticated_report_returns_401() {
        given().when().get("/admin/reports/draws/json").then().statusCode(401);
    }
}
