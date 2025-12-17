package com.robsonbs.controller;

import com.robsonbs.dto.TaskRequestDTO;
import com.robsonbs.dto.TaskResponseDTO;
import com.robsonbs.model.TaskStatus;
import com.robsonbs.service.TaskService;
import com.robsonbs.view.BreadcrumbItem;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
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
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;

import io.smallrye.common.annotation.Blocking;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Path("/tasks")
@RolesAllowed({"USER", "ADMIN"})
@Blocking
public class TaskController {

    @Inject
    TaskService taskService;

    @Inject
    Template tasks;

    @Inject
    Template taskForm;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance listTasks(@Context UriInfo uriInfo) {
        List<TaskResponseDTO> userTasks = taskService.findByOwner().stream()
                .map(TaskResponseDTO::new)
                .collect(Collectors.toList());
        var params = uriInfo.getQueryParameters();
        String successMessage = resolveSuccessMessage(params.getFirst("success"));
        String errorMessage = params.getFirst("error");
        return tasks
                .data("tasks", userTasks)
                .data("successMessage", successMessage)
                .data("errorMessage", errorMessage)
                .data("statuses", TaskStatus.values())
                .data("breadcrumb", List.of(
                        BreadcrumbItem.link("Início", "/"),
                        BreadcrumbItem.current("Minhas tarefas")
                ));
    }

    @GET
    @Path("/new")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance newTaskForm(@Context UriInfo uriInfo) {
        var params = uriInfo.getQueryParameters();
        Map<String, String> formData = extractFormData(params, "title", "description", "dueDate", "status");
        return taskForm
                .data("task", null)
                .data("initialTitle", resolveInitialValue(formData.get("title"), null))
                .data("initialDescription", resolveInitialValue(formData.get("description"), null))
                .data("initialDueDate", resolveInitialValue(formData.get("dueDate"), null))
                .data("initialStatus", resolveInitialValue(formData.get("status"), TaskStatus.PENDING.name()))
                .data("errorMessage", params.getFirst("error"))
                .data("statuses", TaskStatus.values())
                .data("breadcrumb", List.of(
                        BreadcrumbItem.link("Início", "/"),
                        BreadcrumbItem.link("Minhas tarefas", "/tasks"),
                        BreadcrumbItem.current("Nova tarefa")
                ));
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response createTask(@BeanParam TaskRequestDTO taskRequestDTO) {
        try {
            taskService.create(taskRequestDTO);
            URI location = UriBuilder.fromPath("/tasks")
                    .queryParam("success", "created")
                    .build();
            return Response.seeOther(location).build();
        } catch (WebApplicationException ex) {
            URI location = UriBuilder.fromPath("/tasks/new")
                    .queryParam("error", sanitizeMessage(ex.getMessage()))
                    .queryParam("title", safeValue(taskRequestDTO.getTitle()))
                    .queryParam("description", safeValue(taskRequestDTO.getDescription()))
                    .queryParam("dueDate", safeValue(taskRequestDTO.getDueDate()))
                    .queryParam("status", safeValue(taskRequestDTO.getStatus()))
                    .build();
            return Response.seeOther(location).build();
        }
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance editTask(@PathParam("id") Long id, @Context UriInfo uriInfo) {
        TaskResponseDTO task = new TaskResponseDTO(taskService.findById(id));
        var params = uriInfo.getQueryParameters();
        Map<String, String> formData = extractFormData(params, "title", "description", "dueDate", "status");
        return taskForm
                .data("task", task)
                .data("initialTitle", resolveInitialValue(formData.get("title"), task.getTitle()))
                .data("initialDescription", resolveInitialValue(formData.get("description"), task.getDescription()))
                .data("initialDueDate", resolveInitialValue(formData.get("dueDate"), task.getDueDate() != null ? task.getDueDate().toString() : ""))
                .data("initialStatus", resolveInitialValue(formData.get("status"), task.getStatus().name()))
                .data("errorMessage", params.getFirst("error"))
                .data("statuses", TaskStatus.values())
                .data("breadcrumb", List.of(
                        BreadcrumbItem.link("Início", "/"),
                        BreadcrumbItem.link("Minhas tarefas", "/tasks"),
                        BreadcrumbItem.current("Editar tarefa")
                ));
    }

    @POST
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response updateTask(@PathParam("id") Long id, @BeanParam TaskRequestDTO taskRequestDTO) {
        try {
            taskService.update(id, taskRequestDTO);
            URI location = UriBuilder.fromPath("/tasks")
                    .queryParam("success", "updated")
                    .build();
            return Response.seeOther(location).build();
        } catch (WebApplicationException ex) {
            URI location = UriBuilder.fromPath("/tasks/{id}")
                    .resolveTemplate("id", id)
                    .queryParam("error", sanitizeMessage(ex.getMessage()))
                    .queryParam("title", safeValue(taskRequestDTO.getTitle()))
                    .queryParam("description", safeValue(taskRequestDTO.getDescription()))
                    .queryParam("dueDate", safeValue(taskRequestDTO.getDueDate()))
                    .queryParam("status", safeValue(taskRequestDTO.getStatus()))
                    .build();
            return Response.seeOther(location).build();
        }
    }

    @POST
    @Path("/{id}/delete")
    public Response deleteTask(@PathParam("id") Long id) {
        try {
            taskService.delete(id);
            URI location = UriBuilder.fromPath("/tasks")
                    .queryParam("success", "deleted")
                    .build();
            return Response.seeOther(location).build();
        } catch (WebApplicationException ex) {
            URI location = UriBuilder.fromPath("/tasks")
                    .queryParam("error", sanitizeMessage(ex.getMessage()))
                    .build();
            return Response.seeOther(location).build();
        }
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

    private String resolveInitialValue(String primary, String fallback) {
        if (primary != null && !primary.isBlank()) {
            return primary;
        }
        return fallback != null ? fallback : "";
    }

    private String resolveSuccessMessage(String key) {
        if (key == null) {
            return null;
        }
        return switch (key) {
            case "created" -> "Tarefa criada com sucesso.";
            case "updated" -> "Tarefa atualizada com sucesso.";
            case "deleted" -> "Tarefa removida com sucesso.";
            default -> null;
        };
    }

    private String sanitizeMessage(String message) {
        if (message == null || message.isBlank()) {
            return "Não foi possível concluir a operação.";
        }
        return message;
    }

    private String safeValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
