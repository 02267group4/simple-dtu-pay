// java
package dk.dtu.pay.token.adapter.in.rest;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class TokenResourceIT {

    @Test
    void createToken_endpoint_returnsCreatedWithToken() {
        given()
                .contentType("application/json")
                .body("{\"customerId\":\"cust-x\"}")
                .when()
                .post("/tokens")
                .then()
                .statusCode(201)
                .body("token", not(nullValue()))
                .body("token.length()", greaterThan(0));
    }

    @Test
    void createToken_missingCustomerId_returnsBadRequest() {
        given()
                .contentType("application/json")
                .body("{}")
                .when()
                .post("/tokens")
                .then()
                .statusCode(400);
    }
}
