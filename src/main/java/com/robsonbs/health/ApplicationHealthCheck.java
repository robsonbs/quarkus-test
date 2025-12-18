package com.robsonbs.health;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;

/**
 * Health check básico da aplicação.
 * 
 * <p>Verifica se a aplicação está em execução e responde a requisições.
 * Este é o health check mais básico, sempre retornando UP se a aplicação
 * estiver rodando.</p>
 * 
 * <h2>Informações Expostas</h2>
 * <ul>
 *   <li>Nome da aplicação</li>
 *   <li>Versão atual</li>
 *   <li>Status de execução</li>
 * </ul>
 * 
 * @author Sistema de Monitoramento
 * @version 1.0
 * @since 1.0
 * @see HealthCheck
 */
@Liveness
@ApplicationScoped
public class ApplicationHealthCheck implements HealthCheck {

    /**
     * Nome da aplicação para identificação nos health checks.
     */
    private static final String APPLICATION_NAME = "quarkus-test";
    
    /**
     * Versão atual da aplicação.
     */
    private static final String APPLICATION_VERSION = "1.0.0";

    /**
     * Executa a verificação de saúde da aplicação.
     * 
     * @return resposta do health check sempre UP com metadados
     */
    @Override
    public HealthCheckResponse call() {
        return HealthCheckResponse.named("application")
            .up()
            .withData("name", APPLICATION_NAME)
            .withData("version", APPLICATION_VERSION)
            .withData("status", "running")
            .build();
    }
}
