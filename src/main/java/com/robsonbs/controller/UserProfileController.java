package com.robsonbs.controller;

import com.robsonbs.dto.UserProfileRequestDTO;
import com.robsonbs.dto.UserProfileResponseDTO;
import com.robsonbs.service.UserProfileService;
import com.robsonbs.view.BreadcrumbItem;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.smallrye.common.annotation.Blocking;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
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

import java.net.URI;
import java.util.List;

@Path("/profiles")
@RolesAllowed("ADMIN")
@Blocking
public class UserProfileController {

    @Inject
    UserProfileService userProfileService;

    @Inject
    Template profiles;

    @Inject
    Template profileForm;

    @GET
    @Produces(MediaType.TEXT_HTML)
        public TemplateInstance listProfiles(@Context UriInfo uriInfo) {
        List<UserProfileResponseDTO> items = userProfileService.listAllOrdered();
        String successMessage = resolveProfilesSuccessMessage(uriInfo.getQueryParameters().getFirst("success"));
        String errorMessage = uriInfo.getQueryParameters().getFirst("error");
        return profiles
                .data("profiles", items)
                .data("successMessage", successMessage)
            .data("errorMessage", errorMessage)
                .data("breadcrumb", List.of(
                        BreadcrumbItem.link("Início", "/"),
                        BreadcrumbItem.current("Perfis")
                ));
    }

    @GET
    @Path("/new")
    @Produces(MediaType.TEXT_HTML)
        public TemplateInstance newProfileForm(@Context UriInfo uriInfo) {
        String errorMessage = uriInfo.getQueryParameters().getFirst("error");
        String namePrefill = uriInfo.getQueryParameters().getFirst("name");
        return profileForm
                .data("profile", null)
            .data("formName", namePrefill)
            .data("errorMessage", errorMessage)
                .data("breadcrumb", List.of(
                        BreadcrumbItem.link("Início", "/"),
                        BreadcrumbItem.link("Perfis", "/profiles"),
                        BreadcrumbItem.current("Novo Perfil")
                ));
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response createProfile(@Valid UserProfileRequestDTO requestDTO) {
        try {
            userProfileService.create(requestDTO);
            URI location = UriBuilder.fromPath("/profiles")
                .queryParam("success", "created")
                .build();
            return Response.seeOther(location).build();
        } catch (WebApplicationException ex) {
            URI location = UriBuilder.fromPath("/profiles/new")
                .queryParam("error", sanitizeMessage(ex.getMessage()))
                .queryParam("name", safeValue(requestDTO.getName()))
                .build();
            return Response.seeOther(location).build();
        }
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.TEXT_HTML)
        public TemplateInstance editProfileForm(@PathParam("id") Long id, @Context UriInfo uriInfo) {
        UserProfileResponseDTO profile = userProfileService.findById(id);
        String errorMessage = uriInfo.getQueryParameters().getFirst("error");
        String namePrefill = uriInfo.getQueryParameters().getFirst("name");
        return profileForm
                .data("profile", profile)
            .data("formName", namePrefill != null ? namePrefill : profile.getName())
            .data("errorMessage", errorMessage)
                .data("breadcrumb", List.of(
                        BreadcrumbItem.link("Início", "/"),
                        BreadcrumbItem.link("Perfis", "/profiles"),
                        BreadcrumbItem.current("Editar Perfil")
                ));
    }

    @POST
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response updateProfile(@PathParam("id") Long id, @Valid UserProfileRequestDTO requestDTO) {
        try {
            userProfileService.update(id, requestDTO);
            URI location = UriBuilder.fromPath("/profiles")
                    .queryParam("success", "updated")
                    .build();
            return Response.seeOther(location).build();
        } catch (WebApplicationException ex) {
            URI location = UriBuilder.fromPath("/profiles/{id}")
                    .resolveTemplate("id", id)
                    .queryParam("error", sanitizeMessage(ex.getMessage()))
                .queryParam("name", safeValue(requestDTO.getName()))
                    .build();
            return Response.seeOther(location).build();
        }
    }

    @POST
    @Path("/{id}/delete")
    public Response deleteProfile(@PathParam("id") Long id) {
        try {
            userProfileService.delete(id);
            URI location = UriBuilder.fromPath("/profiles")
                    .queryParam("success", "deleted")
                    .build();
            return Response.seeOther(location).build();
        } catch (WebApplicationException ex) {
            URI location = UriBuilder.fromPath("/profiles")
                    .queryParam("error", sanitizeMessage(ex.getMessage()))
                    .build();
            return Response.seeOther(location).build();
        }
    }

    private String resolveProfilesSuccessMessage(String key) {
        if (key == null) {
            return null;
        }
        return switch (key) {
            case "created" -> "Perfil cadastrado com sucesso.";
            case "updated" -> "Perfil atualizado com sucesso.";
            case "deleted" -> "Perfil removido com sucesso.";
            default -> null;
        };
    }

    private String sanitizeMessage(String message) {
        return message == null ? "Operação não pôde ser concluída." : message;
    }

    private String safeValue(String value) {
        return value == null ? "" : value;
    }
}
