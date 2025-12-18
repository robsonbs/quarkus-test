package com.robsonbs.service;

import com.robsonbs.dao.TaskDao;
import com.robsonbs.dao.UserDao;
import com.robsonbs.dto.TaskRequestDTO;
import com.robsonbs.model.Task;
import com.robsonbs.model.TaskStatus;
import com.robsonbs.model.User;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Serviço de negócio para gerenciamento de tarefas.
 * 
 * <p>Esta classe implementa a camada de regras de negócio para operações
 * CRUD de tarefas ({@link Task}), incluindo workflow de status, validação
 * de datas e isolamento por usuário (multi-tenancy).</p>
 * 
 * <h2>Modelo de Propriedade</h2>
 * <p>Cada tarefa pertence exclusivamente a um usuário (owner). Este
 * serviço garante que:</p>
 * <ul>
 *   <li>Usuários só visualizam suas próprias tarefas</li>
 *   <li>Operações de edição/exclusão verificam propriedade</li>
 *   <li>Tentativas de acesso não autorizado resultam em {@link ForbiddenException}</li>
 * </ul>
 * 
 * <h2>Workflow de Status</h2>
 * <p>As tarefas seguem um ciclo de vida definido por {@link TaskStatus}:</p>
 * <ul>
 *   <li>{@code PENDING} - Estado inicial, tarefa aguardando início</li>
 *   <li>{@code IN_PROGRESS} - Tarefa em execução</li>
 *   <li>{@code COMPLETED} - Tarefa finalizada com sucesso</li>
 *   <li>{@code CANCELLED} - Tarefa cancelada</li>
 * </ul>
 * <p>O status padrão para novas tarefas é {@code PENDING}.</p>
 * 
 * <h2>Validação de Data de Entrega</h2>
 * <p>A data de entrega (dueDate):</p>
 * <ul>
 *   <li>É opcional (pode ser {@code null})</li>
 *   <li>Deve estar no formato ISO (yyyy-MM-dd)</li>
 *   <li><strong>Não pode ser no passado</strong> - validação de negócio</li>
 * </ul>
 * 
 * <h2>Validações de Negócio</h2>
 * <ul>
 *   <li><strong>Título:</strong> Obrigatório, não pode ser vazio</li>
 *   <li><strong>Descrição:</strong> Opcional, trimada se fornecida</li>
 *   <li><strong>Status:</strong> Se fornecido, deve ser válido (case-insensitive)</li>
 * </ul>
 * 
 * <h2>Auditoria</h2>
 * <p>Eventos registrados:</p>
 * <ul>
 *   <li>{@code TASK_CREATED} - Criação de nova tarefa</li>
 *   <li>{@code TASK_UPDATED} - Atualização de tarefa existente</li>
 *   <li>{@code TASK_DELETED} - Remoção de tarefa</li>
 * </ul>
 * 
 * <h2>Exemplo de Uso</h2>
 * <pre>{@code
 * @Inject
 * TaskService taskService;
 * 
 * // Listar tarefas do usuário autenticado
 * List<Task> myTasks = taskService.findByOwner();
 * 
 * // Criar nova tarefa
 * TaskRequestDTO dto = new TaskRequestDTO();
 * dto.setTitle("Implementar feature X");
 * dto.setDescription("Detalhes da implementação");
 * dto.setDueDate("2025-01-15");
 * dto.setStatus("PENDING");
 * taskService.create(dto);
 * 
 * // Atualizar status para em progresso
 * dto.setStatus("IN_PROGRESS");
 * taskService.update(1L, dto);
 * 
 * // Remover tarefa
 * taskService.delete(1L);
 * }</pre>
 * 
 * @author Sistema de Gerenciamento
 * @version 1.0
 * @since 1.0
 * @see Task
 * @see TaskStatus
 * @see TaskDao
 * @see TaskRequestDTO
 */
@ApplicationScoped
public class TaskService {

    /**
     * DAO para operações de persistência de tarefas.
     */
    @Inject
    TaskDao taskDao;

    /**
     * DAO para operações de usuários.
     * Usado para resolver o usuário autenticado como owner.
     */
    @Inject
    UserDao userDao;

    /**
     * Identidade de segurança do usuário autenticado.
     */
    @Inject
    SecurityIdentity securityIdentity;

    /**
     * Serviço de auditoria para registro de eventos de domínio.
     */
    @Inject
    AuditLogService auditLogService;

    /**
     * Lista todas as tarefas do usuário autenticado.
     * 
     * <p>Retorna apenas as tarefas onde o usuário é o proprietário,
     * garantindo isolamento de dados entre usuários.</p>
     * 
     * @return lista de tarefas do usuário; nunca {@code null}
     */
    public List<Task> findByOwner() {
        User owner = currentUser();
        return taskDao.findByOwnerId(owner.getId());
    }

    /**
     * Busca uma tarefa pelo ID, verificando propriedade.
     * 
     * <p>Além de buscar a tarefa, este método verifica se o usuário
     * autenticado é o proprietário. Se não for, lança exceção de acesso negado.</p>
     * 
     * @param id identificador da tarefa
     * @return tarefa encontrada
     * @throws NotFoundException se tarefa não existir
     * @throws ForbiddenException se usuário não for o proprietário
     */
    public Task findById(Long id) {
        Task task = taskDao.findById(id);
        if (task == null) {
            throw new NotFoundException("Tarefa não encontrada");
        }
        verifyOwnership(task);
        return task;
    }

    /**
     * Cria uma nova tarefa para o usuário autenticado.
     * 
     * <p>O usuário autenticado é automaticamente definido como
     * proprietário da tarefa. O status padrão é {@code PENDING}.</p>
     * 
     * @param dto dados da nova tarefa
     * @throws BadRequestException se título for nulo/vazio ou data inválida
     */
    @Transactional
    public void create(TaskRequestDTO dto) {
        User owner = currentUser();
        Task task = new Task();
        populateTask(task, dto);
        task.setOwner(owner);
        taskDao.persist(task);
        taskDao.flush();

        auditLogService.recordDomainEvent(
                owner.getEmail(),
                "TASK_CREATED",
                "/tasks",
                "Task",
                task.getId() != null ? task.getId().toString() : null,
                "Tarefa criada",
                Response.Status.CREATED.getStatusCode()
        );
    }

    /**
     * Atualiza uma tarefa existente.
     * 
     * <p>Verifica propriedade antes de permitir a atualização.</p>
     * 
     * @param id identificador da tarefa a atualizar
     * @param dto novos dados da tarefa
     * @throws NotFoundException se tarefa não existir
     * @throws ForbiddenException se usuário não for o proprietário
     * @throws BadRequestException se dados forem inválidos
     */
    @Transactional
    public void update(Long id, TaskRequestDTO dto) {
        Task task = findById(id);
        populateTask(task, dto);
        auditLogService.recordDomainEvent(
                currentActor(),
                "TASK_UPDATED",
                "/tasks/" + id,
                "Task",
                task.getId() != null ? task.getId().toString() : null,
                "Tarefa atualizada",
                Response.Status.OK.getStatusCode()
        );
    }

    /**
     * Remove uma tarefa do sistema.
     * 
     * <p>Verifica propriedade antes de permitir a exclusão.
     * A exclusão é física (hard delete).</p>
     * 
     * @param id identificador da tarefa a remover
     * @throws NotFoundException se tarefa não existir
     * @throws ForbiddenException se usuário não for o proprietário
     */
    @Transactional
    public void delete(Long id) {
        Task task = findById(id);
        taskDao.delete(task);
        auditLogService.recordDomainEvent(
                currentActor(),
                "TASK_DELETED",
                "/tasks/" + id + "/delete",
                "Task",
                task.getId() != null ? task.getId().toString() : null,
                "Tarefa removida",
                Response.Status.OK.getStatusCode()
        );
    }

    /**
     * Popula os campos da tarefa a partir do DTO.
     * 
     * <p>Valida título obrigatório, sanitiza descrição,
     * resolve data de entrega e status.</p>
     * 
     * @param task entidade a popular
     * @param dto fonte dos dados
     * @throws BadRequestException se dados forem inválidos
     */
    private void populateTask(Task task, TaskRequestDTO dto) {
        if (dto == null) {
            throw new BadRequestException("Requisição inválida");
        }
        String title = sanitize(dto.getTitle());
        if (title == null || title.isBlank()) {
            throw new BadRequestException("Título é obrigatório");
        }
        task.setTitle(title);
        task.setDescription(trimToNull(dto.getDescription()));
        task.setDueDate(resolveDueDate(dto.getDueDate()));
        task.setStatus(resolveStatus(dto.getStatus()));
    }

    /**
     * Resolve o status da tarefa a partir de string.
     * 
     * <p>Se não fornecido ou vazio, retorna {@code PENDING} como padrão.
     * A conversão é case-insensitive.</p>
     * 
     * @param status string do status
     * @return enum TaskStatus correspondente
     * @throws BadRequestException se status for inválido
     */
    private TaskStatus resolveStatus(String status) {
        if (status == null || status.isBlank()) {
            return TaskStatus.PENDING;
        }
        try {
            return TaskStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Status inválido");
        }
    }

    /**
     * Resolve a data de entrega a partir de string ISO.
     * 
     * <p>Valida que a data não esteja no passado. Datas nulas ou
     * vazias são permitidas (tarefa sem prazo).</p>
     * 
     * @param dueDate string da data no formato ISO (yyyy-MM-dd)
     * @return data parseada ou {@code null}
     * @throws BadRequestException se formato inválido ou data no passado
     */
    private LocalDate resolveDueDate(String dueDate) {
        if (dueDate == null || dueDate.isBlank()) {
            return null;
        }
        try {
            LocalDate parsed = LocalDate.parse(dueDate);
            if (parsed.isBefore(LocalDate.now())) {
                throw new BadRequestException("Data de entrega não pode ser no passado");
            }
            return parsed;
        } catch (DateTimeParseException ex) {
            throw new BadRequestException("Data de entrega inválida");
        }
    }

    /**
     * Verifica se o usuário autenticado é o proprietário da tarefa.
     * 
     * @param task tarefa a verificar
     * @throws NotFoundException se tarefa não tiver proprietário
     * @throws ForbiddenException se usuário não for o proprietário
     */
    private void verifyOwnership(Task task) {
        User owner = task.getOwner();
        if (owner == null) {
            throw new NotFoundException("Proprietário da tarefa não encontrado");
        }
        String authenticatedEmail = currentActor();
        if (!owner.getEmail().equalsIgnoreCase(authenticatedEmail)) {
            throw new ForbiddenException("Você não tem permissão para alterar esta tarefa.");
        }
    }

    /**
     * Obtém a entidade User do usuário autenticado.
     * 
     * @return usuário autenticado
     * @throws NotFoundException se usuário não existir no banco
     */
    private User currentUser() {
        String email = currentActor();
        return userDao.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuário autenticado não encontrado"));
    }

    /**
     * Obtém o email do usuário autenticado.
     * 
     * @return email do usuário ou "system" se não autenticado
     */
    private String currentActor() {
        if (securityIdentity != null && securityIdentity.getPrincipal() != null) {
            return securityIdentity.getPrincipal().getName();
        }
        return "system";
    }

    /**
     * Converte string vazia para {@code null} após trim.
     * 
     * @param value valor a processar
     * @return valor trimado ou {@code null} se vazio
     */
    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * Sanitiza valor removendo espaços das extremidades.
     * 
     * @param value valor a sanitizar
     * @return valor trimado ou {@code null}
     */
    private String sanitize(String value) {
        if (value == null) {
            return null;
        }
        return value.trim();
    }
}
