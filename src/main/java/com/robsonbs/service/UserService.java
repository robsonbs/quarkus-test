package com.robsonbs.service;

import com.robsonbs.dao.UserDao;
import com.robsonbs.dao.UserProfileDao;
import com.robsonbs.dto.UserRequestDTO;
import com.robsonbs.model.User;
import com.robsonbs.model.UserProfile;
import com.robsonbs.service.AuditLogService;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.util.List;

@ApplicationScoped
public class UserService {

    @Inject
    UserDao userDao;

    @Inject
    UserProfileDao userProfileDao;

    @Inject
    AuditLogService auditLogService;

    @Inject
    SecurityIdentity securityIdentity;

    public List<User> listAll() {
        return userDao.listAll();
    }

    @Transactional
    public void save(UserRequestDTO userRequestDTO) {
        validatePassword(userRequestDTO.getPassword());
        ensureEmailAvailable(userRequestDTO.getEmail(), null);

        User user = new User();
        user.setName(userRequestDTO.getName());
        user.setEmail(userRequestDTO.getEmail());
        user.setPassword(BcryptUtil.bcryptHash(userRequestDTO.getPassword()));

        UserProfile profile = resolveProfile(userRequestDTO.getProfileId());
        user.setProfile(profile);

        userDao.persist(user);
        userDao.flush();

        auditLogService.recordDomainEvent(
                currentActor(),
                "USER_CREATED",
                "/users",
                "User",
                user.getId() != null ? user.getId().toString() : null,
                "Usuário criado: " + sanitize(user.getEmail()),
                Response.Status.CREATED.getStatusCode()
        );
    }

    public User findById(Long id) {
        User user = userDao.findById(id);
        if (user == null) {
            throw new NotFoundException("User not found");
        }
        return user;
    }

    @Transactional
    public void update(Long id, UserRequestDTO userRequestDTO) {
        User user = findById(id);
        if (user == null) {
            throw new NotFoundException("User not found");
        }
        user.setName(userRequestDTO.getName());
        ensureEmailAvailable(userRequestDTO.getEmail(), id);
        user.setEmail(userRequestDTO.getEmail());
        if (userRequestDTO.getPassword() != null && !userRequestDTO.getPassword().isEmpty()) {
            validatePassword(userRequestDTO.getPassword());
            user.setPassword(BcryptUtil.bcryptHash(userRequestDTO.getPassword()));
        }

        UserProfile profile = resolveProfile(userRequestDTO.getProfileId());
        user.setProfile(profile);

        userDao.persist(user);
        auditLogService.recordDomainEvent(
                currentActor(),
                "USER_UPDATED",
                "/users/" + id,
                "User",
                user.getId() != null ? user.getId().toString() : null,
                "Usuário atualizado: " + sanitize(user.getEmail()),
                Response.Status.OK.getStatusCode()
        );
    }

    @Transactional
    public void delete(Long id) {
        boolean removed = userDao.deleteById(id);
        if (!removed) {
            throw new NotFoundException("User not found");
        }

        auditLogService.recordDomainEvent(
                currentActor(),
                "USER_DELETED",
                "/users/" + id + "/delete",
                "User",
                id != null ? id.toString() : null,
                "Usuário removido",
                Response.Status.OK.getStatusCode()
        );
    }

    private UserProfile resolveProfile(Long profileId) {
        if (profileId == null) {
            throw new BadRequestException("Profile is required");
        }
        UserProfile profile = userProfileDao.findById(profileId);
        if (profile == null) {
            throw new NotFoundException("Profile not found");
        }
        return profile;
    }

    private void ensureEmailAvailable(String email, Long idToIgnore) {
        userDao.findByEmail(email).ifPresent(existing -> {
            if (idToIgnore == null || !existing.getId().equals(idToIgnore)) {
                throw new WebApplicationException("Email já está em uso", Response.Status.CONFLICT);
            }
        });
    }

    private void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new BadRequestException("Senha é obrigatória");
        }
        if (password.length() < 6) {
            throw new BadRequestException("Senha deve ter pelo menos 6 caracteres");
        }
    }

    private String currentActor() {
        if (securityIdentity != null && !securityIdentity.isAnonymous() && securityIdentity.getPrincipal() != null) {
            return securityIdentity.getPrincipal().getName();
        }
        return "system";
    }

    private String sanitize(String value) {
        if (value == null) {
            return "";
        }
        return value.length() > 120 ? value.substring(0, 120) : value;
    }
}
