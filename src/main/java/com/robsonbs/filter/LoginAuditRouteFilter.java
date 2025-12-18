package com.robsonbs.filter;

import com.robsonbs.service.AuditLogService;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.vertx.http.runtime.security.QuarkusHttpUser;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

/**
 * Filtro Vert.x para captura e auditoria de eventos de autenticação.
 * 
 * <p>Este filtro opera no nível do Vert.x Router para interceptar
 * requisições de autenticação ({@code /j_security_check}) que são
 * processadas pelo Elytron antes de chegarem aos endpoints JAX-RS.</p>
 * 
 * <h2>Motivação</h2>
 * <p>O Quarkus com Form Authentication processa {@code /j_security_check}
 * internamente no Elytron Security, tornando impossível a interceptação
 * via filtros JAX-RS convencionais. Este filtro Vert.x executa em uma
 * camada mais baixa da stack HTTP.</p>
 * 
 * <h2>Funcionamento</h2>
 * <ol>
 *   <li>Registra-se como filtro Vert.x com prioridade 1000 (após autenticação)</li>
 *   <li>Intercepta apenas POST para {@code /j_security_check}</li>
 *   <li>Adiciona handler no fim do body para análise pós-processamento</li>
 *   <li>Analisa o Location header e status code para determinar sucesso/falha</li>
 *   <li>Executa registro de auditoria em worker thread (para suportar JTA)</li>
 * </ol>
 * 
 * <h2>Detecção de Resultado</h2>
 * <ul>
 *   <li><strong>Sucesso:</strong> Status 302/303 com Location que não contém "error"</li>
 *   <li><strong>Falha:</strong> Status 302/303 com Location contendo "error"</li>
 * </ul>
 * 
 * <h2>Execução Assíncrona</h2>
 * <p>O registro de auditoria é executado em worker thread do Vert.x
 * para permitir operações JTA/transacionais, já que o contexto
 * Vert.x event loop não suporta transações bloqueantes.</p>
 * 
 * <h2>Resolução de IP</h2>
 * <p>Similar a {@link AuditLogFilter}, considera proxies reversos:</p>
 * <ol>
 *   <li>{@code X-Forwarded-For}</li>
 *   <li>{@code X-Real-IP}</li>
 *   <li>Endereço remoto direto</li>
 * </ol>
 * 
 * @author Sistema de Auditoria
 * @version 1.0
 * @since 1.0
 * @see AuthenticationAuditObserver
 * @see AuditLogService
 * @see AuditLogFilter
 */
@ApplicationScoped
public class LoginAuditRouteFilter {

    /**
     * Serviço de auditoria para persistência dos eventos.
     */
    @Inject
    AuditLogService auditLogService;

    /**
     * Instância do Vert.x para execução em worker threads.
     */
    @Inject
    Vertx vertx;

    /**
     * Registra o filtro de auditoria no router do Vert.x.
     * 
     * <p>Usa CDI @Observes para capturar o evento de configuração
     * de filtros HTTP e registrar o handler de auditoria.</p>
     * 
     * <p>A prioridade 1000 garante execução após a autenticação
     * Elytron (que tem prioridade menor), permitindo acesso ao
     * resultado da autenticação.</p>
     * 
     * @param filters registro de filtros HTTP do Quarkus
     */
    public void registerRoute(@Observes io.quarkus.vertx.http.runtime.filters.Filters filters) {
        filters.register(createLoginAuditHandler(), 1000);
    }

    /**
     * Cria o handler Vert.x para interceptação de login.
     * 
     * <p>O handler:</p>
     * <ol>
     *   <li>Verifica se é POST para {@code /j_security_check}</li>
     *   <li>Adiciona body end handler para análise pós-resposta</li>
     *   <li>Analisa resultado e registra evento apropriado</li>
     *   <li>Chama {@code ctx.next()} para continuar o processamento</li>
     * </ol>
     * 
     * @return handler configurado para auditoria de login
     */
    private Handler<RoutingContext> createLoginAuditHandler() {
        return ctx -> {
            String path = ctx.normalizedPath();
            
            // Só processa requisições de login
            if ("/j_security_check".equals(path) && "POST".equalsIgnoreCase(ctx.request().method().name())) {
                // Adiciona um handler para executar após a resposta ser enviada
                ctx.addBodyEndHandler(v -> {
                    try {
                        int statusCode = ctx.response().getStatusCode();
                        String clientIp = resolveClientIp(ctx);
                        String userAgent = ctx.request().getHeader("User-Agent");
                        
                        // Verifica se o login foi bem-sucedido (redirect para landing page)
                        // e se há um usuário autenticado
                        if (statusCode == 302 || statusCode == 303) {
                            String location = ctx.response().headers().get("Location");
                            
                            // Se redireciona para a página de erro, é falha de login
                            if (location != null && location.contains("error")) {
                                String username = ctx.request().getFormAttribute("j_username");
                                // Executa em worker thread para permitir transações JTA
                                executeInWorkerThread(() -> recordLoginFailure(username, clientIp, userAgent));
                            } else {
                                // Login bem-sucedido - tenta obter o username
                                String username = null;
                                if (ctx.user() instanceof QuarkusHttpUser quarkusUser) {
                                    SecurityIdentity identity = quarkusUser.getSecurityIdentity();
                                    if (identity != null && !identity.isAnonymous()) {
                                        username = identity.getPrincipal().getName();
                                    }
                                }
                                
                                // Se não conseguiu pelo user, tenta pelo form
                                if (username == null || username.isBlank()) {
                                    username = ctx.request().getFormAttribute("j_username");
                                }
                                
                                if (username != null && !username.isBlank()) {
                                    final String finalUsername = username;
                                    // Executa em worker thread para permitir transações JTA
                                    executeInWorkerThread(() -> recordLoginSuccess(finalUsername, clientIp, userAgent));
                                }
                            }
                        }
                    } catch (Exception e) {
                        // Log silencioso para não quebrar o fluxo de autenticação
                        System.err.println("Erro ao registrar auditoria de login: " + e.getMessage());
                    }
                });
            }
            
            ctx.next();
        };
    }

    /**
     * Executa tarefa em worker thread do Vert.x.
     * 
     * <p>Necessário porque operações JPA/JTA não podem executar
     * no event loop do Vert.x. O worker thread pool permite
     * operações bloqueantes como persistência de banco de dados.</p>
     * 
     * @param task tarefa a executar no worker thread
     */
    private void executeInWorkerThread(Runnable task) {
        vertx.executeBlocking(promise -> {
            try {
                task.run();
                promise.complete();
            } catch (Exception e) {
                System.err.println("Erro ao executar tarefa de auditoria: " + e.getMessage());
                promise.fail(e);
            }
        }, false);
    }

    /**
     * Registra evento de login bem-sucedido.
     * 
     * @param username  nome do usuário autenticado
     * @param clientIp  IP do cliente
     * @param userAgent User-Agent do navegador
     */
    private void recordLoginSuccess(String username, String clientIp, String userAgent) {
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
     * Registra evento de falha de login.
     * 
     * @param username  nome de usuário tentado ou {@code null}
     * @param clientIp  IP do cliente
     * @param userAgent User-Agent do navegador
     */
    private void recordLoginFailure(String username, String clientIp, String userAgent) {
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
     * Resolve o IP real do cliente considerando proxies reversos.
     * 
     * @param ctx contexto de roteamento Vert.x
     * @return IP do cliente ou "unknown" se não disponível
     */
    private String resolveClientIp(RoutingContext ctx) {
        String forwarded = ctx.request().getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        String realIp = ctx.request().getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp;
        }
        if (ctx.request().remoteAddress() != null) {
            return ctx.request().remoteAddress().host();
        }
        return "unknown";
    }
}
