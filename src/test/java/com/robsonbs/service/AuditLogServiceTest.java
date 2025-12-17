package com.robsonbs.service;

import com.robsonbs.dao.AuditLogDao;
import com.robsonbs.model.AuditLog;
import com.robsonbs.service.AuditLogService.AuditLogCriteria;
import com.robsonbs.service.AuditLogService.AuditLogPage;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários e de integração para o AuditLogService.
 * Cobre: record(), search(), recordDomainEvent() e paginação.
 */
@QuarkusTest
class AuditLogServiceTest {

    @Inject
    AuditLogService auditLogService;

    @Inject
    AuditLogDao auditLogDao;

    @Test
    @TestSecurity(user = "test@example.com", roles = {"USER"})
    void shouldRecordBasicAuditLog() {
        String uniqueAction = "TEST_ACTION_" + UUID.randomUUID();
        
        auditLogService.record(
            "test@example.com",
            uniqueAction,
            "GET",
            "/test/resource",
            "127.0.0.1"
        );

        AuditLog log = auditLogDao.find("action", uniqueAction).firstResult();
        
        assertNotNull(log, "Log de auditoria deve ser persistido");
        assertEquals("test@example.com", log.getUsername());
        assertEquals("GET", log.getHttpMethod());
        assertEquals("/test/resource", log.getResourcePath());
        assertEquals("127.0.0.1", log.getClientIp());
        assertNotNull(log.getOccurredAt());
    }

    @Test
    @TestSecurity(user = "test@example.com", roles = {"USER"})
    void shouldRecordCompleteAuditLog() {
        String uniqueAction = "COMPLETE_TEST_" + UUID.randomUUID();
        
        auditLogService.record(
            "admin@example.com",
            uniqueAction,
            "POST",
            "/api/users",
            "192.168.1.100",
            "User",
            "42",
            "Criação de usuário de teste",
            "Mozilla/5.0 Test Agent",
            201
        );

        AuditLog log = auditLogDao.find("action", uniqueAction).firstResult();
        
        assertNotNull(log);
        assertEquals("admin@example.com", log.getUsername());
        assertEquals("POST", log.getHttpMethod());
        assertEquals("/api/users", log.getResourcePath());
        assertEquals("192.168.1.100", log.getClientIp());
        assertEquals("User", log.getEntityType());
        assertEquals("42", log.getEntityId());
        assertEquals("Criação de usuário de teste", log.getDetails());
        assertEquals("Mozilla/5.0 Test Agent", log.getUserAgent());
        assertEquals(201, log.getStatusCode());
    }

    @Test
    @TestSecurity(user = "test@example.com", roles = {"USER"})
    void shouldRecordDomainEvent() {
        String uniqueAction = "DOMAIN_EVENT_" + UUID.randomUUID();
        
        auditLogService.recordDomainEvent(
            "service@system.com",
            uniqueAction,
            "/tasks/99",
            "Task",
            "99",
            "Status alterado para COMPLETED",
            200
        );

        AuditLog log = auditLogDao.find("action", uniqueAction).firstResult();
        
        assertNotNull(log);
        assertEquals("service@system.com", log.getUsername());
        assertEquals("DOMAIN", log.getHttpMethod()); // Domain events use "DOMAIN" as method
        assertEquals("/tasks/99", log.getResourcePath());
        assertEquals("Task", log.getEntityType());
        assertEquals("99", log.getEntityId());
        assertEquals("Status alterado para COMPLETED", log.getDetails());
    }

    @Test
    @TestSecurity(user = "test@example.com", roles = {"USER"})
    void shouldListRecentLogs() {
        // Cria alguns logs de teste
        String prefix = "RECENT_" + UUID.randomUUID().toString().substring(0, 8);
        for (int i = 0; i < 5; i++) {
            auditLogService.record(
                "test@example.com",
                prefix + "_" + i,
                "GET",
                "/test/" + i,
                "127.0.0.1"
            );
        }

        List<AuditLog> recent = auditLogService.listRecent(10);
        
        assertNotNull(recent);
        assertFalse(recent.isEmpty());
        // Os logs mais recentes devem aparecer primeiro (ordenação descendente)
        assertTrue(recent.get(0).getOccurredAt().isAfter(recent.get(recent.size() - 1).getOccurredAt()) ||
                   recent.get(0).getOccurredAt().equals(recent.get(recent.size() - 1).getOccurredAt()));
    }

    @Test
    @TestSecurity(user = "test@example.com", roles = {"USER"})
    void shouldSearchByUsername() {
        String uniqueUsername = "searchuser_" + UUID.randomUUID() + "@test.com";
        String uniqueAction = "SEARCH_USER_" + UUID.randomUUID();
        
        auditLogService.record(uniqueUsername, uniqueAction, "GET", "/search", "127.0.0.1");

        AuditLogCriteria criteria = AuditLogCriteria.of(
            uniqueUsername, null, null, null, null, null, null, 0, 10
        );
        
        AuditLogPage result = auditLogService.search(criteria);
        
        assertNotNull(result);
        assertTrue(result.total() >= 1, "Deve encontrar pelo menos 1 registro");
        assertTrue(result.items().stream()
            .anyMatch(log -> log.getUsername().equals(uniqueUsername)));
    }

    @Test
    @TestSecurity(user = "test@example.com", roles = {"USER"})
    void shouldSearchByHttpMethod() {
        String uniqueAction = "METHOD_TEST_" + UUID.randomUUID();
        
        auditLogService.record("test@example.com", uniqueAction, "DELETE", "/resource/1", "127.0.0.1");

        AuditLogCriteria criteria = AuditLogCriteria.of(
            null, "DELETE", null, null, null, null, null, 0, 100
        );
        
        AuditLogPage result = auditLogService.search(criteria);
        
        assertNotNull(result);
        assertTrue(result.items().stream()
            .allMatch(log -> "DELETE".equals(log.getHttpMethod())));
    }

    @Test
    @TestSecurity(user = "test@example.com", roles = {"USER"})
    void shouldSearchByResourcePath() {
        String uniquePath = "/unique/path/" + UUID.randomUUID();
        String uniqueAction = "PATH_TEST_" + UUID.randomUUID();
        
        auditLogService.record("test@example.com", uniqueAction, "GET", uniquePath, "127.0.0.1");

        AuditLogCriteria criteria = AuditLogCriteria.of(
            null, null, uniquePath, null, null, null, null, 0, 10
        );
        
        AuditLogPage result = auditLogService.search(criteria);
        
        assertNotNull(result);
        assertEquals(1, result.total());
        assertEquals(uniquePath, result.items().get(0).getResourcePath());
    }

    @Test
    @TestSecurity(user = "test@example.com", roles = {"USER"})
    void shouldSearchByEntityTypeAndId() {
        String uniqueEntityId = UUID.randomUUID().toString();
        String uniqueAction = "ENTITY_TEST_" + UUID.randomUUID();
        
        auditLogService.record(
            "test@example.com", uniqueAction, "PUT", "/notes/" + uniqueEntityId, "127.0.0.1",
            "Note", uniqueEntityId, "Atualização de nota", null, 200
        );

        AuditLogCriteria criteria = AuditLogCriteria.of(
            null, null, null, "Note", uniqueEntityId, null, null, 0, 10
        );
        
        AuditLogPage result = auditLogService.search(criteria);
        
        assertNotNull(result);
        assertEquals(1, result.total());
        assertEquals("Note", result.items().get(0).getEntityType());
        assertEquals(uniqueEntityId, result.items().get(0).getEntityId());
    }

    @Test
    @TestSecurity(user = "test@example.com", roles = {"USER"})
    void shouldSearchByDateRange() {
        LocalDateTime now = LocalDateTime.now();
        String uniqueAction = "DATE_RANGE_" + UUID.randomUUID();
        
        auditLogService.record("test@example.com", uniqueAction, "GET", "/date-test", "127.0.0.1");

        // Busca logs da última hora
        AuditLogCriteria criteria = AuditLogCriteria.of(
            null, null, null, null, null, 
            now.minusHours(1), 
            now.plusHours(1), 
            0, 100
        );
        
        AuditLogPage result = auditLogService.search(criteria);
        
        assertNotNull(result);
        assertTrue(result.total() >= 1, "Deve encontrar logs no intervalo de tempo");
    }

    @Test
    @TestSecurity(user = "test@example.com", roles = {"USER"})
    void shouldPaginateResults() {
        // Cria logs suficientes para testar paginação
        String prefix = "PAGINATION_" + UUID.randomUUID().toString().substring(0, 8);
        for (int i = 0; i < 15; i++) {
            auditLogService.record(
                "pagination@test.com",
                prefix + "_" + i,
                "GET",
                "/pagination/" + i,
                "127.0.0.1"
            );
        }

        // Primeira página (5 itens)
        AuditLogCriteria page1Criteria = AuditLogCriteria.of(
            "pagination@test.com", null, null, null, null, null, null, 0, 5
        );
        AuditLogPage page1 = auditLogService.search(page1Criteria);
        
        assertEquals(5, page1.items().size());
        assertTrue(page1.total() >= 15);
        assertEquals(0, page1.page());
        assertEquals(5, page1.size());

        // Segunda página
        AuditLogCriteria page2Criteria = AuditLogCriteria.of(
            "pagination@test.com", null, null, null, null, null, null, 1, 5
        );
        AuditLogPage page2 = auditLogService.search(page2Criteria);
        
        assertEquals(5, page2.items().size());
        assertEquals(1, page2.page());

        // Verifica que as páginas têm itens diferentes
        assertNotEquals(page1.items().get(0).getAction(), page2.items().get(0).getAction());
    }

    @Test
    @TestSecurity(user = "test@example.com", roles = {"USER"})
    void shouldCalculateTotalPages() {
        AuditLogPage page = new AuditLogPage(List.of(), 25, 0, 10);
        assertEquals(3, page.totalPages()); // 25 itens / 10 por página = 3 páginas

        AuditLogPage page2 = new AuditLogPage(List.of(), 20, 0, 10);
        assertEquals(2, page2.totalPages()); // 20 itens / 10 por página = 2 páginas

        AuditLogPage page3 = new AuditLogPage(List.of(), 0, 0, 10);
        assertEquals(0, page3.totalPages()); // 0 itens = 0 páginas

        AuditLogPage page4 = new AuditLogPage(List.of(), 5, 0, 0);
        assertEquals(1, page4.totalPages()); // size 0 retorna 1 página (edge case)
    }

    @Test
    @TestSecurity(user = "test@example.com", roles = {"USER"})
    void shouldSearchWithMultipleCriteria() {
        String uniqueUsername = "multi_" + UUID.randomUUID() + "@test.com";
        String uniqueAction = "MULTI_SEARCH_" + UUID.randomUUID();
        
        auditLogService.record(
            uniqueUsername, uniqueAction, "POST", "/multi/test", "10.0.0.1",
            "MultiTest", "123", "Teste de múltiplos critérios", null, 201
        );

        // Busca combinando username + method + entityType
        AuditLogCriteria criteria = AuditLogCriteria.of(
            uniqueUsername, "POST", null, "MultiTest", null, null, null, 0, 10
        );
        
        AuditLogPage result = auditLogService.search(criteria);
        
        assertNotNull(result);
        assertEquals(1, result.total());
        AuditLog found = result.items().get(0);
        assertEquals(uniqueUsername, found.getUsername());
        assertEquals("POST", found.getHttpMethod());
        assertEquals("MultiTest", found.getEntityType());
    }

    @Test
    @TestSecurity(user = "test@example.com", roles = {"USER"})
    void shouldHandleEmptyCriteria() {
        // Busca sem filtros (retorna todos os logs paginados)
        AuditLogCriteria criteria = AuditLogCriteria.of(
            null, null, null, null, null, null, null, 0, 10
        );
        
        AuditLogPage result = auditLogService.search(criteria);
        
        assertNotNull(result);
        assertNotNull(result.items());
        assertTrue(result.total() >= 0);
    }

    @Test
    @TestSecurity(user = "test@example.com", roles = {"USER"})
    void shouldSearchCaseInsensitiveUsername() {
        String uniqueUsername = "CaseTest_" + UUID.randomUUID() + "@Test.COM";
        String uniqueAction = "CASE_TEST_" + UUID.randomUUID();
        
        auditLogService.record(uniqueUsername, uniqueAction, "GET", "/case", "127.0.0.1");

        // Busca com username em lowercase
        AuditLogCriteria criteria = AuditLogCriteria.of(
            uniqueUsername.toLowerCase(), null, null, null, null, null, null, 0, 10
        );
        
        AuditLogPage result = auditLogService.search(criteria);
        
        assertNotNull(result);
        assertTrue(result.total() >= 1);
    }

    @Test
    @TestSecurity(user = "test@example.com", roles = {"USER"})
    void shouldSearchByPartialUsername() {
        String uniquePart = "partial" + UUID.randomUUID().toString().substring(0, 6);
        String uniqueUsername = uniquePart + "@domain.com";
        String uniqueAction = "PARTIAL_" + UUID.randomUUID();
        
        auditLogService.record(uniqueUsername, uniqueAction, "GET", "/partial", "127.0.0.1");

        // Busca com parte do username
        AuditLogCriteria criteria = AuditLogCriteria.of(
            uniquePart, null, null, null, null, null, null, 0, 10
        );
        
        AuditLogPage result = auditLogService.search(criteria);
        
        assertNotNull(result);
        assertTrue(result.total() >= 1);
        assertTrue(result.items().stream()
            .anyMatch(log -> log.getUsername().contains(uniquePart)));
    }
}
