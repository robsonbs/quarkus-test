package com.robsonbs.model;

import jakarta.persistence.*;
import java.util.List;

/**
 * Representa um perfil de usuário (UserProfile) no sistema.
 * Define um papel ou um conjunto de permissões (ex: "ADMIN", "USER").
 */
@Entity
@Table(name = "user_profiles")
public class UserProfile {

    /**
     * O identificador único do perfil.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * O nome do perfil (ex: "ADMIN"). Deve ser único.
     */
    @Column(nullable = false, unique = true)
    private String name;

    /**
     * A lista de usuários associados a este perfil.
     */
    @OneToMany(mappedBy = "profile")
    private List<User> users;

    // Constructors
    public UserProfile() {
    }

    public UserProfile(String name) {
        this.name = name;
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

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }
}
