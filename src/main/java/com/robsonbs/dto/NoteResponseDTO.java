package com.robsonbs.dto;

import com.robsonbs.model.Note;
import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) para retornar dados de nota para a camada de apresentação.
 * 
 * <p>Este DTO é utilizado para transferir dados de notas/anotações para templates Qute
 * e respostas de API. Inclui apenas dados relevantes para exibição, protegendo
 * a entidade JPA de exposição direta.</p>
 * 
 * <h2>Privacidade</h2>
 * <p><strong>IMPORTANTE:</strong> Este DTO não inclui o campo {@code owner},
 * pois a listagem de notas já é filtrada por proprietário no Service.
 * Isso evita expor informações desnecessárias sobre o usuário.</p>
 * 
 * <h2>Campos Expostos</h2>
 * <table border="1">
 *   <tr><th>Campo</th><th>Tipo</th><th>Descrição</th></tr>
 *   <tr><td>id</td><td>Long</td><td>Identificador único</td></tr>
 *   <tr><td>title</td><td>String</td><td>Título da nota</td></tr>
 *   <tr><td>content</td><td>String</td><td>Conteúdo detalhado</td></tr>
 *   <tr><td>createdAt</td><td>LocalDateTime</td><td>Data de criação</td></tr>
 *   <tr><td>updatedAt</td><td>LocalDateTime</td><td>Data de última atualização</td></tr>
 * </table>
 * 
 * <h2>Exemplo de Uso</h2>
 * <pre>{@code
 * // No Service
 * public List<NoteResponseDTO> findByOwner(String ownerEmail) {
 *     User owner = userDao.findByEmail(ownerEmail).orElseThrow();
 *     return noteDao.findByOwnerId(owner.getId()).stream()
 *         .map(NoteResponseDTO::new)
 *         .toList();
 * }
 * 
 * // No Template Qute
 * {#for note in notes}
 *     <div class="note-card">
 *         <h3>{note.title}</h3>
 *         <p>{note.content}</p>
 *         <small>Criado em: {note.createdAt}</small>
 *     </div>
 * {/for}
 * }</pre>
 * 
 * @author Sistema de Gestão de Notas e Tarefas
 * @version 1.0
 * @since 2025-12-01
 * @see Note
 * @see NoteRequestDTO
 * @see com.robsonbs.service.NoteService
 */
public class NoteResponseDTO {
    
    /** Identificador único da nota. */
    private Long id;
    
    /** Título da nota. */
    private String title;
    
    /** Conteúdo detalhado da nota. */
    private String content;
    
    /** Data e hora de criação. */
    private LocalDateTime createdAt;
    
    /** Data e hora da última atualização. */
    private LocalDateTime updatedAt;

    /**
     * Construtor que converte uma entidade {@link Note} para DTO.
     * 
     * @param note a entidade Note a ser convertida
     */
    public NoteResponseDTO(Note note) {
        this.id = note.getId();
        this.title = note.getTitle();
        this.content = note.getContent();
        this.createdAt = note.getCreatedAt();
        this.updatedAt = note.getUpdatedAt();
    }

    /**
     * Obtém o ID da nota.
     * @return o identificador único
     */
    public Long getId() {
        return id;
    }

    /**
     * Define o ID da nota.
     * @param id o identificador único
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Obtém o título da nota.
     * @return o título
     */
    public String getTitle() {
        return title;
    }

    /**
     * Define o título da nota.
     * @param title o título
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Obtém o conteúdo da nota.
     * @return o conteúdo
     */
    public String getContent() {
        return content;
    }

    /**
     * Define o conteúdo da nota.
     * @param content o conteúdo
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Obtém a data de criação.
     * @return o timestamp de criação
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Define a data de criação.
     * @param createdAt o timestamp de criação
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Obtém a data de última atualização.
     * @return o timestamp de atualização ou {@code null}
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Define a data de última atualização.
     * @param updatedAt o timestamp de atualização
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
