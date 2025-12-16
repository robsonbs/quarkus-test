package com.robsonbs.service;

import com.robsonbs.dao.UserDao;
import com.robsonbs.dao.UserProfileDao;
import com.robsonbs.dto.UserProfileRequestDTO;
import com.robsonbs.dto.UserProfileResponseDTO;
import com.robsonbs.model.UserProfile;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import com.robsonbs.service.AuditLogService;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class UserProfileService {

    @Inject
    UserProfileDao userProfileDao;

    @Inject
    UserDao userDao;

    @Inject
    AuditLogService auditLogService;

    @Inject
    SecurityIdentity securityIdentity;

    public List<UserProfileResponseDTO> listAll() {
        return userProfileDao.listAll().stream()
                .map(profile -> new UserProfileResponseDTO(profile, countUsers(profile)))
                .collect(Collectors.toList());
    }

    public List<UserProfileResponseDTO> listAllOrdered() {
        return userProfileDao.listAll().stream()
            .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
                .map(profile -> new UserProfileResponseDTO(profile, countUsers(profile)))
                .collect(Collectors.toList());
    }

    @Transactional
    public void create(UserProfileRequestDTO requestDTO) {
        String name = sanitizeName(requestDTO.getName());
        ensureNameAvailable(name, null);

        UserProfile profile = new UserProfile();
        profile.setName(name);
        userProfileDao.persist(profile);

        auditLogService.recordDomainEvent(
            currentActor(),
            "PROFILE_CREATED",
            "/profiles",
            "UserProfile",
            profile.getId() != null ? profile.getId().toString() : null,
            "Perfil criado: " + name,
            Response.Status.CREATED.getStatusCode()
        );
    }

    public UserProfile findEntityById(Long id) {
        UserProfile profile = userProfileDao.findById(id);
        if (profile == null) {
            throw new NotFoundException("Perfil não encontrado");
        }
        return profile;
    }

    public UserProfileResponseDTO findById(Long id) {
        UserProfile profile = findEntityById(id);
        return new UserProfileResponseDTO(profile, countUsers(profile));
    }

    @Transactional
    public void update(Long id, UserProfileRequestDTO requestDTO) {
        UserProfile profile = findEntityById(id);
        String name = sanitizeName(requestDTO.getName());
        ensureNameAvailable(name, id);
        profile.setName(name);

        auditLogService.recordDomainEvent(
            currentActor(),
            "PROFILE_UPDATED",
            "/profiles/" + id,
            "UserProfile",
            profile.getId() != null ? profile.getId().toString() : null,
            "Perfil atualizado: " + name,
            Response.Status.OK.getStatusCode()
        );
    }

    @Transactional
    public void delete(Long id) {
        UserProfile profile = findEntityById(id);
        long usage = countUsers(profile);
        if (usage > 0) {
            throw new WebApplicationException("Não é possível remover perfis com usuários associados", Response.Status.CONFLICT);
        }
        userProfileDao.delete(profile);

        auditLogService.recordDomainEvent(
                currentActor(),
                "PROFILE_DELETED",
                "/profiles/" + id + "/delete",
                "UserProfile",
                id != null ? id.toString() : null,
                "Perfil removido",
                Response.Status.OK.getStatusCode()
        );
    }

    public List<UserProfileResponseDTO> listForSelection() {
        return listAllOrdered();
    }

    private long countUsers(UserProfile profile) {
        return userDao.count("profile", profile);
    }

    private void ensureNameAvailable(String name, Long id) {
        userProfileDao.findByName(name).ifPresent(existing -> {
            if (id == null || !existing.getId().equals(id)) {
                throw new WebApplicationException("Já existe um perfil com esse nome", Response.Status.CONFLICT);
            }
        });
    }

    private String sanitizeName(String name) {
        if (name == null || name.isBlank()) {
            throw new BadRequestException("Nome do perfil é obrigatório");
        }
        return name.trim().toUpperCase();
    }

    private String currentActor() {
        if (securityIdentity != null && !securityIdentity.isAnonymous() && securityIdentity.getPrincipal() != null) {
            return securityIdentity.getPrincipal().getName();
        }
        return "system";
    }
}
