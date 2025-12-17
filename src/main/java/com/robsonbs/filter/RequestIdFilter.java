package com.robsonbs.filter;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.MDC;

import java.io.IOException;
import java.util.UUID;

/**
 * Filtro JAX-RS para injeção de Request ID em todas as requisições.
 * 
 * <p>Este filtro implementa o padrão de correlação de requisições (Correlation ID),
 * essencial para rastreabilidade em ambientes distribuídos e conformidade com o
 * <strong>Twelve-Factor App - Fator XI (Logs)</strong>.</p>
 * 
 * <h2>Funcionamento</h2>
 * <ol>
 *   <li><strong>Request:</strong> Gera ou reutiliza um UUID único para a requisição</li>
 *   <li><strong>MDC:</strong> Injeta o ID no Mapped Diagnostic Context para logging</li>
 *   <li><strong>Response:</strong> Retorna o ID no header {@code X-Request-ID}</li>
 *   <li><strong>Cleanup:</strong> Remove o ID do MDC após a resposta</li>
 * </ol>
 * 
 * <h2>Uso em Logs</h2>
 * <p>Com o MDC configurado, todos os logs emitidos durante o processamento
 * da requisição incluirão automaticamente o {@code requestId}, permitindo
 * correlacionar eventos em ferramentas como ELK, Splunk ou Loki.</p>
 * 
 * <h2>Header de Entrada</h2>
 * <p>Se o cliente enviar o header {@code X-Request-ID}, o filtro reutiliza
 * esse valor, permitindo rastreamento end-to-end em arquiteturas de microserviços.</p>
 * 
 * <h2>Exemplo de Log JSON</h2>
 * <pre>{@code
 * {
 *   "timestamp": "2025-01-15T10:30:00Z",
 *   "level": "INFO",
 *   "message": "User created task",
 *   "mdc": {
 *     "requestId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
 *   }
 * }
 * }</pre>
 * 
 * @author Twelve-Factor Implementation
 * @version 1.0
 * @since 1.0
 * @see ContainerRequestFilter
 * @see ContainerResponseFilter
 */
@Provider
@Priority(Priorities.HEADER_DECORATOR)
public class RequestIdFilter implements ContainerRequestFilter, ContainerResponseFilter {

    /**
     * Nome do header HTTP para o Request ID.
     */
    private static final String REQUEST_ID_HEADER = "X-Request-ID";

    /**
     * Chave do MDC para o Request ID.
     */
    private static final String MDC_REQUEST_ID = "requestId";

    /**
     * Chave do contexto da requisição para armazenar o Request ID.
     */
    private static final String CONTEXT_REQUEST_ID = "quarkus.request.id";

    /**
     * Intercepta a requisição de entrada e injeta o Request ID no MDC.
     * 
     * <p>Se o header {@code X-Request-ID} estiver presente, reutiliza o valor.
     * Caso contrário, gera um novo UUID.</p>
     *
     * @param requestContext contexto da requisição
     * @throws IOException em caso de erro de I/O
     */
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String requestId = requestContext.getHeaderString(REQUEST_ID_HEADER);
        
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }
        
        // Armazena no contexto para recuperar na resposta
        requestContext.setProperty(CONTEXT_REQUEST_ID, requestId);
        
        // Injeta no MDC para logging estruturado
        MDC.put(MDC_REQUEST_ID, requestId);
    }

    /**
     * Intercepta a resposta e adiciona o Request ID no header de saída.
     * 
     * <p>Também remove o Request ID do MDC para evitar vazamento
     * de contexto entre requisições (especialmente em threads reutilizadas).</p>
     *
     * @param requestContext contexto da requisição
     * @param responseContext contexto da resposta
     * @throws IOException em caso de erro de I/O
     */
    @Override
    public void filter(ContainerRequestContext requestContext, 
                       ContainerResponseContext responseContext) throws IOException {
        // Recupera o Request ID do contexto
        String requestId = (String) requestContext.getProperty(CONTEXT_REQUEST_ID);
        
        if (requestId != null) {
            // Adiciona ao header de resposta para rastreabilidade do cliente
            responseContext.getHeaders().add(REQUEST_ID_HEADER, requestId);
        }
        
        // Limpa o MDC para evitar vazamento entre requisições
        MDC.remove(MDC_REQUEST_ID);
    }
}
