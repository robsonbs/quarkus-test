package com.robsonbs.filter;

import com.robsonbs.service.AuditLogService;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.vertx.http.runtime.security.FormAuthenticationMechanism;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

import java.time.LocalDateTime;

/**
 * Observa eventos de autenticação do Quarkus para registrar LOGIN e LOGOUT no audit log.
 * 
 * Como o Quarkus não expõe eventos de autenticação via CDI diretamente para form auth,
 * usamos um RouteFilter do Vert.x para interceptar as requisições de login/logout.
 */
@ApplicationScoped
public class AuthenticationAuditObserver {

    @Inject
    AuditLogService auditLogService;

    /**
     * Registra um evento de login bem-sucedido.
     */
    public void recordLoginSuccess(String username, String clientIp, String userAgent) {
        auditLogService.record(
                username,
                "LOGIN_SUCCESS",
                "POST",
                "/j_security_check",
                clientIp,
                "Session",
                null,
                "Usuário autenticado com sucesso",
                userAgent,
                302
        );
    }

    /**
     * Registra um evento de falha de login.
     */
    public void recordLoginFailure(String username, String clientIp, String userAgent) {
        auditLogService.record(
                username != null ? username : "anonymous",
                "LOGIN_FAILURE",
                "POST",
                "/j_security_check",
                clientIp,
                "Session",
                null,
                "Tentativa de login falhou",
                userAgent,
                302
        );
    }

    /**
     * Registra um evento de logout.
     */
    public void recordLogout(String username, String clientIp, String userAgent) {
        auditLogService.record(
                username,
                "LOGOUT",
                "GET",
                "/logout",
                clientIp,
                "Session",
                null,
                "Usuário encerrou a sessão",
                userAgent,
                302
        );
    }
}
