package com.robsonbs.dto;

import com.robsonbs.model.AuditLog;
import java.time.LocalDateTime;

/**
 * Data Transfer Object para apresentação de registros de auditoria.
 * 
 * <p>Esta classe encapsula todos os dados de um registro de auditoria para
 * exibição na interface de consulta de logs. Serve como camada de transferência
 * entre a entidade {@link AuditLog} e a view, garantindo que apenas os dados
 * necessários sejam expostos e formatados adequadamente.</p>
 * 
 * <h2>Dados Capturados</h2>
 * <p>O DTO transporta informações completas sobre cada evento auditado:</p>
 * <ul>
 *   <li><strong>Identificação:</strong> ID único do registro</li>
 *   <li><strong>Autoria:</strong> Usuário que realizou a ação</li>
 *   <li><strong>Ação:</strong> Tipo de operação (LOGIN, CREATE, UPDATE, DELETE, etc.)</li>
 *   <li><strong>Requisição HTTP:</strong> Método, caminho do recurso e código de status</li>
 *   <li><strong>Contexto:</strong> IP do cliente, User-Agent do navegador</li>
 *   <li><strong>Entidade:</strong> Tipo e identificador da entidade afetada</li>
 *   <li><strong>Detalhes:</strong> Informações adicionais em formato JSON/texto</li>
 *   <li><strong>Timestamp:</strong> Data e hora exata do evento</li>
 * </ul>
 * 
 * <h2>Segurança e Compliance</h2>
 * <p>Este DTO é fundamental para atender requisitos de:</p>
 * <ul>
 *   <li>LGPD - Rastreabilidade de acesso a dados pessoais</li>
 *   <li>Auditoria interna - Histórico de alterações no sistema</li>
 *   <li>Segurança - Detecção de acessos suspeitos ou não autorizados</li>
 *   <li>Compliance - Evidências para auditorias externas</li>
 * </ul>
 * 
 * <h2>Exemplo de Uso</h2>
 * <pre>{@code
 * // No Controller de auditoria
 * List<AuditLogResponseDTO> logs = auditLogs.stream()
 *     .map(AuditLogResponseDTO::new)
 *     .collect(Collectors.toList());
 * 
 * // Exibição na template Qute
 * // {#for log in logs}
 * //   {log.username} - {log.action} em {log.occurredAt}
 * // {/for}
 * }</pre>
 * 
 * <h2>Padrão Imutável</h2>
 * <p>Este DTO é efetivamente imutável após construção (sem setters),
 * garantindo integridade dos dados de auditoria durante o transporte.</p>
 * 
 * @author Sistema de Auditoria
 * @version 1.0
 * @since 1.0
 * @see AuditLog
 * @see com.robsonbs.dto.AuditLogFilterDTO
 * @see com.robsonbs.controller.AuditController
 */
public class AuditLogResponseDTO {
    
    /**
     * Identificador único do registro de auditoria.
     * 
     * <p>Gerado automaticamente pelo banco de dados. Pode ser usado
     * para referência específica a um evento de auditoria.</p>
     */
    private Long id;
    
    /**
     * Nome de usuário (email) que executou a ação.
     * 
     * <p>Corresponde ao e-mail do usuário autenticado no momento
     * da ação. Para requisições não autenticadas, pode conter
     * valores como "anonymous" ou "system".</p>
     */
    private String username;
    
    /**
     * Tipo de ação executada no sistema.
     * 
     * <p>Valores típicos incluem:</p>
     * <ul>
     *   <li>{@code LOGIN_SUCCESS} - Autenticação bem-sucedida</li>
     *   <li>{@code LOGIN_FAILURE} - Tentativa de login falhou</li>
     *   <li>{@code LOGOUT} - Encerramento de sessão</li>
     *   <li>{@code USER_CREATED} - Criação de usuário</li>
     *   <li>{@code USER_UPDATED} - Atualização de usuário</li>
     *   <li>{@code USER_DELETED} - Remoção de usuário</li>
     *   <li>{@code TASK_CREATED}, {@code TASK_UPDATED}, {@code TASK_DELETED}</li>
     *   <li>{@code NOTE_CREATED}, {@code NOTE_UPDATED}, {@code NOTE_DELETED}</li>
     * </ul>
     */
    private String action;
    
    /**
     * Método HTTP da requisição que gerou o evento.
     * 
     * <p>Valores possíveis: GET, POST, PUT, DELETE, PATCH.
     * Útil para distinguir operações de leitura de escrita.</p>
     */
    private String httpMethod;
    
    /**
     * Caminho do recurso acessado na requisição.
     * 
     * <p>Exemplo: {@code /users/5}, {@code /tasks}, {@code /login}.
     * Permite rastrear qual endpoint foi acessado.</p>
     */
    private String resourcePath;
    
    /**
     * Endereço IP do cliente que fez a requisição.
     * 
     * <p>Pode ser usado para identificar a origem geográfica do acesso
     * ou detectar padrões de acesso suspeitos. Em ambientes com proxy
     * reverso, pode refletir o IP do X-Forwarded-For.</p>
     */
    private String clientIp;
    
    /**
     * Código de status HTTP retornado pela requisição.
     * 
     * <p>Valores típicos:</p>
     * <ul>
     *   <li>{@code 200} - Sucesso (OK)</li>
     *   <li>{@code 201} - Recurso criado</li>
     *   <li>{@code 302} - Redirecionamento (após form submit)</li>
     *   <li>{@code 400} - Requisição inválida</li>
     *   <li>{@code 401} - Não autenticado</li>
     *   <li>{@code 403} - Acesso negado</li>
     *   <li>{@code 404} - Recurso não encontrado</li>
     *   <li>{@code 500} - Erro interno do servidor</li>
     * </ul>
     */
    private Integer statusCode;
    
    /**
     * Tipo da entidade afetada pela operação.
     * 
     * <p>Identifica qual modelo de domínio foi manipulado.
     * Valores típicos: {@code User}, {@code Task}, {@code Note}, {@code UserProfile}.</p>
     */
    private String entityType;
    
    /**
     * Identificador da entidade específica afetada.
     * 
     * <p>Geralmente corresponde ao ID primário da entidade (ex: "5" para User ID 5).
     * Pode também conter identificadores compostos ou valores de negócio.</p>
     */
    private String entityId;
    
    /**
     * User-Agent do navegador/cliente que fez a requisição.
     * 
     * <p>Informação extraída do cabeçalho HTTP User-Agent. Útil para
     * identificar o tipo de dispositivo e navegador utilizado.</p>
     * 
     * <p>Exemplo: {@code Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0.0.0}</p>
     */
    private String userAgent;
    
    /**
     * Detalhes adicionais sobre a operação.
     * 
     * <p>Campo de texto livre que pode conter informações contextuais
     * como dados alterados, parâmetros da requisição ou mensagens
     * de erro. Frequentemente em formato JSON para facilitar parsing.</p>
     */
    private String details;
    
    /**
     * Data e hora em que o evento ocorreu.
     * 
     * <p>Timestamp preciso do momento da requisição, usado para
     * ordenação cronológica e filtros por período. Armazenado
     * sem fuso horário (LocalDateTime), assumindo timezone do servidor.</p>
     */
    private LocalDateTime occurredAt;

    /**
     * Constrói um DTO de resposta a partir de uma entidade de auditoria.
     * 
     * <p>Realiza cópia de todos os campos da entidade {@link AuditLog}
     * para o DTO, garantindo desacoplamento entre camada de persistência
     * e apresentação.</p>
     * 
     * @param log entidade de auditoria fonte dos dados; não pode ser {@code null}
     * @throws NullPointerException se {@code log} for {@code null}
     */
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

    /**
     * Retorna o identificador único do registro de auditoria.
     * 
     * @return ID do registro, gerado pelo banco de dados
     */
    public Long getId() {
        return id;
    }

    /**
     * Retorna o nome de usuário que executou a ação.
     * 
     * @return e-mail do usuário autenticado ou identificador de sistema
     */
    public String getUsername() {
        return username;
    }

    /**
     * Retorna o tipo de ação executada.
     * 
     * @return código da ação (ex: LOGIN_SUCCESS, USER_CREATED)
     */
    public String getAction() {
        return action;
    }

    /**
     * Retorna o método HTTP da requisição.
     * 
     * @return método HTTP (GET, POST, PUT, DELETE, etc.)
     */
    public String getHttpMethod() {
        return httpMethod;
    }

    /**
     * Retorna o caminho do recurso acessado.
     * 
     * @return URI do endpoint acessado
     */
    public String getResourcePath() {
        return resourcePath;
    }

    /**
     * Retorna o endereço IP do cliente.
     * 
     * @return endereço IP de origem da requisição
     */
    public String getClientIp() {
        return clientIp;
    }

    /**
     * Retorna a data e hora do evento.
     * 
     * @return timestamp do momento em que a ação ocorreu
     */
    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    /**
     * Retorna o código de status HTTP da resposta.
     * 
     * @return código de status HTTP (200, 201, 400, 403, 500, etc.)
     */
    public Integer getStatusCode() {
        return statusCode;
    }

    /**
     * Retorna o tipo da entidade afetada.
     * 
     * @return nome da classe/modelo afetado pela operação
     */
    public String getEntityType() {
        return entityType;
    }

    /**
     * Retorna o identificador da entidade afetada.
     * 
     * @return ID ou identificador único da entidade manipulada
     */
    public String getEntityId() {
        return entityId;
    }

    /**
     * Retorna o User-Agent do cliente.
     * 
     * @return string do User-Agent do navegador/aplicação cliente
     */
    public String getUserAgent() {
        return userAgent;
    }

    /**
     * Retorna os detalhes adicionais do evento.
     * 
     * @return informações contextuais em texto livre ou JSON
     */
    public String getDetails() {
        return details;
    }
}
