package com.robsonbs.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidade JPA que representa uma tarefa no sistema de gestão.
 * 
 * <p>Uma tarefa é uma unidade de trabalho com título, descrição, prazo e status,
 * vinculada a um usuário proprietário ({@link User}). O sistema implementa um
 * workflow de status que permite acompanhar o progresso das tarefas.</p>
 * 
 * <h2>Características Principais</h2>
 * <ul>
 *   <li><strong>Ownership:</strong> Cada tarefa pertence a um único usuário</li>
 *   <li><strong>Workflow:</strong> Status evolui de PENDING → IN_PROGRESS → COMPLETED</li>
 *   <li><strong>Prazo:</strong> Data limite opcional com validação (não permite datas passadas)</li>
 *   <li><strong>Auditoria:</strong> Timestamps de criação e atualização automáticos</li>
 * </ul>
 * 
 * <h2>Mapeamento de Banco de Dados</h2>
 * <pre>
 * CREATE TABLE tasks (
 *     id BIGSERIAL PRIMARY KEY,
 *     title VARCHAR(255) NOT NULL,
 *     description TEXT,
 *     due_date DATE,
 *     status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
 *     owner_id BIGINT NOT NULL REFERENCES users(id),
 *     created_at TIMESTAMP NOT NULL,
 *     updated_at TIMESTAMP
 * );
 * </pre>
 * 
 * <h2>Exemplo de Uso</h2>
 * <pre>{@code
 * Task task = new Task();
 * task.setTitle("Implementar funcionalidade X");
 * task.setDescription("Desenvolver e testar a feature");
 * task.setDueDate(LocalDate.now().plusDays(7));
 * task.setStatus(TaskStatus.PENDING);
 * task.setOwner(currentUser);
 * taskDao.persist(task);
 * }</pre>
 * 
 * @author Sistema de Gestão de Notas e Tarefas
 * @version 1.0
 * @since 2025-12-01
 * @see TaskStatus
 * @see User
 * @see com.robsonbs.dao.TaskDao
 * @see com.robsonbs.service.TaskService
 */
@Entity
@Table(name = "tasks")
public class Task {

    /**
     * Identificador único da tarefa, gerado automaticamente pelo banco de dados.
     * <p>Utiliza estratégia IDENTITY para geração automática de sequência.</p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Título da tarefa, campo obrigatório.
     * <p>Deve ser conciso e descritivo, representando o objetivo principal da tarefa.</p>
     * 
     * @see com.robsonbs.service.TaskService#validateTitle(String)
     */
    @Column(nullable = false)
    private String title;

    /**
     * Descrição detalhada da tarefa.
     * <p>Campo opcional que permite detalhar os requisitos, critérios de aceite
     * ou qualquer informação adicional relevante para a execução da tarefa.</p>
     * <p>Mapeado como TEXT no banco para suportar descrições longas.</p>
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Data limite para conclusão da tarefa.
     * <p>Campo opcional. Quando informado, não pode ser uma data no passado
     * (validação realizada no {@link com.robsonbs.service.TaskService}).</p>
     * 
     * @see com.robsonbs.service.TaskService#validateDueDate(LocalDate)
     */
    @Column(name = "due_date")
    private LocalDate dueDate;

    /**
     * Status atual da tarefa no workflow.
     * <p>Valor padrão: {@link TaskStatus#PENDING}. O status é persistido como
     * String no banco de dados (EnumType.STRING) para maior legibilidade.</p>
     * 
     * @see TaskStatus
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status = TaskStatus.PENDING;

    /**
     * Usuário proprietário da tarefa.
     * <p>Relacionamento Many-to-One com carregamento lazy para otimização.
     * Este campo implementa o conceito de "ownership" - apenas o proprietário
     * pode visualizar, editar ou excluir suas próprias tarefas.</p>
     * 
     * @see User
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    /**
     * Data e hora de criação da tarefa.
     * <p>Preenchido automaticamente pelo callback {@link #onCreate()} e
     * não pode ser alterado após a persistência inicial (updatable = false).</p>
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Data e hora da última atualização da tarefa.
     * <p>Atualizado automaticamente pelo callback {@link #onUpdate()}
     * sempre que a entidade é modificada.</p>
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Callback JPA executado antes da persistência inicial.
     * <p>Define os timestamps de criação e atualização com o momento atual.</p>
     */
    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    /**
     * Callback JPA executado antes de cada atualização.
     * <p>Atualiza o timestamp de última modificação.</p>
     */
    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Obtém o identificador único da tarefa.
     * @return o ID da tarefa ou {@code null} se ainda não persistida
     */
    public Long getId() {
        return id;
    }

    /**
     * Define o identificador da tarefa.
     * <p><strong>Nota:</strong> Normalmente não deve ser chamado diretamente,
     * pois o ID é gerado automaticamente pelo banco de dados.</p>
     * @param id o novo ID da tarefa
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Obtém o título da tarefa.
     * @return o título da tarefa
     */
    public String getTitle() {
        return title;
    }

    /**
     * Define o título da tarefa.
     * @param title o novo título (não pode ser nulo ou vazio)
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Obtém a descrição detalhada da tarefa.
     * @return a descrição ou {@code null} se não definida
     */
    public String getDescription() {
        return description;
    }

    /**
     * Define a descrição detalhada da tarefa.
     * @param description a nova descrição
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Obtém a data limite para conclusão.
     * @return a data limite ou {@code null} se não definida
     */
    public LocalDate getDueDate() {
        return dueDate;
    }

    /**
     * Define a data limite para conclusão.
     * @param dueDate a nova data limite (deve ser futura, validação no Service)
     */
    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    /**
     * Obtém o status atual da tarefa.
     * @return o status da tarefa (nunca {@code null})
     */
    public TaskStatus getStatus() {
        return status;
    }

    /**
     * Define o status da tarefa.
     * @param status o novo status
     * @see TaskStatus
     */
    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    /**
     * Obtém o usuário proprietário da tarefa.
     * @return o usuário proprietário
     */
    public User getOwner() {
        return owner;
    }

    /**
     * Define o usuário proprietário da tarefa.
     * @param owner o novo proprietário (não pode ser nulo)
     */
    public void setOwner(User owner) {
        this.owner = owner;
    }

    /**
     * Obtém a data e hora de criação da tarefa.
     * @return o timestamp de criação
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Obtém a data e hora da última atualização.
     * @return o timestamp de atualização
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
