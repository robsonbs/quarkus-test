package com.robsonbs.controller;

import com.robsonbs.dto.AuditLogFilterDTO;
import com.robsonbs.dto.AuditLogResponseDTO;
import com.robsonbs.service.AuditLogService;
import com.robsonbs.view.BreadcrumbItem;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.smallrye.common.annotation.Blocking;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller REST para visualização de logs de auditoria do sistema.
 * 
 * <p>Este controller fornece interface HTML para administradores consultarem
 * o histórico de eventos do sistema, incluindo operações CRUD, acessos,
 * logins/logouts e outras ações auditadas.</p>
 * 
 * <h2>Funcionalidades</h2>
 * <ul>
 *   <li>Listagem paginada de logs de auditoria</li>
 *   <li>Filtros por usuário, método HTTP, recurso, entidade e período</li>
 *   <li>Navegação entre páginas</li>
 *   <li>Exibição formatada de detalhes dos eventos</li>
 * </ul>
 * 
 * <h2>Filtros Disponíveis</h2>
 * <table border="1">
 *   <tr><th>Parâmetro</th><th>Descrição</th><th>Tipo</th></tr>
 *   <tr><td>username</td><td>Filtro por nome de usuário (parcial)</td><td>String</td></tr>
 *   <tr><td>method</td><td>Método HTTP (GET, POST, DELETE, DOMAIN)</td><td>String</td></tr>
 *   <tr><td>resource</td><td>Caminho do recurso (parcial)</td><td>String</td></tr>
 *   <tr><td>entityType</td><td>Tipo de entidade (User, Task, Note)</td><td>String</td></tr>
 *   <tr><td>entityId</td><td>ID específico da entidade</td><td>String</td></tr>
 *   <tr><td>from</td><td>Data inicial (formato: yyyy-MM-dd)</td><td>LocalDate</td></tr>
 *   <tr><td>to</td><td>Data final (formato: yyyy-MM-dd)</td><td>LocalDate</td></tr>
 *   <tr><td>page</td><td>Número da página (0-indexed)</td><td>int</td></tr>
 *   <tr><td>size</td><td>Tamanho da página (padrão: 25)</td><td>int</td></tr>
 * </table>
 * 
 * <h2>Segurança</h2>
 * <p>Acesso restrito a administradores (role ADMIN). A visualização de
 * logs de auditoria é uma operação sensível que requer privilégios
 * elevados.</p>
 * 
 * <h2>Paginação</h2>
 * <p>Os resultados são sempre paginados para performance. O controller
 * fornece URLs de navegação para próxima/anterior página mantendo
 * os filtros aplicados.</p>
 * 
 * @author Sistema de Auditoria
 * @version 1.0
 * @since 1.0
 * @see AuditLogService
 * @see AuditLogFilterDTO
 * @see AuditLogResponseDTO
 */
@Path("/audit")
@RolesAllowed("ADMIN")
@Blocking
public class AuditController {

    /**
     * Lista de métodos HTTP disponíveis para filtro.
     * Inclui DOMAIN para eventos de domínio (não-HTTP).
     */
    private static final List<String> HTTP_METHODS = List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "DOMAIN");

    /**
     * Tamanho padrão de página para listagem.
     */
    private static final int DEFAULT_PAGE_SIZE = 25;

    /**
     * Serviço de auditoria para consulta de logs.
     */
    @Inject
    AuditLogService auditLogService;

    /**
     * Template para visualização de logs.
     * Corresponde a {@code templates/audit.html}.
     */
    @Inject
    Template audit;

    /**
     * Lista logs de auditoria com filtros e paginação.
     * 
     * <p>Processa query parameters para construir critérios de busca,
     * executa a pesquisa paginada e prepara dados para renderização.</p>
     * 
     * @param uriInfo informações da URI com query parameters de filtro
     * @return template renderizado com logs e controles de paginação
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance listAuditLogs(@jakarta.ws.rs.core.Context UriInfo uriInfo) {
        AuditLogFilterDTO filter = bindFilters(uriInfo.getQueryParameters());

        AuditLogService.AuditLogCriteria criteria = AuditLogService.AuditLogCriteria.of(
                sanitize(filter.getUsername()),
                sanitize(filter.getMethod()),
                sanitize(filter.getResource()),
                sanitize(filter.getEntityType()),
                sanitize(filter.getEntityId()),
                toStartOfDay(filter.fromDate().orElse(null)),
                toEndOfDay(filter.toDate().orElse(null)),
                filter.getPage(),
                filter.getSize()
        );

        AuditLogService.AuditLogPage page = auditLogService.search(criteria);

        List<AuditLogResponseDTO> logs = page.items().stream()
                .map(AuditLogResponseDTO::new)
                .collect(Collectors.toList());

        int currentPage = page.page();
        long totalPages = page.totalPages();
        long totalItems = page.total();

        String previousPageUrl = currentPage > 0
                ? buildPageUrl(uriInfo, filter, currentPage - 1)
                : null;
        String nextPageUrl = (currentPage + 1) < totalPages
                ? buildPageUrl(uriInfo, filter, currentPage + 1)
                : null;

        return audit
                .data("logs", logs)
                .data("filters", filter)
                .data("httpMethods", HTTP_METHODS)
                .data("currentPage", currentPage)
                .data("totalPages", totalPages)
                .data("totalItems", totalItems)
                .data("hasPrevious", previousPageUrl != null)
                .data("hasNext", nextPageUrl != null)
                .data("previousPageUrl", previousPageUrl)
                .data("nextPageUrl", nextPageUrl)
                .data("breadcrumb", List.of(
                        BreadcrumbItem.link("Início", "/"),
                        BreadcrumbItem.current("Auditoria")
                ));
    }

    /**
     * Extrai e vincula filtros dos query parameters.
     * 
     * @param queryParams parâmetros da requisição
     * @return DTO com filtros populados
     */
    private AuditLogFilterDTO bindFilters(MultivaluedMap<String, String> queryParams) {
        AuditLogFilterDTO filter = AuditLogFilterDTO.defaults();
        filter.setUsername(queryParams.getFirst("username"));
        filter.setMethod(queryParams.getFirst("method"));
        filter.setResource(queryParams.getFirst("resource"));
        filter.setEntityType(queryParams.getFirst("entityType"));
        filter.setEntityId(queryParams.getFirst("entityId"));
        filter.setFrom(queryParams.getFirst("from"));
        filter.setTo(queryParams.getFirst("to"));
        filter.setSize(parseInt(queryParams.getFirst("size"), DEFAULT_PAGE_SIZE));
        filter.setPage(parseInt(queryParams.getFirst("page"), 0));
        return filter;
    }

    /**
     * Converte string para inteiro com valor padrão.
     * 
     * @param value string a converter
     * @param fallback valor padrão se conversão falhar
     * @return inteiro convertido ou fallback
     */
    private int parseInt(String value, int fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    /**
     * Converte LocalDate para início do dia (00:00:00).
     * 
     * @param date data a converter
     * @return LocalDateTime no início do dia ou {@code null}
     */
    private LocalDateTime toStartOfDay(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.atStartOfDay();
    }

    /**
     * Converte LocalDate para fim do dia (23:59:59.999999999).
     * 
     * @param date data a converter
     * @return LocalDateTime no fim do dia ou {@code null}
     */
    private LocalDateTime toEndOfDay(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.atTime(LocalTime.MAX);
    }

    /**
     * Constrói URL de navegação para página específica mantendo filtros.
     * 
     * @param uriInfo informações da URI atual
     * @param filter filtros a preservar
     * @param targetPage página de destino
     * @return URL completa com filtros e página
     */
    private String buildPageUrl(UriInfo uriInfo, AuditLogFilterDTO filter, int targetPage) {
        UriBuilder builder = UriBuilder.fromUri(uriInfo.getRequestUri());
        setQueryParamIfPresent(builder, "username", sanitize(filter.getUsername()));
        setQueryParamIfPresent(builder, "method", sanitize(filter.getMethod()));
        setQueryParamIfPresent(builder, "resource", sanitize(filter.getResource()));
        setQueryParamIfPresent(builder, "entityType", sanitize(filter.getEntityType()));
        setQueryParamIfPresent(builder, "entityId", sanitize(filter.getEntityId()));
        setQueryParamIfPresent(builder, "from", sanitize(filter.getFrom()));
        setQueryParamIfPresent(builder, "to", sanitize(filter.getTo()));
        builder.replaceQueryParam("page", targetPage);
        builder.replaceQueryParam("size", filter.getSize());
        return builder.build().toString();
    }

    /**
     * Define query parameter se valor presente, remove se ausente.
     * 
     * @param builder UriBuilder a modificar
     * @param name nome do parâmetro
     * @param value valor do parâmetro ou {@code null}
     */
    private void setQueryParamIfPresent(UriBuilder builder, String name, String value) {
        if (value != null) {
            builder.replaceQueryParam(name, value);
        } else {
            builder.replaceQueryParam(name);
        }
    }

    /**
     * Sanitiza valor removendo espaços e convertendo vazio para {@code null}.
     * 
     * @param value valor a sanitizar
     * @return valor trimado ou {@code null} se vazio
     */
    private String sanitize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
