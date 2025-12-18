package com.robsonbs.filter;

import com.robsonbs.service.AuditLogService;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

import java.util.Set;

/**
 * Filtro JAX-RS para registro automático de auditoria de requisições HTTP.
 * 
 * <p>Este filtro intercepta todas as requisições JAX-RS e registra informações
 * de auditoria para usuários autenticados, criando um log detalhado de todas
 * as ações realizadas no sistema.</p>
 * 
 * <h2>Funcionamento</h2>
 * <ol>
 *   <li><strong>Request Filter:</strong> Captura informações da requisição
 *       (usuário, método, path, IP, User-Agent) e armazena no contexto</li>
 *   <li><strong>Response Filter:</strong> Após processamento, persiste o
 *       registro de auditoria incluindo o status code da resposta</li>
 * </ol>
 * 
 * <h2>Caminhos Excluídos</h2>
 * <p>Por performance e relevância, alguns caminhos são ignorados:</p>
 * <ul>
 *   <li>{@code /login} - Processado por {@link LoginAuditRouteFilter}</li>
 *   <li>{@code /j_security_check} - Autenticação (filtro dedicado)</li>
 *   <li>{@code /logout} - Processado em {@link com.robsonbs.controller.LogoutController}</li>
 *   <li>{@code /css}, {@code /favicon.ico} - Recursos estáticos</li>
 *   <li>{@code /q}, {@code /metrics}, {@code /health} - Endpoints técnicos</li>
 * </ul>
 * 
 * <h2>Resolução de IP do Cliente</h2>
 * <p>O filtro considera proxies reversos verificando headers na ordem:</p>
 * <ol>
 *   <li>{@code X-Forwarded-For} (primeiro IP da lista)</li>
 *   <li>{@code X-Real-IP}</li>
 *   <li>Endereço remoto direto da conexão</li>
 * </ol>
 * 
 * <h2>Prioridade</h2>
 * <p>Executa após filtros de autorização ({@code Priorities.USER + 10})
 * garantindo que apenas requisições de usuários autenticados sejam logadas.</p>
 * 
 * @author Sistema de Auditoria
 * @version 1.0
 * @since 1.0
 * @see ContainerRequestFilter
 * @see ContainerResponseFilter
 * @see AuditLogService
 * @see LoginAuditRouteFilter
 */
@Provider
@Priority(Priorities.USER + 10)
public class AuditLogFilter implements ContainerRequestFilter, ContainerResponseFilter {

    /**
     * Conjunto de prefixos de caminhos excluídos da auditoria.
     * Inclui autenticação, recursos estáticos e endpoints técnicos.
     */
    private static final Set<String> EXCLUDED_PATHS = Set.of(
            "login",
            "j_security_check",
            "logout",
            "css",
            "favicon.ico",
            "q",
            "metrics",
            "health"
    );

    /**
     * Chave para armazenar contexto de auditoria entre request e response.
     */
    private static final String CONTEXT_PROPERTY = "__AUDIT_CONTEXT__";

    /**
     * Serviço de auditoria para persistência dos registros.
     */
    @Inject
    AuditLogService auditLogService;

    /**
     * Identidade de segurança do usuário atual.
     */
    @Inject
    SecurityIdentity securityIdentity;

    /**
     * Intercepta requisições para capturar informações de auditoria.
     * 
     * <p>Extrai e armazena no contexto da requisição:</p>
     * <ul>
     *   <li>Nome do usuário autenticado</li>
     *   <li>Método HTTP (GET, POST, etc.)</li>
     *   <li>Caminho do recurso acessado</li>
     *   <li>IP do cliente (considerando proxies)</li>
     *   <li>User-Agent do navegador</li>
     * </ul>
     * 
     * <p>Ignora requisições anônimas e caminhos excluídos.</p>
     * 
     * @param requestContext contexto da requisição JAX-RS
     */
    @Override
    public void filter(ContainerRequestContext requestContext) {
        if (securityIdentity == null || securityIdentity.isAnonymous()) {
            return;
        }

        String rawPath = requestContext.getUriInfo().getPath();
        if (isExcluded(rawPath)) {
            return;
        }

        String path = normalizePath(rawPath);
        String method = requestContext.getMethod();
        String clientIp = resolveClientIp(requestContext);
        String userAgent = requestContext.getHeaderString("User-Agent");

        AuditRequestContext context = new AuditRequestContext();
        context.username = securityIdentity.getPrincipal().getName();
        context.method = method;
        context.resourcePath = path;
        context.clientIp = clientIp;
        context.userAgent = userAgent;
        requestContext.setProperty(CONTEXT_PROPERTY, context);
    }

    /**
     * Intercepta respostas para persistir o registro de auditoria.
     * 
     * <p>Recupera o contexto armazenado no request filter e cria
     * o registro de auditoria com o status code da resposta.</p>
     * 
     * @param requestContext  contexto da requisição original
     * @param responseContext contexto da resposta com status code
     */
    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        Object attribute = requestContext.getProperty(CONTEXT_PROPERTY);
        if (!(attribute instanceof AuditRequestContext context)) {
            return;
        }

        String action = context.method + " " + context.resourcePath;
        auditLogService.record(
                context.username,
                action,
                context.method,
                context.resourcePath,
                context.clientIp,
                null,
                null,
                null,
                context.userAgent,
                responseContext != null ? responseContext.getStatus() : null
        );
    }

    /**
     * Verifica se o caminho deve ser excluído da auditoria.
     * 
     * @param path caminho da requisição
     * @return {@code true} se o caminho começa com um prefixo excluído
     */
    private boolean isExcluded(String path) {
        if (path == null || path.isBlank()) {
            return false;
        }
        String normalized = path.startsWith("/") ? path.substring(1) : path;
        return EXCLUDED_PATHS.stream().anyMatch(normalized::startsWith);
    }

    /**
     * Normaliza o caminho garantindo que inicie com barra.
     * 
     * @param path caminho a normalizar
     * @return caminho começando com "/" ou "/" se vazio
     */
    private String normalizePath(String path) {
        if (path == null || path.isBlank()) {
            return "/";
        }
        return path.startsWith("/") ? path : "/" + path;
    }

    /**
     * Resolve o IP real do cliente considerando proxies reversos.
     * 
     * <p>Verifica headers na ordem:</p>
     * <ol>
     *   <li>{@code X-Forwarded-For} - primeiro IP da lista (cliente original)</li>
     *   <li>{@code X-Real-IP} - IP definido pelo proxy</li>
     *   <li>Endereço remoto direto da propriedade Quarkus</li>
     * </ol>
     * 
     * @param context contexto da requisição
     * @return IP do cliente ou {@code null} se não disponível
     */
    private String resolveClientIp(ContainerRequestContext context) {
        String forwarded = context.getHeaderString("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        String realIp = context.getHeaderString("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp;
        }
        Object remote = context.getProperty("io.quarkus.http.remote-address");
        return remote != null ? remote.toString() : null;
    }

    /**
     * Classe interna para armazenar contexto de auditoria entre filtros.
     * 
     * <p>Usado para passar informações do request filter para o
     * response filter através do contexto da requisição.</p>
     */
    private static final class AuditRequestContext {
        /** Nome do usuário autenticado. */
        private String username;
        /** Método HTTP da requisição. */
        private String method;
        /** Caminho do recurso acessado. */
        private String resourcePath;
        /** IP do cliente. */
        private String clientIp;
        /** User-Agent do navegador. */
        private String userAgent;
    }
}
