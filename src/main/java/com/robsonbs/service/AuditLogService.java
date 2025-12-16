package com.robsonbs.service;

import com.robsonbs.dao.AuditLogDao;
import com.robsonbs.model.AuditLog;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class AuditLogService {

    private static final Sort DEFAULT_SORT = Sort.by("occurredAt").descending();

    @Inject
    AuditLogDao auditLogDao;

    @Transactional
    public void record(String username,
                       String action,
                       String httpMethod,
                       String resourcePath,
                       String clientIp) {
        record(username, action, httpMethod, resourcePath, clientIp, null, null, null, null, null);
    }

    @Transactional
    public void record(String username,
                       String action,
                       String httpMethod,
                       String resourcePath,
                       String clientIp,
                       String entityType,
                       String entityId,
                       String details,
                       String userAgent,
                       Integer statusCode) {
        AuditLog log = new AuditLog();
        log.setUsername(username);
        log.setAction(action);
        log.setHttpMethod(httpMethod);
        log.setResourcePath(resourcePath);
        log.setClientIp(clientIp);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setDetails(details);
        log.setUserAgent(userAgent);
        log.setStatusCode(statusCode);
        auditLogDao.persist(log);
    }

    public List<AuditLog> listRecent(int limit) {
        return auditLogDao.findAll(DEFAULT_SORT).page(0, limit).list();
    }

    public List<AuditLog> listAll() {
        return auditLogDao.findAll(DEFAULT_SORT).list();
    }

    public AuditLogPage search(AuditLogCriteria criteria) {
        StringBuilder jpql = new StringBuilder("1=1");
        Map<String, Object> params = new HashMap<>();

        criteria.username().filter(s -> !s.isBlank()).ifPresent(username -> {
            jpql.append(" and lower(username) like :username");
            params.put("username", "%" + username.toLowerCase() + "%");
        });

        criteria.method().filter(s -> !s.isBlank()).ifPresent(method -> {
            jpql.append(" and httpMethod = :method");
            params.put("method", method);
        });

        criteria.resource().filter(s -> !s.isBlank()).ifPresent(resource -> {
            jpql.append(" and lower(resourcePath) like :resource");
            params.put("resource", "%" + resource.toLowerCase() + "%");
        });

        criteria.entityType().filter(s -> !s.isBlank()).ifPresent(type -> {
            jpql.append(" and entityType = :entityType");
            params.put("entityType", type);
        });

        criteria.entityId().filter(s -> !s.isBlank()).ifPresent(id -> {
            jpql.append(" and entityId = :entityId");
            params.put("entityId", id);
        });

        criteria.from().ifPresent(from -> {
            jpql.append(" and occurredAt >= :from");
            params.put("from", from);
        });

        criteria.to().ifPresent(to -> {
            jpql.append(" and occurredAt <= :to");
            params.put("to", to);
        });

        io.quarkus.hibernate.orm.panache.PanacheQuery<AuditLog> query =
                auditLogDao.find(jpql.toString(), DEFAULT_SORT, params);

        Page page = Page.of(criteria.page(), criteria.size());
        query.page(page);

        List<AuditLog> results = query.list();
        long total = query.count();
        return new AuditLogPage(results, total, criteria.page(), criteria.size());
    }

    public void recordDomainEvent(String username,
                                  String action,
                                  String resourcePath,
                                  String entityType,
                                  String entityId,
                                  String details,
                                  Integer statusCode) {
        record(username, action, "DOMAIN", resourcePath, null, entityType, entityId, details, null, statusCode);
    }

    public record AuditLogCriteria(Optional<String> username,
                                   Optional<String> method,
                                   Optional<String> resource,
                                   Optional<String> entityType,
                                   Optional<String> entityId,
                                   Optional<LocalDateTime> from,
                                   Optional<LocalDateTime> to,
                                   int page,
                                   int size) {

        public static AuditLogCriteria of(String username,
                                          String method,
                                          String resource,
                                          String entityType,
                                          String entityId,
                                          LocalDateTime from,
                                          LocalDateTime to,
                                          int page,
                                          int size) {
            return new AuditLogCriteria(Optional.ofNullable(username),
                    Optional.ofNullable(method),
                    Optional.ofNullable(resource),
                    Optional.ofNullable(entityType),
                    Optional.ofNullable(entityId),
                    Optional.ofNullable(from),
                    Optional.ofNullable(to),
                    page,
                    size);
        }
    }

    public record AuditLogPage(List<AuditLog> items, long total, int page, int size) {
        public long totalPages() {
            if (size <= 0) {
                return 1;
            }
            return (total + size - 1) / size;
        }
    }
}
