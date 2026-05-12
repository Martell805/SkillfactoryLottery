package ru.vovandiya;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
class ExampleResourceTest {

    @Test
    @TestSecurity(user = "testuser", roles = "user")
    void testHelloEndpoint() {
        given()
            .when().get("/example/hello")
            .then()
            .statusCode(200)
            .body(is("Hello from Quarkus REST to testuser"));
    }

    @Test
    @TestSecurity(user = "adminuser", roles = "admin")
    void testSecretEndpointAsAdmin() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"number\": 5}")
            .when().get("/example/secret")
            .then()
            .statusCode(200)
            .body(is("Hello from Quarkus REST to admin! 5 users was banned!"));
    }

    @Test
    @TestSecurity(user = "testuser", roles = "user")
    void testSecretEndpointAsUser() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"number\": 5}")
            .when().get("/example/secret")
            .then()
            .statusCode(403);
    }

    @Test
    void testHelloEndpointUnauthorized() {
        given()
            .when().get("/example/hello")
            .then()
            .statusCode(401);
    }
}