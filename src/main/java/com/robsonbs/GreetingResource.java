package com.robsonbs;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * Recurso REST de exemplo para verificação da aplicação.
 * 
 * <p>Este endpoint simples é gerado automaticamente pelo Quarkus e serve
 * como exemplo de implementação JAX-RS e para testes de saúde básicos.</p>
 * 
 * <h2>Uso</h2>
 * <pre>{@code
 * # Testar o endpoint
 * curl http://localhost:8080/hello
 * # Retorna: "Hello from RESTEasy Reactive"
 * }</pre>
 * 
 * <h2>Testes</h2>
 * <p>Este recurso é coberto por {@code GreetingResourceTest} que valida
 * a resposta e o status HTTP.</p>
 * 
 * @author Quarkus Starter
 * @version 1.0
 * @since 1.0
 * @see jakarta.ws.rs.Path
 */
@Path("/hello")
public class GreetingResource {

    /**
     * Retorna uma mensagem de saudação simples.
     * 
     * <p>Endpoint de teste para verificar que a aplicação está
     * funcionando corretamente.</p>
     * 
     * @return mensagem de saudação em texto plano
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "Hello from RESTEasy Reactive";
    }
}
