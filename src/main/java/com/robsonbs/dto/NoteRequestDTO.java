package com.robsonbs.dto;

import org.jboss.resteasy.reactive.RestForm;

public class NoteRequestDTO {
    @RestForm
    private String title;
    
    @RestForm
    private String content;

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
