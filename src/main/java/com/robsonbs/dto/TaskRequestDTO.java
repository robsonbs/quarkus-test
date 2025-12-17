package com.robsonbs.dto;

import org.jboss.resteasy.reactive.RestForm;

/**
 * Data Transfer Object (DTO) para receber dados de criação ou atualização de tarefa.
 * 
 * <p>Este DTO é utilizado para capturar dados enviados via formulário HTML
 * nas operações de criação e edição de tarefas.</p>
 * 
 * <h2>Campos do Formulário</h2>
 * <table border="1">
 *   <tr><th>Campo</th><th>Tipo</th><th>Obrigatório</th><th>Descrição</th></tr>
 *   <tr><td>title</td><td>String</td><td>Sim</td><td>Título da tarefa</td></tr>
 *   <tr><td>description</td><td>String</td><td>Não</td><td>Descrição detalhada</td></tr>
 *   <tr><td>dueDate</td><td>String</td><td>Não</td><td>Data limite (formato: yyyy-MM-dd)</td></tr>
 *   <tr><td>status</td><td>String</td><td>Não</td><td>Status: PENDING, IN_PROGRESS, COMPLETED</td></tr>
 * </table>
 * 
 * <h2>Conversão de Tipos</h2>
 * <p>Os campos {@code dueDate} e {@code status} são recebidos como String
 * e convertidos no {@link com.robsonbs.service.TaskService}:</p>
 * <ul>
 *   <li>{@code dueDate}: Convertido para {@code LocalDate} usando ISO format</li>
 *   <li>{@code status}: Convertido para {@link com.robsonbs.model.TaskStatus} enum</li>
 * </ul>
 * 
 * <h2>Validações</h2>
 * <p>Validações de negócio são realizadas no {@link com.robsonbs.service.TaskService}:</p>
 * <ul>
 *   <li>Título é obrigatório</li>
 *   <li>Data limite não pode ser no passado</li>
 *   <li>Status deve ser um valor válido do enum</li>
 * </ul>
 * 
 * <h2>Ownership</h2>
 * <p>O proprietário da tarefa é determinado automaticamente pelo Service
 * com base no usuário autenticado ({@code SecurityIdentity}).</p>
 * 
 * <h2>Exemplo de Uso</h2>
 * <pre>{@code
 * // No Controller
 * @POST
 * public Response create(@BeanParam TaskRequestDTO dto) {
 *     String ownerEmail = identity.getPrincipal().getName();
 *     TaskResponseDTO created = taskService.create(dto, ownerEmail);
 *     return Response.seeOther(URI.create("/tasks?success=Tarefa criada")).build();
 * }
 * }</pre>
 * 
 * @author Sistema de Gestão de Notas e Tarefas
 * @version 1.0
 * @since 2025-12-01
 * @see TaskResponseDTO
 * @see com.robsonbs.model.Task
 * @see com.robsonbs.model.TaskStatus
 * @see com.robsonbs.service.TaskService
 */
public class TaskRequestDTO {

    /**
     * Título da tarefa.
     * <p>Campo obrigatório, deve ser descritivo do objetivo da tarefa.</p>
     */
    @RestForm
    private String title;
    
    /**
     * Descrição detalhada da tarefa.
     * <p>Campo opcional para detalhar requisitos, critérios de aceite, etc.</p>
     */
    @RestForm
    private String description;
    
    /**
     * Data limite para conclusão da tarefa.
     * <p>Formato esperado: yyyy-MM-dd (ISO Local Date).
     * Será validado para não permitir datas passadas.</p>
     */
    @RestForm
    private String dueDate;
    
    /**
     * Status atual da tarefa no workflow.
     * <p>Valores válidos: PENDING, IN_PROGRESS, COMPLETED.
     * Se não informado na criação, assume PENDING.</p>
     * 
     * @see com.robsonbs.model.TaskStatus
     */
    @RestForm
    private String status;

    /**
     * Obtém o título da tarefa.
     * @return o título
     */
    public String getTitle() {
        return title;
    }

    /**
     * Define o título da tarefa.
     * @param title o título (obrigatório)
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Obtém a descrição da tarefa.
     * @return a descrição ou {@code null}
     */
    public String getDescription() {
        return description;
    }

    /**
     * Define a descrição da tarefa.
     * @param description a descrição detalhada
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Obtém a data limite como String.
     * @return a data no formato yyyy-MM-dd ou {@code null}
     */
    public String getDueDate() {
        return dueDate;
    }

    /**
     * Define a data limite.
     * @param dueDate a data no formato yyyy-MM-dd
     */
    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    /**
     * Obtém o status da tarefa como String.
     * @return o status (PENDING, IN_PROGRESS, COMPLETED) ou {@code null}
     */
    public String getStatus() {
        return status;
    }

    /**
     * Define o status da tarefa.
     * @param status o status como String
     */
    public void setStatus(String status) {
        this.status = status;
    }
}
