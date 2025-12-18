package com.robsonbs.controller;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Testes de integração HTTP para controle de acesso.
 * Verifica que recursos protegidos redirecionam usuários não autenticados.
 * 
 * NOTA: Testes com @TestSecurity são feitos nos testes de serviço.
 * Estes testes focam apenas no comportamento HTTP para usuários anônimos.
 */
@QuarkusTest
class AccessControlIntegrationTest {

    // =====================================================
    // Testes de Redirecionamento para Login
    // =====================================================

    @Test
    @DisplayName("Usuário anônimo é redirecionado para login ao acessar /users")
    void anonymousUserRedirectedFromUsersList() {
        given()
                .redirects().follow(false)
                .when().get("/users")
                .then()
                .statusCode(302)
                .header("Location", containsString("/login"));
    }

    @Test
    @DisplayName("Usuário anônimo é redirecionado para login ao acessar /users/new")
    void anonymousUserRedirectedFromNewUser() {
        given()
                .redirects().follow(false)
                .when().get("/users/new")
                .then()
                .statusCode(302)
                .header("Location", containsString("/login"));
    }

    @Test
    @DisplayName("Usuário anônimo é redirecionado para login ao acessar /profiles")
    void anonymousUserRedirectedFromProfilesList() {
        given()
                .redirects().follow(false)
                .when().get("/profiles")
                .then()
                .statusCode(302)
                .header("Location", containsString("/login"));
    }

    @Test
    @DisplayName("Usuário anônimo é redirecionado para login ao acessar /profiles/new")
    void anonymousUserRedirectedFromNewProfile() {
        given()
                .redirects().follow(false)
                .when().get("/profiles/new")
                .then()
                .statusCode(302)
                .header("Location", containsString("/login"));
    }

    @Test
    @DisplayName("Usuário anônimo é redirecionado para login ao acessar /notes")
    void anonymousUserRedirectedFromNotesList() {
        given()
                .redirects().follow(false)
                .when().get("/notes")
                .then()
                .statusCode(302)
                .header("Location", containsString("/login"));
    }

    @Test
    @DisplayName("Usuário anônimo é redirecionado para login ao acessar /notes/new")
    void anonymousUserRedirectedFromNewNote() {
        given()
                .redirects().follow(false)
                .when().get("/notes/new")
                .then()
                .statusCode(302)
                .header("Location", containsString("/login"));
    }

    @Test
    @DisplayName("Usuário anônimo é redirecionado para login ao acessar /tasks")
    void anonymousUserRedirectedFromTasksList() {
        given()
                .redirects().follow(false)
                .when().get("/tasks")
                .then()
                .statusCode(302)
                .header("Location", containsString("/login"));
    }

    @Test
    @DisplayName("Usuário anônimo é redirecionado para login ao acessar /tasks/new")
    void anonymousUserRedirectedFromNewTask() {
        given()
                .redirects().follow(false)
                .when().get("/tasks/new")
                .then()
                .statusCode(302)
                .header("Location", containsString("/login"));
    }

    @Test
    @DisplayName("Usuário anônimo é redirecionado para login ao acessar /audit")
    void anonymousUserRedirectedFromAudit() {
        given()
                .redirects().follow(false)
                .when().get("/audit")
                .then()
                .statusCode(302)
                .header("Location", containsString("/login"));
    }

    // =====================================================
    // Testes de Páginas Públicas
    // =====================================================

    @Test
    @DisplayName("Página inicial redireciona para login quando não autenticado")
    void homePageRedirectsToLogin() {
        given()
                .redirects().follow(false)
                .when().get("/")
                .then()
                .statusCode(302)
                .header("Location", containsString("/login"));
    }

    @Test
    @DisplayName("Página de documentação redireciona para login quando não autenticado")
    void docsPageRedirectsToLogin() {
        given()
                .redirects().follow(false)
                .when().get("/docs")
                .then()
                .statusCode(302)
                .header("Location", containsString("/login"));
    }

    @Test
    @DisplayName("Página de login é acessível anonimamente")
    void loginPageIsAccessibleAnonymously() {
        given()
                .when().get("/login")
                .then()
                .statusCode(200)
                .contentType(containsString("text/html"))
                .body(containsString("Acesse sua conta"));
    }

    @Test
    @DisplayName("Página de login contém formulário de autenticação")
    void loginPageHasAuthForm() {
        given()
                .when().get("/login")
                .then()
                .statusCode(200)
                .body(containsString("j_security_check"))
                .body(containsString("j_username"))
                .body(containsString("j_password"));
    }

    @Test
    @DisplayName("Página de login exibe mensagem de erro com parâmetro error=true")
    void loginPageShowsErrorMessage() {
        given()
                .queryParam("error", "true")
                .when().get("/login")
                .then()
                .statusCode(200)
                .body(containsString("Usuário ou senha inválidos"));
    }

    @Test
    @DisplayName("Logout redireciona para login")
    void logoutRedirectsToLogin() {
        given()
                .redirects().follow(false)
                .when().get("/logout")
                .then()
                .statusCode(302);
    }

    // =====================================================
    // Testes de Páginas de Erro
    // =====================================================

    @Test
    @DisplayName("Rota inexistente redireciona para login quando não autenticado")
    void invalidRouteRedirectsToLogin() {
        given()
                .redirects().follow(false)
                .when().get("/pagina-inexistente-12345")
                .then()
                .statusCode(302)
                .header("Location", containsString("/login"));
    }
}
