package com.robsonbs.service;

import com.robsonbs.dao.AuditLogDao;
import com.robsonbs.dao.NoteDao;
import com.robsonbs.dto.NoteRequestDTO;
import com.robsonbs.model.AuditLog;
import com.robsonbs.model.Note;
import io.quarkus.hibernate.orm.panache.Panache;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class NoteServiceTest {

    @Inject
    NoteService noteService;

    @Inject
    NoteDao noteDao;

    @Inject
    AuditLogDao auditLogDao;

    @Test
    @TestSecurity(user = "user@example.com", roles = {"USER"})
    void shouldManageNoteLifecycleWithAuditing() {
        String uniqueTitle = "Nota QA " + UUID.randomUUID();

        NoteRequestDTO createDto = new NoteRequestDTO();
        createDto.setTitle(uniqueTitle);
        createDto.setContent("Conteúdo inicial");

        noteService.create(createDto);

        Note persisted = noteDao.find("title", uniqueTitle).firstResultOptional()
                .orElseThrow(() -> new AssertionError("Nota não foi persistida"));
        assertNotNull(persisted.getId());

        Optional<AuditLog> creationLog = auditLogDao.find("entityType = ?1 and entityId = ?2 and action = ?3",
                "Note",
                persisted.getId().toString(),
                "NOTE_CREATED").firstResultOptional();
        assertTrue(creationLog.isPresent(), "Auditoria de criação não encontrada");

        NoteRequestDTO updateDto = new NoteRequestDTO();
        updateDto.setTitle(uniqueTitle);
        updateDto.setContent("Conteúdo atualizado");

        noteService.update(persisted.getId(), updateDto);

        Panache.getEntityManager().clear();
        Note updated = noteDao.findById(persisted.getId());
        assertEquals("Conteúdo atualizado", updated.getContent());

        Optional<AuditLog> updateLog = auditLogDao.find("entityType = ?1 and entityId = ?2 and action = ?3",
                "Note",
                persisted.getId().toString(),
                "NOTE_UPDATED").firstResultOptional();
        assertTrue(updateLog.isPresent(), "Auditoria de atualização não encontrada");

        noteService.delete(persisted.getId());

        Optional<AuditLog> deleteLog = auditLogDao.find("entityType = ?1 and entityId = ?2 and action = ?3",
                "Note",
                persisted.getId().toString(),
                "NOTE_DELETED").firstResultOptional();
        assertTrue(deleteLog.isPresent(), "Auditoria de exclusão não encontrada");
    }

    @Test
    @TestSecurity(user = "user@example.com", roles = {"USER"})
    void shouldRejectBlankTitle() {
        NoteRequestDTO dto = new NoteRequestDTO();
        dto.setTitle("   ");
        dto.setContent("Qualquer conteúdo");

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> noteService.create(dto));

        assertEquals("Título é obrigatório", exception.getMessage());
    }
}
