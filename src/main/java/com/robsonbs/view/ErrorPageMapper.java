package com.robsonbs.view;

import com.robsonbs.service.AuditLogService;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.identity.SecurityIdentity;
import io.vertx.core.Vertx;
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
 * Mapeador de exceções HTTP para páginas de erro personalizadas.
 * 
 * <p>Este componente intercepta exceções não tratadas durante o processamento
 * de requisições JAX-RS e renderiza páginas HTML amigáveis ao usuário,
 * mantendo a consistência visual da aplicação.</p>
 * 
 * <h2>Exceções Tratadas</h2>
 * <table border="1">
 *   <tr><th>Exceção</th><th>Status</th><th>Template</th><th>Descrição</th></tr>
 *   <tr><td>{@link ForbiddenException}</td><td>403</td><td>errors/403.html</td><td>Acesso negado</td></tr>
 *   <tr><td>{@link NotFoundException}</td><td>404</td><td>errors/404.html</td><td>Recurso não encontrado</td></tr>
 *   <tr><td>{@link Throwable}</td><td>500</td><td>errors/500.html</td><td>Erro interno do servidor</td></tr>
 * </table>
 * 
 * <h2>Auditoria de Erros 500</h2>
 * <p>Erros internos são automaticamente auditados com:</p>
 * <ul>
 *   <li>Identificador único do incidente (UUID)</li>
 *   <li>Classe e mensagem da exceção</li>
 *   <li>Usuário autenticado ou "anonymous"</li>
 *   <li>Caminho do recurso que causou o erro</li>
 * </ul>
 * 
 * <h2>Execução Assíncrona</h2>
 * <p>O registro de auditoria para erros 500 é executado em worker thread
 * do Vert.x para evitar problemas com transações JTA no event loop.</p>
 * 
 * <h2>Sanitização</h2>
 * <p>Mensagens de erro são sanitizadas antes de exibição para evitar
 * exposição de informações sensíveis. Detalhes técnicos são truncados
 * a 500 caracteres no log de auditoria.</p>
 * 
 * @author Sistema de Tratamento de Erros
 * @version 1.0
 * @since 1.0
 * @see ServerExceptionMapper
 * @see AuditLogService
 * @see BreadcrumbItem
 */
public class ErrorPageMapper {

    /**
     * Logger para registro de exceções não tratadas.
     */
    private static final Logger LOGGER = Logger.getLogger(ErrorPageMapper.class);

    /**
     * Template para página de erro 403 (Acesso Negado).
     */
    @Inject
    @Location("errors/403.html")
    Template error403;

    /**
     * Template para página de erro 404 (Não Encontrado).
     */
    @Inject
    @Location("errors/404.html")
    Template error404;

    /**
     * Template para página de erro 500 (Erro Interno).
     */
    @Inject
    @Location("errors/500.html")
    Template error500;

    /**
     * Serviço de auditoria para registro de incidentes.
     */
    @Inject
    AuditLogService auditLogService;

    /**
     * Identidade de segurança do usuário atual.
     */
    @Inject
    SecurityIdentity securityIdentity;

    /**
     * Instância Vert.x para execução em worker threads.
     */
    @Inject
    Vertx vertx;

    /**
     * Trata exceções de acesso negado (HTTP 403).
     * 
     * <p>Renderiza página amigável informando que o usuário não tem
     * permissão para acessar o recurso solicitado.</p>
     * 
     * @param exception exceção ForbiddenException capturada
     * @return resposta HTTP 403 com página renderizada
     */
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

    /**
     * Trata exceções de recurso não encontrado (HTTP 404).
     * 
     * <p>Renderiza página informando que o recurso solicitado
     * não existe ou foi removido.</p>
     * 
     * @param exception exceção NotFoundException capturada
     * @return resposta HTTP 404 com página renderizada
     */
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

    /**
     * Trata exceções não mapeadas (HTTP 500).
     * 
     * <p>Captura qualquer exceção não tratada, gera um ID de incidente
     * único, registra na auditoria e renderiza página genérica de erro.</p>
     * 
     * <p>O ID do incidente é exibido ao usuário para referência em
     * suporte técnico e pode ser usado para correlacionar logs.</p>
     * 
     * @param exception  exceção não tratada
     * @param requestContext contexto da requisição para extração de metadados
     * @return resposta HTTP 500 com página renderizada e ID do incidente
     */
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

    /**
     * Sanitiza mensagem de erro removendo valores nulos ou vazios.
     * 
     * @param message mensagem a sanitizar
     * @return mensagem ou {@code null} se vazia
     */
    private String sanitize(String message) {
        if (message == null || message.isBlank()) {
            return null;
        }
        return message;
    }

    /**
     * Registra incidente de erro 500 na auditoria.
     * 
     * <p>Extrai informações do contexto e exceção, construindo um
     * registro detalhado para análise posterior. Executa em worker
     * thread para suportar transações JTA.</p>
     * 
     * @param exception  exceção capturada
     * @param context    contexto da requisição
     * @param incidentId identificador único do incidente
     */
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
            
            // Captura variáveis finais para uso na lambda
            final String finalUsername = username;
            final String finalResourcePath = resourcePath;
            final String finalDetails = details;
            final String exceptionClass = exception != null ? exception.getClass().getName() : "Unknown";
            
            // Executa em worker thread para evitar erro JTA na thread IO
            vertx.executeBlocking(() -> {
                auditLogService.recordDomainEvent(
                        finalUsername,
                        "ERROR_500",
                        finalResourcePath,
                        "Exception",
                        exceptionClass,
                        finalDetails,
                        Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()
                );
                return null;
            });

            if (exception != null) {
                LOGGER.errorf(exception, "Unhandled exception captured (incidentId=%s, path=%s)", incidentId, resourcePath);
            } else {
                LOGGER.errorf("Unhandled exception captured without throwable (incidentId=%s, path=%s)", incidentId, resourcePath);
            }
        } catch (Exception logError) {
            LOGGER.error("Failed to record incident in audit log", logError);
        }
    }

    /**
     * Constrói descrição detalhada do incidente.
     * 
     * @param exception  exceção capturada
     * @param incidentId identificador do incidente
     * @return string formatada com detalhes da exceção
     */
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

    /**
     * Trunca string para tamanho máximo.
     * 
     * @param value     valor a truncar
     * @param maxLength tamanho máximo permitido
     * @return valor truncado ou original se menor que maxLength
     */
    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
