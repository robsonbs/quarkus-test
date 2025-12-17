package com.robsonbs;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

@QuarkusTest
class SecurityIntegrationTest {

    @Test
    void anonymousUserIsRedirectedToLoginWhenAccessingProtectedResource() {
        given()
                .redirects().follow(false)
                .when().get("/users")
                .then()
                .statusCode(302)
                .header("Location", containsString("/login"));
    }

    @Test
    void loginPageLoadsSuccessfully() {
        given()
                .when().get("/login")
                .then()
                .statusCode(200)
                .body(containsString("Acesse sua conta"));
    }
}
