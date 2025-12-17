package com.robsonbs.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * Entidade JPA que representa um registro de auditoria no sistema.
 * 
 * <p>Cada instância desta classe armazena informações detalhadas sobre uma ação
 * executada por um usuário, permitindo rastreabilidade completa das operações
 * realizadas no sistema. Este é um componente essencial para conformidade com
 * requisitos de segurança e auditoria.</p>
 * 
 * <h2>Tipos de Informação Registrada</h2>
 * <ul>
 *   <li><strong>Identificação do Ator:</strong> Username do usuário que executou a ação</li>
 *   <li><strong>Ação Realizada:</strong> Tipo de operação (ex: USER_CREATED, TASK_DELETED)</li>
 *   <li><strong>Contexto HTTP:</strong> Método, path, IP do cliente, status code</li>
 *   <li><strong>Entidade Afetada:</strong> Tipo e ID da entidade manipulada</li>
 *   <li><strong>Metadados:</strong> User-Agent, detalhes adicionais em JSON</li>
 *   <li><strong>Timestamp:</strong> Momento exato da ocorrência</li>
 * </ul>
 * 
 * <h2>Eventos Auditados</h2>
 * <table border="1">
 *   <tr><th>Módulo</th><th>Eventos</th></tr>
 *   <tr><td>Usuários</td><td>USER_CREATED, USER_UPDATED, USER_DELETED</td></tr>
 *   <tr><td>Perfis</td><td>PROFILE_CREATED, PROFILE_UPDATED, PROFILE_DELETED</td></tr>
 *   <tr><td>Notas</td><td>NOTE_CREATED, NOTE_UPDATED, NOTE_DELETED</td></tr>
 *   <tr><td>Tarefas</td><td>TASK_CREATED, TASK_UPDATED, TASK_DELETED</td></tr>
 *   <tr><td>Autenticação</td><td>USER_LOGIN, USER_LOGOUT</td></tr>
 * </table>
 * 
 * <h2>Mapeamento de Banco de Dados</h2>
 * <pre>
 * CREATE TABLE audit_logs (
 *     id BIGSERIAL PRIMARY KEY,
 *     username VARCHAR(255) NOT NULL,
 *     action VARCHAR(255) NOT NULL,
 *     http_method VARCHAR(10) NOT NULL,
 *     resource_path VARCHAR(500) NOT NULL,
 *     client_ip VARCHAR(50),
 *     status_code INTEGER,
 *     entity_type VARCHAR(100),
 *     entity_id VARCHAR(100),
 *     user_agent TEXT,
 *     details TEXT,
 *     occurred_at TIMESTAMP NOT NULL
 * );
 * </pre>
 * 
 * <h2>Exemplo de Uso</h2>
 * <pre>{@code
 * // Registro via AuditLogService (forma recomendada)
 * auditLogService.recordDomainEvent(
 *     "TASK_CREATED",
 *     "Task",
 *     task.getId().toString(),
 *     "Tarefa criada: " + task.getTitle()
 * );
 * }</pre>
 * 
 * @author Sistema de Gestão de Notas e Tarefas
 * @version 1.0
 * @since 2025-12-01
 * @see com.robsonbs.service.AuditLogService
 * @see com.robsonbs.dao.AuditLogDao
 * @see com.robsonbs.filter.AuditFilter
 */
@Entity
@Table(name = "audit_logs")
public class AuditLog {

    /**
     * Identificador único do registro de auditoria.
     * <p>Gerado automaticamente pelo banco de dados usando estratégia IDENTITY.</p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nome de usuário (e-mail) que executou a ação.
     * <p>Obtido do {@link io.quarkus.security.identity.SecurityIdentity} no momento
     * do registro. Para usuários não autenticados, pode conter "anonymous".</p>
     */
    @Column(nullable = false)
    private String username;

    /**
     * Identificador da ação realizada.
     * <p>Segue o padrão ENTIDADE_VERBO (ex: USER_CREATED, TASK_DELETED).
     * Utilizado para filtrar e categorizar eventos no log de auditoria.</p>
     * 
     * @see com.robsonbs.controller.AuditController#list(String, String, String, Integer, Integer)
     */
    @Column(nullable = false)
    private String action;

    /**
     * Método HTTP da requisição que gerou o evento.
     * <p>Valores típicos: GET, POST, PUT, DELETE.
     * Útil para identificar o tipo de operação realizada.</p>
     */
    @Column(name = "http_method", nullable = false)
    private String httpMethod;

    /**
     * Caminho do recurso acessado na requisição.
     * <p>Exemplo: "/users/123", "/tasks/new".
     * Permite rastrear qual endpoint foi acessado.</p>
     */
    @Column(name = "resource_path", nullable = false)
    private String resourcePath;

    /**
     * Endereço IP do cliente que realizou a requisição.
     * <p>Útil para identificar a origem de ações suspeitas ou
     * para fins de segurança e compliance.</p>
     */
    @Column(name = "client_ip")
    private String clientIp;

    /**
     * Código de status HTTP da resposta.
     * <p>Permite identificar se a operação foi bem-sucedida (2xx),
     * resultou em erro de cliente (4xx) ou erro de servidor (5xx).</p>
     */
    @Column(name = "status_code")
    private Integer statusCode;

    /**
     * Tipo da entidade afetada pela ação.
     * <p>Valores típicos: "User", "UserProfile", "Note", "Task".
     * Utilizado em conjunto com {@link #entityId} para identificar
     * o registro específico manipulado.</p>
     */
    @Column(name = "entity_type")
    private String entityType;

    /**
     * Identificador da entidade afetada pela ação.
     * <p>Geralmente o ID primário da entidade como String.
     * Pode ser usado para rastrear todas as ações sobre um registro específico.</p>
     */
    @Column(name = "entity_id")
    private String entityId;

    /**
     * User-Agent do navegador/cliente que realizou a requisição.
     * <p>Útil para identificar o tipo de cliente (browser, API client, etc.)
     * e para fins de análise de segurança.</p>
     */
    @Column(name = "user_agent")
    private String userAgent;

    /**
     * Detalhes adicionais da operação em formato livre.
     * <p>Pode conter JSON com dados específicos da ação, como valores
     * antigos e novos em uma atualização, ou descrição da entidade criada.</p>
     * <p>Mapeado como TEXT para suportar descrições extensas.</p>
     */
    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    /**
     * Data e hora exata em que a ação ocorreu.
     * <p>Preenchido automaticamente pelo callback {@link #onPersist()}
     * no momento da persistência do registro.</p>
     */
    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    /**
     * Callback JPA executado antes da persistência.
     * <p>Define automaticamente o timestamp de ocorrência com o momento atual.</p>
     */
    @PrePersist
    void onPersist() {
        occurredAt = LocalDateTime.now();
    }

    /**
     * Obtém o identificador único do registro.
     * @return o ID do registro de auditoria
     */
    public Long getId() {
        return id;
    }

    /**
     * Define o identificador do registro.
     * @param id o novo ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Obtém o nome de usuário que executou a ação.
     * @return o username (e-mail) do ator
     */
    public String getUsername() {
        return username;
    }

    /**
     * Define o nome de usuário que executou a ação.
     * @param username o e-mail do usuário
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Obtém o identificador da ação realizada.
     * @return o tipo de ação (ex: USER_CREATED)
     */
    public String getAction() {
        return action;
    }

    /**
     * Define o identificador da ação.
     * @param action o tipo de ação
     */
    public void setAction(String action) {
        this.action = action;
    }

    /**
     * Obtém o método HTTP da requisição.
     * @return o método HTTP (GET, POST, PUT, DELETE)
     */
    public String getHttpMethod() {
        return httpMethod;
    }

    /**
     * Define o método HTTP da requisição.
     * @param httpMethod o método HTTP
     */
    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    /**
     * Obtém o caminho do recurso acessado.
     * @return o path da requisição
     */
    public String getResourcePath() {
        return resourcePath;
    }

    /**
     * Define o caminho do recurso acessado.
     * @param resourcePath o path da requisição
     */
    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    /**
     * Obtém o endereço IP do cliente.
     * @return o IP do cliente ou {@code null}
     */
    public String getClientIp() {
        return clientIp;
    }

    /**
     * Define o endereço IP do cliente.
     * @param clientIp o IP do cliente
     */
    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    /**
     * Obtém o código de status HTTP da resposta.
     * @return o status code ou {@code null}
     */
    public Integer getStatusCode() {
        return statusCode;
    }

    /**
     * Define o código de status HTTP.
     * @param statusCode o status code
     */
    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    /**
     * Obtém o tipo da entidade afetada.
     * @return o nome da classe da entidade
     */
    public String getEntityType() {
        return entityType;
    }

    /**
     * Define o tipo da entidade afetada.
     * @param entityType o nome da classe
     */
    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    /**
     * Obtém o ID da entidade afetada.
     * @return o identificador da entidade como String
     */
    public String getEntityId() {
        return entityId;
    }

    /**
     * Define o ID da entidade afetada.
     * @param entityId o identificador da entidade
     */
    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    /**
     * Obtém o User-Agent do cliente.
     * @return o header User-Agent
     */
    public String getUserAgent() {
        return userAgent;
    }

    /**
     * Define o User-Agent do cliente.
     * @param userAgent o header User-Agent
     */
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    /**
     * Obtém os detalhes adicionais da operação.
     * @return os detalhes em formato livre
     */
    public String getDetails() {
        return details;
    }

    /**
     * Define os detalhes adicionais da operação.
     * @param details os detalhes em formato livre (pode ser JSON)
     */
    public void setDetails(String details) {
        this.details = details;
    }

    /**
     * Obtém a data e hora de ocorrência da ação.
     * @return o timestamp de quando a ação ocorreu
     */
    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(LocalDateTime occurredAt) {
        this.occurredAt = occurredAt;
    }
}
