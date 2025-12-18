package com.robsonbs.service;

import com.robsonbs.dao.AuditLogDao;
import com.robsonbs.dao.UserDao;
import com.robsonbs.dto.UserProfileResponseDTO;
import com.robsonbs.dto.UserRequestDTO;
import com.robsonbs.model.AuditLog;
import com.robsonbs.model.User;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class UserServiceTest {

    @Inject
    UserService userService;

    @Inject
    UserProfileService userProfileService;

    @Inject
    UserDao userDao;

    @Inject
    AuditLogDao auditLogDao;

    @Test
    @TestSecurity(user = "admin@example.com", roles = {"ADMIN"})
    @Transactional
    void shouldRecordAuditTrailForUserLifecycle() {
        Long profileId = userProfileService.listForSelection().stream()
                .filter(profile -> "USER".equalsIgnoreCase(profile.getName()))
                .map(UserProfileResponseDTO::getId)
                .findFirst()
                .orElseThrow();

        String email = "teste." + UUID.randomUUID() + "@example.com";

        UserRequestDTO createDto = new UserRequestDTO();
        createDto.setName("Usuário de Teste");
        createDto.setEmail(email);
        createDto.setPassword("senha123");
        createDto.setProfileId(profileId);

        userService.save(createDto);

        User persisted = userDao.findByEmail(email).orElseThrow();
        assertNotNull(persisted.getId());

        Optional<AuditLog> creationLog = auditLogDao.find("entityType = ?1 and entityId = ?2 and action = ?3",
                "User",
                String.valueOf(persisted.getId()),
                "USER_CREATED").firstResultOptional();
        assertTrue(creationLog.isPresent());

        UserRequestDTO updateDto = new UserRequestDTO();
        updateDto.setName("Usuário Atualizado");
        updateDto.setEmail(email);
        updateDto.setProfileId(profileId);
        updateDto.setPassword("senha456");

        userService.update(persisted.getId(), updateDto);

        Optional<AuditLog> updateLog = auditLogDao.find("entityType = ?1 and entityId = ?2 and action = ?3",
                "User",
                String.valueOf(persisted.getId()),
                "USER_UPDATED").firstResultOptional();
        assertTrue(updateLog.isPresent());

        userService.delete(persisted.getId());

        Optional<AuditLog> deleteLog = auditLogDao.find("entityType = ?1 and entityId = ?2 and action = ?3",
                "User",
                String.valueOf(persisted.getId()),
                "USER_DELETED").firstResultOptional();
        assertTrue(deleteLog.isPresent());
    }
}
