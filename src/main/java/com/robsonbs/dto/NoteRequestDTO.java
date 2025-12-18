package com.robsonbs.dto;

import org.jboss.resteasy.reactive.RestForm;

/**
 * Data Transfer Object (DTO) para receber dados de criação ou atualização de nota.
 * 
 * <p>Este DTO é utilizado para capturar dados enviados via formulário HTML
 * nas operações de criação e edição de notas/anotações pessoais.</p>
 * 
 * <h2>Campos do Formulário</h2>
 * <table border="1">
 *   <tr><th>Campo</th><th>Tipo</th><th>Obrigatório</th><th>Descrição</th></tr>
 *   <tr><td>title</td><td>String</td><td>Sim</td><td>Título da nota</td></tr>
 *   <tr><td>content</td><td>String</td><td>Não</td><td>Conteúdo detalhado</td></tr>
 * </table>
 * 
 * <h2>Ownership</h2>
 * <p>O proprietário da nota (owner) NÃO é definido neste DTO.
 * Ele é determinado automaticamente pelo {@link com.robsonbs.service.NoteService}
 * com base no usuário autenticado ({@code SecurityIdentity}).</p>
 * 
 * <h2>Validações</h2>
 * <p>Validações de negócio são realizadas no {@link com.robsonbs.service.NoteService}:</p>
 * <ul>
 *   <li>Título não pode ser vazio</li>
 *   <li>Título não pode ser apenas espaços em branco</li>
 * </ul>
 * 
 * <h2>Exemplo de Uso</h2>
 * <pre>{@code
 * // No Controller
 * @POST
 * public Response create(@BeanParam NoteRequestDTO dto) {
 *     String ownerEmail = identity.getPrincipal().getName();
 *     NoteResponseDTO created = noteService.create(dto, ownerEmail);
 *     return Response.seeOther(URI.create("/notes?success=Nota criada")).build();
 * }
 * }</pre>
 * 
 * @author Sistema de Gestão de Notas e Tarefas
 * @version 1.0
 * @since 2025-12-01
 * @see NoteResponseDTO
 * @see com.robsonbs.model.Note
 * @see com.robsonbs.service.NoteService
 */
public class NoteRequestDTO {
    
    /**
     * Título da nota.
     * <p>Campo obrigatório, deve ser descritivo do conteúdo da anotação.</p>
     */
    @RestForm
    private String title;
    
    /**
     * Conteúdo detalhado da nota.
     * <p>Campo opcional que permite armazenar o texto completo da anotação.</p>
     */
    @RestForm
    private String content;

    /**
     * Obtém o título da nota.
     * @return o título
     */
    public String getTitle() {
        return title;
    }

    /**
     * Define o título da nota.
     * @param title o título (obrigatório)
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Obtém o conteúdo da nota.
     * @return o conteúdo ou {@code null}
     */
    public String getContent() {
        return content;
    }

    /**
     * Define o conteúdo da nota.
     * @param content o conteúdo detalhado
     */
    public void setContent(String content) {
        this.content = content;
    }
}
