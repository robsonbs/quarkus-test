package com.robsonbs.dto;

import com.robsonbs.model.AuditLog;
import java.time.LocalDateTime;

public class AuditLogResponseDTO {
    private Long id;
    private String username;
    private String action;
    private String httpMethod;
    private String resourcePath;
    private String clientIp;
    private Integer statusCode;
    private String entityType;
    private String entityId;
    private String userAgent;
    private String details;
    private LocalDateTime occurredAt;

    public AuditLogResponseDTO(AuditLog log) {
        this.id = log.getId();
        this.username = log.getUsername();
        this.action = log.getAction();
        this.httpMethod = log.getHttpMethod();
        this.resourcePath = log.getResourcePath();
        this.clientIp = log.getClientIp();
        this.statusCode = log.getStatusCode();
        this.entityType = log.getEntityType();
        this.entityId = log.getEntityId();
        this.userAgent = log.getUserAgent();
        this.details = log.getDetails();
        this.occurredAt = log.getOccurredAt();
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getAction() {
        return action;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    public String getClientIp() {
        return clientIp;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public String getEntityType() {
        return entityType;
    }

    public String getEntityId() {
        return entityId;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public String getDetails() {
        return details;
    }
}
