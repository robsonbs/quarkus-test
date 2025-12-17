package com.robsonbs.controller;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/login")
public class LoginController {

    @jakarta.inject.Inject
    Template login;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance login(@QueryParam("error") String error,
                                  @QueryParam("success") String success) {
        boolean hasError = error != null && !error.isBlank();
        String successMessage = (success != null && !success.isBlank()) ? success : null;
        return login
                .data("error", hasError)
                .data("success", successMessage);
    }
}
