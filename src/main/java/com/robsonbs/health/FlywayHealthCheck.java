package com.robsonbs.health;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Readiness;
import org.flywaydb.core.Flyway;

/**
 * Health check para verificar o status das migrações Flyway.
 * 
 * <p>Este health check valida se todas as migrações de banco de dados
 * foram aplicadas corretamente. É usado como {@code @Readiness} probe
 * porque a aplicação só está pronta para receber tráfego quando o
 * schema do banco está atualizado.</p>
 * 
 * <h2>Estados Possíveis</h2>
 * <ul>
 *   <li><b>UP:</b> Todas as migrações foram aplicadas</li>
 *   <li><b>DOWN:</b> Existem migrações pendentes ou erro</li>
 * </ul>
 * 
 * <h2>Informações Expostas</h2>
 * <ul>
 *   <li>Status atual das migrações</li>
 *   <li>Versão atual do schema</li>
 *   <li>Número de migrações pendentes (se houver)</li>
 * </ul>
 * 
 * @author Sistema de Monitoramento
 * @version 1.0
 * @since 1.0
 * @see HealthCheck
 * @see Flyway
 */
@Readiness
@ApplicationScoped
public class FlywayHealthCheck implements HealthCheck {

    /**
     * Instância do Flyway para consultar informações de migração.
     */
    @Inject
    Flyway flyway;

    /**
     * Executa a verificação de saúde das migrações Flyway.
     * 
     * @return resposta do health check indicando status das migrações
     */
    @Override
    public HealthCheckResponse call() {
        HealthCheckResponseBuilder builder = HealthCheckResponse.named("flyway-migrations");
        
        try {
            var info = flyway.info();
            var current = info.current();
            var pending = info.pending();
            
            if (pending != null && pending.length > 0) {
                return builder
                    .down()
                    .withData("status", "pending-migrations")
                    .withData("pending", pending.length)
                    .build();
            }
            
            return builder
                .up()
                .withData("status", "up-to-date")
                .withData("current-version", current != null ? current.getVersion().toString() : "none")
                .build();
                
        } catch (Exception e) {
            return builder
                .down()
                .withData("error", e.getMessage())
                .build();
        }
    }
}
