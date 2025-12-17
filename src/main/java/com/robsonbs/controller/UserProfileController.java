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

import java.net.URI;
import java.util.List;

/**
 * Controller REST para gerenciamento de perfis de usuário.
 * 
 * <p>Este controller implementa operações CRUD para perfis de usuário,
 * que definem roles e permissões no sistema. Perfis são usados para
 * controle de acesso baseado em papéis (RBAC).</p>
 * 
 * <h2>Endpoints Disponíveis</h2>
 * <table border="1">
 *   <tr><th>Método</th><th>Rota</th><th>Descrição</th></tr>
 *   <tr><td>GET</td><td>/profiles</td><td>Lista todos os perfis</td></tr>
 *   <tr><td>GET</td><td>/profiles/new</td><td>Formulário de novo perfil</td></tr>
 *   <tr><td>POST</td><td>/profiles</td><td>Criar novo perfil</td></tr>
 *   <tr><td>GET</td><td>/profiles/{id}</td><td>Formulário de edição</td></tr>
 *   <tr><td>POST</td><td>/profiles/{id}</td><td>Atualizar perfil</td></tr>
 *   <tr><td>POST</td><td>/profiles/{id}/delete</td><td>Remover perfil</td></tr>
 * </table>
 * 
 * <h2>Segurança</h2>
 * <p>Todos os endpoints requerem role ADMIN. Apenas administradores
 * podem gerenciar perfis do sistema.</p>
 * 
 * <h2>Validações</h2>
 * <ul>
 *   <li>Nome do perfil é obrigatório</li>
 *   <li>Nome é normalizado para MAIÚSCULAS</li>
 *   <li>Nomes devem ser únicos</li>
 *   <li>Perfis com usuários associados não podem ser excluídos</li>
 * </ul>
 * 
 * <h2>Padrão PRG</h2>
 * <p>Segue o padrão Post-Redirect-Get para todas as operações de escrita,
 * redirecionando com query params de sucesso/erro.</p>
 * 
 * @author Sistema de Gerenciamento
 * @version 1.0
 * @since 1.0
 * @see UserProfileService
 * @see UserProfileRequestDTO
 * @see UserProfileResponseDTO
 */
@Path("/profiles")
@RolesAllowed("ADMIN")
@Blocking
public class UserProfileController {

    /**
     * Serviço de negócio para operações com perfis.
     */
    @Inject
    UserProfileService userProfileService;

    /**
     * Template para listagem de perfis.
     * Corresponde a {@code templates/profiles.html}.
     */
    @Inject
    Template profiles;

    /**
     * Template para formulário de criação/edição.
     * Corresponde a {@code templates/profileForm.html}.
     */
    @Inject
    Template profileForm;

    /**
     * Lista todos os perfis cadastrados.
     * 
     * <p>Exibe tabela com todos os perfis, incluindo contagem
     * de usuários associados a cada um.</p>
     * 
     * @param uriInfo informações da URI com query params
     * @return template renderizado com lista de perfis
     */
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

    /**
     * Exibe formulário para criação de novo perfil.
     * 
     * @param uriInfo informações da URI com possíveis dados preservados
     * @return template de formulário vazio ou pré-populado
     */
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

    /**
     * Processa criação de novo perfil.
     * 
     * @param requestDTO dados do formulário
     * @return resposta de redirecionamento
     */
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response createProfile(@Valid @BeanParam UserProfileRequestDTO requestDTO) {
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

    /**
     * Exibe formulário de edição de perfil existente.
     * 
     * @param id identificador do perfil a editar
     * @param uriInfo informações da URI
     * @return template de formulário com dados do perfil
     */
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

    /**
     * Processa atualização de perfil existente.
     * 
     * @param id identificador do perfil a atualizar
     * @param requestDTO novos dados do formulário
     * @return resposta de redirecionamento
     */
    @POST
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response updateProfile(@PathParam("id") Long id, @Valid @BeanParam UserProfileRequestDTO requestDTO) {
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

    /**
     * Remove um perfil do sistema.
     * 
     * <p>Falha se houver usuários associados ao perfil.</p>
     * 
     * @param id identificador do perfil a remover
     * @return resposta de redirecionamento para lista
     */
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

    /**
     * Converte código de sucesso em mensagem amigável.
     * 
     * @param key código da operação
     * @return mensagem de sucesso em português ou {@code null}
     */
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

    /**
     * Sanitiza mensagem de erro para exibição.
     * 
     * @param message mensagem original
     * @return mensagem sanitizada
     */
    private String sanitizeMessage(String message) {
        return message == null ? "Operação não pôde ser concluída." : message;
    }

    /**
     * Retorna valor seguro para query parameter.
     * 
     * @param value valor a processar
     * @return string do valor ou vazio se nulo
     */
    private String safeValue(String value) {
        return value == null ? "" : value;
    }
}
