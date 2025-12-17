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

@Path("/docs")
@Blocking
public class DocsController {

    @Inject
    Template docs;

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
