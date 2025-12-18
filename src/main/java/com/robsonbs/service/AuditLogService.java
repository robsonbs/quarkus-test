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

/**
 * Serviço central de auditoria para registro e consulta de eventos do sistema.
 * 
 * <p>Esta classe é o ponto único de entrada para todas as operações de auditoria,
 * responsável por registrar eventos de segurança, operações de domínio e fornecer
 * capacidades de consulta e pesquisa nos logs.</p>
 * 
 * <h2>Tipos de Eventos</h2>
 * <p>O serviço suporta dois padrões principais de registro:</p>
 * <ul>
 *   <li><strong>Eventos HTTP:</strong> Capturados pelo {@code AuditFilter}, incluem
 *       método HTTP, caminho, IP do cliente, User-Agent e código de status</li>
 *   <li><strong>Eventos de Domínio:</strong> Registrados pelos Services, representam
 *       operações de negócio como criação, atualização e exclusão de entidades</li>
 * </ul>
 * 
 * <h2>Arquitetura de Registro</h2>
 * <pre>
 * ┌─────────────┐     ┌──────────────────┐     ┌───────────────┐
 * │ AuditFilter │────▶│ AuditLogService  │────▶│ AuditLogDao   │
 * └─────────────┘     │                  │     └───────────────┘
 *                     │  record()        │
 * ┌─────────────┐     │  recordDomain()  │
 * │  Services   │────▶│                  │
 * └─────────────┘     └──────────────────┘
 * </pre>
 * 
 * <h2>Capacidades de Pesquisa</h2>
 * <p>O método {@link #search(AuditLogCriteria)} suporta filtros combinados:</p>
 * <ul>
 *   <li>Por usuário (busca parcial, case-insensitive)</li>
 *   <li>Por método HTTP</li>
 *   <li>Por caminho do recurso (busca parcial)</li>
 *   <li>Por tipo de entidade</li>
 *   <li>Por ID de entidade</li>
 *   <li>Por período (data inicial e final)</li>
 * </ul>
 * <p>Os resultados são sempre ordenados por data decrescente e paginados.</p>
 * 
 * <h2>Compliance e Segurança</h2>
 * <p>Este serviço atende requisitos de:</p>
 * <ul>
 *   <li>LGPD - Rastreabilidade de acesso a dados pessoais</li>
 *   <li>SOX/ISO 27001 - Trilha de auditoria para operações sensíveis</li>
 *   <li>Segurança - Detecção de atividades suspeitas</li>
 * </ul>
 * 
 * <h2>Exemplo de Uso</h2>
 * <pre>{@code
 * // Registro de evento HTTP (via AuditFilter)
 * auditLogService.record(
 *     "user@example.com",
 *     "HTTP_REQUEST",
 *     "POST",
 *     "/users",
 *     "192.168.1.1"
 * );
 * 
 * // Registro de evento de domínio (via Services)
 * auditLogService.recordDomainEvent(
 *     "admin@example.com",
 *     "USER_CREATED",
 *     "/users",
 *     "User",
 *     "123",
 *     "Usuário criado: novo@example.com",
 *     201
 * );
 * 
 * // Pesquisa com filtros
 * AuditLogCriteria criteria = AuditLogCriteria.of(
 *     "admin",      // username (parcial)
 *     "POST",       // método HTTP
 *     null,         // resource
 *     "User",       // entityType
 *     null,         // entityId
 *     startDate,    // from
 *     endDate,      // to
 *     0,            // página
 *     20            // tamanho
 * );
 * AuditLogPage results = auditLogService.search(criteria);
 * }</pre>
 * 
 * @author Sistema de Auditoria
 * @version 1.0
 * @since 1.0
 * @see AuditLog
 * @see AuditLogDao
 * @see com.robsonbs.filter.AuditFilter
 */
@ApplicationScoped
public class AuditLogService {

    /**
     * Ordenação padrão para consultas: data decrescente (mais recente primeiro).
     */
    private static final Sort DEFAULT_SORT = Sort.by("occurredAt").descending();

    /**
     * DAO para operações de persistência de logs de auditoria.
     */
    @Inject
    AuditLogDao auditLogDao;

    /**
     * Registra um evento de auditoria básico (sobrecarga simplificada).
     * 
     * <p>Método de conveniência para registros que não precisam de
     * informações de entidade, User-Agent ou código de status.</p>
     * 
     * @param username nome do usuário que executou a ação
     * @param action tipo de ação executada
     * @param httpMethod método HTTP da requisição
     * @param resourcePath caminho do recurso acessado
     * @param clientIp endereço IP do cliente
     */
    @Transactional
    public void record(String username,
                       String action,
                       String httpMethod,
                       String resourcePath,
                       String clientIp) {
        record(username, action, httpMethod, resourcePath, clientIp, null, null, null, null, null);
    }

    /**
     * Registra um evento de auditoria completo.
     * 
     * <p>Método principal de registro que aceita todos os campos possíveis
     * de um evento de auditoria. Campos não aplicáveis podem ser {@code null}.</p>
     * 
     * @param username nome do usuário (email) que executou a ação
     * @param action código da ação (ex: LOGIN_SUCCESS, USER_CREATED)
     * @param httpMethod método HTTP (GET, POST, PUT, DELETE, DOMAIN)
     * @param resourcePath caminho do recurso (ex: /users, /tasks/5)
     * @param clientIp endereço IP de origem da requisição
     * @param entityType tipo da entidade afetada (ex: User, Task, Note)
     * @param entityId identificador da entidade afetada
     * @param details informações adicionais em texto livre ou JSON
     * @param userAgent string do User-Agent do cliente
     * @param statusCode código de status HTTP da resposta
     */
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

    /**
     * Lista os registros de auditoria mais recentes.
     * 
     * <p>Útil para dashboards e visualizações rápidas de atividade.</p>
     * 
     * @param limit número máximo de registros a retornar
     * @return lista dos registros mais recentes, ordenados por data decrescente
     */
    public List<AuditLog> listRecent(int limit) {
        return auditLogDao.findAll(DEFAULT_SORT).page(0, limit).list();
    }

    /**
     * Lista todos os registros de auditoria.
     * 
     * <p><strong>Atenção:</strong> Para grandes volumes de dados, prefira
     * usar {@link #search(AuditLogCriteria)} com paginação.</p>
     * 
     * @return lista completa de registros, ordenados por data decrescente
     */
    public List<AuditLog> listAll() {
        return auditLogDao.findAll(DEFAULT_SORT).list();
    }

    /**
     * Pesquisa registros de auditoria com filtros e paginação.
     * 
     * <p>Método principal de consulta que suporta múltiplos critérios
     * de filtro combinados. Todos os filtros são opcionais e aplicados
     * com AND lógico quando presentes.</p>
     * 
     * <h3>Filtros Disponíveis</h3>
     * <ul>
     *   <li><strong>username:</strong> Busca parcial, case-insensitive</li>
     *   <li><strong>method:</strong> Match exato do método HTTP</li>
     *   <li><strong>resource:</strong> Busca parcial no caminho do recurso</li>
     *   <li><strong>entityType:</strong> Match exato do tipo de entidade</li>
     *   <li><strong>entityId:</strong> Match exato do ID da entidade</li>
     *   <li><strong>from/to:</strong> Filtro de período por data/hora</li>
     * </ul>
     * 
     * @param criteria objeto com critérios de busca e paginação
     * @return página de resultados com metadados de paginação
     */
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

    /**
     * Registra um evento de domínio (operação de negócio).
     * 
     * <p>Método especializado para eventos originados dos Services de domínio,
     * que não possuem informações HTTP típicas. O método HTTP é registrado
     * como "DOMAIN" para distinguir de requisições HTTP reais.</p>
     * 
     * @param username email do usuário que executou a operação
     * @param action código da ação de domínio (ex: USER_CREATED, TASK_UPDATED)
     * @param resourcePath caminho lógico do recurso (ex: /users/5)
     * @param entityType tipo da entidade manipulada (ex: User, Task, Note)
     * @param entityId identificador da entidade afetada
     * @param details descrição adicional da operação
     * @param statusCode código de status HTTP equivalente (201, 200, etc.)
     */
    public void recordDomainEvent(String username,
                                  String action,
                                  String resourcePath,
                                  String entityType,
                                  String entityId,
                                  String details,
                                  Integer statusCode) {
        record(username, action, "DOMAIN", resourcePath, null, entityType, entityId, details, null, statusCode);
    }

    /**
     * Record imutável para critérios de pesquisa de auditoria.
     * 
     * <p>Encapsula todos os parâmetros possíveis de filtro e paginação
     * para consultas ao log de auditoria. Todos os campos de filtro
     * são {@link Optional} para suportar ausência de filtro.</p>
     * 
     * @param username filtro por nome de usuário (busca parcial)
     * @param method filtro por método HTTP (match exato)
     * @param resource filtro por caminho do recurso (busca parcial)
     * @param entityType filtro por tipo de entidade (match exato)
     * @param entityId filtro por ID de entidade (match exato)
     * @param from filtro de data inicial (inclusive)
     * @param to filtro de data final (inclusive)
     * @param page número da página (0-indexed)
     * @param size quantidade de registros por página
     */
    public record AuditLogCriteria(Optional<String> username,
                                   Optional<String> method,
                                   Optional<String> resource,
                                   Optional<String> entityType,
                                   Optional<String> entityId,
                                   Optional<LocalDateTime> from,
                                   Optional<LocalDateTime> to,
                                   int page,
                                   int size) {

        /**
         * Factory method para criação de critérios com valores nullable.
         * 
         * <p>Converte valores {@code null} para {@link Optional#empty()},
         * simplificando a criação de critérios a partir de parâmetros
         * de requisição HTTP.</p>
         * 
         * @param username filtro por usuário ou {@code null}
         * @param method filtro por método ou {@code null}
         * @param resource filtro por recurso ou {@code null}
         * @param entityType filtro por tipo de entidade ou {@code null}
         * @param entityId filtro por ID de entidade ou {@code null}
         * @param from data inicial ou {@code null}
         * @param to data final ou {@code null}
         * @param page número da página (0-indexed)
         * @param size tamanho da página
         * @return instância de critérios com Optional wrapping
         */
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

    /**
     * Record imutável representando uma página de resultados de auditoria.
     * 
     * <p>Encapsula os resultados da pesquisa junto com metadados de paginação
     * para navegação na interface do usuário.</p>
     * 
     * @param items lista de registros de auditoria na página atual
     * @param total número total de registros que atendem aos critérios
     * @param page número da página atual (0-indexed)
     * @param size tamanho da página (registros por página)
     */
    public record AuditLogPage(List<AuditLog> items, long total, int page, int size) {
        
        /**
         * Calcula o número total de páginas disponíveis.
         * 
         * <p>Usa arredondamento para cima para garantir que todos os
         * registros sejam acessíveis via paginação.</p>
         * 
         * @return número total de páginas (mínimo 1)
         */
        public long totalPages() {
            if (size <= 0) {
                return 1;
            }
            return (total + size - 1) / size;
        }
    }
}
