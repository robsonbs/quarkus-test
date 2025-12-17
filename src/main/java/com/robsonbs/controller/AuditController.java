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

@Path("/audit")
@RolesAllowed("ADMIN")
@Blocking
public class AuditController {

    private static final List<String> HTTP_METHODS = List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "DOMAIN");
    private static final int DEFAULT_PAGE_SIZE = 25;

    @Inject
    AuditLogService auditLogService;

    @Inject
    Template audit;

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

    private LocalDateTime toStartOfDay(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.atStartOfDay();
    }

    private LocalDateTime toEndOfDay(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.atTime(LocalTime.MAX);
    }

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

    private void setQueryParamIfPresent(UriBuilder builder, String name, String value) {
        if (value != null) {
            builder.replaceQueryParam(name, value);
        } else {
            builder.replaceQueryParam(name);
        }
    }

    private String sanitize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
