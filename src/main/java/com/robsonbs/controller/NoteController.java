package com.robsonbs.controller;

import com.robsonbs.dto.NoteRequestDTO;
import com.robsonbs.dto.NoteResponseDTO;
import com.robsonbs.service.NoteService;
import com.robsonbs.view.BreadcrumbItem;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.smallrye.common.annotation.Blocking;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.core.MultivaluedMap;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller REST para gerenciamento de anotações pessoais.
 * 
 * <p>Este controller implementa operações CRUD para anotações ({@link com.robsonbs.model.Note}),
 * permitindo que usuários gerenciem suas notas pessoais. Cada usuário só tem
 * acesso às suas próprias anotações (isolamento por proprietário).</p>
 * 
 * <h2>Endpoints Disponíveis</h2>
 * <table border="1">
 *   <tr><th>Método</th><th>Rota</th><th>Descrição</th></tr>
 *   <tr><td>GET</td><td>/notes</td><td>Lista anotações do usuário</td></tr>
 *   <tr><td>GET</td><td>/notes/new</td><td>Formulário de nova anotação</td></tr>
 *   <tr><td>POST</td><td>/notes</td><td>Criar nova anotação</td></tr>
 *   <tr><td>GET</td><td>/notes/{id}</td><td>Formulário de edição</td></tr>
 *   <tr><td>POST</td><td>/notes/{id}</td><td>Atualizar anotação</td></tr>
 *   <tr><td>POST</td><td>/notes/{id}/delete</td><td>Remover anotação</td></tr>
 * </table>
 * 
 * <h2>Segurança</h2>
 * <ul>
 *   <li>Requer autenticação (roles USER ou ADMIN)</li>
 *   <li>Usuários só acessam suas próprias anotações</li>
 *   <li>Tentativas de acesso a anotações de outros resultam em 403</li>
 * </ul>
 * 
 * <h2>Validações</h2>
 * <ul>
 *   <li>Título é obrigatório</li>
 *   <li>Conteúdo é obrigatório</li>
 * </ul>
 * 
 * @author Sistema de Gerenciamento
 * @version 1.0
 * @since 1.0
 * @see NoteService
 * @see NoteRequestDTO
 * @see NoteResponseDTO
 */
@Path("/notes")
@RolesAllowed({"USER", "ADMIN"})
@Blocking
public class NoteController {

    /**
     * Serviço de negócio para operações com anotações.
     */
    @Inject
    NoteService noteService;

    /**
     * Template para listagem de anotações.
     * Corresponde a {@code templates/notes.html}.
     */
    @Inject
    Template notes;

    /**
     * Template para formulário de criação/edição.
     * Corresponde a {@code templates/noteForm.html}.
     */
    @Inject
    Template noteForm;

    /**
     * Lista todas as anotações do usuário autenticado.
     * 
     * @param uriInfo informações da URI com query params
     * @return template renderizado com lista de anotações
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance listNotes(@Context UriInfo uriInfo) {
        List<NoteResponseDTO> userNotes = noteService.findByOwner().stream()
                .map(NoteResponseDTO::new)
                .collect(Collectors.toList());
        var params = uriInfo.getQueryParameters();
        String successMessage = resolveNotesSuccessMessage(params.getFirst("success"));
        String errorMessage = params.getFirst("error");
        return notes
                .data("notes", userNotes)
                .data("successMessage", successMessage)
            .data("errorMessage", errorMessage)
                .data("breadcrumb", List.of(
                        BreadcrumbItem.link("Início", "/"),
                        BreadcrumbItem.current("Minhas anotações")
                ));
    }

    /**
     * Exibe formulário para criação de nova anotação.
     * 
     * @param uriInfo informações da URI com possíveis dados preservados
     * @return template de formulário
     */
    @GET
    @Path("/new")
    @Produces(MediaType.TEXT_HTML)
        public TemplateInstance newNoteForm(@Context UriInfo uriInfo) {
        var params = uriInfo.getQueryParameters();
        Map<String, String> formData = extractFormData(params, "title", "content");
            String initialTitle = resolveInitialValue(formData.get("title"), null);
            String initialContent = resolveInitialValue(formData.get("content"), null);
        String errorMessage = params.getFirst("error");
        return noteForm
                .data("note", null)
                .data("initialTitle", initialTitle)
                .data("initialContent", initialContent)
            .data("errorMessage", errorMessage)
                .data("breadcrumb", List.of(
                        BreadcrumbItem.link("Início", "/"),
                        BreadcrumbItem.link("Minhas anotações", "/notes"),
                        BreadcrumbItem.current("Nova anotação")
                ));
    }

    /**
     * Processa criação de nova anotação.
     * 
     * @param noteRequestDTO dados do formulário
     * @return resposta de redirecionamento
     */
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response createNote(@BeanParam NoteRequestDTO noteRequestDTO) {
        try {
            noteService.create(noteRequestDTO);
            URI location = UriBuilder.fromPath("/notes")
                .queryParam("success", "created")
                .build();
            return Response.seeOther(location).build();
        } catch (WebApplicationException ex) {
            URI location = UriBuilder.fromPath("/notes/new")
                .queryParam("error", sanitizeMessage(ex.getMessage()))
                .queryParam("title", safeValue(noteRequestDTO.getTitle()))
                .queryParam("content", safeValue(noteRequestDTO.getContent()))
                .build();
            return Response.seeOther(location).build();
        }
    }

    /**
     * Exibe formulário de edição de anotação existente.
     * 
     * @param id identificador da anotação a editar
     * @param uriInfo informações da URI
     * @return template de formulário com dados da anotação
     */
    @GET
    @Path("/{id}")
    @Produces(MediaType.TEXT_HTML)
        public TemplateInstance getNote(@PathParam("id") Long id, @Context UriInfo uriInfo) {
        NoteResponseDTO note = new NoteResponseDTO(noteService.findById(id));
        var params = uriInfo.getQueryParameters();
        Map<String, String> formData = extractFormData(params, "title", "content");
            String initialTitle = resolveInitialValue(formData.get("title"), note.getTitle());
            String initialContent = resolveInitialValue(formData.get("content"), note.getContent());
        String errorMessage = params.getFirst("error");
        return noteForm
                .data("note", note)
                .data("initialTitle", initialTitle)
                .data("initialContent", initialContent)
            .data("errorMessage", errorMessage)
                .data("breadcrumb", List.of(
                        BreadcrumbItem.link("Início", "/"),
                        BreadcrumbItem.link("Minhas anotações", "/notes"),
                        BreadcrumbItem.current("Editar anotação")
                ));
    }

    /**
     * Processa atualização de anotação existente.
     * 
     * @param id identificador da anotação a atualizar
     * @param noteRequestDTO novos dados do formulário
     * @return resposta de redirecionamento
     */
    @POST
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response updateNote(@PathParam("id") Long id, @BeanParam NoteRequestDTO noteRequestDTO) {
        try {
            noteService.update(id, noteRequestDTO);
            URI location = UriBuilder.fromPath("/notes")
                .queryParam("success", "updated")
                .build();
            return Response.seeOther(location).build();
        } catch (WebApplicationException ex) {
            URI location = UriBuilder.fromPath("/notes/{id}")
                .resolveTemplate("id", id)
                .queryParam("error", sanitizeMessage(ex.getMessage()))
                .queryParam("title", safeValue(noteRequestDTO.getTitle()))
                .queryParam("content", safeValue(noteRequestDTO.getContent()))
                .build();
            return Response.seeOther(location).build();
        }
    }

    /**
     * Remove uma anotação do sistema.
     * 
     * @param id identificador da anotação a remover
     * @return resposta de redirecionamento para lista
     */
    @POST
    @Path("/{id}/delete")
    public Response deleteNote(@PathParam("id") Long id) {
        try {
            noteService.delete(id);
            URI location = UriBuilder.fromPath("/notes")
                .queryParam("success", "deleted")
                .build();
            return Response.seeOther(location).build();
        } catch (WebApplicationException ex) {
            URI location = UriBuilder.fromPath("/notes")
                .queryParam("error", sanitizeMessage(ex.getMessage()))
                .build();
            return Response.seeOther(location).build();
        }
    }

    /**
     * Converte código de sucesso em mensagem amigável.
     * 
     * @param key código da operação
     * @return mensagem de sucesso em português ou {@code null}
     */
    private String resolveNotesSuccessMessage(String key) {
        if (key == null) {
            return null;
        }
        return switch (key) {
            case "created" -> "Anotação criada com sucesso.";
            case "updated" -> "Anotação atualizada com sucesso.";
            case "deleted" -> "Anotação removida com sucesso.";
            default -> null;
        };
    }

    /**
     * Extrai dados de formulário dos query parameters.
     * 
     * @param params mapa de query parameters
     * @param keys chaves a extrair
     * @return mapa com valores encontrados
     */
    private Map<String, String> extractFormData(MultivaluedMap<String, String> params, String... keys) {
        Map<String, String> data = new HashMap<>();
        for (String key : keys) {
            String value = params.getFirst(key);
            if (value != null) {
                data.put(key, value);
            }
        }
        return data;
    }

    /**
     * Sanitiza mensagem de erro para exibição ao usuário.
     * 
     * @param message mensagem original
     * @return mensagem traduzida/sanitizada
     */
    private String sanitizeMessage(String message) {
        if (message == null || message.isBlank()) {
            return "Não foi possível concluir a operação.";
        }
        return switch (message) {
            case "Anotação não encontrada" -> message;
            case "Note not found" -> "Anotação não encontrada.";
            case "Note owner not found" -> "Proprietário da anotação não encontrado.";
            case "Você não tem permissão para alterar esta anotação." -> message;
            case "You are not allowed to modify this note" -> "Você não tem permissão para alterar esta anotação.";
            case "Título é obrigatório" -> message;
            case "Conteúdo é obrigatório" -> message;
            case "Requisição inválida" -> message;
            case "Usuário autenticado não encontrado" -> message;
            default -> message;
        };
    }

    /**
     * Retorna valor seguro para query parameter.
     * 
     * @param value valor a processar
     * @return string do valor ou vazio se nulo
     */
    private String safeValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    /**
     * Resolve valor inicial para campo de formulário.
     * 
     * @param primary valor primário (dos query params)
     * @param fallback valor fallback (do objeto existente)
     * @return valor a usar ou string vazia
     */
    private String resolveInitialValue(String primary, String fallback) {
        if (primary != null && !primary.isBlank()) {
            return primary;
        }
        return fallback != null ? fallback : "";
    }
}
