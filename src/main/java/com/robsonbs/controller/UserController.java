package com.robsonbs.controller;

import com.robsonbs.dto.UserProfileResponseDTO;
import com.robsonbs.dto.UserRequestDTO;
import com.robsonbs.dto.UserResponseDTO;
import com.robsonbs.service.UserProfileService;
import com.robsonbs.service.UserService;
import com.robsonbs.view.BreadcrumbItem;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
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

@Path("/users")
public class UserController {

    @Inject
    UserService userService;

    @Inject
    UserProfileService userProfileService;

    @Inject
    Template users;

    @Inject
    Template userForm;

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

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @RolesAllowed("ADMIN")
    public Response createUser(UserRequestDTO userRequestDTO) {
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

    @POST
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @RolesAllowed("ADMIN")
    public Response updateUser(@PathParam("id") Long id, UserRequestDTO userRequestDTO) {
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

    @GET
    @Path("/api")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"ADMIN", "USER"})
    public List<UserResponseDTO> listUsersJson() {
        return userService.listAll().stream()
                .map(UserResponseDTO::new)
                .collect(Collectors.toList());
    }

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

    private String determineSelectedProfileId(Map<String, String> formData, Long fallbackProfileId) {
        if (formData.containsKey("profileId")) {
            return formData.get("profileId");
        }
        return fallbackProfileId != null ? String.valueOf(fallbackProfileId) : "";
    }

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

    private String safeValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private boolean hasSelectedProfile(String selectedProfileId) {
        return selectedProfileId != null && !selectedProfileId.isBlank();
    }

    private String resolveInitialValue(String primary, String fallback) {
        if (primary != null && !primary.isBlank()) {
            return primary;
        }
        return fallback != null ? fallback : "";
    }
}
