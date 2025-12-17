package com.robsonbs.controller;

import com.robsonbs.dto.UserProfileResponseDTO;
import com.robsonbs.dto.UserRequestDTO;
import com.robsonbs.dto.UserResponseDTO;
import com.robsonbs.service.UserProfileService;
import com.robsonbs.service.UserService;
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
 * Controller REST para gerenciamento de usuários do sistema.
 * 
 * <p>Este controller implementa operações CRUD completas para usuários,
 * disponibilizando tanto interface HTML (para navegadores) quanto
 * endpoint JSON (para integrações API).</p>
 * 
 * <h2>Endpoints Disponíveis</h2>
 * <table border="1">
 *   <tr><th>Método</th><th>Rota</th><th>Descrição</th></tr>
 *   <tr><td>GET</td><td>/users</td><td>Lista todos os usuários (HTML)</td></tr>
 *   <tr><td>GET</td><td>/users/new</td><td>Formulário de novo usuário</td></tr>
 *   <tr><td>POST</td><td>/users</td><td>Criar novo usuário</td></tr>
 *   <tr><td>GET</td><td>/users/{id}</td><td>Formulário de edição</td></tr>
 *   <tr><td>POST</td><td>/users/{id}</td><td>Atualizar usuário</td></tr>
 *   <tr><td>POST</td><td>/users/{id}/delete</td><td>Remover usuário</td></tr>
 *   <tr><td>GET</td><td>/users/api</td><td>Lista usuários (JSON)</td></tr>
 * </table>
 * 
 * <h2>Segurança</h2>
 * <p>Todos os endpoints (exceto /users/api) requerem role ADMIN.
 * O endpoint /users/api aceita tanto ADMIN quanto USER.</p>
 * 
 * <h2>Padrão PRG (Post-Redirect-Get)</h2>
 * <p>Todas as operações de escrita (POST) seguem o padrão PRG:</p>
 * <ol>
 *   <li>Recebem dados via formulário</li>
 *   <li>Processam a operação no service</li>
 *   <li>Redirecionam com query params de sucesso/erro</li>
 *   <li>Em caso de erro, preservam dados do formulário</li>
 * </ol>
 * 
 * <h2>Tratamento de Erros</h2>
 * <p>Quando uma operação falha (ex: email duplicado), o controller:</p>
 * <ul>
 *   <li>Captura a {@link WebApplicationException}</li>
 *   <li>Sanitiza a mensagem de erro</li>
 *   <li>Redireciona para o formulário com a mensagem e dados preservados</li>
 * </ul>
 * 
 * @author Sistema de Gerenciamento
 * @version 1.0
 * @since 1.0
 * @see UserService
 * @see UserRequestDTO
 * @see UserResponseDTO
 */
@Path("/users")
@Blocking
public class UserController {

    /**
     * Serviço de negócio para operações com usuários.
     */
    @Inject
    UserService userService;

    /**
     * Serviço de negócio para operações com perfis.
     * Usado para popular selects de perfil nos formulários.
     */
    @Inject
    UserProfileService userProfileService;

    /**
     * Template para listagem de usuários.
     * Corresponde a {@code templates/users.html}.
     */
    @Inject
    Template users;

    /**
     * Template para formulário de criação/edição.
     * Corresponde a {@code templates/userForm.html}.
     */
    @Inject
    Template userForm;

    /**
     * Lista todos os usuários cadastrados (interface HTML).
     * 
     * <p>Exibe tabela com todos os usuários e seus perfis,
     * incluindo mensagens de sucesso/erro de operações anteriores.</p>
     * 
     * @param uriInfo informações da URI com query params
     * @return template renderizado com lista de usuários
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    @RolesAllowed("ADMIN")
    public TemplateInstance listUsers(@Context UriInfo uriInfo) {
        List<UserResponseDTO> allUsers = userService.listAll().stream()
                .map(UserResponseDTO::new)
                .collect(Collectors.toList());
        var params = uriInfo.getQueryParameters();
        String successMessage = resolveUsersSuccessMessage(params.getFirst("success"));
        String errorMessage = params.getFirst("error");
        return users
                .data("users", allUsers)
                .data("successMessage", successMessage)
            .data("errorMessage", errorMessage)
                .data("breadcrumb", List.of(
                        BreadcrumbItem.link("Início", "/"),
                        BreadcrumbItem.current("Usuários")
                ));
    }

    /**
     * Exibe formulário para criação de novo usuário.
     * 
     * <p>Carrega lista de perfis para seleção e pré-popula
     * campos se dados foram preservados de submissão anterior.</p>
     * 
     * @param uriInfo informações da URI com possíveis dados preservados
     * @return template de formulário vazio ou pré-populado
     */
    @GET
    @Path("/new")
    @Produces(MediaType.TEXT_HTML)
    @RolesAllowed("ADMIN")
        public TemplateInstance newUserForm(@Context UriInfo uriInfo) {
        List<UserProfileResponseDTO> profiles = userProfileService.listForSelection();
        var params = uriInfo.getQueryParameters();
        Map<String, String> formData = extractFormData(params, "name", "email", "profileId");
        String selectedProfileId = determineSelectedProfileId(formData, null);
        boolean hasSelectedProfile = hasSelectedProfile(selectedProfileId);
        String initialName = resolveInitialValue(formData.get("name"), null);
        String initialEmail = resolveInitialValue(formData.get("email"), null);
        String errorMessage = params.getFirst("error");
        return userForm
            .data("user", null)
            .data("profiles", profiles)
            .data("initialName", initialName)
            .data("initialEmail", initialEmail)
            .data("selectedProfileId", selectedProfileId)
            .data("hasSelectedProfile", hasSelectedProfile)
            .data("errorMessage", errorMessage)
                .data("breadcrumb", List.of(
                        BreadcrumbItem.link("Início", "/"),
                        BreadcrumbItem.link("Usuários", "/users"),
                        BreadcrumbItem.current("Novo Usuário")
                ));
    }

    /**
     * Processa criação de novo usuário.
     * 
     * <p>Valida dados, persiste usuário e redireciona:
     * - Sucesso: /users?success=created
     * - Erro: /users/new?error=mensagem&amp;campos...</p>
     * 
     * @param userRequestDTO dados do formulário via @BeanParam
     * @return resposta de redirecionamento (303 See Other)
     */
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @RolesAllowed("ADMIN")
    public Response createUser(@BeanParam UserRequestDTO userRequestDTO) {
        try {
            userService.save(userRequestDTO);
            URI location = UriBuilder.fromPath("/users")
                    .queryParam("success", "created")
                    .build();
            return Response.seeOther(location).build();
        } catch (WebApplicationException ex) {
            URI location = UriBuilder.fromPath("/users/new")
                    .queryParam("error", sanitizeMessage(ex.getMessage()))
                    .queryParam("name", safeValue(userRequestDTO.getName()))
                    .queryParam("email", safeValue(userRequestDTO.getEmail()))
                    .queryParam("profileId", safeValue(userRequestDTO.getProfileId()))
                    .build();
            return Response.seeOther(location).build();
        }
    }

    /**
     * Exibe formulário de edição de usuário existente.
     * 
     * <p>Carrega dados do usuário e lista de perfis.
     * Pré-popula com dados preservados se houver erro anterior.</p>
     * 
     * @param id identificador do usuário a editar
     * @param uriInfo informações da URI com possíveis dados preservados
     * @return template de formulário com dados do usuário
     */
    @GET
    @Path("/{id}")
    @Produces(MediaType.TEXT_HTML)
    @RolesAllowed("ADMIN")
        public TemplateInstance getUser(@PathParam("id") Long id, @Context UriInfo uriInfo) {
        UserResponseDTO user = new UserResponseDTO(userService.findById(id));
        List<UserProfileResponseDTO> profiles = userProfileService.listForSelection();
        var params = uriInfo.getQueryParameters();
        Map<String, String> formData = extractFormData(params, "name", "email", "profileId");
        String selectedProfileId = determineSelectedProfileId(formData, user.getProfileId());
        boolean hasSelectedProfile = hasSelectedProfile(selectedProfileId);
        String initialName = resolveInitialValue(formData.get("name"), user.getName());
        String initialEmail = resolveInitialValue(formData.get("email"), user.getEmail());
        String errorMessage = params.getFirst("error");
        return userForm
            .data("user", user)
            .data("profiles", profiles)
            .data("initialName", initialName)
            .data("initialEmail", initialEmail)
            .data("selectedProfileId", selectedProfileId)
            .data("hasSelectedProfile", hasSelectedProfile)
            .data("errorMessage", errorMessage)
                .data("breadcrumb", List.of(
                        BreadcrumbItem.link("Início", "/"),
                        BreadcrumbItem.link("Usuários", "/users"),
                        BreadcrumbItem.current("Editar Usuário")
                ));
    }

    /**
     * Processa atualização de usuário existente.
     * 
     * <p>Valida dados, atualiza usuário e redireciona:
     * - Sucesso: /users?success=updated
     * - Erro: /users/{id}?error=mensagem&amp;campos...</p>
     * 
     * @param id identificador do usuário a atualizar
     * @param userRequestDTO novos dados do formulário
     * @return resposta de redirecionamento (303 See Other)
     */
    @POST
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @RolesAllowed("ADMIN")
    public Response updateUser(@PathParam("id") Long id, @BeanParam UserRequestDTO userRequestDTO) {
        try {
            userService.update(id, userRequestDTO);
            URI location = UriBuilder.fromPath("/users")
                    .queryParam("success", "updated")
                    .build();
            return Response.seeOther(location).build();
        } catch (WebApplicationException ex) {
            URI location = UriBuilder.fromPath("/users/{id}")
                    .resolveTemplate("id", id)
                    .queryParam("error", sanitizeMessage(ex.getMessage()))
                    .queryParam("name", safeValue(userRequestDTO.getName()))
                    .queryParam("email", safeValue(userRequestDTO.getEmail()))
                    .queryParam("profileId", safeValue(userRequestDTO.getProfileId()))
                    .build();
            return Response.seeOther(location).build();
        }
    }

    /**
     * Remove um usuário do sistema.
     * 
     * <p>Exclui o usuário e redireciona para lista com mensagem apropriada.</p>
     * 
     * @param id identificador do usuário a remover
     * @return resposta de redirecionamento para lista de usuários
     */
    @POST
    @Path("/{id}/delete")
    @RolesAllowed("ADMIN")
    public Response deleteUser(@PathParam("id") Long id) {
        try {
            userService.delete(id);
            URI location = UriBuilder.fromPath("/users")
                .queryParam("success", "deleted")
                .build();
            return Response.seeOther(location).build();
        } catch (WebApplicationException ex) {
            URI location = UriBuilder.fromPath("/users")
                .queryParam("error", sanitizeMessage(ex.getMessage()))
                .build();
            return Response.seeOther(location).build();
        }
    }

    /**
     * Lista usuários em formato JSON (endpoint API).
     * 
     * <p>Endpoint para integrações que necessitam dados de usuários
     * em formato JSON. Aceita roles ADMIN e USER.</p>
     * 
     * @return lista de usuários como JSON
     */
    @GET
    @Path("/api")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"ADMIN", "USER"})
    public List<UserResponseDTO> listUsersJson() {
        return userService.listAll().stream()
                .map(UserResponseDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Converte código de sucesso em mensagem amigável.
     * 
     * @param key código da operação (created, updated, deleted)
     * @return mensagem de sucesso em português ou {@code null}
     */
    private String resolveUsersSuccessMessage(String key) {
        if (key == null) {
            return null;
        }
        return switch (key) {
            case "created" -> "Usuário criado com sucesso.";
            case "updated" -> "Usuário atualizado com sucesso.";
            case "deleted" -> "Usuário removido com sucesso.";
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
     * Determina o ID do perfil selecionado para o formulário.
     * 
     * @param formData dados do formulário
     * @param fallbackProfileId ID padrão se não especificado
     * @return ID do perfil como string
     */
    private String determineSelectedProfileId(Map<String, String> formData, Long fallbackProfileId) {
        if (formData.containsKey("profileId")) {
            return formData.get("profileId");
        }
        return fallbackProfileId != null ? String.valueOf(fallbackProfileId) : "";
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
            case "User not found" -> "Usuário não encontrado.";
            case "Profile not found" -> "Perfil não encontrado.";
            case "Profile is required" -> "Selecione um perfil para o usuário.";
            default -> message;
        };
    }

    /**
     * Retorna valor seguro para uso em query parameter.
     * 
     * @param value valor a processar
     * @return string do valor ou vazio se nulo
     */
    private String safeValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    /**
     * Verifica se há perfil selecionado.
     * 
     * @param selectedProfileId ID do perfil selecionado
     * @return true se houver seleção válida
     */
    private boolean hasSelectedProfile(String selectedProfileId) {
        return selectedProfileId != null && !selectedProfileId.isBlank();
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
