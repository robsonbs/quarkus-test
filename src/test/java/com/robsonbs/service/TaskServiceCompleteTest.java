package com.robsonbs.service;

import com.robsonbs.dao.AuditLogDao;
import com.robsonbs.dao.TaskDao;
import com.robsonbs.dto.TaskRequestDTO;
import com.robsonbs.model.AuditLog;
import com.robsonbs.model.Task;
import com.robsonbs.model.TaskStatus;
import io.quarkus.hibernate.orm.panache.Panache;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes completos para TaskService cobrindo todos os fluxos.
 */
@QuarkusTest
class TaskServiceCompleteTest {

    @Inject
    TaskService taskService;

    @Inject
    TaskDao taskDao;

    @Inject
    AuditLogDao auditLogDao;

    // =====================================================
    // Testes de Criação
    // =====================================================

    @Test
    @TestSecurity(user = "user@example.com", roles = {"USER"})
    @Transactional
    void shouldCreateTaskWithAllFields() {
        String uniqueTitle = "Tarefa Completa " + UUID.randomUUID();

        TaskRequestDTO dto = new TaskRequestDTO();
        dto.setTitle(uniqueTitle);
        dto.setDescription("Descrição detalhada da tarefa");
        dto.setDueDate(LocalDate.now().plusDays(7).toString());
        dto.setStatus("PENDING");

        taskService.create(dto);

        Task task = taskDao.find("title", uniqueTitle).firstResult();
        assertNotNull(task);
        assertEquals(uniqueTitle, task.getTitle());
        assertEquals("Descrição detalhada da tarefa", task.getDescription());
        assertEquals(LocalDate.now().plusDays(7), task.getDueDate());
        assertEquals(TaskStatus.PENDING, task.getStatus());
        assertNotNull(task.getOwner());
        assertEquals("user@example.com", task.getOwner().getEmail());
    }

    @Test
    @TestSecurity(user = "user@example.com", roles = {"USER"})
    @Transactional
    void shouldCreateTaskWithMinimalFields() {
        String uniqueTitle = "Tarefa Mínima " + UUID.randomUUID();

        TaskRequestDTO dto = new TaskRequestDTO();
        dto.setTitle(uniqueTitle);

        taskService.create(dto);

        Task task = taskDao.find("title", uniqueTitle).firstResult();
        assertNotNull(task);
        assertEquals(uniqueTitle, task.getTitle());
        assertNull(task.getDescription());
        assertNull(task.getDueDate());
        assertEquals(TaskStatus.PENDING, task.getStatus()); // Default
    }

    @Test
    @TestSecurity(user = "user@example.com", roles = {"USER"})
    void shouldRejectNullDTO() {
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> taskService.create(null));

        assertEquals("Requisição inválida", exception.getMessage());
    }

    @Test
    @TestSecurity(user = "user@example.com", roles = {"USER"})
    void shouldRejectEmptyTitle() {
        TaskRequestDTO dto = new TaskRequestDTO();
        dto.setTitle("");

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> taskService.create(dto));

        assertEquals("Título é obrigatório", exception.getMessage());
    }

    @Test
    @TestSecurity(user = "user@example.com", roles = {"USER"})
    void shouldRejectBlankTitle() {
        TaskRequestDTO dto = new TaskRequestDTO();
        dto.setTitle("   ");

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> taskService.create(dto));

        assertEquals("Título é obrigatório", exception.getMessage());
    }

    @Test
    @TestSecurity(user = "user@example.com", roles = {"USER"})
    void shouldRejectNullTitle() {
        TaskRequestDTO dto = new TaskRequestDTO();
        dto.setTitle(null);
        dto.setDescription("Alguma descrição");

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> taskService.create(dto));

        assertEquals("Título é obrigatório", exception.getMessage());
    }

    // =====================================================
    // Testes de Validação de Data
    // =====================================================

    @Test
    @TestSecurity(user = "user@example.com", roles = {"USER"})
    void shouldRejectPastDueDate() {
        TaskRequestDTO dto = new TaskRequestDTO();
        dto.setTitle("Tarefa com data passada");
        dto.setDueDate(LocalDate.now().minusDays(1).toString());

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> taskService.create(dto));

        assertEquals("Data de entrega não pode ser no passado", exception.getMessage());
    }

    @Test
    @TestSecurity(user = "user@example.com", roles = {"USER"})
    void shouldRejectInvalidDateFormat() {
        TaskRequestDTO dto = new TaskRequestDTO();
        dto.setTitle("Tarefa com data inválida");
        dto.setDueDate("31/12/2025"); // Formato errado

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> taskService.create(dto));

        assertEquals("Data de entrega inválida", exception.getMessage());
    }

    @Test
    @TestSecurity(user = "user@example.com", roles = {"USER"})
    @Transactional
    void shouldAcceptTodayAsDueDate() {
        String uniqueTitle = "Tarefa para hoje " + UUID.randomUUID();
        TaskRequestDTO dto = new TaskRequestDTO();
        dto.setTitle(uniqueTitle);
        dto.setDueDate(LocalDate.now().toString());

        taskService.create(dto);

        Task task = taskDao.find("title", uniqueTitle).firstResult();
        assertNotNull(task);
        assertEquals(LocalDate.now(), task.getDueDate());
    }

    @Test
    @TestSecurity(user = "user@example.com", roles = {"USER"})
    @Transactional
    void shouldAcceptEmptyDueDate() {
        String uniqueTitle = "Tarefa sem data " + UUID.randomUUID();
        TaskRequestDTO dto = new TaskRequestDTO();
        dto.setTitle(uniqueTitle);
        dto.setDueDate("");

        taskService.create(dto);

        Task task = taskDao.find("title", uniqueTitle).firstResult();
        assertNotNull(task);
        assertNull(task.getDueDate());
    }

    // =====================================================
    // Testes de Validação de Status
    // =====================================================

    @Test
    @TestSecurity(user = "user@example.com", roles = {"USER"})
    void shouldRejectInvalidStatus() {
        TaskRequestDTO dto = new TaskRequestDTO();
        dto.setTitle("Tarefa com status inválido");
        dto.setStatus("INVALID_STATUS");

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> taskService.create(dto));

        assertEquals("Status inválido", exception.getMessage());
    }

    @Test
    @TestSecurity(user = "user@example.com", roles = {"USER"})
    @Transactional
    void shouldAcceptAllValidStatuses() {
        for (TaskStatus status : TaskStatus.values()) {
            String uniqueTitle = "Tarefa " + status + " " + UUID.randomUUID();
            TaskRequestDTO dto = new TaskRequestDTO();
            dto.setTitle(uniqueTitle);
            dto.setStatus(status.name());

            taskService.create(dto);

            Task task = taskDao.find("title", uniqueTitle).firstResult();
            assertNotNull(task, "Tarefa com status " + status + " não foi criada");
            assertEquals(status, task.getStatus());
        }
    }

    @Test
    @TestSecurity(user = "user@example.com", roles = {"USER"})
    @Transactional
    void shouldDefaultToPendingStatus() {
        String uniqueTitle = "Tarefa sem status " + UUID.randomUUID();
        TaskRequestDTO dto = new TaskRequestDTO();
        dto.setTitle(uniqueTitle);
        dto.setStatus(null);

        taskService.create(dto);

        Task task = taskDao.find("title", uniqueTitle).firstResult();
        assertEquals(TaskStatus.PENDING, task.getStatus());
    }

    @Test
    @TestSecurity(user = "user@example.com", roles = {"USER"})
    @Transactional
    void shouldAcceptLowercaseStatus() {
        String uniqueTitle = "Tarefa lowercase " + UUID.randomUUID();
        TaskRequestDTO dto = new TaskRequestDTO();
        dto.setTitle(uniqueTitle);
        dto.setStatus("in_progress");

        taskService.create(dto);

        Task task = taskDao.find("title", uniqueTitle).firstResult();
        assertEquals(TaskStatus.IN_PROGRESS, task.getStatus());
    }

    // =====================================================
    // Testes de Atualização
    // =====================================================

    @Test
    @TestSecurity(user = "user@example.com", roles = {"USER"})
    @Transactional
    void shouldUpdateTaskSuccessfully() {
        // Criar tarefa
        String uniqueTitle = "Tarefa para atualizar " + UUID.randomUUID();
        TaskRequestDTO createDto = new TaskRequestDTO();
        createDto.setTitle(uniqueTitle);
        createDto.setStatus("PENDING");
        taskService.create(createDto);

        Task created = taskDao.find("title", uniqueTitle).firstResult();

        // Atualizar tarefa
        TaskRequestDTO updateDto = new TaskRequestDTO();
        updateDto.setTitle(uniqueTitle + " ATUALIZADA");
        updateDto.setDescription("Nova descrição");
        updateDto.setDueDate(LocalDate.now().plusDays(10).toString());
        updateDto.setStatus("IN_PROGRESS");

        taskService.update(created.getId(), updateDto);

        Task updated = taskDao.findById(created.getId());
        assertEquals(uniqueTitle + " ATUALIZADA", updated.getTitle());
        assertEquals("Nova descrição", updated.getDescription());
        assertEquals(TaskStatus.IN_PROGRESS, updated.getStatus());
    }

    @Test
    @TestSecurity(user = "user@example.com", roles = {"USER"})
    void shouldRejectUpdateNonExistentTask() {
        TaskRequestDTO dto = new TaskRequestDTO();
        dto.setTitle("Tarefa inexistente");

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> taskService.update(999999L, dto));

        assertEquals("Tarefa não encontrada", exception.getMessage());
    }

    // =====================================================
    // Testes de Ownership
    // =====================================================

    @Test
    @TestSecurity(user = "user@example.com", roles = {"USER"})
    @Transactional
    void shouldFindTasksByOwner() {
        String uniqueTitle = "Tarefa do owner " + UUID.randomUUID();
        TaskRequestDTO dto = new TaskRequestDTO();
        dto.setTitle(uniqueTitle);
        taskService.create(dto);

        List<Task> tasks = taskService.findByOwner();

        assertFalse(tasks.isEmpty());
        boolean found = tasks.stream().anyMatch(t -> t.getTitle().equals(uniqueTitle));
        assertTrue(found, "Deve encontrar a tarefa criada");
    }

    @Test
    @TestSecurity(user = "admin@example.com", roles = {"ADMIN"})
    @Transactional
    void shouldNotFindTasksFromOtherUser() {
        // Admin não deve ver as tarefas criadas pelo user
        List<Task> adminTasks = taskService.findByOwner();

        // Verifica que o admin não tem as tarefas do user (a menos que tenha criado)
        boolean hasUserTask = adminTasks.stream()
                .anyMatch(t -> t.getOwner() != null && "user@example.com".equals(t.getOwner().getEmail()));

        assertFalse(hasUserTask, "Admin não deve ver tarefas de outros usuários via findByOwner");
    }

    // =====================================================
    // Testes de Exclusão
    // =====================================================

    @Test
    @TestSecurity(user = "user@example.com", roles = {"USER"})
    void shouldDeleteTaskSuccessfully() {
        String uniqueTitle = "Tarefa para excluir " + UUID.randomUUID();
        TaskRequestDTO dto = new TaskRequestDTO();
        dto.setTitle(uniqueTitle);
        taskService.create(dto);

        Task created = taskDao.find("title", uniqueTitle).firstResult();
        Long taskId = created.getId();
        assertNotNull(taskId, "Tarefa deve ser criada com ID válido");

        taskService.delete(taskId);

        // Limpa o cache do EntityManager para forçar nova consulta ao banco
        Panache.getEntityManager().clear();

        Task deleted = taskDao.findById(taskId);
        assertNull(deleted, "Tarefa deve ser excluída");
    }

    @Test
    @TestSecurity(user = "user@example.com", roles = {"USER"})
    void shouldRejectDeleteNonExistentTask() {
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> taskService.delete(999999L));

        assertEquals("Tarefa não encontrada", exception.getMessage());
    }

    // =====================================================
    // Testes de Auditoria
    // =====================================================

    @Test
    @TestSecurity(user = "user@example.com", roles = {"USER"})
    @Transactional
    void shouldRecordAuditOnCreate() {
        String uniqueTitle = "Tarefa Auditada " + UUID.randomUUID();
        TaskRequestDTO dto = new TaskRequestDTO();
        dto.setTitle(uniqueTitle);
        taskService.create(dto);

        Task created = taskDao.find("title", uniqueTitle).firstResult();

        Optional<AuditLog> audit = auditLogDao.find(
                "entityType = ?1 and entityId = ?2 and action = ?3",
                "Task", created.getId().toString(), "TASK_CREATED"
        ).firstResultOptional();

        assertTrue(audit.isPresent(), "Deve criar registro de auditoria");
        assertEquals("user@example.com", audit.get().getUsername());
    }

    @Test
    @TestSecurity(user = "user@example.com", roles = {"USER"})
    @Transactional
    void shouldRecordAuditOnUpdate() {
        String uniqueTitle = "Tarefa Update Audit " + UUID.randomUUID();
        TaskRequestDTO dto = new TaskRequestDTO();
        dto.setTitle(uniqueTitle);
        taskService.create(dto);

        Task created = taskDao.find("title", uniqueTitle).firstResult();

        TaskRequestDTO updateDto = new TaskRequestDTO();
        updateDto.setTitle(uniqueTitle + " Updated");
        updateDto.setStatus("COMPLETED");
        taskService.update(created.getId(), updateDto);

        Optional<AuditLog> audit = auditLogDao.find(
                "entityType = ?1 and entityId = ?2 and action = ?3",
                "Task", created.getId().toString(), "TASK_UPDATED"
        ).firstResultOptional();

        assertTrue(audit.isPresent(), "Deve criar registro de auditoria de update");
    }

    @Test
    @TestSecurity(user = "user@example.com", roles = {"USER"})
    @Transactional
    void shouldRecordAuditOnDelete() {
        String uniqueTitle = "Tarefa Delete Audit " + UUID.randomUUID();
        TaskRequestDTO dto = new TaskRequestDTO();
        dto.setTitle(uniqueTitle);
        taskService.create(dto);

        Task created = taskDao.find("title", uniqueTitle).firstResult();
        String taskId = created.getId().toString();

        taskService.delete(created.getId());

        Optional<AuditLog> audit = auditLogDao.find(
                "entityType = ?1 and entityId = ?2 and action = ?3",
                "Task", taskId, "TASK_DELETED"
        ).firstResultOptional();

        assertTrue(audit.isPresent(), "Deve criar registro de auditoria de delete");
    }

    // =====================================================
    // Testes de Transição de Status
    // =====================================================

    @Test
    @TestSecurity(user = "user@example.com", roles = {"USER"})
    @Transactional
    void shouldAllowStatusTransitionPendingToInProgress() {
        String uniqueTitle = "Transição Status " + UUID.randomUUID();
        TaskRequestDTO dto = new TaskRequestDTO();
        dto.setTitle(uniqueTitle);
        dto.setStatus("PENDING");
        taskService.create(dto);

        Task created = taskDao.find("title", uniqueTitle).firstResult();

        TaskRequestDTO updateDto = new TaskRequestDTO();
        updateDto.setTitle(uniqueTitle);
        updateDto.setStatus("IN_PROGRESS");
        taskService.update(created.getId(), updateDto);

        Task updated = taskDao.findById(created.getId());
        assertEquals(TaskStatus.IN_PROGRESS, updated.getStatus());
    }

    @Test
    @TestSecurity(user = "user@example.com", roles = {"USER"})
    @Transactional
    void shouldAllowStatusTransitionToCompleted() {
        String uniqueTitle = "Completar Tarefa " + UUID.randomUUID();
        TaskRequestDTO dto = new TaskRequestDTO();
        dto.setTitle(uniqueTitle);
        dto.setStatus("IN_PROGRESS");
        taskService.create(dto);

        Task created = taskDao.find("title", uniqueTitle).firstResult();

        TaskRequestDTO updateDto = new TaskRequestDTO();
        updateDto.setTitle(uniqueTitle);
        updateDto.setStatus("COMPLETED");
        taskService.update(created.getId(), updateDto);

        Task updated = taskDao.findById(created.getId());
        assertEquals(TaskStatus.COMPLETED, updated.getStatus());
    }
}
