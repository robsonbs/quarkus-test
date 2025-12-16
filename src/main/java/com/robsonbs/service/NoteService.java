package com.robsonbs.service;

import com.robsonbs.dao.NoteDao;
import com.robsonbs.dao.UserDao;
import com.robsonbs.dto.NoteRequestDTO;
import com.robsonbs.model.Note;
import com.robsonbs.model.User;
import com.robsonbs.service.AuditLogService;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;

import java.util.List;

@ApplicationScoped
public class NoteService {

    @Inject
    NoteDao noteDao;

    @Inject
    UserDao userDao;

    @Inject
    SecurityIdentity securityIdentity;

    @Inject
    AuditLogService auditLogService;

    public List<Note> findByOwner() {
        User owner = currentUser();
        return noteDao.findByOwnerId(owner.getId());
    }

    @Transactional
    public void create(NoteRequestDTO noteRequestDTO) {
        User owner = currentUser();
        validateNote(noteRequestDTO);
        String title = noteRequestDTO.getTitle().trim();
        String content = noteRequestDTO.getContent().trim();

        Note note = new Note();
        note.setTitle(title);
        note.setContent(content);
        note.setOwner(owner);
        noteDao.persist(note);
        noteDao.flush();

        auditLogService.recordDomainEvent(
            owner.getEmail(),
            "NOTE_CREATED",
            "/notes",
            "Note",
            note.getId() != null ? note.getId().toString() : null,
            "Nota criada",
            Response.Status.CREATED.getStatusCode()
        );
    }

    public Note findById(Long id) {
        Note note = noteDao.findById(id);
        if (note == null) {
            throw new NotFoundException("Anotação não encontrada");
        }
        verifyOwnership(note);
        return note;
    }

    @Transactional
    public void update(Long id, NoteRequestDTO noteRequestDTO) {
        Note note = findById(id);
        validateNote(noteRequestDTO);
        String title = noteRequestDTO.getTitle().trim();
        String content = noteRequestDTO.getContent().trim();
        note.setTitle(title);
        note.setContent(content);
        noteDao.persist(note);

        auditLogService.recordDomainEvent(
            currentUserEmail(),
            "NOTE_UPDATED",
            "/notes/" + id,
            "Note",
            note.getId() != null ? note.getId().toString() : null,
            "Nota atualizada",
            Response.Status.OK.getStatusCode()
        );
    }

    @Transactional
    public void delete(Long id) {
        Note note = findById(id);
        noteDao.delete(note);

        auditLogService.recordDomainEvent(
            currentUserEmail(),
            "NOTE_DELETED",
            "/notes/" + id + "/delete",
            "Note",
            note.getId() != null ? note.getId().toString() : null,
            "Nota removida",
            Response.Status.OK.getStatusCode()
        );
    }

    private User currentUser() {
        String userEmail = securityIdentity.getPrincipal().getName();
        return userDao.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("Usuário autenticado não encontrado"));
    }

    private String currentUserEmail() {
        return securityIdentity != null && securityIdentity.getPrincipal() != null
                ? securityIdentity.getPrincipal().getName()
                : "system";
    }

    private void verifyOwnership(Note note) {
        User owner = note.getOwner();
        if (owner == null) {
            throw new NotFoundException("Proprietário da anotação não encontrado");
        }
        String authenticatedEmail = securityIdentity.getPrincipal().getName();
        if (!owner.getEmail().equalsIgnoreCase(authenticatedEmail)) {
            throw new ForbiddenException("Você não tem permissão para alterar esta anotação.");
        }
    }

    private void validateNote(NoteRequestDTO requestDTO) {
        if (requestDTO == null) {
            throw new BadRequestException("Requisição inválida");
        }
        if (requestDTO.getTitle() == null || requestDTO.getTitle().trim().isEmpty()) {
            throw new BadRequestException("Título é obrigatório");
        }
        if (requestDTO.getContent() == null || requestDTO.getContent().trim().isEmpty()) {
            throw new BadRequestException("Conteúdo é obrigatório");
        }
    }
}
