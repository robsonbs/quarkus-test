package com.robsonbs.dao;

import com.robsonbs.model.UserProfile;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

/**
 * Repositório de acesso a dados (DAO) para a entidade {@link UserProfile}.
 * 
 * <p>Esta classe implementa o padrão Data Access Object utilizando o Quarkus Panache,
 * fornecendo operações de persistência para perfis de usuário (roles).</p>
 * 
 * <h2>Função no Sistema</h2>
 * <p>Os perfis de usuário definem as permissões de acesso no sistema.
 * Este DAO é utilizado pelo {@link com.robsonbs.service.UserProfileService}
 * para gerenciar os perfis disponíveis (ADMIN, USER, etc.).</p>
 * 
 * <h2>Padrão de Arquitetura</h2>
 * <pre>
 * Controller → Service → DAO → Database
 * </pre>
 * 
 * <h2>Métodos Herdados do Panache</h2>
 * <ul>
 *   <li>{@code persist(entity)} - Persiste novo perfil</li>
 *   <li>{@code findById(id)} - Busca por ID</li>
 *   <li>{@code listAll()} - Lista todos os perfis</li>
 *   <li>{@code delete(entity)} - Remove perfil</li>
 *   <li>{@code count()} - Conta perfis</li>
 * </ul>
 * 
 * <h2>Exemplo de Uso</h2>
 * <pre>{@code
 * @Inject
 * UserProfileDao profileDao;
 * 
 * // Buscar perfil por nome
 * Optional<UserProfile> admin = profileDao.findByName("ADMIN");
 * 
 * // Listar todos os perfis
 * List<UserProfile> profiles = profileDao.listAll();
 * }</pre>
 * 
 * @author Sistema de Gestão de Notas e Tarefas
 * @version 1.0
 * @since 2025-12-01
 * @see UserProfile
 * @see com.robsonbs.service.UserProfileService
 * @see PanacheRepository
 */
@ApplicationScoped
public class UserProfileDao implements PanacheRepository<UserProfile> {

	/**
	 * Busca um perfil pelo nome (role).
	 * 
	 * <p>Este método é utilizado para:</p>
	 * <ul>
	 *   <li>Validação de unicidade antes de criar perfil</li>
	 *   <li>Buscar perfil para associar a usuário</li>
	 *   <li>Verificar existência de perfil específico</li>
	 * </ul>
	 * 
	 * @param name o nome do perfil a buscar (ex: "ADMIN", "USER")
	 * @return {@link Optional} contendo o perfil se encontrado,
	 *         ou {@link Optional#empty()} caso contrário
	 * 
	 * @see com.robsonbs.service.UserProfileService#findByName(String)
	 */
	public Optional<UserProfile> findByName(String name) {
		return find("name", name).firstResultOptional();
	}
}
