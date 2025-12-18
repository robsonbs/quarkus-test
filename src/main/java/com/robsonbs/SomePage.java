package com.robsonbs;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import static java.util.Objects.requireNonNull;

/**
 * Endpoint REST de exemplo demonstrando integração com Qute Templates.
 * 
 * <p>Esta classe é gerada pelo Quarkus como exemplo de renderização
 * de templates HTML usando o motor de templates Qute.</p>
 * 
 * <h2>Injeção de Template</h2>
 * <p>O template é injetado via construtor, seguindo o padrão de injeção
 * de dependência. O Quarkus automaticamente localiza o template
 * {@code page.qute.html} no diretório {@code templates/}.</p>
 * 
 * <h2>Uso</h2>
 * <pre>{@code
 * # Acessar a página
 * GET /some-page?name=Usuario
 * 
 * # O template recebe a variável "name" para renderização
 * }</pre>
 * 
 * <h2>Template Correspondente</h2>
 * <p>O arquivo {@code templates/page.qute.html} deve existir e pode
 * usar a variável {@code name} passada pelo método {@code get()}.</p>
 * 
 * @author Quarkus Starter
 * @version 1.0
 * @since 1.0
 * @see Template
 * @see TemplateInstance
 */
@Path("/some-page")
public class SomePage {

    /**
     * Template Qute para renderização da página.
     * Corresponde a {@code templates/page.qute.html}.
     */
    private final Template page;

    /**
     * Construtor com injeção do template.
     * 
     * @param page template Qute injetado pelo CDI
     * @throws NullPointerException se page for nulo
     */
    public SomePage(Template page) {
        this.page = requireNonNull(page, "page is required");
    }

    /**
     * Renderiza a página com o nome fornecido.
     * 
     * @param name nome a ser exibido no template (query parameter opcional)
     * @return instância do template com dados para renderização
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance get(@QueryParam("name") String name) {
        return page.data("name", name);
    }

}
