package com.robsonbs.filter;

import com.robsonbs.service.AuditLogService;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

import java.util.Set;

@Provider
@Priority(Priorities.USER + 10)
public class AuditLogFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Set<String> EXCLUDED_PATHS = Set.of(
            "login",
            "j_security_check",
            "logout",
            "css",
            "favicon.ico",
            "q",
            "metrics",
            "health"
    );

    private static final String CONTEXT_PROPERTY = "__AUDIT_CONTEXT__";

    @Inject
    AuditLogService auditLogService;

    @Inject
    SecurityIdentity securityIdentity;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        if (securityIdentity == null || securityIdentity.isAnonymous()) {
            return;
        }

        String rawPath = requestContext.getUriInfo().getPath();
        if (isExcluded(rawPath)) {
            return;
        }

        String path = normalizePath(rawPath);
        String method = requestContext.getMethod();
        String clientIp = resolveClientIp(requestContext);
        String userAgent = requestContext.getHeaderString("User-Agent");

        AuditRequestContext context = new AuditRequestContext();
        context.username = securityIdentity.getPrincipal().getName();
        context.method = method;
        context.resourcePath = path;
        context.clientIp = clientIp;
        context.userAgent = userAgent;
        requestContext.setProperty(CONTEXT_PROPERTY, context);
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        Object attribute = requestContext.getProperty(CONTEXT_PROPERTY);
        if (!(attribute instanceof AuditRequestContext context)) {
            return;
        }

        String action = context.method + " " + context.resourcePath;
        auditLogService.record(
                context.username,
                action,
                context.method,
                context.resourcePath,
                context.clientIp,
                null,
                null,
                null,
                context.userAgent,
                responseContext != null ? responseContext.getStatus() : null
        );
    }

    private boolean isExcluded(String path) {
        if (path == null || path.isBlank()) {
            return false;
        }
        String normalized = path.startsWith("/") ? path.substring(1) : path;
        return EXCLUDED_PATHS.stream().anyMatch(normalized::startsWith);
    }

    private String normalizePath(String path) {
        if (path == null || path.isBlank()) {
            return "/";
        }
        return path.startsWith("/") ? path : "/" + path;
    }

    private String resolveClientIp(ContainerRequestContext context) {
        String forwarded = context.getHeaderString("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        String realIp = context.getHeaderString("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp;
        }
        Object remote = context.getProperty("io.quarkus.http.remote-address");
        return remote != null ? remote.toString() : null;
    }

    private static final class AuditRequestContext {
        private String username;
        private String method;
        private String resourcePath;
        private String clientIp;
        private String userAgent;
    }
}
