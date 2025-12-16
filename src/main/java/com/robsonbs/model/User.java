package com.robsonbs.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Representa um usuário (User) no sistema.
 * Contém informações de identificação, credenciais e o perfil de acesso.
 */
@Entity
@Table(name = "users")
public class User {

    /**
     * O identificador único do usuário.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * O nome completo do usuário.
     */
    @Column(nullable = false)
    private String name;

    /**
     * O endereço de e-mail do usuário, usado para login.
     * Deve ser único no sistema.
     */
    @Column(nullable = false, unique = true)
    private String email;

    /**
     * A senha do usuário, armazenada em formato de hash.
     */
    @Column(nullable = false)
    private String password;

    /**
     * O perfil de acesso do usuário (ex: ADMIN, USER).
     * Define as permissões do usuário no sistema.
     */
    @ManyToOne
    @JoinColumn(name = "profile_id")
    private UserProfile profile;

    /**
     * A data e hora em que o usuário foi registrado.
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Método executado antes da persistência inicial da entidade.
     * Define o timestamp de criação.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Constructors
    public User() {
    }

    public User(String name, String email, String password, UserProfile profile) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.profile = profile;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public UserProfile getProfile() {
        return profile;
    }

    public void setProfile(UserProfile profile) {
        this.profile = profile;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
