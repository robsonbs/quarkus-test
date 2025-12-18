package com.robsonbs.dto;

import com.robsonbs.model.User;

import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) para retornar dados de usuário para a camada de apresentação.
 * 
 * <p>Este DTO é utilizado para transferir dados de usuário para templates Qute
 * e respostas de API, protegendo a entidade JPA de exposição direta e permitindo
 * formatação adequada dos dados para exibição.</p>
 * 
 * <h2>Segurança</h2>
 * <p><strong>IMPORTANTE:</strong> Este DTO NÃO inclui o campo {@code password},
 * garantindo que a senha (mesmo hasheada) nunca seja exposta ao front-end.</p>
 * 
 * <h2>Campos Expostos</h2>
 * <table border="1">
 *   <tr><th>Campo</th><th>Tipo</th><th>Descrição</th></tr>
 *   <tr><td>id</td><td>Long</td><td>Identificador único do usuário</td></tr>
 *   <tr><td>name</td><td>String</td><td>Nome completo</td></tr>
 *   <tr><td>email</td><td>String</td><td>E-mail de login</td></tr>
 *   <tr><td>profileName</td><td>String</td><td>Nome do perfil (role)</td></tr>
 *   <tr><td>profileId</td><td>Long</td><td>ID do perfil</td></tr>
 *   <tr><td>createdAt</td><td>LocalDateTime</td><td>Data de criação</td></tr>
 * </table>
 * 
 * <h2>Conversão de Entidade</h2>
 * <p>A conversão de {@link User} para DTO é feita no construtor,
 * garantindo que o perfil seja carregado de forma segura (null-safe).</p>
 * 
 * <h2>Exemplo de Uso</h2>
 * <pre>{@code
 * // No Service
 * public List<UserResponseDTO> findAll() {
 *     return userDao.listAll().stream()
 *         .map(UserResponseDTO::new)
 *         .toList();
 * }
 * 
 * // No Template Qute
 * {#for user in users}
 *     <tr>
 *         <td>{user.name}</td>
 *         <td>{user.email}</td>
 *         <td>{user.profileName}</td>
 *     </tr>
 * {/for}
 * }</pre>
 * 
 * @author Sistema de Gestão de Notas e Tarefas
 * @version 1.0
 * @since 2025-12-01
 * @see User
 * @see UserRequestDTO
 * @see com.robsonbs.service.UserService
 */
public class UserResponseDTO {
    
    /** Identificador único do usuário. */
    private Long id;
    
    /** Nome completo do usuário. */
    private String name;
    
    /** Endereço de e-mail (login). */
    private String email;
    
    /** Nome do perfil de acesso (ex: "ADMIN", "USER"). */
    private String profileName;
    
    /** ID do perfil de acesso. */
    private Long profileId;
    
    /** Data e hora de criação do registro. */
    private LocalDateTime createdAt;

    /**
     * Construtor que converte uma entidade {@link User} para DTO.
     * 
     * <p>Realiza a conversão de forma null-safe para o perfil associado,
     * evitando NullPointerException quando o usuário não possui perfil.</p>
     * 
     * @param user a entidade User a ser convertida
     */
    public UserResponseDTO(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.createdAt = user.getCreatedAt();
        if (user.getProfile() != null) {
            this.profileName = user.getProfile().getName();
            this.profileId = user.getProfile().getId();
        }
    }

    /**
     * Obtém o ID do usuário.
     * @return o identificador único
     */
    public Long getId() {
        return id;
    }

    /**
     * Define o ID do usuário.
     * @param id o identificador único
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Obtém o nome do usuário.
     * @return o nome completo
     */
    public String getName() {
        return name;
    }

    /**
     * Define o nome do usuário.
     * @param name o nome completo
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Obtém o e-mail do usuário.
     * @return o endereço de e-mail
     */
    public String getEmail() {
        return email;
    }

    /**
     * Define o e-mail do usuário.
     * @param email o endereço de e-mail
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Obtém o nome do perfil de acesso.
     * @return o nome do perfil ou {@code null}
     */
    public String getProfileName() {
        return profileName;
    }

    /**
     * Define o nome do perfil de acesso.
     * @param profileName o nome do perfil
     */
    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    /**
     * Obtém o ID do perfil de acesso.
     * @return o ID do perfil ou {@code null}
     */
    public Long getProfileId() {
        return profileId;
    }

    /**
     * Define o ID do perfil de acesso.
     * @param profileId o ID do perfil
     */
    public void setProfileId(Long profileId) {
        this.profileId = profileId;
    }

    /**
     * Obtém a data de criação do registro.
     * @return o timestamp de criação
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Define a data de criação do registro.
     * @param createdAt o timestamp de criação
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
