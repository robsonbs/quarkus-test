package com.robsonbs;

import com.robsonbs.dao.AuditLogDao;
import com.robsonbs.model.AuditLog;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class SecurityIntegrationTest {

    @Inject
    AuditLogDao auditLogDao;

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

    @Test
    void successfulLoginGeneratesAuditLog() {
        long countBefore = auditLogDao.count("action", "LOGIN_SUCCESS");

        // Realiza login
        Response response = given()
                .contentType(ContentType.URLENC)
                .formParam("j_username", "admin@example.com")
                .formParam("j_password", "123")
                .redirects().follow(false)
                .when()
                .post("/j_security_check");

        String location = response.getHeader("Location");
        
        // Verifica redirecionamento (302 ou 303 são válidos)
        int statusCode = response.getStatusCode();
        assertTrue(statusCode == 302 || statusCode == 303, "Deve redirecionar após login");
        assertNotNull(location, "Deve ter header Location");
        
        // Se redireciona para /users ou outra página protegida, login foi bem-sucedido
        // Se redireciona para /login?error, login falhou
        boolean loginFailed = location.contains("error");
        
        if (!loginFailed) {
            // Aguarda um pouco para o registro assíncrono
            try { Thread.sleep(1000); } catch (InterruptedException ignored) {}

            long countAfter = auditLogDao.count("action", "LOGIN_SUCCESS");
            assertTrue(countAfter > countBefore, "Deve registrar LOGIN_SUCCESS na auditoria");

            // Verifica detalhes do registro
            AuditLog latestLogin = auditLogDao.find("action = ?1 order by occurredAt desc", "LOGIN_SUCCESS")
                    .firstResult();
            assertNotNull(latestLogin, "Deve existir registro de login");
            assertEquals("admin@example.com", latestLogin.getUsername());
            assertEquals("/j_security_check", latestLogin.getResourcePath());
        } else {
            // Se o login falhou, o teste deve falhar com mensagem clara
            fail("Login falhou - redirecionou para: " + location);
        }
    }

    @Test
    void failedLoginGeneratesAuditLog() {
        long countBefore = auditLogDao.count("action", "LOGIN_FAILURE");

        // Tenta login com senha errada
        given()
                .contentType(ContentType.URLENC)
                .formParam("j_username", "admin@example.com")
                .formParam("j_password", "wrongpassword")
                .redirects().follow(false)
                .when()
                .post("/j_security_check")
                .then()
                .statusCode(302)
                .header("Location", containsString("error"));

        // Aguarda um pouco para o registro assíncrono
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}

        long countAfter = auditLogDao.count("action", "LOGIN_FAILURE");
        assertTrue(countAfter > countBefore, "Deve registrar LOGIN_FAILURE na auditoria");
    }

    @Test
    void logoutGeneratesAuditLog() {
        // Primeiro faz login para obter cookies de sessão
        Map<String, String> cookies = given()
                .contentType(ContentType.URLENC)
                .formParam("j_username", "admin@example.com")
                .formParam("j_password", "123")
                .redirects().follow(false)
                .when()
                .post("/j_security_check")
                .then()
                .extract()
                .cookies();

        // Aguarda processamento do login
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}

        long countBefore = auditLogDao.count("action", "LOGOUT");

        // Realiza logout
        given()
                .cookies(cookies)
                .redirects().follow(false)
                .when()
                .get("/logout")
                .then()
                .statusCode(303) // HTTP 303 See Other (Response.seeOther)
                .header("Location", containsString("/login"));

        // Aguarda processamento do logout
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}

        long countAfter = auditLogDao.count("action", "LOGOUT");
        assertTrue(countAfter > countBefore, "Deve registrar LOGOUT na auditoria");

        // Verifica detalhes do registro
        AuditLog latestLogout = auditLogDao.find("action = ?1 order by occurredAt desc", "LOGOUT")
                .firstResult();
        assertNotNull(latestLogout, "Deve existir registro de logout");
        assertEquals("admin@example.com", latestLogout.getUsername());
        assertEquals("/logout", latestLogout.getResourcePath());
    }
}
