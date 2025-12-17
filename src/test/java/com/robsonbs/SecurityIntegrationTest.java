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

/**
 * Testes de integração para segurança e autenticação.
 * Verifica fluxos de login, logout e auditoria de autenticação.
 */
@QuarkusTest
class SecurityIntegrationTest {

    @Inject
    AuditLogDao auditLogDao;

    // =====================================================
    // Testes de Redirecionamento para Login
    // =====================================================

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
    void anonymousUserIsRedirectedFromNotesPage() {
        given()
                .redirects().follow(false)
                .when().get("/notes")
                .then()
                .statusCode(302)
                .header("Location", containsString("/login"));
    }

    @Test
    void anonymousUserIsRedirectedFromTasksPage() {
        given()
                .redirects().follow(false)
                .when().get("/tasks")
                .then()
                .statusCode(302)
                .header("Location", containsString("/login"));
    }

    @Test
    void anonymousUserIsRedirectedFromAuditPage() {
        given()
                .redirects().follow(false)
                .when().get("/audit")
                .then()
                .statusCode(302)
                .header("Location", containsString("/login"));
    }

    // =====================================================
    // Testes da Página de Login
    // =====================================================

    @Test
    void loginPageLoadsSuccessfully() {
        given()
                .when().get("/login")
                .then()
                .statusCode(200)
                .body(containsString("Acesse sua conta"));
    }

    @Test
    void loginPageDisplaysErrorOnInvalidCredentials() {
        given()
                .when().get("/login?error=true")
                .then()
                .statusCode(200)
                .body(containsString("Usuário ou senha inválidos"));
    }

    // =====================================================
    // Testes de Auditoria de Login/Logout
    // =====================================================

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
                .statusCode(303)
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

    // =====================================================
    // Testes de Acesso Autenticado (com login real)
    // =====================================================

    @Test
    void adminCanAccessUsersPageAfterLogin() {
        // Faz login como admin
        Map<String, String> cookies = loginAs("admin@example.com", "123");

        // Acessa página de usuários
        given()
                .cookies(cookies)
                .when().get("/users")
                .then()
                .statusCode(200)
                .body(containsString("Gerenciamento de Usuários"));
    }

    @Test
    void adminCanAccessProfilesPageAfterLogin() {
        Map<String, String> cookies = loginAs("admin@example.com", "123");

        given()
                .cookies(cookies)
                .when().get("/profiles")
                .then()
                .statusCode(200)
                .body(containsString("Perfis de Usuário"));
    }

    @Test
    void adminCanAccessAuditPageAfterLogin() {
        Map<String, String> cookies = loginAs("admin@example.com", "123");

        given()
                .cookies(cookies)
                .when().get("/audit")
                .then()
                .statusCode(200)
                .body(containsString("Auditoria"));
    }

    @Test
    void userCanAccessNotesPageAfterLogin() {
        Map<String, String> cookies = loginAs("user@example.com", "123");

        given()
                .cookies(cookies)
                .when().get("/notes")
                .then()
                .statusCode(200)
                .body(containsString("Minhas Anotações"));
    }

    @Test
    void userCanAccessTasksPageAfterLogin() {
        Map<String, String> cookies = loginAs("user@example.com", "123");

        given()
                .cookies(cookies)
                .when().get("/tasks")
                .then()
                .statusCode(200)
                .body(containsString("Minhas Tarefas"));
    }

    @Test
    void userCanAccessDocsPageAfterLogin() {
        Map<String, String> cookies = loginAs("user@example.com", "123");

        given()
                .cookies(cookies)
                .when().get("/docs")
                .then()
                .statusCode(200)
                .body(containsString("Documentação"));
    }

    // =====================================================
    // Testes de Autorização por Perfil (RBAC)
    // =====================================================

    @Test
    void userCannotAccessUsersPage() {
        Map<String, String> cookies = loginAs("user@example.com", "123");

        given()
                .cookies(cookies)
                .when().get("/users")
                .then()
                .statusCode(403);
    }

    @Test
    void userCannotAccessProfilesPage() {
        Map<String, String> cookies = loginAs("user@example.com", "123");

        given()
                .cookies(cookies)
                .when().get("/profiles")
                .then()
                .statusCode(403);
    }

    @Test
    void userCannotAccessNewUserForm() {
        Map<String, String> cookies = loginAs("user@example.com", "123");

        given()
                .cookies(cookies)
                .when().get("/users/new")
                .then()
                .statusCode(403);
    }

    @Test
    void userCannotAccessNewProfileForm() {
        Map<String, String> cookies = loginAs("user@example.com", "123");

        given()
                .cookies(cookies)
                .when().get("/profiles/new")
                .then()
                .statusCode(403);
    }

    // =====================================================
    // Helper Methods
    // =====================================================

    /**
     * Realiza login e retorna os cookies de sessão.
     */
    private Map<String, String> loginAs(String username, String password) {
        return given()
                .contentType(ContentType.URLENC)
                .formParam("j_username", username)
                .formParam("j_password", password)
                .redirects().follow(false)
                .when()
                .post("/j_security_check")
                .then()
                .extract()
                .cookies();
    }
}
