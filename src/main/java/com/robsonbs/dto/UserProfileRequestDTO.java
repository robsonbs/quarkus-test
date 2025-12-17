package com.robsonbs.dto;

import jakarta.validation.constraints.NotBlank;
import org.jboss.resteasy.reactive.RestForm;

public class UserProfileRequestDTO {

    @RestForm
    @NotBlank(message = "Nome do perfil é obrigatório")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
