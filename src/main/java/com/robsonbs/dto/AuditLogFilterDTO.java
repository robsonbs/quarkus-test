package com.robsonbs.dto;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

/**
 * Data Transfer Object (DTO) para filtros de consulta de auditoria.
 * 
 * <p>Este DTO encapsula os parâmetros de filtro informados pelo usuário
 * para consultas paginadas no log de auditoria. Permite filtragem por
 * múltiplos critérios combinados.</p>
 * 
 * <h2>Critérios de Filtro</h2>
 * <table border="1">
 *   <tr><th>Campo</th><th>Tipo</th><th>Descrição</th></tr>
 *   <tr><td>username</td><td>String</td><td>E-mail do usuário que executou a ação</td></tr>
 *   <tr><td>method</td><td>String</td><td>Método HTTP (GET, POST, PUT, DELETE)</td></tr>
 *   <tr><td>resource</td><td>String</td><td>Caminho do recurso acessado</td></tr>
 *   <tr><td>entityType</td><td>String</td><td>Tipo da entidade (User, Task, Note)</td></tr>
 *   <tr><td>entityId</td><td>String</td><td>ID da entidade afetada</td></tr>
 *   <tr><td>from</td><td>String</td><td>Data inicial (yyyy-MM-dd)</td></tr>
 *   <tr><td>to</td><td>String</td><td>Data final (yyyy-MM-dd)</td></tr>
 *   <tr><td>page</td><td>int</td><td>Número da página (0-based)</td></tr>
 *   <tr><td>size</td><td>int</td><td>Tamanho da página (max: 100)</td></tr>
 * </table>
 * 
 * <h2>Paginação</h2>
 * <p>Os parâmetros de paginação possuem validação automática:</p>
 * <ul>
 *   <li>{@code page}: Mínimo 0 (negativo vira 0)</li>
 *   <li>{@code size}: Padrão 25, máximo 100</li>
 * </ul>
 * 
 * <h2>Conversão de Datas</h2>
 * <p>Os campos {@code from} e {@code to} são recebidos como String
 * e convertidos via métodos {@link #fromDate()} e {@link #toDate()}
 * que retornam {@link Optional} para tratamento seguro.</p>
 * 
 * <h2>Exemplo de Uso</h2>
 * <pre>{@code
 * // No Controller
 * @GET
 * public TemplateInstance list(
 *     @QueryParam("username") String username,
 *     @QueryParam("from") String from,
 *     @QueryParam("page") @DefaultValue("0") int page
 * ) {
 *     AuditLogFilterDTO filter = AuditLogFilterDTO.defaults();
 *     filter.setUsername(username);
 *     filter.setFrom(from);
 *     filter.setPage(page);
 *     
 *     List<AuditLogResponseDTO> logs = auditService.search(filter);
 *     return audit.data("logs", logs);
 * }
 * }</pre>
 * 
 * @author Sistema de Gestão de Notas e Tarefas
 * @version 1.0
 * @since 2025-12-01
 * @see AuditLogResponseDTO
 * @see com.robsonbs.model.AuditLog
 * @see com.robsonbs.service.AuditLogService
 * @see com.robsonbs.controller.AuditController
 */
public class AuditLogFilterDTO {

    /** Formatador para parsing de datas ISO (yyyy-MM-dd). */
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    /** Filtro por username (e-mail do ator). */
    private String username;
    
    /** Filtro por método HTTP. */
    private String method;
    
    /** Filtro por caminho do recurso. */
    private String resource;
    
    /** Filtro por tipo de entidade. */
    private String entityType;
    
    /** Filtro por ID da entidade. */
    private String entityId;
    
    /** Data inicial do período (formato: yyyy-MM-dd). */
    private String from;
    
    /** Data final do período (formato: yyyy-MM-dd). */
    private String to;
    
    /** Número da página (0-based). */
    private int page;
    
    /** Quantidade de registros por página. */
    private int size;

    /**
     * Cria uma instância com valores padrão.
     * <p>Valores padrão: page=0, size=25.</p>
     * 
     * @return nova instância com valores padrão
     */
    public static AuditLogFilterDTO defaults() {
        AuditLogFilterDTO dto = new AuditLogFilterDTO();
        dto.size = 25;
        dto.page = 0;
        return dto;
    }

    /**
     * Converte a data inicial de String para LocalDate.
     * 
     * @return {@link Optional} com a data se válida, ou empty se nula/inválida
     */
    public Optional<LocalDate> fromDate() {
        return parseDate(from);
    }

    /**
     * Converte a data final de String para LocalDate.
     * 
     * @return {@link Optional} com a data se válida, ou empty se nula/inválida
     */
    public Optional<LocalDate> toDate() {
        return parseDate(to);
    }

    /**
     * Método auxiliar para parsing de datas.
     * 
     * @param value a string de data no formato yyyy-MM-dd
     * @return {@link Optional} com a data convertida ou empty se inválida
     */
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

    /**
     * Obtém o filtro de username.
     * @return o e-mail do usuário ou {@code null}
     */
    public String getUsername() {
        return username;
    }

    /**
     * Define o filtro de username.
     * @param username o e-mail do usuário
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Obtém o filtro de método HTTP.
     * @return o método HTTP ou {@code null}
     */
    public String getMethod() {
        return method;
    }

    /**
     * Define o filtro de método HTTP.
     * @param method o método HTTP (GET, POST, PUT, DELETE)
     */
    public void setMethod(String method) {
        this.method = method;
    }

    /**
     * Obtém o filtro de caminho do recurso.
     * @return o path ou {@code null}
     */
    public String getResource() {
        return resource;
    }

    /**
     * Define o filtro de caminho do recurso.
     * @param resource o path do recurso
     */
    public void setResource(String resource) {
        this.resource = resource;
    }

    /**
     * Obtém o filtro de tipo de entidade.
     * @return o tipo da entidade ou {@code null}
     */
    public String getEntityType() {
        return entityType;
    }

    /**
     * Define o filtro de tipo de entidade.
     * @param entityType o tipo (User, Task, Note, etc.)
     */
    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    /**
     * Obtém o filtro de ID da entidade.
     * @return o ID ou {@code null}
     */
    public String getEntityId() {
        return entityId;
    }

    /**
     * Define o filtro de ID da entidade.
     * @param entityId o ID da entidade
     */
    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    /**
     * Obtém a data inicial como String.
     * @return a data no formato yyyy-MM-dd ou {@code null}
     */
    public String getFrom() {
        return from;
    }

    /**
     * Define a data inicial.
     * @param from a data no formato yyyy-MM-dd
     */
    public void setFrom(String from) {
        this.from = from;
    }

    /**
     * Obtém a data final como String.
     * @return a data no formato yyyy-MM-dd ou {@code null}
     */
    public String getTo() {
        return to;
    }

    /**
     * Define a data final.
     * @param to a data no formato yyyy-MM-dd
     */
    public void setTo(String to) {
        this.to = to;
    }

    /**
     * Obtém o número da página atual.
     * @return o número da página (0-based)
     */
    public int getPage() {
        return page;
    }

    /**
     * Define o número da página.
     * <p>Valores negativos são convertidos para 0.</p>
     * @param page o número da página
     */
    public void setPage(int page) {
        this.page = Math.max(page, 0);
    }

    /**
     * Obtém o tamanho da página.
     * @return quantidade de registros por página
     */
    public int getSize() {
        return size;
    }

    /**
     * Define o tamanho da página.
     * <p>Se menor ou igual a 0, assume 25. Máximo permitido: 100.</p>
     * @param size quantidade de registros por página
     */
    public void setSize(int size) {
        this.size = size <= 0 ? 25 : Math.min(size, 100);
    }
}
