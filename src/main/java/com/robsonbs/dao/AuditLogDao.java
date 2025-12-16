package com.robsonbs.dao;

import com.robsonbs.model.AuditLog;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AuditLogDao implements PanacheRepository<AuditLog> {
}
