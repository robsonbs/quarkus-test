package com.robsonbs;

import com.robsonbs.dao.AuditLogDao;
import com.robsonbs.dao.UserDao;
import com.robsonbs.dao.UserProfileDao;
import com.robsonbs.dto.UserRequestDTO;
import com.robsonbs.model.AuditLog;
import com.robsonbs.model.User;
import com.robsonbs.model.UserProfile;
import com.robsonbs.service.UserService;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

@QuarkusTest
class UserServiceTest {

    @Inject
    UserService userService;

    @Inject
    UserDao userDao;

    @Inject
    UserProfileDao userProfileDao;

    @Inject
    AuditLogDao auditLogDao;

    @Test
    @TestSecurity(user = "admin@example.com", roles = {"ADMIN"})
    void shouldPersistUserAndRecordAudit() {
        String email = randomEmail();
        UserRequestDTO dto = new UserRequestDTO();
        dto.setName("Usuário QA");
        dto.setEmail(email);
        dto.setPassword("123456");
        dto.setProfileId(resolveProfileId("USER"));

        userService.save(dto);

        User saved = userDao.findByEmail(email)
                .orElseThrow(() -> new AssertionError("Usuário não foi persistido"));

        AuditLog auditLog = auditLogDao.find("entityType = ?1 and entityId = ?2 order by occurredAt desc",
                "User", saved.getId().toString()).firstResult();

        Assertions.assertNotNull(auditLog, "Registro de auditoria não encontrado");
        Assertions.assertEquals("USER_CREATED", auditLog.getAction());
        Assertions.assertEquals("/users", auditLog.getResourcePath());

        userService.delete(saved.getId());
    }

    @Test
    @TestSecurity(user = "admin@example.com", roles = {"ADMIN"})
    void shouldRejectDuplicateEmail() {
        UserRequestDTO dto = new UserRequestDTO();
        dto.setName("Duplicado");
        dto.setEmail("admin@example.com");
        dto.setPassword("123456");
        dto.setProfileId(resolveProfileId("ADMIN"));

        WebApplicationException exception = Assertions.assertThrows(WebApplicationException.class,
                () -> userService.save(dto));

        Assertions.assertEquals(Response.Status.CONFLICT.getStatusCode(), exception.getResponse().getStatus());
    }

    private Long resolveProfileId(String profileName) {
        Optional<UserProfile> profile = userProfileDao.findByName(profileName);
        return profile.map(UserProfile::getId)
                .orElseThrow(() -> new IllegalStateException("Perfil não encontrado: " + profileName));
    }

    private String randomEmail() {
        return "qa-user-" + UUID.randomUUID() + "@example.com";
    }
}
