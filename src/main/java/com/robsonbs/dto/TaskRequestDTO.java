package com.robsonbs.dto;

import org.jboss.resteasy.reactive.RestForm;

public class TaskRequestDTO {

    @RestForm
    private String title;
    
    @RestForm
    private String description;
    
    @RestForm
    private String dueDate;
    
    @RestForm
    private String status;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
