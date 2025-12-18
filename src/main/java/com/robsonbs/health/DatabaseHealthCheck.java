package com.robsonbs.health;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Liveness;
import org.eclipse.microprofile.health.Readiness;

/**
 * Health check para verificar a conectividade com o banco de dados.
 * 
 * <p>Este health check implementa tanto {@code @Liveness} quanto {@code @Readiness}
 * para ser usado pelo Kubernetes como probe de saúde da aplicação.</p>
 * 
 * <h2>Endpoints</h2>
 * <ul>
 *   <li>{@code /q/health/live} - Liveness probe</li>
 *   <li>{@code /q/health/ready} - Readiness probe</li>
 * </ul>
 * 
 * <h2>Verificação</h2>
 * <p>Executa uma query simples ({@code SELECT 1}) para validar que a conexão
 * com o PostgreSQL está funcionando corretamente.</p>
 * 
 * @author Sistema de Monitoramento
 * @version 1.0
 * @since 1.0
 * @see HealthCheck
 */
@Liveness
@Readiness
@ApplicationScoped
public class DatabaseHealthCheck implements HealthCheck {

    /**
     * EntityManager para executar queries de verificação.
     */
    @Inject
    EntityManager entityManager;

    /**
     * Executa a verificação de saúde do banco de dados.
     * 
     * @return resposta do health check indicando status UP ou DOWN
     */
    @Override
    public HealthCheckResponse call() {
        HealthCheckResponseBuilder builder = HealthCheckResponse.named("database");
        
        try {
            // Executa query simples para verificar conexão
            entityManager.createNativeQuery("SELECT 1").getSingleResult();
            
            return builder
                .up()
                .withData("type", "postgresql")
                .withData("status", "connected")
                .build();
                
        } catch (Exception e) {
            return builder
                .down()
                .withData("type", "postgresql")
                .withData("status", "disconnected")
                .withData("error", e.getMessage())
                .build();
        }
    }
}
