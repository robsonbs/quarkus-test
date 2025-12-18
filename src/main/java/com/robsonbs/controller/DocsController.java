package com.robsonbs.controller;

import com.robsonbs.view.BreadcrumbItem;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import io.smallrye.common.annotation.Blocking;

import java.util.List;

/**
 * Controller responsável pela página de documentação do sistema.
 * 
 * <p>Este controller serve a página de documentação que contém
 * informações sobre o uso do sistema, APIs disponíveis e guias
 * para desenvolvedores e usuários.</p>
 * 
 * <h2>Características</h2>
 * <ul>
 *   <li><strong>Rota:</strong> {@code GET /docs}</li>
 *   <li><strong>Acesso:</strong> Público (ou conforme configuração)</li>
 *   <li><strong>Template:</strong> {@code templates/docs.html}</li>
 *   <li><strong>Breadcrumb:</strong> Início → Documentação</li>
 * </ul>
 * 
 * <h2>Navegação</h2>
 * <p>A página inclui breadcrumb para navegação consistente:</p>
 * <pre>{@code
 * Início > Documentação
 * }</pre>
 * 
 * <h2>Conteúdo da Documentação</h2>
 * <p>O template {@code docs.html} tipicamente contém:</p>
 * <ul>
 *   <li>Visão geral do sistema</li>
 *   <li>Guia de uso para usuários finais</li>
 *   <li>Documentação de APIs (se aplicável)</li>
 *   <li>Informações técnicas para desenvolvedores</li>
 * </ul>
 * 
 * @author Sistema Web
 * @version 1.0
 * @since 1.0
 * @see BreadcrumbItem
 */
@Path("/docs")
@Blocking
public class DocsController {

    /**
     * Template Qute para a página de documentação.
     * Corresponde ao arquivo {@code templates/docs.html}.
     */
    @Inject
    Template docs;

    /**
     * Renderiza a página de documentação do sistema.
     * 
     * <p>Prepara o breadcrumb de navegação e renderiza o template
     * de documentação com conteúdo estático ou dinâmico.</p>
     * 
     * @return instância do template com breadcrumb configurado
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance documentation() {
        return docs
                .data("breadcrumb", List.of(
                        BreadcrumbItem.link("Início", "/"),
                        BreadcrumbItem.current("Documentação")
                ));
    }
}
