package com.robsonbs.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Representa uma anotação (Note) no sistema.
 * Cada anotação pertence a um usuário e contém um título, conteúdo e timestamps de criação/atualização.
 */
@Entity
@Table(name = "notes")
public class Note {

    /**
     * O identificador único da anotação.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * O título da anotação. Não pode ser nulo.
     */
    @Column(nullable = false)
    private String title;

    /**
     * O conteúdo detalhado da anotação.
     */
    @Column(columnDefinition = "TEXT")
    private String content;

    /**
     * O usuário (proprietário) ao qual esta anotação pertence.
     * O relacionamento é lazy para otimizar o carregamento.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    /**
     * A data e hora em que a anotação foi criada.
     * Este campo não pode ser atualizado.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * A data e hora da última atualização da anotação.
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Método executado antes da persistência inicial da entidade.
     * Define o timestamp de criação.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /**
     * Método executado antes de uma atualização na entidade.
     * Define o timestamp de atualização.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
