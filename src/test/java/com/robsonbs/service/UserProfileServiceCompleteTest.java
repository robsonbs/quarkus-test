package com.robsonbs.service;

import com.robsonbs.dao.AuditLogDao;
import com.robsonbs.dao.UserDao;
import com.robsonbs.dao.UserProfileDao;
import com.robsonbs.dto.UserProfileRequestDTO;
import com.robsonbs.dto.UserProfileResponseDTO;
import com.robsonbs.dto.UserRequestDTO;
import com.robsonbs.model.AuditLog;
import com.robsonbs.model.UserProfile;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes completos para UserProfileService cobrindo todos os fluxos.
 */
@QuarkusTest
class UserProfileServiceCompleteTest {

    @Inject
    UserProfileService userProfileService;

    @Inject
    UserProfileDao userProfileDao;

    @Inject
    UserDao userDao;

    @Inject
    UserService userService;

    @Inject
    AuditLogDao auditLogDao;

    // =====================================================
    // Testes de Criação
    // =====================================================

    @Test
    @TestSecurity(user = "admin@example.com", roles = {"ADMIN"})
    void shouldCreateProfileWithValidName() {
        String uniqueName = "PERFIL" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        UserProfileRequestDTO dto = new UserProfileRequestDTO();
        dto.setName(uniqueName);

        userProfileService.create(dto);

        UserProfile profile = userProfileDao.findByName(uniqueName).orElseThrow();
        assertNotNull(profile.getId());
        assertEquals(uniqueName, profile.getName());

        // Cleanup
        userProfileService.delete(profile.getId());
    }

    @Test
    @TestSecurity(user = "admin@example.com", roles = {"ADMIN"})
    void shouldNormalizeProfileNameToUpperCase() {
        String baseName = "perfil" + UUID.randomUUID().toString().substring(0, 8);

        UserProfileRequestDTO dto = new UserProfileRequestDTO();
        dto.setName(baseName); // minúsculo

        userProfileService.create(dto);

        String expectedName = baseName.toUpperCase();
        UserProfile profile = userProfileDao.findByName(expectedName).orElseThrow();
        assertEquals(expectedName, profile.getName());

        // Cleanup
        userProfileService.delete(profile.getId());
    }

    @Test
    @TestSecurity(user = "admin@example.com", roles = {"ADMIN"})
    void shouldTrimProfileName() {
        String baseName = "PERFILSPACE" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        UserProfileRequestDTO dto = new UserProfileRequestDTO();
        dto.setName("  " + baseName + "  ");

        userProfileService.create(dto);

        UserProfile profile = userProfileDao.findByName(baseName).orElseThrow();
        assertEquals(baseName, profile.getName());

        // Cleanup
        userProfileService.delete(profile.getId());
    }

    @Test
    @TestSecurity(user = "admin@example.com", roles = {"ADMIN"})
    void shouldRejectNullName() {
        UserProfileRequestDTO dto = new UserProfileRequestDTO();
        dto.setName(null);

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> userProfileService.create(dto));

        assertEquals("Nome do perfil é obrigatório", exception.getMessage());
    }

    @Test
    @TestSecurity(user = "admin@example.com", roles = {"ADMIN"})
    void shouldRejectEmptyName() {
        UserProfileRequestDTO dto = new UserProfileRequestDTO();
        dto.setName("");

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> userProfileService.create(dto));

        assertEquals("Nome do perfil é obrigatório", exception.getMessage());
    }

    @Test
    @TestSecurity(user = "admin@example.com", roles = {"ADMIN"})
    void shouldRejectBlankName() {
        UserProfileRequestDTO dto = new UserProfileRequestDTO();
        dto.setName("   ");

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> userProfileService.create(dto));

        assertEquals("Nome do perfil é obrigatório", exception.getMessage());
    }

    @Test
    @TestSecurity(user = "admin@example.com", roles = {"ADMIN"})
    void shouldRejectDuplicateName() {
        // Tentar criar perfil com nome existente
        UserProfileRequestDTO dto = new UserProfileRequestDTO();
        dto.setName("ADMIN"); // Já existe

        WebApplicationException exception = assertThrows(WebApplicationException.class,
                () -> userProfileService.create(dto));

        assertEquals(Response.Status.CONFLICT.getStatusCode(), exception.getResponse().getStatus());
        assertTrue(exception.getMessage().contains("Já existe um perfil com esse nome"));
    }

    // =====================================================
    // Testes de Listagem
    // =====================================================

    @Test
    @TestSecurity(user = "admin@example.com", roles = {"ADMIN"})
    void shouldListAllProfiles() {
        List<UserProfileResponseDTO> profiles = userProfileService.listAll();

        assertNotNull(profiles);
        assertFalse(profiles.isEmpty());
        assertTrue(profiles.stream().anyMatch(p -> "ADMIN".equals(p.getName())));
        assertTrue(profiles.stream().anyMatch(p -> "USER".equals(p.getName())));
    }

    @Test
    @TestSecurity(user = "admin@example.com", roles = {"ADMIN"})
    void shouldListAllOrderedProfiles() {
        List<UserProfileResponseDTO> profiles = userProfileService.listAllOrdered();

        assertNotNull(profiles);
        assertFalse(profiles.isEmpty());

        // Verificar se está ordenado alfabeticamente
        for (int i = 0; i < profiles.size() - 1; i++) {
            assertTrue(
                    profiles.get(i).getName().compareToIgnoreCase(profiles.get(i + 1).getName()) <= 0,
                    "Lista deve estar ordenada alfabeticamente"
            );
        }
    }

    @Test
    @TestSecurity(user = "admin@example.com", roles = {"ADMIN"})
    void shouldListForSelection() {
        List<UserProfileResponseDTO> profiles = userProfileService.listForSelection();

        assertNotNull(profiles);
        assertFalse(profiles.isEmpty());
        // listForSelection deve retornar a mesma coisa que listAllOrdered
        List<UserProfileResponseDTO> ordered = userProfileService.listAllOrdered();
        assertEquals(ordered.size(), profiles.size());
    }

    @Test
    @TestSecurity(user = "admin@example.com", roles = {"ADMIN"})
    void shouldIncludeUserCountInResponse() {
        List<UserProfileResponseDTO> profiles = userProfileService.listAll();

        UserProfileResponseDTO adminProfile = profiles.stream()
                .filter(p -> "ADMIN".equals(p.getName()))
                .findFirst()
                .orElseThrow();

        // O perfil ADMIN tem pelo menos 1 usuário (admin@example.com)
        assertTrue(adminProfile.getUsersCount() >= 1);
    }

    // =====================================================
    // Testes de Busca por ID
    // =====================================================

    @Test
    @TestSecurity(user = "admin@example.com", roles = {"ADMIN"})
    void shouldFindProfileById() {
        UserProfile admin = userProfileDao.findByName("ADMIN").orElseThrow();

        UserProfileResponseDTO found = userProfileService.findById(admin.getId());

        assertNotNull(found);
        assertEquals("ADMIN", found.getName());
        assertEquals(admin.getId(), found.getId());
    }

    @Test
    @TestSecurity(user = "admin@example.com", roles = {"ADMIN"})
    void shouldFindEntityById() {
        UserProfile admin = userProfileDao.findByName("ADMIN").orElseThrow();

        UserProfile found = userProfileService.findEntityById(admin.getId());

        assertNotNull(found);
        assertEquals("ADMIN", found.getName());
    }

    @Test
    @TestSecurity(user = "admin@example.com", roles = {"ADMIN"})
    void shouldRejectFindNonExistentProfile() {
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userProfileService.findById(999999L));

        assertEquals("Perfil não encontrado", exception.getMessage());
    }

    @Test
    @TestSecurity(user = "admin@example.com", roles = {"ADMIN"})
    void shouldRejectFindEntityNonExistent() {
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userProfileService.findEntityById(999999L));

        assertEquals("Perfil não encontrado", exception.getMessage());
    }

    // =====================================================
    // Testes de Atualização
    // =====================================================

    @Test
    @TestSecurity(user = "admin@example.com", roles = {"ADMIN"})
    @Transactional
    void shouldUpdateProfileName() {
        String originalName = "ORIGINAL" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String newName = "UPDATED" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // Criar perfil
        UserProfileRequestDTO createDto = new UserProfileRequestDTO();
        createDto.setName(originalName);
        userProfileService.create(createDto);

        UserProfile created = userProfileDao.findByName(originalName).orElseThrow();

        // Atualizar
        UserProfileRequestDTO updateDto = new UserProfileRequestDTO();
        updateDto.setName(newName);
        userProfileService.update(created.getId(), updateDto);

        UserProfile updated = userProfileDao.findById(created.getId());
        assertEquals(newName, updated.getName());

        // Cleanup
        userProfileService.delete(updated.getId());
    }

    @Test
    @TestSecurity(user = "admin@example.com", roles = {"ADMIN"})
    @Transactional
    void shouldAllowUpdateToSameName() {
        String name = "SAMENAME" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // Criar perfil
        UserProfileRequestDTO createDto = new UserProfileRequestDTO();
        createDto.setName(name);
        userProfileService.create(createDto);

        UserProfile created = userProfileDao.findByName(name).orElseThrow();

        // Atualizar com o mesmo nome (deve funcionar)
        UserProfileRequestDTO updateDto = new UserProfileRequestDTO();
        updateDto.setName(name);
        userProfileService.update(created.getId(), updateDto);

        UserProfile updated = userProfileDao.findById(created.getId());
        assertEquals(name, updated.getName());

        // Cleanup
        userProfileService.delete(updated.getId());
    }

    @Test
    @TestSecurity(user = "admin@example.com", roles = {"ADMIN"})
    void shouldRejectUpdateToExistingName() {
        String uniqueName = "CONFLICT" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // Criar perfil
        UserProfileRequestDTO createDto = new UserProfileRequestDTO();
        createDto.setName(uniqueName);
        userProfileService.create(createDto);

        UserProfile created = userProfileDao.findByName(uniqueName).orElseThrow();

        // Tentar atualizar para nome existente (ADMIN)
        UserProfileRequestDTO updateDto = new UserProfileRequestDTO();
        updateDto.setName("ADMIN");

        WebApplicationException exception = assertThrows(WebApplicationException.class,
                () -> userProfileService.update(created.getId(), updateDto));

        assertEquals(Response.Status.CONFLICT.getStatusCode(), exception.getResponse().getStatus());

        // Cleanup
        userProfileService.delete(created.getId());
    }

    @Test
    @TestSecurity(user = "admin@example.com", roles = {"ADMIN"})
    void shouldRejectUpdateWithEmptyName() {
        UserProfile user = userProfileDao.findByName("USER").orElseThrow();

        UserProfileRequestDTO dto = new UserProfileRequestDTO();
        dto.setName("");

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> userProfileService.update(user.getId(), dto));

        assertEquals("Nome do perfil é obrigatório", exception.getMessage());
    }

    @Test
    @TestSecurity(user = "admin@example.com", roles = {"ADMIN"})
    void shouldRejectUpdateNonExistentProfile() {
        UserProfileRequestDTO dto = new UserProfileRequestDTO();
        dto.setName("QUALQUER");

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userProfileService.update(999999L, dto));

        assertEquals("Perfil não encontrado", exception.getMessage());
    }

    // =====================================================
    // Testes de Exclusão
    // =====================================================

    @Test
    @TestSecurity(user = "admin@example.com", roles = {"ADMIN"})
    @Transactional
    void shouldDeleteUnusedProfile() {
        String uniqueName = "DELETEME" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // Criar perfil
        UserProfileRequestDTO dto = new UserProfileRequestDTO();
        dto.setName(uniqueName);
        userProfileService.create(dto);

        UserProfile created = userProfileDao.findByName(uniqueName).orElseThrow();
        Long profileId = created.getId();

        // Deletar
        userProfileService.delete(profileId);

        Optional<UserProfile> deleted = userProfileDao.findByName(uniqueName);
        assertTrue(deleted.isEmpty(), "Perfil deve ser excluído");
    }

    @Test
    @TestSecurity(user = "admin@example.com", roles = {"ADMIN"})
    void shouldRejectDeleteProfileInUse() {
        // Perfil ADMIN tem usuários associados
        UserProfile admin = userProfileDao.findByName("ADMIN").orElseThrow();

        WebApplicationException exception = assertThrows(WebApplicationException.class,
                () -> userProfileService.delete(admin.getId()));

        assertEquals(Response.Status.CONFLICT.getStatusCode(), exception.getResponse().getStatus());
        assertTrue(exception.getMessage().contains("Não é possível remover perfis com usuários associados"));
    }

    @Test
    @TestSecurity(user = "admin@example.com", roles = {"ADMIN"})
    void shouldRejectDeleteUserProfileInUse() {
        // Perfil USER tem usuários associados
        UserProfile user = userProfileDao.findByName("USER").orElseThrow();

        WebApplicationException exception = assertThrows(WebApplicationException.class,
                () -> userProfileService.delete(user.getId()));

        assertEquals(Response.Status.CONFLICT.getStatusCode(), exception.getResponse().getStatus());
    }

    @Test
    @TestSecurity(user = "admin@example.com", roles = {"ADMIN"})
    void shouldRejectDeleteNonExistentProfile() {
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userProfileService.delete(999999L));

        assertEquals("Perfil não encontrado", exception.getMessage());
    }

    // =====================================================
    // Testes de Auditoria
    // =====================================================

    @Test
    @TestSecurity(user = "admin@example.com", roles = {"ADMIN"})
    void shouldRecordAuditOnCreate() {
        String uniqueName = "AUDITCRT" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        UserProfileRequestDTO dto = new UserProfileRequestDTO();
        dto.setName(uniqueName);

        userProfileService.create(dto);

        UserProfile created = userProfileDao.findByName(uniqueName).orElseThrow();

        Optional<AuditLog> audit = auditLogDao.find(
                "entityType = ?1 and entityId = ?2 and action = ?3",
                "UserProfile", created.getId().toString(), "PROFILE_CREATED"
        ).firstResultOptional();

        assertTrue(audit.isPresent(), "Deve criar registro de auditoria");
        assertEquals("admin@example.com", audit.get().getUsername());
        assertTrue(audit.get().getDetails().contains(uniqueName));

        // Cleanup
        userProfileService.delete(created.getId());
    }

    @Test
    @TestSecurity(user = "admin@example.com", roles = {"ADMIN"})
    @Transactional
    void shouldRecordAuditOnUpdate() {
        String originalName = "AUDITUPD" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String newName = "AUDITUP2" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        UserProfileRequestDTO createDto = new UserProfileRequestDTO();
        createDto.setName(originalName);
        userProfileService.create(createDto);

        UserProfile created = userProfileDao.findByName(originalName).orElseThrow();

        UserProfileRequestDTO updateDto = new UserProfileRequestDTO();
        updateDto.setName(newName);
        userProfileService.update(created.getId(), updateDto);

        Optional<AuditLog> audit = auditLogDao.find(
                "entityType = ?1 and entityId = ?2 and action = ?3",
                "UserProfile", created.getId().toString(), "PROFILE_UPDATED"
        ).firstResultOptional();

        assertTrue(audit.isPresent(), "Deve criar registro de auditoria de update");

        // Cleanup
        userProfileService.delete(created.getId());
    }

    @Test
    @TestSecurity(user = "admin@example.com", roles = {"ADMIN"})
    @Transactional
    void shouldRecordAuditOnDelete() {
        String uniqueName = "AUDITDEL" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        UserProfileRequestDTO dto = new UserProfileRequestDTO();
        dto.setName(uniqueName);
        userProfileService.create(dto);

        UserProfile created = userProfileDao.findByName(uniqueName).orElseThrow();
        String profileId = created.getId().toString();

        userProfileService.delete(created.getId());

        Optional<AuditLog> audit = auditLogDao.find(
                "entityType = ?1 and entityId = ?2 and action = ?3",
                "UserProfile", profileId, "PROFILE_DELETED"
        ).firstResultOptional();

        assertTrue(audit.isPresent(), "Deve criar registro de auditoria de delete");
    }

    // =====================================================
    // Testes de Ciclo de Vida Completo
    // =====================================================

    @Test
    @TestSecurity(user = "admin@example.com", roles = {"ADMIN"})
    @Transactional
    void shouldManageFullProfileLifecycle() {
        String name = "LIFECYCLE" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // CREATE
        UserProfileRequestDTO createDto = new UserProfileRequestDTO();
        createDto.setName(name);
        userProfileService.create(createDto);

        UserProfile created = userProfileDao.findByName(name).orElseThrow();
        assertNotNull(created.getId());

        // READ
        UserProfileResponseDTO found = userProfileService.findById(created.getId());
        assertEquals(name, found.getName());
        assertEquals(0, found.getUsersCount());

        // UPDATE
        String newName = name + "V2";
        UserProfileRequestDTO updateDto = new UserProfileRequestDTO();
        updateDto.setName(newName);
        userProfileService.update(created.getId(), updateDto);

        UserProfile updated = userProfileDao.findById(created.getId());
        assertEquals(newName, updated.getName());

        // DELETE
        userProfileService.delete(created.getId());

        Optional<UserProfile> deleted = userProfileDao.findByName(newName);
        assertTrue(deleted.isEmpty());

        // Verificar auditorias
        List<AuditLog> audits = auditLogDao.find(
                "entityType = ?1 and entityId = ?2 order by occurredAt",
                "UserProfile", created.getId().toString()
        ).list();

        assertEquals(3, audits.size(), "Deve ter 3 registros de auditoria");
        assertEquals("PROFILE_CREATED", audits.get(0).getAction());
        assertEquals("PROFILE_UPDATED", audits.get(1).getAction());
        assertEquals("PROFILE_DELETED", audits.get(2).getAction());
    }
}
