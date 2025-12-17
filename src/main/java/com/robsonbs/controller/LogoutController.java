package com.robsonbs.controller;

import com.robsonbs.filter.AuthenticationAuditObserver;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.net.URI;

/**
 * Controller responsável pelo logout do usuário.
 * Registra o evento de logout na auditoria antes de invalidar a sessão.
 */
@Path("/logout")
public class LogoutController {

    @Inject
    SecurityIdentity securityIdentity;

    @Inject
    AuthenticationAuditObserver auditObserver;

    @GET
    @RolesAllowed({"ADMIN", "USER"})
    public Response logout(@Context HttpHeaders headers, @Context UriInfo uriInfo) {
        String username = securityIdentity.getPrincipal().getName();
        String clientIp = resolveClientIp(headers);
        String userAgent = headers.getHeaderString("User-Agent");

        // Registra o logout na auditoria ANTES de invalidar a sessão
        auditObserver.recordLogout(username, clientIp, userAgent);

        // Redireciona para login com mensagem de sucesso
        // O Quarkus irá invalidar a sessão automaticamente após o redirect
        return Response.seeOther(URI.create("/login?success=Logout+realizado+com+sucesso"))
                .header("Cache-Control", "no-cache, no-store, must-revalidate")
                .build();
    }

    private String resolveClientIp(HttpHeaders headers) {
        String forwarded = headers.getHeaderString("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        String realIp = headers.getHeaderString("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp;
        }
        return "unknown";
    }
}
