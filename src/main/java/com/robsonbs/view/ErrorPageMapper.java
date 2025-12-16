package com.robsonbs.view;

import com.robsonbs.service.AuditLogService;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.inject.Inject;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.container.ContainerRequestContext;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.UUID;

/**
 * Mapeia exceções HTTP comuns para páginas HTML personalizadas.
 */
public class ErrorPageMapper {

    private static final Logger LOGGER = Logger.getLogger(ErrorPageMapper.class);

    @Inject
    @Location("errors/403.html")
    Template error403;

    @Inject
    @Location("errors/404.html")
    Template error404;

    @Inject
    @Location("errors/500.html")
    Template error500;

    @Inject
    AuditLogService auditLogService;

    @Inject
    SecurityIdentity securityIdentity;

    @ServerExceptionMapper(ForbiddenException.class)
    public Response forbidden(ForbiddenException exception) {
        TemplateInstance page = error403
                .data("message", sanitize(exception.getMessage()))
                .data("breadcrumb", List.of(
                        BreadcrumbItem.link("Início", "/"),
                        BreadcrumbItem.current("Acesso negado")
                ));
        return Response.status(Response.Status.FORBIDDEN).entity(page).build();
    }

    @ServerExceptionMapper(NotFoundException.class)
    public Response notFound(NotFoundException exception) {
        TemplateInstance page = error404
                .data("message", sanitize(exception.getMessage()))
                .data("breadcrumb", List.of(
                        BreadcrumbItem.link("Início", "/"),
                        BreadcrumbItem.current("Recurso não encontrado")
                ));
        return Response.status(Response.Status.NOT_FOUND).entity(page).build();
    }

    @ServerExceptionMapper
    public Response internalError(Throwable exception, ContainerRequestContext requestContext) {
        String incidentId = UUID.randomUUID().toString();
        recordIncident(exception, requestContext, incidentId);

        TemplateInstance page = error500
                .data("message", null)
                .data("incidentId", incidentId)
                .data("breadcrumb", List.of(
                        BreadcrumbItem.link("Início", "/"),
                        BreadcrumbItem.current("Erro interno")
                ));
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(page).build();
    }

    private String sanitize(String message) {
        if (message == null || message.isBlank()) {
            return null;
        }
        return message;
    }

    private void recordIncident(Throwable exception, ContainerRequestContext context, String incidentId) {
        try {
            String username = (securityIdentity != null && !securityIdentity.isAnonymous())
                    ? securityIdentity.getPrincipal().getName()
                    : "anonymous";

            String resourcePath = "/";
            if (context != null && context.getUriInfo() != null && context.getUriInfo().getRequestUri() != null) {
                resourcePath = context.getUriInfo().getRequestUri().getPath();
            }

            String details = truncate(buildDetails(exception, incidentId), 500);
            auditLogService.recordDomainEvent(
                    username,
                    "ERROR_500",
                    resourcePath,
                    "Exception",
                    exception != null ? exception.getClass().getName() : "Unknown",
                    details,
                    Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()
            );

            if (exception != null) {
                LOGGER.errorf(exception, "Unhandled exception captured (incidentId=%s, path=%s)", incidentId, resourcePath);
            } else {
                LOGGER.errorf("Unhandled exception captured without throwable (incidentId=%s, path=%s)", incidentId, resourcePath);
            }
        } catch (Exception logError) {
            LOGGER.error("Failed to record incident in audit log", logError);
        }
    }

    private String buildDetails(Throwable exception, String incidentId) {
        if (exception == null) {
            return "Unhandled exception (incidentId=" + incidentId + ")";
        }
        StringBuilder builder = new StringBuilder();
        builder.append(exception.getClass().getName());
        if (exception.getMessage() != null && !exception.getMessage().isBlank()) {
            builder.append(": ").append(exception.getMessage());
        }
        builder.append(" [incidentId=").append(incidentId).append("]");
        return builder.toString();
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
