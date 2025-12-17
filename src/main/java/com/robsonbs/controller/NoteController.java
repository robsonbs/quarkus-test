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

@Path("/notes")
@RolesAllowed({"USER", "ADMIN"})
@Blocking
public class NoteController {

    @Inject
    NoteService noteService;

    @Inject
    Template notes;

    @Inject
    Template noteForm;

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

    private String safeValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private String resolveInitialValue(String primary, String fallback) {
        if (primary != null && !primary.isBlank()) {
            return primary;
        }
        return fallback != null ? fallback : "";
    }
}
