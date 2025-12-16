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

@ApplicationScoped
public class TaskService {

    @Inject
    TaskDao taskDao;

    @Inject
    UserDao userDao;

    @Inject
    SecurityIdentity securityIdentity;

    @Inject
    AuditLogService auditLogService;

    public List<Task> findByOwner() {
        User owner = currentUser();
        return taskDao.findByOwnerId(owner.getId());
    }

    public Task findById(Long id) {
        Task task = taskDao.findById(id);
        if (task == null) {
            throw new NotFoundException("Tarefa não encontrada");
        }
        verifyOwnership(task);
        return task;
    }

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

    private User currentUser() {
        String email = currentActor();
        return userDao.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuário autenticado não encontrado"));
    }

    private String currentActor() {
        if (securityIdentity != null && securityIdentity.getPrincipal() != null) {
            return securityIdentity.getPrincipal().getName();
        }
        return "system";
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String sanitize(String value) {
        if (value == null) {
            return null;
        }
        return value.trim();
    }
}
