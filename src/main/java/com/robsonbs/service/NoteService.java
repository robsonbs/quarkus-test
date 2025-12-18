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

/**
 * Serviço de negócio para gerenciamento de anotações pessoais.
 * 
 * <p>Esta classe implementa a camada de regras de negócio para operações
 * CRUD de anotações ({@link Note}), garantindo isolamento por usuário
 * (multi-tenancy) e validações de propriedade.</p>
 * 
 * <h2>Modelo de Propriedade</h2>
 * <p>Cada anotação pertence exclusivamente a um usuário (owner). Este
 * serviço garante que:</p>
 * <ul>
 *   <li>Usuários só visualizam suas próprias anotações</li>
 *   <li>Operações de edição/exclusão verificam propriedade</li>
 *   <li>Tentativas de acesso não autorizado resultam em {@link ForbiddenException}</li>
 * </ul>
 * 
 * <h2>Validações de Negócio</h2>
 * <ul>
 *   <li><strong>Título:</strong> Obrigatório, não pode ser vazio</li>
 *   <li><strong>Conteúdo:</strong> Obrigatório, não pode ser vazio</li>
 *   <li>Ambos os campos são trimados antes da persistência</li>
 * </ul>
 * 
 * <h2>Auditoria</h2>
 * <p>Eventos registrados:</p>
 * <ul>
 *   <li>{@code NOTE_CREATED} - Criação de nova anotação</li>
 *   <li>{@code NOTE_UPDATED} - Atualização de anotação existente</li>
 *   <li>{@code NOTE_DELETED} - Remoção de anotação</li>
 * </ul>
 * 
 * <h2>Exemplo de Uso</h2>
 * <pre>{@code
 * @Inject
 * NoteService noteService;
 * 
 * // Listar anotações do usuário autenticado
 * List<Note> myNotes = noteService.findByOwner();
 * 
 * // Criar nova anotação
 * NoteRequestDTO dto = new NoteRequestDTO();
 * dto.setTitle("Reunião importante");
 * dto.setContent("Discutir roadmap Q1 2025");
 * noteService.create(dto);
 * 
 * // Buscar anotação específica (verifica propriedade)
 * Note note = noteService.findById(1L);
 * 
 * // Atualizar (verifica propriedade)
 * noteService.update(1L, dto);
 * 
 * // Remover (verifica propriedade)
 * noteService.delete(1L);
 * }</pre>
 * 
 * @author Sistema de Gerenciamento
 * @version 1.0
 * @since 1.0
 * @see Note
 * @see NoteDao
 * @see NoteRequestDTO
 */
@ApplicationScoped
public class NoteService {

    /**
     * DAO para operações de persistência de anotações.
     */
    @Inject
    NoteDao noteDao;

    /**
     * DAO para operações de usuários.
     * Usado para resolver o usuário autenticado como owner.
     */
    @Inject
    UserDao userDao;

    /**
     * Identidade de segurança do usuário autenticado.
     * Fornece o email do usuário para verificação de propriedade.
     */
    @Inject
    SecurityIdentity securityIdentity;

    /**
     * Serviço de auditoria para registro de eventos de domínio.
     */
    @Inject
    AuditLogService auditLogService;

    /**
     * Lista todas as anotações do usuário autenticado.
     * 
     * <p>Retorna apenas as anotações onde o usuário é o proprietário,
     * garantindo isolamento de dados entre usuários.</p>
     * 
     * @return lista de anotações do usuário; nunca {@code null}
     */
    public List<Note> findByOwner() {
        User owner = currentUser();
        return noteDao.findByOwnerId(owner.getId());
    }

    /**
     * Cria uma nova anotação para o usuário autenticado.
     * 
     * <p>O usuário autenticado é automaticamente definido como
     * proprietário da anotação. Título e conteúdo são validados
     * e trimados antes da persistência.</p>
     * 
     * @param noteRequestDTO dados da nova anotação
     * @throws BadRequestException se título ou conteúdo forem nulos/vazios
     */
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

    /**
     * Busca uma anotação pelo ID, verificando propriedade.
     * 
     * <p>Além de buscar a anotação, este método verifica se o usuário
     * autenticado é o proprietário. Se não for, lança exceção de acesso negado.</p>
     * 
     * @param id identificador da anotação
     * @return anotação encontrada
     * @throws NotFoundException se anotação não existir
     * @throws ForbiddenException se usuário não for o proprietário
     */
    public Note findById(Long id) {
        Note note = noteDao.findById(id);
        if (note == null) {
            throw new NotFoundException("Anotação não encontrada");
        }
        verifyOwnership(note);
        return note;
    }

    /**
     * Atualiza uma anotação existente.
     * 
     * <p>Verifica propriedade antes de permitir a atualização.
     * Título e conteúdo são validados e trimados.</p>
     * 
     * @param id identificador da anotação a atualizar
     * @param noteRequestDTO novos dados da anotação
     * @throws NotFoundException se anotação não existir
     * @throws ForbiddenException se usuário não for o proprietário
     * @throws BadRequestException se título ou conteúdo forem inválidos
     */
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

    /**
     * Remove uma anotação do sistema.
     * 
     * <p>Verifica propriedade antes de permitir a exclusão.
     * A exclusão é física (hard delete).</p>
     * 
     * @param id identificador da anotação a remover
     * @throws NotFoundException se anotação não existir
     * @throws ForbiddenException se usuário não for o proprietário
     */
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

    /**
     * Obtém a entidade User do usuário autenticado.
     * 
     * @return usuário autenticado
     * @throws NotFoundException se usuário não existir no banco
     */
    private User currentUser() {
        String userEmail = securityIdentity.getPrincipal().getName();
        return userDao.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("Usuário autenticado não encontrado"));
    }

    /**
     * Obtém o email do usuário autenticado.
     * 
     * @return email do usuário ou "system" se não autenticado
     */
    private String currentUserEmail() {
        return securityIdentity != null && securityIdentity.getPrincipal() != null
                ? securityIdentity.getPrincipal().getName()
                : "system";
    }

    /**
     * Verifica se o usuário autenticado é o proprietário da anotação.
     * 
     * @param note anotação a verificar
     * @throws NotFoundException se anotação não tiver proprietário
     * @throws ForbiddenException se usuário não for o proprietário
     */
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

    /**
     * Valida os dados da anotação.
     * 
     * @param requestDTO dados a validar
     * @throws BadRequestException se dados forem inválidos
     */
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
