package com.robsonbs.model;

/**
 * Enumeração que representa os possíveis estados de uma {@link Task} no sistema.
 * 
 * <p>O ciclo de vida de uma tarefa segue o workflow:</p>
 * <pre>
 *     PENDING ──► IN_PROGRESS ──► COMPLETED
 *        │              │
 *        └──────────────┘ (pode voltar)
 * </pre>
 * 
 * <h2>Estados Disponíveis</h2>
 * <ul>
 *   <li>{@link #PENDING} - Tarefa criada, aguardando início</li>
 *   <li>{@link #IN_PROGRESS} - Tarefa em andamento</li>
 *   <li>{@link #COMPLETED} - Tarefa finalizada com sucesso</li>
 * </ul>
 * 
 * <h2>Regras de Negócio</h2>
 * <ul>
 *   <li>Toda tarefa é criada com status {@link #PENDING}</li>
 *   <li>Transições entre estados são validadas no {@link com.robsonbs.service.TaskService}</li>
 *   <li>Mudanças de status são registradas no log de auditoria</li>
 * </ul>
 * 
 * @author Sistema de Gestão de Notas e Tarefas
 * @version 1.0
 * @since 2025-12-01
 * @see Task
 * @see com.robsonbs.service.TaskService
 */
public enum TaskStatus {
    
    /**
     * Tarefa pendente, aguardando início de execução.
     * <p>Este é o estado inicial padrão de toda tarefa criada no sistema.</p>
     */
    PENDING,
    
    /**
     * Tarefa em andamento, sendo executada pelo usuário.
     * <p>Indica que o trabalho foi iniciado mas ainda não foi concluído.</p>
     */
    IN_PROGRESS,
    
    /**
     * Tarefa concluída com sucesso.
     * <p>Estado terminal que indica que a tarefa foi finalizada.</p>
     */
    COMPLETED
}
