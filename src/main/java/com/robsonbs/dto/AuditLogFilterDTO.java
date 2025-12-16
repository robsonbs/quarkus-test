package com.robsonbs.dto;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

/**
 * Representa os parâmetros de filtro informados pelo usuário para consulta de auditoria.
 */
public class AuditLogFilterDTO {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private String username;
    private String method;
    private String resource;
    private String entityType;
    private String entityId;
    private String from;
    private String to;
    private int page;
    private int size;

    public static AuditLogFilterDTO defaults() {
        AuditLogFilterDTO dto = new AuditLogFilterDTO();
        dto.size = 25;
        dto.page = 0;
        return dto;
    }

    public Optional<LocalDate> fromDate() {
        return parseDate(from);
    }

    public Optional<LocalDate> toDate() {
        return parseDate(to);
    }

    private Optional<LocalDate> parseDate(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(LocalDate.parse(value, FORMATTER));
        } catch (DateTimeParseException ignored) {
            return Optional.empty();
        }
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = Math.max(page, 0);
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size <= 0 ? 25 : Math.min(size, 100);
    }
}
