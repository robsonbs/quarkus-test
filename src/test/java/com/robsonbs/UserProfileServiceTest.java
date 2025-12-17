package com.robsonbs;

import com.robsonbs.dao.AuditLogDao;
import com.robsonbs.dao.UserProfileDao;
import com.robsonbs.dto.UserProfileRequestDTO;
import com.robsonbs.model.AuditLog;
import com.robsonbs.model.UserProfile;
import com.robsonbs.service.UserProfileService;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

@QuarkusTest
class UserProfileServiceTest {

    @Inject
    UserProfileService userProfileService;

    @Inject
    UserProfileDao userProfileDao;

    @Inject
    AuditLogDao auditLogDao;

    @Test
    @TestSecurity(user = "admin@example.com", roles = {"ADMIN"})
    void shouldCreateProfileAndRecordAudit() {
        String requestedName = "perfil suporte " + UUID.randomUUID();
        UserProfileRequestDTO dto = new UserProfileRequestDTO();
        dto.setName(requestedName);

        userProfileService.create(dto);

        String persistedName = requestedName.trim().toUpperCase();
        UserProfile profile = userProfileDao.findByName(persistedName)
                .orElseThrow(() -> new AssertionError("Perfil não foi persistido"));

        AuditLog auditLog = auditLogDao.find("entityType = ?1 and entityId = ?2 order by occurredAt desc",
                "UserProfile", profile.getId().toString()).firstResult();

        Assertions.assertNotNull(auditLog, "Registro de auditoria não encontrado");
        Assertions.assertEquals("PROFILE_CREATED", auditLog.getAction());
        Assertions.assertEquals("/profiles", auditLog.getResourcePath());

        userProfileService.delete(profile.getId());
    }

    @Test
    @TestSecurity(user = "admin@example.com", roles = {"ADMIN"})
    void shouldRejectDeletionWhenProfileInUse() {
        UserProfile adminProfile = userProfileDao.findByName("ADMIN")
                .orElseThrow(() -> new AssertionError("Perfil ADMIN não encontrado"));

        WebApplicationException exception = Assertions.assertThrows(WebApplicationException.class,
                () -> userProfileService.delete(adminProfile.getId()));

        Assertions.assertEquals(Response.Status.CONFLICT.getStatusCode(), exception.getResponse().getStatus());
    }
}
