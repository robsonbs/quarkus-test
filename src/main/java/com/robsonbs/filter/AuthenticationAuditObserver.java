package com.robsonbs.filter;

import com.robsonbs.service.AuditLogService;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.vertx.http.runtime.security.FormAuthenticationMechanism;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

import java.time.LocalDateTime;

/**
 * Observador CDI para registro de eventos de autenticação.
 * 
 * <p>Esta classe fornece métodos utilitários para registrar eventos de
 * LOGIN, LOGOUT e falhas de autenticação no sistema de auditoria.</p>
 * 
 * <h2>Motivação</h2>
 * <p>O Quarkus com autenticação de formulário (Elytron) não expõe eventos
 * de autenticação diretamente via CDI. Esta classe trabalha em conjunto
 * com {@link LoginAuditRouteFilter} que intercepta as requisições no
 * nível do Vert.x Router para capturar esses eventos.</p>
 * 
 * <h2>Eventos Registrados</h2>
 * <table border="1">
 *   <tr><th>Evento</th><th>Método HTTP</th><th>Recurso</th><th>Descrição</th></tr>
 *   <tr><td>LOGIN_SUCCESS</td><td>POST</td><td>/j_security_check</td><td>Autenticação bem-sucedida</td></tr>
 *   <tr><td>LOGIN_FAILURE</td><td>POST</td><td>/j_security_check</td><td>Credenciais inválidas</td></tr>
 *   <tr><td>LOGOUT</td><td>GET</td><td>/logout</td><td>Encerramento de sessão</td></tr>
 * </table>
 * 
 * <h2>Integração</h2>
 * <p>Os métodos desta classe são chamados por:</p>
 * <ul>
 *   <li>{@link LoginAuditRouteFilter} - para eventos de login</li>
 *   <li>{@link com.robsonbs.controller.LogoutController} - para logout</li>
 * </ul>
 * 
 * @author Sistema de Auditoria
 * @version 1.0
 * @since 1.0
 * @see LoginAuditRouteFilter
 * @see AuditLogService
 */
@ApplicationScoped
public class AuthenticationAuditObserver {

    /**
     * Serviço de auditoria para persistência dos eventos.
     */
    @Inject
    AuditLogService auditLogService;

    /**
     * Registra um evento de login bem-sucedido no sistema de auditoria.
     * 
     * <p>Chamado quando o usuário fornece credenciais válidas e é
     * redirecionado para a página inicial ou recurso solicitado.</p>
     * 
     * @param username  nome de usuário que realizou o login
     * @param clientIp  endereço IP do cliente (considerando proxies)
     * @param userAgent identificador do navegador/cliente HTTP
     */
    public void recordLoginSuccess(String username, String clientIp, String userAgent) {
        auditLogService.record(
                username,
                "LOGIN_SUCCESS",
                "POST",
                "/j_security_check",
                clientIp,
                "Session",
                null,
                "Usuário autenticado com sucesso",
                userAgent,
                302
        );
    }

    /**
     * Registra um evento de falha de login no sistema de auditoria.
     * 
     * <p>Chamado quando o usuário fornece credenciais inválidas e é
     * redirecionado para a página de login com mensagem de erro.</p>
     * 
     * <p>Se o username não estiver disponível, registra como "anonymous"
     * para ainda manter o rastro da tentativa de acesso.</p>
     * 
     * @param username  nome de usuário que tentou login ou {@code null}
     * @param clientIp  endereço IP do cliente (considerando proxies)
     * @param userAgent identificador do navegador/cliente HTTP
     */
    public void recordLoginFailure(String username, String clientIp, String userAgent) {
        auditLogService.record(
                username != null ? username : "anonymous",
                "LOGIN_FAILURE",
                "POST",
                "/j_security_check",
                clientIp,
                "Session",
                null,
                "Tentativa de login falhou",
                userAgent,
                302
        );
    }

    /**
     * Registra um evento de logout no sistema de auditoria.
     * 
     * <p>Chamado quando o usuário encerra sua sessão voluntariamente
     * através do endpoint de logout.</p>
     * 
     * @param username  nome do usuário que realizou logout
     * @param clientIp  endereço IP do cliente (considerando proxies)
     * @param userAgent identificador do navegador/cliente HTTP
     */
    public void recordLogout(String username, String clientIp, String userAgent) {
        auditLogService.record(
                username,
                "LOGOUT",
                "GET",
                "/logout",
                clientIp,
                "Session",
                null,
                "Usuário encerrou a sessão",
                userAgent,
                302
        );
    }
}
