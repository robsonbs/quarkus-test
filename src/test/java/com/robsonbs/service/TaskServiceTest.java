package com.robsonbs.service;

import com.robsonbs.dao.AuditLogDao;
import com.robsonbs.dao.TaskDao;
import com.robsonbs.dto.TaskRequestDTO;
import com.robsonbs.model.AuditLog;
import com.robsonbs.model.Task;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class TaskServiceTest {

    @Inject
    TaskService taskService;

    @Inject
    TaskDao taskDao;

    @Inject
    AuditLogDao auditLogDao;

    @Test
    @TestSecurity(user = "user@example.com", roles = {"USER"})
    @Transactional
    void shouldValidateLifecycleAndAuditEntries() {
        String uniqueTitle = "Tarefa de Teste " + UUID.randomUUID();

        TaskRequestDTO createDto = new TaskRequestDTO();
        createDto.setTitle(uniqueTitle);
        createDto.setDescription("Descrição inicial da tarefa");
        createDto.setDueDate(LocalDate.now().plusDays(3).toString());
        createDto.setStatus("PENDING");

        taskService.create(createDto);

        Task persisted = taskDao.find("title", uniqueTitle).firstResultOptional()
                .orElseThrow();
        assertNotNull(persisted.getId());
        assertEquals(uniqueTitle, persisted.getTitle());

        Optional<AuditLog> creationLog = auditLogDao.find("entityType = ?1 and entityId = ?2 and action = ?3",
                "Task",
                String.valueOf(persisted.getId()),
                "TASK_CREATED").firstResultOptional();
        assertTrue(creationLog.isPresent());

        TaskRequestDTO updateDto = new TaskRequestDTO();
        updateDto.setTitle(uniqueTitle);
        updateDto.setDescription("Descrição atualizada");
        updateDto.setDueDate(LocalDate.now().plusDays(5).toString());
        updateDto.setStatus("IN_PROGRESS");

        taskService.update(persisted.getId(), updateDto);

        Task updated = taskService.findById(persisted.getId());
        assertEquals("IN_PROGRESS", updated.getStatus().name());

        Optional<AuditLog> updateLog = auditLogDao.find("entityType = ?1 and entityId = ?2 and action = ?3",
                "Task",
                String.valueOf(persisted.getId()),
                "TASK_UPDATED").firstResultOptional();
        assertTrue(updateLog.isPresent());

        taskService.delete(persisted.getId());

        Optional<AuditLog> deleteLog = auditLogDao.find("entityType = ?1 and entityId = ?2 and action = ?3",
                "Task",
                String.valueOf(persisted.getId()),
                "TASK_DELETED").firstResultOptional();
        assertTrue(deleteLog.isPresent());
    }
}
