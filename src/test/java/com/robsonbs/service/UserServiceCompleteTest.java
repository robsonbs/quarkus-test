package com.robsonbs.service;

import com.robsonbs.dao.AuditLogDao;
import com.robsonbs.dao.UserDao;
import com.robsonbs.dao.UserProfileDao;
import com.robsonbs.dto.UserProfileResponseDTO;
import com.robsonbs.dto.UserRequestDTO;
import com.robsonbs.model.AuditLog;
import com.robsonbs.model.User;
import com.robsonbs.model.UserProfile;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes completos para UserService cobrindo todos os fluxos.
 */
@QuarkusTest
class UserServiceCompleteTest {

    @Inject
    UserService userService;

    @Inject
    UserProfileService userProfileService;

    @Inject
    UserDao userDao;

    @Inject
    UserProfileDao userProfileDao;

    @Inject
    AuditLogDao auditLogDao;

    // =====================================================
    // Helper Methods
    // =====================================================

    private Long getUserProfileId() {
        return userProfileService.listForSelection().stream()
                .filter(p -> "USER".equalsIgnoreCase(p.getName()))
                .map(UserProfileResponseDTO::getId)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Perfil USER não encontrado"));
    }

    private Long getAdminProfileId() {
        return userProfileService.listForSelection().stream()
                .filter(p -> "ADMIN".equalsIgnoreCase(p.getName()))
                .map(UserProfileResponseDTO::getId)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Perfil ADMIN não encontrado"));
    }

    // =====================================================
    // Testes de Criação
    // =====================================================

    @Test
    @TestSecurity(user = "admin@example.com", roles = {"ADMIN"})
    @Transactional
    void shouldCreateUserWithValidData() {
        String email = "newuser." + UUID.randomUUID() + "@test.com";

        UserRequestDTO dto = new UserRequestDTO();
        dto.setName("Novo Usuário");
        dto.setEmail(email);
        dto.setPassword("senha123456");
        dto.setProfileId(getUserProfileId());

        userService.save(dto);

        User user = userDao.findByEmail(email).orElseThrow();
        assertNotNull(user.getId());
        assertEquals("Novo Usuário", user.getName());
        assertEquals(email, user.getEmail());
        assertNotNull(user.getPassword());
        assertNotEquals("senha123456", user.getPassword()); // Deve ser hash
        assertTrue(BcryptUtil.matches("senha123456", user.getPassword()));
        assertNotNull(user.getProfile());
        assertNotNull(user.getCreatedAt());
    }

    @Test
    @TestSecurity(user = "admin@example.com", roles = {"ADMIN"})
    void shouldRejectNullPassword() {
        String email = "nullpass." + UUID.randomUUID() + "@test.com";

        UserRequestDTO dto = new UserRequestDTO();
        dto.setName("Usuário Sem Senha");
        dto.setEmail(email);
        dto.setPassword(null);
        dto.setProfileId(getUserProfileId());

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> userService.save(dto));

        assertEquals("Senha é obrigatória", exception.getMessage());
    }

    @Test
    @TestSecurity(user = "admin@example.com", roles = {"ADMIN"})
    void shouldRejectEmptyPassword() {
        String email = "emptypass." + UUID.randomUUID() + "@test.com";

        UserRequestDTO dto = new UserRequestDTO();
        dto.setName("Usuário Senha Vazia");
        dto.setEmail(email);
        dto.setPassword("");
        dto.setProfileId(getUserProfileId());

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> userService.save(dto));

        assertEquals("Senha é obrigatória", exception.getMessage());
    }

    @Test
    @TestSecurity(user = "admin@example.com", roles = {"ADMIN"})
    void shouldRejectShortPassword() {
        String email = "shortpass." + UUID.randomUUID() + "@test.com";

        UserRequestDTO dto = new UserRequestDTO();
        dto.setName("Usuário Senha Curta");
        dto.setEmail(email);
        dto.setPassword("12345"); // 5 caracteres, mínimo é 6
        dto.setProfileId(getUserProfileId());

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> userService.save(dto));

        assertEquals("Senha deve ter pelo menos 6 caracteres", exception.getMessage());
    }

    @Test
    @TestSecurity(user = "admin@example.com", roles = {"ADMIN"})
    @Transactional
    void shouldAcceptMinimumPassword() {
        String email = "minpass." + UUID.randomUUID() + "@test.com";

        UserRequestDTO dto = new UserRequestDTO();
        dto.setName("Usuário Senha Mínima");
        dto.setEmail(email);
        dto.setPassword("123456"); // Exatamente 6 caracteres
        dto.setProfileId(getUserProfileId());

        userService.save(dto);

        User user = userDao.findByEmail(email).orElseThrow();
        assertTrue(BcryptUtil.matches("123456", user.getPassword()));
    }

    @Test
    @TestSecurity(user = "admin@example.com", roles = {"ADMIN"})
    void shouldRejectDuplicateEmail() {
        // Tentar criar usuário com email existente
        UserRequestDTO dto = new UserRequestDTO();
        dto.setName("Usuário Duplicado");
        dto.setEmail("admin@example.com"); // Email já existe
        dto.setPassword("senha123456");
        dto.setProfileId(getUserProfileId());

        WebApplicationException exception = assertThrows(WebApplicationException.class,
                () -> userService.save(dto));

        assertEquals(409, exception.getResponse().getStatus());
        assertTrue(exception.getMessage().contains("Email já está em uso"));
    }

    @Test
    @TestSecurity(user = "admin@example.com", roles = {"ADMIN"})
    void shouldRejectNullProfileId() {
        String email = "nullprofile." + UUID.randomUUID() + "@test.com";

        UserRequestDTO dto = new UserRequestDTO();
        dto.setName("Usuário Sem Perfil");
        dto.setEmail(email);
        dto.setPassword("senha123456");
        dto.setProfileId(null);

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> userService.save(dto));

        assertEquals("Profile is required", exception.getMessage());
    }

    @Test
    @TestSecurity(user = "admin@example.com", roles = {"ADMIN"})
    void shouldRejectInvalidProfileId() {
        String email = "invalidprofile." + UUID.randomUUID() + "@test.com";

        UserRequestDTO dto = new UserRequestDTO();
        dto.setName("Usuário Perfil Inválido");
        dto.setEmail(email);
        dto.setPassword("senha123456");
        dto.setProfileId(999999L);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.save(dto));

        assertEquals("Profile not found", exception.getMessage());
    }

    // =====================================================
    // Testes de Listagem
    // =====================================================

    @Test
    @TestSecurity(user = "admin@example.com", roles = {"ADMIN"})
    void shouldListAllUsers() {
        List<User> users = userService.listAll();

        assertNotNull(users);
        assertFalse(users.isEmpty());
        // Deve conter pelo menos admin e user
        assertTrue(users.stream().anyMatch(u -> "admin@example.com".equals(u.getEmail())));
        assertTrue(users.stream().anyMatch(u -> "user@example.com".equals(u.getEmail())));
    }

    // =====================================================
    // Testes de Busca por ID
    // =====================================================

    @Test
    @TestSecurity(user = "admin@example.com", roles = {"ADMIN"})
    void shouldFindUserById() {
        User admin = userDao.findByEmail("admin@example.com").orElseThrow();

        User found = userService.findById(admin.getId());

        assertNotNull(found);
        assertEquals("admin@example.com", found.getEmail());
    }

    @Test
    @TestSecurity(user = "admin@example.com", roles = {"ADMIN"})
    void shouldRejectFindNonExistentUser() {
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.findById(999999L));

        assertEquals("User not found", exception.getMessage());
    }

    // =====================================================
    // Testes de Atualização
    // =====================================================

    @Test
    @TestSecurity(user = "admin@example.com", roles = {"ADMIN"})
    @Transactional
    void shouldUpdateUserWithoutChangingPassword() {
        String email = "update." + UUID.randomUUID() + "@test.com";

        // Criar usuário
        UserRequestDTO createDto = new UserRequestDTO();
        createDto.setName("Usuário Original");
        createDto.setEmail(email);
        createDto.setPassword("senha123456");
        createDto.setProfileId(getUserProfileId());
        userService.save(createDto);

        User created = userDao.findByEmail(email).orElseThrow();
        String originalPassword = created.getPassword();

        // Atualizar sem mudar senha
        UserRequestDTO updateDto = new UserRequestDTO();
        updateDto.setName("Usuário Atualizado");
        updateDto.setEmail(email);
        updateDto.setPassword(""); // Senha vazia = não alterar
        updateDto.setProfileId(getUserProfileId());

        userService.update(created.getId(), updateDto);

        User updated = userDao.findById(created.getId());
        assertEquals("Usuário Atualizado", updated.getName());
        assertEquals(originalPassword, updated.getPassword()); // Senha não mudou
    }

    @Test
    @TestSecurity(user = "admin@example.com", roles = {"ADMIN"})
    @Transactional
    void shouldUpdateUserPassword() {
        String email = "updatepass." + UUID.randomUUID() + "@test.com";

        // Criar usuário
        UserRequestDTO createDto = new UserRequestDTO();
        createDto.setName("Usuário Senha");
        createDto.setEmail(email);
        createDto.setPassword("senhaoriginal");
        createDto.setProfileId(getUserProfileId());
        userService.save(createDto);

        User created = userDao.findByEmail(email).orElseThrow();
        String originalPassword = created.getPassword();

        // Atualizar senha
        UserRequestDTO updateDto = new UserRequestDTO();
        updateDto.setName("Usuário Senha");
        updateDto.setEmail(email);
        updateDto.setPassword("novasenha123");
        updateDto.setProfileId(getUserProfileId());

        userService.update(created.getId(), updateDto);

        User updated = userDao.findById(created.getId());
        assertNotEquals(originalPassword, updated.getPassword());
        assertTrue(BcryptUtil.matches("novasenha123", updated.getPassword()));
    }

    @Test
    @TestSecurity(user = "admin@example.com", roles = {"ADMIN"})
    @Transactional
    void shouldUpdateUserEmail() {
        String originalEmail = "original." + UUID.randomUUID() + "@test.com";
        String newEmail = "updated." + UUID.randomUUID() + "@test.com";

        // Criar usuário
        UserRequestDTO createDto = new UserRequestDTO();
        createDto.setName("Usuário Email");
        createDto.setEmail(originalEmail);
        createDto.setPassword("senha123456");
        createDto.setProfileId(getUserProfileId());
        userService.save(createDto);

        User created = userDao.findByEmail(originalEmail).orElseThrow();

        // Atualizar email
        UserRequestDTO updateDto = new UserRequestDTO();
        updateDto.setName("Usuário Email");
        updateDto.setEmail(newEmail);
        updateDto.setPassword("");
        updateDto.setProfileId(getUserProfileId());

        userService.update(created.getId(), updateDto);

        User updated = userDao.findById(created.getId());
        assertEquals(newEmail, updated.getEmail());
    }

    @Test
    @TestSecurity(user = "admin@example.com", roles = {"ADMIN"})
    void shouldRejectUpdateToExistingEmail() {
        String email = "dupcheck." + UUID.randomUUID() + "@test.com";

        // Criar usuário
        UserRequestDTO createDto = new UserRequestDTO();
        createDto.setName("Usuário DupCheck");
        createDto.setEmail(email);
        createDto.setPassword("senha123456");
        createDto.setProfileId(getUserProfileId());
        userService.save(createDto);

        User created = userDao.findByEmail(email).orElseThrow();

        // Tentar atualizar para email existente
        UserRequestDTO updateDto = new UserRequestDTO();
        updateDto.setName("Usuário DupCheck");
        updateDto.setEmail("admin@example.com"); // Email já existe
        updateDto.setPassword("");
        updateDto.setProfileId(getUserProfileId());

        WebApplicationException exception = assertThrows(WebApplicationException.class,
                () -> userService.update(created.getId(), updateDto));

        assertEquals(409, exception.getResponse().getStatus());
    }

    @Test
    @TestSecurity(user = "admin@example.com", roles = {"ADMIN"})
    @Transactional
    void shouldUpdateUserProfile() {
        String email = "profilechange." + UUID.randomUUID() + "@test.com";

        // Criar usuário como USER
        UserRequestDTO createDto = new UserRequestDTO();
        createDto.setName("Usuário Profile");
        createDto.setEmail(email);
        createDto.setPassword("senha123456");
        createDto.setProfileId(getUserProfileId());
        userService.save(createDto);

        User created = userDao.findByEmail(email).orElseThrow();
        assertEquals("USER", created.getProfile().getName());

        // Atualizar para ADMIN
        UserRequestDTO updateDto = new UserRequestDTO();
        updateDto.setName("Usuário Profile");
        updateDto.setEmail(email);
        updateDto.setPassword("");
        updateDto.setProfileId(getAdminProfileId());

        userService.update(created.getId(), updateDto);

        User updated = userDao.findById(created.getId());
        assertEquals("ADMIN", updated.getProfile().getName());
    }

    @Test
    @TestSecurity(user = "admin@example.com", roles = {"ADMIN"})
    void shouldRejectUpdateNonExistentUser() {
        UserRequestDTO dto = new UserRequestDTO();
        dto.setName("Inexistente");
        dto.setEmail("inexistente@test.com");
        dto.setPassword("");
        dto.setProfileId(getUserProfileId());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.update(999999L, dto));

        assertEquals("User not found", exception.getMessage());
    }

    // =====================================================
    // Testes de Exclusão
    // =====================================================

    @Test
    @TestSecurity(user = "admin@example.com", roles = {"ADMIN"})
    @Transactional
    void shouldDeleteUserSuccessfully() {
        String email = "delete." + UUID.randomUUID() + "@test.com";

        // Criar usuário
        UserRequestDTO dto = new UserRequestDTO();
        dto.setName("Usuário para Deletar");
        dto.setEmail(email);
        dto.setPassword("senha123456");
        dto.setProfileId(getUserProfileId());
        userService.save(dto);

        User created = userDao.findByEmail(email).orElseThrow();
        Long userId = created.getId();

        userService.delete(userId);

        Optional<User> deleted = userDao.findByEmail(email);
        assertTrue(deleted.isEmpty(), "Usuário deve ser excluído");
    }

    @Test
    @TestSecurity(user = "admin@example.com", roles = {"ADMIN"})
    void shouldRejectDeleteNonExistentUser() {
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.delete(999999L));

        assertEquals("User not found", exception.getMessage());
    }

    // =====================================================
    // Testes de Auditoria
    // =====================================================

    @Test
    @TestSecurity(user = "admin@example.com", roles = {"ADMIN"})
    @Transactional
    void shouldRecordAuditOnCreate() {
        String email = "auditcreate." + UUID.randomUUID() + "@test.com";

        UserRequestDTO dto = new UserRequestDTO();
        dto.setName("Usuário Auditoria");
        dto.setEmail(email);
        dto.setPassword("senha123456");
        dto.setProfileId(getUserProfileId());

        userService.save(dto);

        User created = userDao.findByEmail(email).orElseThrow();

        Optional<AuditLog> audit = auditLogDao.find(
                "entityType = ?1 and entityId = ?2 and action = ?3",
                "User", created.getId().toString(), "USER_CREATED"
        ).firstResultOptional();

        assertTrue(audit.isPresent(), "Deve criar registro de auditoria");
        assertEquals("admin@example.com", audit.get().getUsername());
    }

    @Test
    @TestSecurity(user = "admin@example.com", roles = {"ADMIN"})
    @Transactional
    void shouldRecordAuditOnUpdate() {
        String email = "auditupdate." + UUID.randomUUID() + "@test.com";

        UserRequestDTO createDto = new UserRequestDTO();
        createDto.setName("Usuário Audit Update");
        createDto.setEmail(email);
        createDto.setPassword("senha123456");
        createDto.setProfileId(getUserProfileId());
        userService.save(createDto);

        User created = userDao.findByEmail(email).orElseThrow();

        UserRequestDTO updateDto = new UserRequestDTO();
        updateDto.setName("Usuário Audit Update Atualizado");
        updateDto.setEmail(email);
        updateDto.setPassword("");
        updateDto.setProfileId(getUserProfileId());

        userService.update(created.getId(), updateDto);

        Optional<AuditLog> audit = auditLogDao.find(
                "entityType = ?1 and entityId = ?2 and action = ?3",
                "User", created.getId().toString(), "USER_UPDATED"
        ).firstResultOptional();

        assertTrue(audit.isPresent(), "Deve criar registro de auditoria de update");
    }

    @Test
    @TestSecurity(user = "admin@example.com", roles = {"ADMIN"})
    @Transactional
    void shouldRecordAuditOnDelete() {
        String email = "auditdelete." + UUID.randomUUID() + "@test.com";

        UserRequestDTO dto = new UserRequestDTO();
        dto.setName("Usuário Audit Delete");
        dto.setEmail(email);
        dto.setPassword("senha123456");
        dto.setProfileId(getUserProfileId());
        userService.save(dto);

        User created = userDao.findByEmail(email).orElseThrow();
        String oderId = created.getId().toString();

        userService.delete(created.getId());

        Optional<AuditLog> audit = auditLogDao.find(
                "entityType = ?1 and entityId = ?2 and action = ?3",
                "User", oderId, "USER_DELETED"
        ).firstResultOptional();

        assertTrue(audit.isPresent(), "Deve criar registro de auditoria de delete");
    }

    // =====================================================
    // Testes de Ciclo de Vida Completo
    // =====================================================

    @Test
    @TestSecurity(user = "admin@example.com", roles = {"ADMIN"})
    @Transactional
    void shouldManageFullUserLifecycle() {
        String email = "lifecycle." + UUID.randomUUID() + "@test.com";

        // CREATE
        UserRequestDTO createDto = new UserRequestDTO();
        createDto.setName("Usuário Ciclo");
        createDto.setEmail(email);
        createDto.setPassword("senha123456");
        createDto.setProfileId(getUserProfileId());
        userService.save(createDto);

        User created = userDao.findByEmail(email).orElseThrow();
        assertNotNull(created.getId());

        // READ
        User found = userService.findById(created.getId());
        assertEquals(email, found.getEmail());

        // UPDATE
        UserRequestDTO updateDto = new UserRequestDTO();
        updateDto.setName("Usuário Ciclo Atualizado");
        updateDto.setEmail(email);
        updateDto.setPassword("novasenha123");
        updateDto.setProfileId(getAdminProfileId());
        userService.update(created.getId(), updateDto);

        User updated = userDao.findById(created.getId());
        assertEquals("Usuário Ciclo Atualizado", updated.getName());
        assertEquals("ADMIN", updated.getProfile().getName());

        // DELETE
        userService.delete(created.getId());

        Optional<User> deleted = userDao.findByEmail(email);
        assertTrue(deleted.isEmpty());
    }
}
