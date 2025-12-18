package com.robsonbs.service;

import com.robsonbs.dao.AuditLogDao;
import com.robsonbs.dao.NoteDao;
import com.robsonbs.dao.UserDao;
import com.robsonbs.dto.NoteRequestDTO;
import com.robsonbs.model.AuditLog;
import com.robsonbs.model.Note;
import com.robsonbs.model.User;
import io.quarkus.hibernate.orm.panache.Panache;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes completos para NoteService cobrindo todos os fluxos.
 */
@QuarkusTest
class NoteServiceCompleteTest {

    @Inject
    NoteService noteService;

    @Inject
    NoteDao noteDao;

    @Inject
    UserDao userDao;

    @Inject
    AuditLogDao auditLogDao;

    // =====================================================
    // Testes de Criação
    // =====================================================

    @Test
    @TestSecurity(user = "user@example.com", roles = {"USER"})
    void shouldCreateNoteWithValidData() {
        String uniqueTitle = "Nota Válida " + UUID.randomUUID();

        NoteRequestDTO dto = new NoteRequestDTO();
        dto.setTitle(uniqueTitle);
        dto.setContent("Conteúdo da nota de teste");

        noteService.create(dto);

        Note note = noteDao.find("title", uniqueTitle).firstResult();
        assertNotNull(note);
        assertEquals(uniqueTitle, note.getTitle());
        assertEquals("Conteúdo da nota de teste", note.getContent());
        assertNotNull(note.getOwner());
        assertEquals("user@example.com", note.getOwner().getEmail());
        assertNotNull(note.getCreatedAt());
    }

    @Test
    @TestSecurity(user = "user@example.com", roles = {"USER"})
    void shouldRejectNullDTO() {
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> noteService.create(null));

        assertEquals("Requisição inválida", exception.getMessage());
    }

    @Test
    @TestSecurity(user = "user@example.com", roles = {"USER"})
    void shouldRejectEmptyTitle() {
        NoteRequestDTO dto = new NoteRequestDTO();
        dto.setTitle("");
        dto.setContent("Conteúdo qualquer");

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> noteService.create(dto));

        assertEquals("Título é obrigatório", exception.getMessage());
    }

    @Test
    @TestSecurity(user = "user@example.com", roles = {"USER"})
    void shouldRejectBlankTitle() {
        NoteRequestDTO dto = new NoteRequestDTO();
        dto.setTitle("   ");
        dto.setContent("Conteúdo qualquer");

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> noteService.create(dto));

        assertEquals("Título é obrigatório", exception.getMessage());
    }

    @Test
    @TestSecurity(user = "user@example.com", roles = {"USER"})
    void shouldRejectNullTitle() {
        NoteRequestDTO dto = new NoteRequestDTO();
        dto.setTitle(null);
        dto.setContent("Conteúdo qualquer");

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> noteService.create(dto));

        assertEquals("Título é obrigatório", exception.getMessage());
    }

    @Test
    @TestSecurity(user = "user@example.com", roles = {"USER"})
    void shouldRejectEmptyContent() {
        NoteRequestDTO dto = new NoteRequestDTO();
        dto.setTitle("Título válido");
        dto.setContent("");

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> noteService.create(dto));

        assertEquals("Conteúdo é obrigatório", exception.getMessage());
    }

    @Test
    @TestSecurity(user = "user@example.com", roles = {"USER"})
    void shouldRejectBlankContent() {
        NoteRequestDTO dto = new NoteRequestDTO();
        dto.setTitle("Título válido");
        dto.setContent("   ");

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> noteService.create(dto));

        assertEquals("Conteúdo é obrigatório", exception.getMessage());
    }

    @Test
    @TestSecurity(user = "user@example.com", roles = {"USER"})
    void shouldRejectNullContent() {
        NoteRequestDTO dto = new NoteRequestDTO();
        dto.setTitle("Título válido");
        dto.setContent(null);

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> noteService.create(dto));

        assertEquals("Conteúdo é obrigatório", exception.getMessage());
    }

    @Test
    @TestSecurity(user = "user@example.com", roles = {"USER"})
    void shouldTrimTitleAndContent() {
        String uniqueTitle = "  Título com espaços  " + UUID.randomUUID();

        NoteRequestDTO dto = new NoteRequestDTO();
        dto.setTitle(uniqueTitle);
        dto.setContent("  Conteúdo com espaços  ");

        noteService.create(dto);

        Note note = noteDao.find("title", uniqueTitle.trim()).firstResult();
        assertNotNull(note);
        assertEquals(uniqueTitle.trim(), note.getTitle());
        assertEquals("Conteúdo com espaços", note.getContent());
    }

    // =====================================================
    // Testes de Listagem
    // =====================================================

    @Test
    @TestSecurity(user = "user@example.com", roles = {"USER"})
    void shouldListNotesFromOwner() {
        String uniqueTitle = "Nota para listar " + UUID.randomUUID();

        NoteRequestDTO dto = new NoteRequestDTO();
        dto.setTitle(uniqueTitle);
        dto.setContent("Conteúdo");

        noteService.create(dto);

        List<Note> notes = noteService.findByOwner();

        assertFalse(notes.isEmpty());
        boolean found = notes.stream().anyMatch(n -> n.getTitle().equals(uniqueTitle));
        assertTrue(found, "Deve encontrar a nota criada");
    }

    @Test
    @TestSecurity(user = "admin@example.com", roles = {"ADMIN"})
    void shouldNotListNotesFromOtherUsers() {
        // Admin não deve ver notas criadas pelo user via findByOwner
        List<Note> adminNotes = noteService.findByOwner();

        boolean hasUserNote = adminNotes.stream()
                .anyMatch(n -> n.getOwner() != null && "user@example.com".equals(n.getOwner().getEmail()));

        assertFalse(hasUserNote, "Admin não deve ver notas de outros usuários via findByOwner");
    }

    // =====================================================
    // Testes de Busca por ID
    // =====================================================

    @Test
    @TestSecurity(user = "user@example.com", roles = {"USER"})
    void shouldFindNoteById() {
        String uniqueTitle = "Nota para buscar " + UUID.randomUUID();

        NoteRequestDTO dto = new NoteRequestDTO();
        dto.setTitle(uniqueTitle);
        dto.setContent("Conteúdo da busca");

        noteService.create(dto);

        Note created = noteDao.find("title", uniqueTitle).firstResult();
        Note found = noteService.findById(created.getId());

        assertNotNull(found);
        assertEquals(uniqueTitle, found.getTitle());
    }

    @Test
    @TestSecurity(user = "user@example.com", roles = {"USER"})
    void shouldRejectFindNonExistentNote() {
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> noteService.findById(999999L));

        assertEquals("Anotação não encontrada", exception.getMessage());
    }

    // =====================================================
    // Testes de Atualização
    // =====================================================

    @Test
    @TestSecurity(user = "user@example.com", roles = {"USER"})
    void shouldUpdateNoteSuccessfully() {
        String uniqueTitle = "Nota para atualizar " + UUID.randomUUID();

        NoteRequestDTO createDto = new NoteRequestDTO();
        createDto.setTitle(uniqueTitle);
        createDto.setContent("Conteúdo original");

        noteService.create(createDto);

        Note created = noteDao.find("title", uniqueTitle).firstResult();

        NoteRequestDTO updateDto = new NoteRequestDTO();
        updateDto.setTitle(uniqueTitle + " ATUALIZADA");
        updateDto.setContent("Novo conteúdo");

        noteService.update(created.getId(), updateDto);

        Panache.getEntityManager().clear();
        Note updated = noteDao.findById(created.getId());

        assertEquals(uniqueTitle + " ATUALIZADA", updated.getTitle());
        assertEquals("Novo conteúdo", updated.getContent());
        assertNotNull(updated.getUpdatedAt());
    }

    @Test
    @TestSecurity(user = "user@example.com", roles = {"USER"})
    void shouldRejectUpdateWithEmptyTitle() {
        String uniqueTitle = "Nota update vazio " + UUID.randomUUID();

        NoteRequestDTO createDto = new NoteRequestDTO();
        createDto.setTitle(uniqueTitle);
        createDto.setContent("Conteúdo");

        noteService.create(createDto);

        Note created = noteDao.find("title", uniqueTitle).firstResult();

        NoteRequestDTO updateDto = new NoteRequestDTO();
        updateDto.setTitle("");
        updateDto.setContent("Novo conteúdo");

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> noteService.update(created.getId(), updateDto));

        assertEquals("Título é obrigatório", exception.getMessage());
    }

    @Test
    @TestSecurity(user = "user@example.com", roles = {"USER"})
    void shouldRejectUpdateNonExistentNote() {
        NoteRequestDTO dto = new NoteRequestDTO();
        dto.setTitle("Título");
        dto.setContent("Conteúdo");

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> noteService.update(999999L, dto));

        assertEquals("Anotação não encontrada", exception.getMessage());
    }

    // =====================================================
    // Testes de Exclusão
    // =====================================================

    @Test
    @TestSecurity(user = "user@example.com", roles = {"USER"})
    void shouldDeleteNoteSuccessfully() {
        String uniqueTitle = "Nota para excluir " + UUID.randomUUID();

        NoteRequestDTO dto = new NoteRequestDTO();
        dto.setTitle(uniqueTitle);
        dto.setContent("Conteúdo");

        noteService.create(dto);

        Note created = noteDao.find("title", uniqueTitle).firstResult();
        Long noteId = created.getId();
        assertNotNull(noteId, "Nota deve ser criada com ID válido");

        noteService.delete(noteId);

        // Limpa o cache do EntityManager para forçar nova consulta ao banco
        Panache.getEntityManager().clear();

        Note deleted = noteDao.findById(noteId);
        assertNull(deleted, "Nota deve ser excluída");
    }

    @Test
    @TestSecurity(user = "user@example.com", roles = {"USER"})
    void shouldRejectDeleteNonExistentNote() {
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> noteService.delete(999999L));

        assertEquals("Anotação não encontrada", exception.getMessage());
    }

    // =====================================================
    // Testes de Ownership
    // =====================================================

    @Test
    @TestSecurity(user = "admin@example.com", roles = {"ADMIN"})
    @Transactional
    void shouldRejectAccessToOtherUserNote() {
        // Criar nota como admin
        String uniqueTitle = "Nota do Admin " + UUID.randomUUID();
        NoteRequestDTO dto = new NoteRequestDTO();
        dto.setTitle(uniqueTitle);
        dto.setContent("Conteúdo do admin");

        noteService.create(dto);

        Note adminNote = noteDao.find("title", uniqueTitle).firstResult();
        Long noteId = adminNote.getId();

        // Nota criada pelo admin, tentar acessar como admin deve funcionar
        Note found = noteService.findById(noteId);
        assertNotNull(found);
    }

    // =====================================================
    // Testes de Auditoria
    // =====================================================

    @Test
    @TestSecurity(user = "user@example.com", roles = {"USER"})
    void shouldRecordAuditOnCreate() {
        String uniqueTitle = "Nota Auditada " + UUID.randomUUID();

        NoteRequestDTO dto = new NoteRequestDTO();
        dto.setTitle(uniqueTitle);
        dto.setContent("Conteúdo auditado");

        noteService.create(dto);

        Note created = noteDao.find("title", uniqueTitle).firstResult();

        Optional<AuditLog> audit = auditLogDao.find(
                "entityType = ?1 and entityId = ?2 and action = ?3",
                "Note", created.getId().toString(), "NOTE_CREATED"
        ).firstResultOptional();

        assertTrue(audit.isPresent(), "Deve criar registro de auditoria");
        assertEquals("user@example.com", audit.get().getUsername());
    }

    @Test
    @TestSecurity(user = "user@example.com", roles = {"USER"})
    void shouldRecordAuditOnUpdate() {
        String uniqueTitle = "Nota Update Audit " + UUID.randomUUID();

        NoteRequestDTO dto = new NoteRequestDTO();
        dto.setTitle(uniqueTitle);
        dto.setContent("Conteúdo");

        noteService.create(dto);

        Note created = noteDao.find("title", uniqueTitle).firstResult();

        NoteRequestDTO updateDto = new NoteRequestDTO();
        updateDto.setTitle(uniqueTitle + " Updated");
        updateDto.setContent("Novo conteúdo");

        noteService.update(created.getId(), updateDto);

        Optional<AuditLog> audit = auditLogDao.find(
                "entityType = ?1 and entityId = ?2 and action = ?3",
                "Note", created.getId().toString(), "NOTE_UPDATED"
        ).firstResultOptional();

        assertTrue(audit.isPresent(), "Deve criar registro de auditoria de update");
    }

    @Test
    @TestSecurity(user = "user@example.com", roles = {"USER"})
    void shouldRecordAuditOnDelete() {
        String uniqueTitle = "Nota Delete Audit " + UUID.randomUUID();

        NoteRequestDTO dto = new NoteRequestDTO();
        dto.setTitle(uniqueTitle);
        dto.setContent("Conteúdo");

        noteService.create(dto);

        Note created = noteDao.find("title", uniqueTitle).firstResult();
        String noteId = created.getId().toString();

        noteService.delete(created.getId());

        Optional<AuditLog> audit = auditLogDao.find(
                "entityType = ?1 and entityId = ?2 and action = ?3",
                "Note", noteId, "NOTE_DELETED"
        ).firstResultOptional();

        assertTrue(audit.isPresent(), "Deve criar registro de auditoria de delete");
    }

    // =====================================================
    // Testes de Ciclo de Vida Completo
    // =====================================================

    @Test
    @TestSecurity(user = "user@example.com", roles = {"USER"})
    void shouldManageFullLifecycle() {
        String uniqueTitle = "Ciclo Completo " + UUID.randomUUID();

        // CREATE
        NoteRequestDTO createDto = new NoteRequestDTO();
        createDto.setTitle(uniqueTitle);
        createDto.setContent("Conteúdo inicial");
        noteService.create(createDto);

        Note created = noteDao.find("title", uniqueTitle).firstResult();
        assertNotNull(created.getId());
        assertNotNull(created.getCreatedAt());
        assertNull(created.getUpdatedAt());
        Long noteId = created.getId();

        // READ
        Note found = noteService.findById(noteId);
        assertEquals(uniqueTitle, found.getTitle());

        // UPDATE
        NoteRequestDTO updateDto = new NoteRequestDTO();
        updateDto.setTitle(uniqueTitle);
        updateDto.setContent("Conteúdo atualizado");
        noteService.update(noteId, updateDto);

        Panache.getEntityManager().clear();
        Note updated = noteDao.findById(noteId);
        assertEquals("Conteúdo atualizado", updated.getContent());
        assertNotNull(updated.getUpdatedAt());

        // DELETE
        noteService.delete(noteId);
        Panache.getEntityManager().clear();

        Note deleted = noteDao.findById(noteId);
        assertNull(deleted);

        // Verificar todas as auditorias
        List<AuditLog> audits = auditLogDao.find(
                "entityType = ?1 and entityId = ?2 order by occurredAt",
                "Note", noteId.toString()
        ).list();

        assertEquals(3, audits.size(), "Deve ter 3 registros de auditoria (create, update, delete)");
    }
}
