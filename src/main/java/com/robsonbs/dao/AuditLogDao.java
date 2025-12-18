package com.robsonbs.dao;

import com.robsonbs.model.AuditLog;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Repositório de acesso a dados (DAO) para a entidade {@link AuditLog}.
 * 
 * <p>Esta classe implementa o padrão Data Access Object utilizando o Quarkus Panache,
 * fornecendo operações de persistência para registros de auditoria do sistema.</p>
 * 
 * <h2>Função no Sistema</h2>
 * <p>O AuditLogDao é responsável por armazenar e consultar registros de auditoria
 * que rastreiam todas as ações sensíveis realizadas no sistema. Esses registros
 * são essenciais para:</p>
 * <ul>
 *   <li>Conformidade com requisitos de segurança</li>
 *   <li>Rastreabilidade de ações de usuários</li>
 *   <li>Debugging e investigação de incidentes</li>
 *   <li>Análise de uso do sistema</li>
 * </ul>
 * 
 * <h2>Padrão de Arquitetura</h2>
 * <pre>
 * Filter/Service → AuditLogService → AuditLogDao → Database
 * </pre>
 * 
 * <h2>Consultas Comuns</h2>
 * <p>Exemplos de queries que podem ser executadas usando métodos do Panache:</p>
 * <pre>{@code
 * // Por ação
 * find("action", "USER_CREATED").list();
 * 
 * // Por usuário
 * find("username", "admin@example.com").list();
 * 
 * // Por entidade
 * find("entityType = ?1 and entityId = ?2", "Task", "123").list();
 * 
 * // Por período (usando JPQL)
 * find("occurredAt >= ?1 and occurredAt <= ?2", startDate, endDate).list();
 * 
 * // Com paginação
 * find("action", action).page(Page.of(0, 20)).list();
 * }</pre>
 * 
 * <h2>Métodos Herdados do Panache</h2>
 * <ul>
 *   <li>{@code persist(entity)} - Persiste novo registro de auditoria</li>
 *   <li>{@code findById(id)} - Busca por ID</li>
 *   <li>{@code listAll()} - Lista todos os registros</li>
 *   <li>{@code find(query, params)} - Busca com filtros JPQL</li>
 *   <li>{@code count()} - Conta registros</li>
 * </ul>
 * 
 * <h2>Exemplo de Uso</h2>
 * <pre>{@code
 * @Inject
 * AuditLogDao auditLogDao;
 * 
 * // Buscar logs de criação de usuários
 * List<AuditLog> userCreations = auditLogDao
 *     .find("action", "USER_CREATED")
 *     .list();
 * 
 * // Buscar logs por período
 * List<AuditLog> recentLogs = auditLogDao
 *     .find("occurredAt >= ?1", oneHourAgo)
 *     .list();
 * }</pre>
 * 
 * @author Sistema de Gestão de Notas e Tarefas
 * @version 1.0
 * @since 2025-12-01
 * @see AuditLog
 * @see com.robsonbs.service.AuditLogService
 * @see com.robsonbs.controller.AuditController
 * @see PanacheRepository
 */
@ApplicationScoped
public class AuditLogDao implements PanacheRepository<AuditLog> {
}
