package com.robsonbs.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidade JPA que representa um usuário do sistema.
 * 
 * <p>O usuário é a entidade central de autenticação e autorização do sistema.
 * Cada usuário possui credenciais de acesso (e-mail/senha) e está associado a
 * um {@link UserProfile} que define suas permissões no sistema.</p>
 * 
 * <h2>Características Principais</h2>
 * <ul>
 *   <li><strong>Autenticação:</strong> Login via e-mail e senha (hash BCrypt)</li>
 *   <li><strong>Autorização:</strong> Perfil define roles (ADMIN, USER)</li>
 *   <li><strong>Unicidade:</strong> E-mail é único no sistema</li>
 *   <li><strong>Relacionamentos:</strong> Possui notas e tarefas associadas</li>
 * </ul>
 * 
 * <h2>Segurança</h2>
 * <p>As senhas são armazenadas como hash BCrypt, nunca em texto plano.
 * O hash é gerado pelo {@link com.robsonbs.service.UserService} usando
 * {@link io.quarkus.elytron.security.common.BcryptUtil}.</p>
 * 
 * <h2>Mapeamento de Banco de Dados</h2>
 * <pre>
 * CREATE TABLE users (
 *     id BIGSERIAL PRIMARY KEY,
 *     name VARCHAR(255) NOT NULL,
 *     email VARCHAR(255) NOT NULL UNIQUE,
 *     password VARCHAR(255) NOT NULL,
 *     profile_id BIGINT REFERENCES user_profiles(id),
 *     created_at TIMESTAMP NOT NULL
 * );
 * </pre>
 * 
 * <h2>Integração com Elytron Security</h2>
 * <p>O sistema usa Quarkus Elytron JDBC para autenticação. A tabela {@code users}
 * é consultada para validar credenciais, e a role é obtida do perfil associado.</p>
 * 
 * <h2>Exemplo de Uso</h2>
 * <pre>{@code
 * User user = new User();
 * user.setName("João Silva");
 * user.setEmail("joao@example.com");
 * user.setPassword(BcryptUtil.bcryptHash("senha123"));
 * user.setProfile(userProfile);
 * userDao.persist(user);
 * }</pre>
 * 
 * @author Sistema de Gestão de Notas e Tarefas
 * @version 1.0
 * @since 2025-12-01
 * @see UserProfile
 * @see com.robsonbs.dao.UserDao
 * @see com.robsonbs.service.UserService
 */
@Entity
@Table(name = "users")
public class User {

    /**
     * Identificador único do usuário, gerado automaticamente pelo banco de dados.
     * <p>Utiliza estratégia IDENTITY para geração automática de sequência.</p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nome completo do usuário.
     * <p>Campo obrigatório usado para exibição na interface do sistema.</p>
     */
    @Column(nullable = false)
    private String name;

    /**
     * Endereço de e-mail do usuário, usado como identificador de login.
     * <p>Deve ser único no sistema. Utilizado pelo Elytron JDBC para
     * autenticação e pelo sistema para identificar o proprietário de recursos.</p>
     * 
     * @see com.robsonbs.service.UserService#isEmailUnique(String, Long)
     */
    @Column(nullable = false, unique = true)
    private String email;

    /**
     * Hash BCrypt da senha do usuário.
     * <p><strong>IMPORTANTE:</strong> Este campo armazena o hash da senha,
     * nunca a senha em texto plano. O hash é gerado usando
     * {@link io.quarkus.elytron.security.common.BcryptUtil#bcryptHash(String)}.</p>
     * 
     * @see com.robsonbs.service.UserService#create(com.robsonbs.dto.UserRequestDTO)
     */
    @Column(nullable = false)
    private String password;

    /**
     * Perfil de acesso do usuário.
     * <p>Relacionamento Many-to-One com {@link UserProfile}. O perfil define
     * as roles/permissões do usuário (ex: ADMIN, USER). Se nulo, o usuário
     * pode ter acesso limitado.</p>
     * 
     * @see UserProfile
     */
    @ManyToOne
    @JoinColumn(name = "profile_id")
    private UserProfile profile;

    /**
     * Data e hora de criação do registro do usuário.
     * <p>Preenchido automaticamente pelo callback {@link #onCreate()}
     * no momento da persistência inicial.</p>
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Callback JPA executado antes da persistência inicial.
     * <p>Define automaticamente o timestamp de criação com o momento atual.</p>
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /**
     * Construtor padrão requerido pelo JPA.
     */
    public User() {
    }

    /**
     * Construtor com todos os campos obrigatórios.
     * 
     * @param name     o nome completo do usuário
     * @param email    o endereço de e-mail (único)
     * @param password o hash BCrypt da senha
     * @param profile  o perfil de acesso do usuário
     */
    public User(String name, String email, String password, UserProfile profile) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.profile = profile;
    }

    /**
     * Obtém o identificador único do usuário.
     * @return o ID do usuário ou {@code null} se ainda não persistido
     */
    public Long getId() {
        return id;
    }

    /**
     * Define o identificador do usuário.
     * <p><strong>Nota:</strong> Normalmente não deve ser chamado diretamente,
     * pois o ID é gerado automaticamente pelo banco de dados.</p>
     * @param id o novo ID do usuário
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Obtém o nome completo do usuário.
     * @return o nome do usuário
     */
    public String getName() {
        return name;
    }

    /**
     * Define o nome completo do usuário.
     * @param name o novo nome (não pode ser nulo ou vazio)
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Obtém o endereço de e-mail do usuário.
     * @return o e-mail do usuário
     */
    public String getEmail() {
        return email;
    }

    /**
     * Define o endereço de e-mail do usuário.
     * <p><strong>Nota:</strong> O e-mail deve ser único no sistema.
     * A validação de unicidade é feita no {@link com.robsonbs.service.UserService}.</p>
     * @param email o novo e-mail
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Obtém o hash da senha do usuário.
     * <p><strong>AVISO:</strong> Este é o hash BCrypt, não a senha original.</p>
     * @return o hash BCrypt da senha
     */
    public String getPassword() {
        return password;
    }

    /**
     * Define o hash da senha do usuário.
     * <p><strong>IMPORTANTE:</strong> Este método espera receber um hash BCrypt,
     * não a senha em texto plano. Use {@link io.quarkus.elytron.security.common.BcryptUtil#bcryptHash(String)}
     * para gerar o hash antes de chamar este método.</p>
     * @param password o hash BCrypt da senha
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Obtém o perfil de acesso do usuário.
     * @return o perfil ou {@code null} se não associado
     */
    public UserProfile getProfile() {
        return profile;
    }

    /**
     * Define o perfil de acesso do usuário.
     * @param profile o novo perfil de acesso
     */
    public void setProfile(UserProfile profile) {
        this.profile = profile;
    }

    /**
     * Obtém a data e hora de criação do registro.
     * @return o timestamp de criação
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Define a data e hora de criação do registro.
     * <p><strong>Nota:</strong> Normalmente preenchido automaticamente
     * pelo callback {@link #onCreate()}.</p>
     * @param createdAt o timestamp de criação
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
