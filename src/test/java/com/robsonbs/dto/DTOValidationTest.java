package com.robsonbs.dto;

import com.robsonbs.model.*;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para DTOs (Data Transfer Objects).
 * Verifica construtores, getters, setters e conversões de entidades.
 */
@QuarkusTest
class DTOValidationTest {

    // =====================================================
    // Testes de TaskRequestDTO
    // =====================================================

    @Nested
    @DisplayName("TaskRequestDTO")
    class TaskRequestDTOTests {

        @Test
        @DisplayName("Getters e setters funcionam corretamente")
        void gettersAndSettersWork() {
            TaskRequestDTO dto = new TaskRequestDTO();
            
            dto.setTitle("Título da Tarefa");
            dto.setDescription("Descrição da tarefa");
            dto.setDueDate("2025-12-31");
            dto.setStatus("PENDING");
            
            assertEquals("Título da Tarefa", dto.getTitle());
            assertEquals("Descrição da tarefa", dto.getDescription());
            assertEquals("2025-12-31", dto.getDueDate());
            assertEquals("PENDING", dto.getStatus());
        }

        @Test
        @DisplayName("Valores nulos são aceitos")
        void nullValuesAreAccepted() {
            TaskRequestDTO dto = new TaskRequestDTO();
            
            dto.setTitle(null);
            dto.setDescription(null);
            dto.setDueDate(null);
            dto.setStatus(null);
            
            assertNull(dto.getTitle());
            assertNull(dto.getDescription());
            assertNull(dto.getDueDate());
            assertNull(dto.getStatus());
        }

        @Test
        @DisplayName("Valores vazios são aceitos")
        void emptyValuesAreAccepted() {
            TaskRequestDTO dto = new TaskRequestDTO();
            
            dto.setTitle("");
            dto.setDescription("");
            dto.setDueDate("");
            dto.setStatus("");
            
            assertEquals("", dto.getTitle());
            assertEquals("", dto.getDescription());
            assertEquals("", dto.getDueDate());
            assertEquals("", dto.getStatus());
        }
    }

    // =====================================================
    // Testes de TaskResponseDTO
    // =====================================================

    @Nested
    @DisplayName("TaskResponseDTO")
    class TaskResponseDTOTests {

        @Test
        @DisplayName("Construtor popula campos corretamente a partir de Task")
        void constructorPopulatesFieldsFromTask() {
            Task task = new Task();
            task.setId(1L);
            task.setTitle("Tarefa de Teste");
            task.setDescription("Descrição da tarefa");
            task.setDueDate(LocalDate.of(2025, 12, 31));
            task.setStatus(TaskStatus.IN_PROGRESS);
            // createdAt e updatedAt são setados pelo @PrePersist, não testamos diretamente
            
            TaskResponseDTO dto = new TaskResponseDTO(task);
            
            assertEquals(1L, dto.getId());
            assertEquals("Tarefa de Teste", dto.getTitle());
            assertEquals("Descrição da tarefa", dto.getDescription());
            assertEquals(LocalDate.of(2025, 12, 31), dto.getDueDate());
            assertEquals(TaskStatus.IN_PROGRESS, dto.getStatus());
        }

        @Test
        @DisplayName("Trata campos nulos da Task corretamente")
        void handlesNullTaskFieldsCorrectly() {
            Task task = new Task();
            task.setId(1L);
            task.setTitle("Tarefa Mínima");
            task.setStatus(TaskStatus.PENDING);
            // description, dueDate, createdAt, updatedAt são nulos
            
            TaskResponseDTO dto = new TaskResponseDTO(task);
            
            assertEquals(1L, dto.getId());
            assertEquals("Tarefa Mínima", dto.getTitle());
            assertNull(dto.getDescription());
            assertNull(dto.getDueDate());
            assertEquals(TaskStatus.PENDING, dto.getStatus());
        }
    }

    // =====================================================
    // Testes de NoteRequestDTO
    // =====================================================

    @Nested
    @DisplayName("NoteRequestDTO")
    class NoteRequestDTOTests {

        @Test
        @DisplayName("Getters e setters funcionam corretamente")
        void gettersAndSettersWork() {
            NoteRequestDTO dto = new NoteRequestDTO();
            
            dto.setTitle("Título da Nota");
            dto.setContent("Conteúdo da nota");
            
            assertEquals("Título da Nota", dto.getTitle());
            assertEquals("Conteúdo da nota", dto.getContent());
        }

        @Test
        @DisplayName("Valores nulos são aceitos")
        void nullValuesAreAccepted() {
            NoteRequestDTO dto = new NoteRequestDTO();
            
            dto.setTitle(null);
            dto.setContent(null);
            
            assertNull(dto.getTitle());
            assertNull(dto.getContent());
        }
    }

    // =====================================================
    // Testes de NoteResponseDTO
    // =====================================================

    @Nested
    @DisplayName("NoteResponseDTO")
    class NoteResponseDTOTests {

        @Test
        @DisplayName("Construtor popula campos corretamente a partir de Note")
        void constructorPopulatesFieldsFromNote() {
            Note note = new Note();
            note.setId(1L);
            note.setTitle("Nota de Teste");
            note.setContent("Conteúdo da nota");
            // owner e timestamps são setados por outros mecanismos
            
            NoteResponseDTO dto = new NoteResponseDTO(note);
            
            assertEquals(1L, dto.getId());
            assertEquals("Nota de Teste", dto.getTitle());
            assertEquals("Conteúdo da nota", dto.getContent());
        }
    }

    // =====================================================
    // Testes de UserRequestDTO
    // =====================================================

    @Nested
    @DisplayName("UserRequestDTO")
    class UserRequestDTOTests {

        @Test
        @DisplayName("Getters e setters funcionam corretamente")
        void gettersAndSettersWork() {
            UserRequestDTO dto = new UserRequestDTO();
            
            dto.setName("Nome do Usuário");
            dto.setEmail("email@test.com");
            dto.setPassword("senha123");
            dto.setProfileId(1L);
            
            assertEquals("Nome do Usuário", dto.getName());
            assertEquals("email@test.com", dto.getEmail());
            assertEquals("senha123", dto.getPassword());
            assertEquals(1L, dto.getProfileId());
        }

        @Test
        @DisplayName("Valores nulos são aceitos")
        void nullValuesAreAccepted() {
            UserRequestDTO dto = new UserRequestDTO();
            
            dto.setName(null);
            dto.setEmail(null);
            dto.setPassword(null);
            dto.setProfileId(null);
            
            assertNull(dto.getName());
            assertNull(dto.getEmail());
            assertNull(dto.getPassword());
            assertNull(dto.getProfileId());
        }
    }

    // =====================================================
    // Testes de UserResponseDTO
    // =====================================================

    @Nested
    @DisplayName("UserResponseDTO")
    class UserResponseDTOTests {

        @Test
        @DisplayName("Construtor popula campos corretamente a partir de User com Profile")
        void constructorPopulatesFieldsFromUserWithProfile() {
            UserProfile profile = new UserProfile();
            profile.setId(10L);
            profile.setName("ADMIN");
            
            User user = new User();
            user.setId(1L);
            user.setName("Nome do Usuário");
            user.setEmail("email@test.com");
            user.setProfile(profile);
            user.setCreatedAt(LocalDateTime.of(2025, 1, 1, 10, 0));
            
            UserResponseDTO dto = new UserResponseDTO(user);
            
            assertEquals(1L, dto.getId());
            assertEquals("Nome do Usuário", dto.getName());
            assertEquals("email@test.com", dto.getEmail());
            assertEquals("ADMIN", dto.getProfileName());
            assertEquals(10L, dto.getProfileId());
            assertEquals(LocalDateTime.of(2025, 1, 1, 10, 0), dto.getCreatedAt());
        }

        @Test
        @DisplayName("Construtor trata User sem Profile corretamente")
        void constructorHandlesUserWithoutProfile() {
            User user = new User();
            user.setId(1L);
            user.setName("Usuário sem Perfil");
            user.setEmail("semprofile@test.com");
            user.setProfile(null);
            
            UserResponseDTO dto = new UserResponseDTO(user);
            
            assertEquals(1L, dto.getId());
            assertEquals("Usuário sem Perfil", dto.getName());
            assertEquals("semprofile@test.com", dto.getEmail());
            assertNull(dto.getProfileName());
            assertNull(dto.getProfileId());
        }

        @Test
        @DisplayName("Setters funcionam corretamente")
        void settersWork() {
            User user = new User();
            user.setId(1L);
            user.setName("Original");
            user.setEmail("original@test.com");
            
            UserResponseDTO dto = new UserResponseDTO(user);
            
            dto.setId(2L);
            dto.setName("Modificado");
            dto.setEmail("modificado@test.com");
            dto.setProfileName("USER");
            dto.setProfileId(5L);
            dto.setCreatedAt(LocalDateTime.now());
            
            assertEquals(2L, dto.getId());
            assertEquals("Modificado", dto.getName());
            assertEquals("modificado@test.com", dto.getEmail());
            assertEquals("USER", dto.getProfileName());
            assertEquals(5L, dto.getProfileId());
            assertNotNull(dto.getCreatedAt());
        }
    }

    // =====================================================
    // Testes de UserProfileRequestDTO
    // =====================================================

    @Nested
    @DisplayName("UserProfileRequestDTO")
    class UserProfileRequestDTOTests {

        @Test
        @DisplayName("Getters e setters funcionam corretamente")
        void gettersAndSettersWork() {
            UserProfileRequestDTO dto = new UserProfileRequestDTO();
            
            dto.setName("ADMINISTRADOR");
            
            assertEquals("ADMINISTRADOR", dto.getName());
        }

        @Test
        @DisplayName("Valor nulo é aceito")
        void nullValueIsAccepted() {
            UserProfileRequestDTO dto = new UserProfileRequestDTO();
            
            dto.setName(null);
            
            assertNull(dto.getName());
        }
    }

    // =====================================================
    // Testes de UserProfileResponseDTO
    // =====================================================

    @Nested
    @DisplayName("UserProfileResponseDTO")
    class UserProfileResponseDTOTests {

        @Test
        @DisplayName("Construtor popula campos corretamente a partir de UserProfile")
        void constructorPopulatesFieldsFromUserProfile() {
            UserProfile profile = new UserProfile();
            profile.setId(1L);
            profile.setName("ADMIN");
            
            UserProfileResponseDTO dto = new UserProfileResponseDTO(profile, 5L);
            
            assertEquals(1L, dto.getId());
            assertEquals("ADMIN", dto.getName());
            assertEquals(5L, dto.getUsersCount());
        }

        @Test
        @DisplayName("Construtor aceita contagem de usuários zero")
        void constructorAcceptsZeroUserCount() {
            UserProfile profile = new UserProfile();
            profile.setId(2L);
            profile.setName("USER");
            
            UserProfileResponseDTO dto = new UserProfileResponseDTO(profile, 0L);
            
            assertEquals(2L, dto.getId());
            assertEquals("USER", dto.getName());
            assertEquals(0L, dto.getUsersCount());
        }
    }

    // =====================================================
    // Testes de AuditLogResponseDTO
    // =====================================================

    @Nested
    @DisplayName("AuditLogResponseDTO")
    class AuditLogResponseDTOTests {

        @Test
        @DisplayName("Construtor popula campos corretamente a partir de AuditLog")
        void constructorPopulatesFieldsFromAuditLog() {
            AuditLog log = new AuditLog();
            log.setId(1L);
            log.setUsername("admin@example.com");
            log.setAction("LOGIN_SUCCESS");
            log.setResourcePath("/j_security_check");
            log.setOccurredAt(LocalDateTime.of(2025, 1, 1, 10, 0));
            log.setDetails("Login bem-sucedido");
            
            AuditLogResponseDTO dto = new AuditLogResponseDTO(log);
            
            assertEquals(1L, dto.getId());
            assertEquals("admin@example.com", dto.getUsername());
            assertEquals("LOGIN_SUCCESS", dto.getAction());
            assertEquals("/j_security_check", dto.getResourcePath());
            assertEquals(LocalDateTime.of(2025, 1, 1, 10, 0), dto.getOccurredAt());
            assertEquals("Login bem-sucedido", dto.getDetails());
        }

        @Test
        @DisplayName("Trata campos nulos corretamente")
        void handlesNullFieldsCorrectly() {
            AuditLog log = new AuditLog();
            log.setId(1L);
            log.setAction("TEST_ACTION");
            // username, resourcePath, details são nulos
            
            AuditLogResponseDTO dto = new AuditLogResponseDTO(log);
            
            assertEquals(1L, dto.getId());
            assertNull(dto.getUsername());
            assertEquals("TEST_ACTION", dto.getAction());
            assertNull(dto.getResourcePath());
            assertNull(dto.getDetails());
        }
    }

    // =====================================================
    // Testes de AuditLogFilterDTO
    // =====================================================

    @Nested
    @DisplayName("AuditLogFilterDTO")
    class AuditLogFilterDTOTests {

        @Test
        @DisplayName("Getters e setters funcionam corretamente")
        void gettersAndSettersWork() {
            AuditLogFilterDTO dto = new AuditLogFilterDTO();
            
            dto.setUsername("admin@example.com");
            dto.setMethod("POST");
            dto.setResource("/users");
            dto.setPage(2);
            dto.setSize(20);
            
            assertEquals("admin@example.com", dto.getUsername());
            assertEquals("POST", dto.getMethod());
            assertEquals("/users", dto.getResource());
            assertEquals(2, dto.getPage());
            assertEquals(20, dto.getSize());
        }

        @Test
        @DisplayName("Valores padrão são aplicados com defaults()")
        void defaultValuesAreApplied() {
            AuditLogFilterDTO dto = AuditLogFilterDTO.defaults();
            
            assertNull(dto.getUsername());
            assertNull(dto.getMethod());
            assertEquals(0, dto.getPage());
            assertEquals(25, dto.getSize());
        }
    }

    // =====================================================
    // Testes de LoginRequestDTO
    // =====================================================

    @Nested
    @DisplayName("LoginRequestDTO")
    class LoginRequestDTOTests {

        @Test
        @DisplayName("Getters e setters funcionam corretamente")
        void gettersAndSettersWork() {
            LoginRequestDTO dto = new LoginRequestDTO();
            
            dto.setEmail("user@example.com");
            dto.setPassword("senha123");
            
            assertEquals("user@example.com", dto.getEmail());
            assertEquals("senha123", dto.getPassword());
        }

        @Test
        @DisplayName("Valores nulos são aceitos")
        void nullValuesAreAccepted() {
            LoginRequestDTO dto = new LoginRequestDTO();
            
            dto.setEmail(null);
            dto.setPassword(null);
            
            assertNull(dto.getEmail());
            assertNull(dto.getPassword());
        }
    }

    // =====================================================
    // Testes de Imutabilidade e Thread Safety
    // =====================================================

    @Nested
    @DisplayName("Testes de Comportamento")
    class BehaviorTests {

        @Test
        @DisplayName("DTOs Request permitem alteração de valores")
        void requestDtosAllowValueChanges() {
            TaskRequestDTO dto = new TaskRequestDTO();
            
            dto.setTitle("Valor 1");
            assertEquals("Valor 1", dto.getTitle());
            
            dto.setTitle("Valor 2");
            assertEquals("Valor 2", dto.getTitle());
        }

        @Test
        @DisplayName("DTOs suportam caracteres especiais")
        void dtosHandleSpecialCharacters() {
            NoteRequestDTO dto = new NoteRequestDTO();
            
            dto.setTitle("Título com acentuação: é, à, ç, ñ");
            dto.setContent("Conteúdo com emoji: 🎉 e símbolos: @#$%");
            
            assertEquals("Título com acentuação: é, à, ç, ñ", dto.getTitle());
            assertEquals("Conteúdo com emoji: 🎉 e símbolos: @#$%", dto.getContent());
        }

        @Test
        @DisplayName("DTOs suportam strings longas")
        void dtosHandleLongStrings() {
            String longText = "A".repeat(10000);
            NoteRequestDTO dto = new NoteRequestDTO();
            
            dto.setContent(longText);
            
            assertEquals(10000, dto.getContent().length());
        }
    }
}
