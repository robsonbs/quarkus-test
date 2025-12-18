package com.robsonbs.controller;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.smallrye.common.annotation.Blocking;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * Controller responsável pela página inicial (home) do sistema.
 * 
 * <p>Este controller serve a landing page principal da aplicação,
 * acessível na raiz do contexto ({@code /}). A página inicial
 * é pública e serve como ponto de entrada para o sistema.</p>
 * 
 * <h2>Características</h2>
 * <ul>
 *   <li><strong>Rota:</strong> {@code GET /}</li>
 *   <li><strong>Acesso:</strong> Público (sem autenticação necessária)</li>
 *   <li><strong>Template:</strong> {@code templates/index.html}</li>
 *   <li><strong>Tipo de resposta:</strong> HTML</li>
 * </ul>
 * 
 * <h2>Arquitetura</h2>
 * <p>O controller usa a anotação {@code @Blocking} pois renderiza
 * templates Qute que podem realizar operações de I/O. Embora esta
 * página específica não acesse banco de dados, manter a anotação
 * garante consistência com outros controllers e evita problemas
 * caso a template seja expandida no futuro.</p>
 * 
 * <h2>Exemplo de Acesso</h2>
 * <pre>{@code
 * // URL de acesso
 * http://localhost:8080/
 * 
 * // Resposta: página HTML renderizada pelo template index.html
 * }</pre>
 * 
 * @author Sistema Web
 * @version 1.0
 * @since 1.0
 * @see io.quarkus.qute.Template
 */
@Path("/")
@Blocking
public class HomeController {

    /**
     * Template Qute para a página inicial.
     * 
     * <p>Injetado automaticamente pelo CDI com base no nome do campo.
     * Corresponde ao arquivo {@code templates/index.html}.</p>
     */
    @Inject
    Template index;

    /**
     * Renderiza a página inicial do sistema.
     * 
     * <p>Este endpoint serve a landing page principal, tipicamente
     * contendo links de navegação para login e informações sobre
     * o sistema.</p>
     * 
     * @return instância do template renderizada como HTML
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance home() {
        return index.instance();
    }
}
