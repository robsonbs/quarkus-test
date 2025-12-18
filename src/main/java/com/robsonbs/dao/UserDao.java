package com.robsonbs.dao;

import com.robsonbs.model.User;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;

/**
 * Repositório de acesso a dados (DAO) para a entidade {@link User}.
 * 
 * <p>Esta classe implementa o padrão Data Access Object utilizando o Quarkus Panache,
 * que fornece uma abstração sobre o JPA/Hibernate simplificando operações CRUD
 * e consultas personalizadas.</p>
 * 
 * <h2>Padrão de Arquitetura</h2>
 * <p>Segue o padrão DAO conforme requisitos do projeto:</p>
 * <pre>
 * Controller → Service → DAO → Database
 * </pre>
 * <p>O DAO é responsável exclusivamente pelo acesso a dados, sem conter
 * regras de negócio (que ficam no {@link com.robsonbs.service.UserService}).</p>
 * 
 * <h2>Métodos Herdados do Panache</h2>
 * <p>Além dos métodos definidos nesta classe, herda de {@link PanacheRepository}:</p>
 * <ul>
 *   <li>{@code persist(entity)} - Persiste nova entidade</li>
 *   <li>{@code findById(id)} - Busca por ID</li>
 *   <li>{@code listAll()} - Lista todos os registros</li>
 *   <li>{@code delete(entity)} - Remove entidade</li>
 *   <li>{@code deleteById(id)} - Remove por ID</li>
 *   <li>{@code count()} - Conta registros</li>
 *   <li>{@code find(query, params)} - Busca com query JPQL</li>
 * </ul>
 * 
 * <h2>Exemplo de Uso</h2>
 * <pre>{@code
 * @Inject
 * UserDao userDao;
 * 
 * // Buscar por e-mail
 * Optional<User> user = userDao.findByEmail("admin@example.com");
 * 
 * // Listar todos
 * List<User> users = userDao.listAll();
 * 
 * // Persistir novo usuário
 * userDao.persist(newUser);
 * }</pre>
 * 
 * @author Sistema de Gestão de Notas e Tarefas
 * @version 1.0
 * @since 2025-12-01
 * @see User
 * @see com.robsonbs.service.UserService
 * @see PanacheRepository
 */
@ApplicationScoped
public class UserDao implements PanacheRepository<User> {

    /**
     * Busca um usuário pelo endereço de e-mail.
     * 
     * <p>Este método é utilizado principalmente para:</p>
     * <ul>
     *   <li>Autenticação - verificar se usuário existe</li>
     *   <li>Validação de unicidade - antes de criar/atualizar</li>
     *   <li>Identificação de proprietário de recursos</li>
     * </ul>
     * 
     * @param email o endereço de e-mail a buscar (case-sensitive)
     * @return {@link Optional} contendo o usuário se encontrado,
     *         ou {@link Optional#empty()} caso contrário
     * 
     * @see com.robsonbs.service.UserService#findByEmail(String)
     * @see com.robsonbs.service.AuthService
     */
    public Optional<User> findByEmail(String email) {
        return find("email", email).firstResultOptional();
    }
}
