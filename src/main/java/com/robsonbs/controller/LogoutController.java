package com.robsonbs.controller;

import com.robsonbs.filter.AuthenticationAuditObserver;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.common.annotation.Blocking;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.net.URI;

/**
 * Controller responsável pelo logout de usuários do sistema.
 * 
 * <p>Este controller gerencia o processo de encerramento de sessão,
 * registrando o evento de logout na auditoria antes de invalidar
 * a sessão do usuário e redirecioná-lo para a página de login.</p>
 * 
 * <h2>Processo de Logout</h2>
 * <ol>
 *   <li>Obtém informações do usuário autenticado</li>
 *   <li>Extrai IP do cliente e User-Agent para auditoria</li>
 *   <li>Registra evento de logout via {@link AuthenticationAuditObserver}</li>
 *   <li>Redireciona para {@code /login} com mensagem de sucesso</li>
 *   <li>Quarkus invalida a sessão automaticamente após o redirect</li>
 * </ol>
 * 
 * <h2>Segurança</h2>
 * <ul>
 *   <li><strong>Rota:</strong> {@code GET /logout}</li>
 *   <li><strong>Acesso:</strong> Requer autenticação (ADMIN ou USER)</li>
 *   <li><strong>Cache:</strong> Desabilitado via headers</li>
 * </ul>
 * 
 * <h2>Auditoria</h2>
 * <p>O evento de logout é registrado ANTES da invalidação da sessão
 * para garantir que as informações do usuário ainda estejam disponíveis.
 * Dados registrados:</p>
 * <ul>
 *   <li>Username (email do usuário)</li>
 *   <li>IP do cliente (via X-Forwarded-For ou X-Real-IP)</li>
 *   <li>User-Agent do navegador</li>
 * </ul>
 * 
 * <h2>Headers de Cache</h2>
 * <p>A resposta inclui {@code Cache-Control: no-cache, no-store, must-revalidate}
 * para evitar que o navegador faça cache da página de logout e prevenir
 * comportamentos inesperados com o botão voltar.</p>
 * 
 * @author Sistema de Segurança
 * @version 1.0
 * @since 1.0
 * @see LoginController
 * @see AuthenticationAuditObserver
 */
@Path("/logout")
@Blocking
public class LogoutController {

    /**
     * Identidade de segurança do usuário autenticado.
     * Fornece acesso ao principal (email) do usuário.
     */
    @Inject
    SecurityIdentity securityIdentity;

    /**
     * Observer de auditoria para registro de eventos de autenticação.
     * Usado para registrar o evento de logout.
     */
    @Inject
    AuthenticationAuditObserver auditObserver;

    /**
     * Processa a requisição de logout do usuário.
     * 
     * <p>Registra o evento de logout na auditoria e redireciona
     * para a página de login com mensagem de sucesso. A sessão
     * é invalidada automaticamente pelo Quarkus.</p>
     * 
     * @param headers headers HTTP contendo User-Agent e informações de proxy
     * @param uriInfo informações da URI da requisição
     * @return resposta de redirecionamento (302) para /login
     */
    @GET
    @RolesAllowed({"ADMIN", "USER"})
    public Response logout(@Context HttpHeaders headers, @Context UriInfo uriInfo) {
        String username = securityIdentity.getPrincipal().getName();
        String clientIp = resolveClientIp(headers);
        String userAgent = headers.getHeaderString("User-Agent");

        // Registra o logout na auditoria ANTES de invalidar a sessão
        auditObserver.recordLogout(username, clientIp, userAgent);

        // Redireciona para login com mensagem de sucesso
        // O Quarkus irá invalidar a sessão automaticamente após o redirect
        return Response.seeOther(URI.create("/login?success=Logout+realizado+com+sucesso"))
                .header("Cache-Control", "no-cache, no-store, must-revalidate")
                .build();
    }

    /**
     * Resolve o endereço IP real do cliente.
     * 
     * <p>Em ambientes com proxy reverso ou load balancer, o IP real
     * do cliente é passado nos headers X-Forwarded-For ou X-Real-IP.
     * Este método verifica esses headers na ordem de precedência.</p>
     * 
     * @param headers headers HTTP da requisição
     * @return IP do cliente ou "unknown" se não identificável
     */
    private String resolveClientIp(HttpHeaders headers) {
        String forwarded = headers.getHeaderString("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        String realIp = headers.getHeaderString("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp;
        }
        return "unknown";
    }
}
