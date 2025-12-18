package com.robsonbs.dto;

import com.robsonbs.model.Task;
import com.robsonbs.model.TaskStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) para retornar dados de tarefa para a camada de apresentação.
 * 
 * <p>Este DTO é utilizado para transferir dados de tarefas para templates Qute
 * e respostas de API. Inclui todos os campos relevantes para exibição e
 * gerenciamento de tarefas.</p>
 * 
 * <h2>Campos Expostos</h2>
 * <table border="1">
 *   <tr><th>Campo</th><th>Tipo</th><th>Descrição</th></tr>
 *   <tr><td>id</td><td>Long</td><td>Identificador único</td></tr>
 *   <tr><td>title</td><td>String</td><td>Título da tarefa</td></tr>
 *   <tr><td>description</td><td>String</td><td>Descrição detalhada</td></tr>
 *   <tr><td>dueDate</td><td>LocalDate</td><td>Data limite</td></tr>
 *   <tr><td>status</td><td>TaskStatus</td><td>Status atual (enum)</td></tr>
 *   <tr><td>createdAt</td><td>LocalDateTime</td><td>Data de criação</td></tr>
 *   <tr><td>updatedAt</td><td>LocalDateTime</td><td>Data de última atualização</td></tr>
 * </table>
 * 
 * <h2>Workflow de Status</h2>
 * <p>O campo {@code status} representa a posição no workflow:</p>
 * <pre>
 * PENDING → IN_PROGRESS → COMPLETED
 * </pre>
 * 
 * <h2>Privacidade</h2>
 * <p>Este DTO não inclui o campo {@code owner}, pois a listagem já é
 * filtrada por proprietário. Isso evita exposição desnecessária.</p>
 * 
 * <h2>Exemplo de Uso</h2>
 * <pre>{@code
 * // No Service
 * public List<TaskResponseDTO> findByOwner(String ownerEmail) {
 *     User owner = userDao.findByEmail(ownerEmail).orElseThrow();
 *     return taskDao.findByOwnerId(owner.getId()).stream()
 *         .map(TaskResponseDTO::new)
 *         .toList();
 * }
 * 
 * // No Template Qute
 * {#for task in tasks}
 *     <tr>
 *         <td>{task.title}</td>
 *         <td>{task.status}</td>
 *         <td>{task.dueDate}</td>
 *     </tr>
 * {/for}
 * }</pre>
 * 
 * @author Sistema de Gestão de Notas e Tarefas
 * @version 1.0
 * @since 2025-12-01
 * @see Task
 * @see TaskStatus
 * @see TaskRequestDTO
 * @see com.robsonbs.service.TaskService
 */
public class TaskResponseDTO {

    /** Identificador único da tarefa. */
    private Long id;
    
    /** Título da tarefa. */
    private String title;
    
    /** Descrição detalhada da tarefa. */
    private String description;
    
    /** Data limite para conclusão. */
    private LocalDate dueDate;
    
    /** Status atual no workflow. */
    private TaskStatus status;
    
    /** Data e hora de criação. */
    private LocalDateTime createdAt;
    
    /** Data e hora da última atualização. */
    private LocalDateTime updatedAt;

    /**
     * Construtor que converte uma entidade {@link Task} para DTO.
     * 
     * @param task a entidade Task a ser convertida
     */
    public TaskResponseDTO(Task task) {
        this.id = task.getId();
        this.title = task.getTitle();
        this.description = task.getDescription();
        this.dueDate = task.getDueDate();
        this.status = task.getStatus();
        this.createdAt = task.getCreatedAt();
        this.updatedAt = task.getUpdatedAt();
    }

    /**
     * Obtém o ID da tarefa.
     * @return o identificador único
     */
    public Long getId() {
        return id;
    }

    /**
     * Obtém o título da tarefa.
     * @return o título
     */
    public String getTitle() {
        return title;
    }

    /**
     * Obtém a descrição da tarefa.
     * @return a descrição ou {@code null}
     */
    public String getDescription() {
        return description;
    }

    /**
     * Obtém a data limite.
     * @return a data limite ou {@code null}
     */
    public LocalDate getDueDate() {
        return dueDate;
    }

    /**
     * Obtém o status atual da tarefa.
     * @return o status no workflow
     */
    public TaskStatus getStatus() {
        return status;
    }

    /**
     * Obtém a data de criação.
     * @return o timestamp de criação
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Obtém a data de última atualização.
     * @return o timestamp de atualização
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
