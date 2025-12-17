package com.robsonbs.filter;

import com.robsonbs.service.AuditLogService;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.vertx.http.runtime.security.QuarkusHttpUser;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

/**
 * Filtro Vert.x que intercepta requisições de autenticação (/j_security_check)
 * para registrar eventos de login na auditoria.
 * 
 * Este filtro é necessário porque o JAX-RS filter não consegue interceptar
 * as requisições de autenticação que são processadas pelo Elytron antes de
 * chegarem aos endpoints REST.
 */
@ApplicationScoped
public class LoginAuditRouteFilter {

    @Inject
    AuditLogService auditLogService;

    @Inject
    Vertx vertx;

    /**
     * Registra o filtro no router do Vert.x.
     * Prioridade alta (ordem baixa) para executar após a autenticação.
     */
    public void registerRoute(@Observes io.quarkus.vertx.http.runtime.filters.Filters filters) {
        filters.register(createLoginAuditHandler(), 1000);
    }

    private Handler<RoutingContext> createLoginAuditHandler() {
        return ctx -> {
            String path = ctx.normalizedPath();
            
            // Só processa requisições de login
            if ("/j_security_check".equals(path) && "POST".equalsIgnoreCase(ctx.request().method().name())) {
                // Adiciona um handler para executar após a resposta ser enviada
                ctx.addBodyEndHandler(v -> {
                    try {
                        int statusCode = ctx.response().getStatusCode();
                        String clientIp = resolveClientIp(ctx);
                        String userAgent = ctx.request().getHeader("User-Agent");
                        
                        // Verifica se o login foi bem-sucedido (redirect para landing page)
                        // e se há um usuário autenticado
                        if (statusCode == 302 || statusCode == 303) {
                            String location = ctx.response().headers().get("Location");
                            
                            // Se redireciona para a página de erro, é falha de login
                            if (location != null && location.contains("error")) {
                                String username = ctx.request().getFormAttribute("j_username");
                                // Executa em worker thread para permitir transações JTA
                                executeInWorkerThread(() -> recordLoginFailure(username, clientIp, userAgent));
                            } else {
                                // Login bem-sucedido - tenta obter o username
                                String username = null;
                                if (ctx.user() instanceof QuarkusHttpUser quarkusUser) {
                                    SecurityIdentity identity = quarkusUser.getSecurityIdentity();
                                    if (identity != null && !identity.isAnonymous()) {
                                        username = identity.getPrincipal().getName();
                                    }
                                }
                                
                                // Se não conseguiu pelo user, tenta pelo form
                                if (username == null || username.isBlank()) {
                                    username = ctx.request().getFormAttribute("j_username");
                                }
                                
                                if (username != null && !username.isBlank()) {
                                    final String finalUsername = username;
                                    // Executa em worker thread para permitir transações JTA
                                    executeInWorkerThread(() -> recordLoginSuccess(finalUsername, clientIp, userAgent));
                                }
                            }
                        }
                    } catch (Exception e) {
                        // Log silencioso para não quebrar o fluxo de autenticação
                        System.err.println("Erro ao registrar auditoria de login: " + e.getMessage());
                    }
                });
            }
            
            ctx.next();
        };
    }

    private void executeInWorkerThread(Runnable task) {
        vertx.executeBlocking(promise -> {
            try {
                task.run();
                promise.complete();
            } catch (Exception e) {
                System.err.println("Erro ao executar tarefa de auditoria: " + e.getMessage());
                promise.fail(e);
            }
        }, false);
    }

    private void recordLoginSuccess(String username, String clientIp, String userAgent) {
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

    private void recordLoginFailure(String username, String clientIp, String userAgent) {
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

    private String resolveClientIp(RoutingContext ctx) {
        String forwarded = ctx.request().getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        String realIp = ctx.request().getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp;
        }
        if (ctx.request().remoteAddress() != null) {
            return ctx.request().remoteAddress().host();
        }
        return "unknown";
    }
}
