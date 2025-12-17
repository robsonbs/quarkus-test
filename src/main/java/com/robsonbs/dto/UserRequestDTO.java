package com.robsonbs.dto;

import org.jboss.resteasy.reactive.RestForm;

public class UserRequestDTO {
    @RestForm
    private String name;
    
    @RestForm
    private String email;
    
    @RestForm
    private String password;
    
    @RestForm
    private Long profileId;

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Long getProfileId() {
        return profileId;
    }

    public void setProfileId(Long profileId) {
        this.profileId = profileId;
    }
}
