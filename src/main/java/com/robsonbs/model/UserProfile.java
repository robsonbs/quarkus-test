package com.robsonbs.model;

import jakarta.persistence.*;
import java.util.List;

/**
 * Entidade JPA que representa um perfil de usuário (role) no sistema.
 * 
 * <p>O perfil define um conjunto de permissões e controla o acesso às
 * funcionalidades do sistema. Cada {@link User} está associado a um perfil
 * que determina suas autorizações.</p>
 * 
 * <h2>Perfis Padrão</h2>
 * <table border="1">
 *   <tr><th>Perfil</th><th>Permissões</th></tr>
 *   <tr>
 *     <td>ADMIN</td>
 *     <td>Acesso total: gerenciar usuários, perfis, visualizar auditoria</td>
 *   </tr>
 *   <tr>
 *     <td>USER</td>
 *     <td>Acesso limitado: gerenciar próprias notas e tarefas</td>
 *   </tr>
 * </table>
 * 
 * <h2>Integração com Segurança</h2>
 * <p>O nome do perfil é usado como role pelo Quarkus Elytron JDBC.
 * Controllers usam {@code @RolesAllowed("ADMIN")} ou {@code @RolesAllowed({"USER", "ADMIN"})}
 * para restringir acesso baseado no perfil do usuário autenticado.</p>
 * 
 * <h2>Mapeamento de Banco de Dados</h2>
 * <pre>
 * CREATE TABLE user_profiles (
 *     id BIGSERIAL PRIMARY KEY,
 *     name VARCHAR(255) NOT NULL UNIQUE
 * );
 * </pre>
 * 
 * <h2>Exemplo de Uso</h2>
 * <pre>{@code
 * UserProfile profile = new UserProfile("MANAGER");
 * userProfileDao.persist(profile);
 * 
 * // Associar a um usuário
 * user.setProfile(profile);
 * }</pre>
 * 
 * @author Sistema de Gestão de Notas e Tarefas
 * @version 1.0
 * @since 2025-12-01
 * @see User
 * @see com.robsonbs.dao.UserProfileDao
 * @see com.robsonbs.service.UserProfileService
 */
@Entity
@Table(name = "user_profiles")
public class UserProfile {

    /**
     * Identificador único do perfil, gerado automaticamente pelo banco de dados.
     * <p>Utiliza estratégia IDENTITY para geração automática de sequência.</p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nome do perfil (role), usado para identificação e autorização.
     * <p>Deve ser único no sistema. Convenção: usar UPPER_CASE
     * (ex: ADMIN, USER, MANAGER).</p>
     * <p>Este valor é usado pelo Elytron como role para controle de acesso.</p>
     * 
     * @see com.robsonbs.service.UserProfileService#isNameUnique(String, Long)
     */
    @Column(nullable = false, unique = true)
    private String name;

    /**
     * Lista de usuários associados a este perfil.
     * <p>Relacionamento One-to-Many bidirecional com {@link User}.
     * O lado "many" (User) é o proprietário do relacionamento.</p>
     * <p><strong>Nota:</strong> Usar com cuidado devido ao carregamento
     * potencialmente grande de dados.</p>
     */
    @OneToMany(mappedBy = "profile")
    private List<User> users;

    /**
     * Construtor padrão requerido pelo JPA.
     */
    public UserProfile() {
    }

    /**
     * Construtor que inicializa o perfil com um nome.
     * 
     * @param name o nome do perfil (ex: "ADMIN", "USER")
     */
    public UserProfile(String name) {
        this.name = name;
    }

    /**
     * Obtém o identificador único do perfil.
     * @return o ID do perfil ou {@code null} se ainda não persistido
     */
    public Long getId() {
        return id;
    }

    /**
     * Define o identificador do perfil.
     * <p><strong>Nota:</strong> Normalmente não deve ser chamado diretamente,
     * pois o ID é gerado automaticamente pelo banco de dados.</p>
     * @param id o novo ID do perfil
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Obtém o nome do perfil.
     * @return o nome do perfil (role)
     */
    public String getName() {
        return name;
    }

    /**
     * Define o nome do perfil.
     * <p><strong>Nota:</strong> O nome deve ser único no sistema.
     * A validação de unicidade é feita no {@link com.robsonbs.service.UserProfileService}.</p>
     * @param name o novo nome do perfil
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Obtém a lista de usuários associados a este perfil.
     * <p><strong>Cuidado:</strong> Esta operação pode carregar muitos dados
     * se houver muitos usuários com este perfil.</p>
     * @return lista de usuários ou {@code null}
     */
    public List<User> getUsers() {
        return users;
    }

    /**
     * Define a lista de usuários associados a este perfil.
     * @param users a nova lista de usuários
     */
    public void setUsers(List<User> users) {
        this.users = users;
    }
}
