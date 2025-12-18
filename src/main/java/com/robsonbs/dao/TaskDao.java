package com.robsonbs.dao;

import com.robsonbs.model.Task;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

/**
 * Repositório de acesso a dados (DAO) para a entidade {@link Task}.
 * 
 * <p>Esta classe implementa o padrão Data Access Object utilizando o Quarkus Panache,
 * fornecendo operações de persistência para tarefas do sistema.</p>
 * 
 * <h2>Ownership e Privacidade</h2>
 * <p>Similar às notas, o sistema implementa "ownership" para tarefas.
 * Cada tarefa pertence a um único usuário, e este DAO fornece métodos
 * para filtrar tarefas por proprietário.</p>
 * 
 * <h2>Workflow de Status</h2>
 * <p>As tarefas possuem um workflow de status que pode ser consultado
 * através de queries personalizadas:</p>
 * <pre>
 * PENDING → IN_PROGRESS → COMPLETED
 * </pre>
 * 
 * <h2>Padrão de Arquitetura</h2>
 * <pre>
 * Controller → Service → DAO → Database
 *                 ↓
 *   Validação de Ownership e Status
 * </pre>
 * 
 * <h2>Métodos Herdados do Panache</h2>
 * <ul>
 *   <li>{@code persist(entity)} - Persiste nova tarefa</li>
 *   <li>{@code findById(id)} - Busca por ID</li>
 *   <li>{@code listAll()} - Lista todas as tarefas</li>
 *   <li>{@code delete(entity)} - Remove tarefa</li>
 *   <li>{@code count()} - Conta tarefas</li>
 *   <li>{@code find(query, params)} - Busca com filtros</li>
 * </ul>
 * 
 * <h2>Exemplo de Uso</h2>
 * <pre>{@code
 * @Inject
 * TaskDao taskDao;
 * 
 * // Buscar tarefas do usuário
 * List<Task> userTasks = taskDao.findByOwnerId(userId);
 * 
 * // Buscar tarefas pendentes
 * List<Task> pending = taskDao.find("status", TaskStatus.PENDING).list();
 * 
 * // Persistir nova tarefa
 * taskDao.persist(newTask);
 * }</pre>
 * 
 * @author Sistema de Gestão de Notas e Tarefas
 * @version 1.0
 * @since 2025-12-01
 * @see Task
 * @see com.robsonbs.model.TaskStatus
 * @see com.robsonbs.service.TaskService
 * @see PanacheRepository
 */
@ApplicationScoped
public class TaskDao implements PanacheRepository<Task> {

    /**
     * Lista todas as tarefas pertencentes a um usuário específico.
     * 
     * <p>Este método implementa o conceito de "ownership", garantindo
     * que cada usuário só visualize suas próprias tarefas.</p>
     * 
     * <p>A query utiliza navegação de propriedade JPA ({@code owner.id})
     * para filtrar pelo ID do proprietário.</p>
     * 
     * @param ownerId o ID do usuário proprietário das tarefas
     * @return lista de tarefas do usuário (pode ser vazia, nunca {@code null})
     * 
     * @see com.robsonbs.service.TaskService#findByOwner(String)
     */
    public List<Task> findByOwnerId(Long ownerId) {
        return find("owner.id", ownerId).list();
    }
}
