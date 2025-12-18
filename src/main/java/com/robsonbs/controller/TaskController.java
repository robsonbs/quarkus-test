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
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller REST para gerenciamento de tarefas pessoais.
 * 
 * <p>Este controller implementa operações CRUD para tarefas ({@link com.robsonbs.model.Task}),
 * permitindo que usuários gerenciem suas tarefas pessoais com workflow de status,
 * datas de entrega e descrições. Cada usuário só tem acesso às suas próprias tarefas.</p>
 * 
 * <h2>Endpoints Disponíveis</h2>
 * <table border="1">
 *   <tr><th>Método</th><th>Rota</th><th>Descrição</th></tr>
 *   <tr><td>GET</td><td>/tasks</td><td>Lista tarefas do usuário</td></tr>
 *   <tr><td>GET</td><td>/tasks/new</td><td>Formulário de nova tarefa</td></tr>
 *   <tr><td>POST</td><td>/tasks</td><td>Criar nova tarefa</td></tr>
 *   <tr><td>GET</td><td>/tasks/{id}</td><td>Formulário de edição</td></tr>
 *   <tr><td>POST</td><td>/tasks/{id}</td><td>Atualizar tarefa</td></tr>
 *   <tr><td>POST</td><td>/tasks/{id}/delete</td><td>Remover tarefa</td></tr>
 * </table>
 * 
 * <h2>Workflow de Status</h2>
 * <p>As tarefas suportam os seguintes status via {@link TaskStatus}:</p>
 * <ul>
 *   <li>{@code PENDING} - Aguardando início (padrão)</li>
 *   <li>{@code IN_PROGRESS} - Em execução</li>
 *   <li>{@code COMPLETED} - Finalizada</li>
 *   <li>{@code CANCELLED} - Cancelada</li>
 * </ul>
 * 
 * <h2>Segurança</h2>
 * <ul>
 *   <li>Requer autenticação (roles USER ou ADMIN)</li>
 *   <li>Usuários só acessam suas próprias tarefas</li>
 *   <li>Tentativas de acesso a tarefas de outros resultam em 403</li>
 * </ul>
 * 
 * <h2>Validações</h2>
 * <ul>
 *   <li>Título é obrigatório</li>
 *   <li>Data de entrega não pode ser no passado</li>
 *   <li>Status deve ser válido</li>
 * </ul>
 * 
 * @author Sistema de Gerenciamento
 * @version 1.0
 * @since 1.0
 * @see TaskService
 * @see TaskRequestDTO
 * @see TaskResponseDTO
 * @see TaskStatus
 */
@Path("/tasks")
@RolesAllowed({"USER", "ADMIN"})
@Blocking
@Tag(name = "Tarefas", description = "Gerenciamento de tarefas pessoais com workflow de status")
public class TaskController {

    /**
     * Serviço de negócio para operações com tarefas.
     */
    @Inject
    TaskService taskService;

    /**
     * Template para listagem de tarefas.
     * Corresponde a {@code templates/tasks.html}.
     */
    @Inject
    Template tasks;

    /**
     * Template para formulário de criação/edição.
     * Corresponde a {@code templates/taskForm.html}.
     */
    @Inject
    Template taskForm;

    /**
     * Lista todas as tarefas do usuário autenticado.
     * 
     * <p>Inclui lista de status disponíveis para filtros na interface.</p>
     * 
     * @param uriInfo informações da URI com query params
     * @return template renderizado com lista de tarefas
     */
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

    /**
     * Exibe formulário para criação de nova tarefa.
     * 
     * <p>Pré-seleciona status PENDING como padrão.</p>
     * 
     * @param uriInfo informações da URI com possíveis dados preservados
     * @return template de formulário
     */
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

    /**
     * Processa criação de nova tarefa.
     * 
     * @param taskRequestDTO dados do formulário
     * @return resposta de redirecionamento
     */
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

    /**
     * Exibe formulário de edição de tarefa existente.
     * 
     * @param id identificador da tarefa a editar
     * @param uriInfo informações da URI
     * @return template de formulário com dados da tarefa
     */
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

    /**
     * Processa atualização de tarefa existente.
     * 
     * @param id identificador da tarefa a atualizar
     * @param taskRequestDTO novos dados do formulário
     * @return resposta de redirecionamento
     */
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

    /**
     * Remove uma tarefa do sistema.
     * 
     * @param id identificador da tarefa a remover
     * @return resposta de redirecionamento para lista
     */
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

    /**
     * Converte código de sucesso em mensagem amigável.
     * 
     * @param key código da operação
     * @return mensagem de sucesso em português ou {@code null}
     */
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

    /**
     * Sanitiza mensagem de erro para exibição.
     * 
     * @param message mensagem original
     * @return mensagem sanitizada
     */
    private String sanitizeMessage(String message) {
        if (message == null || message.isBlank()) {
            return "Não foi possível concluir a operação.";
        }
        return message;
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
}
