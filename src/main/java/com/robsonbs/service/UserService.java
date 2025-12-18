package com.robsonbs.service;

import com.robsonbs.dao.UserDao;
import com.robsonbs.dao.UserProfileDao;
import com.robsonbs.dto.UserRequestDTO;
import com.robsonbs.model.User;
import com.robsonbs.model.UserProfile;
import com.robsonbs.service.AuditLogService;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.util.List;

/**
 * Serviço de negócio para gerenciamento de usuários do sistema.
 * 
 * <p>Esta classe implementa a camada de regras de negócio (Business Object)
 * para operações CRUD de usuários, incluindo validações, segurança de senhas
 * e integração com auditoria.</p>
 * 
 * <h2>Responsabilidades</h2>
 * <ul>
 *   <li><strong>Criação:</strong> Validação de senha, verificação de unicidade
 *       de email, hash bcrypt da senha, associação de perfil</li>
 *   <li><strong>Atualização:</strong> Revalidação de email único, atualização
 *       condicional de senha, manutenção de perfil</li>
 *   <li><strong>Exclusão:</strong> Remoção segura com auditoria</li>
 *   <li><strong>Consulta:</strong> Busca por ID com tratamento de não encontrado</li>
 * </ul>
 * 
 * <h2>Segurança de Senhas</h2>
 * <p>Todas as senhas são processadas com {@link BcryptUtil#bcryptHash(String)}
 * antes da persistência. Requisitos de senha:</p>
 * <ul>
 *   <li>Obrigatória (não pode ser nula ou em branco)</li>
 *   <li>Mínimo de 6 caracteres</li>
 * </ul>
 * 
 * <h2>Unicidade de Email</h2>
 * <p>O sistema garante que cada email seja único através do método
 * {@link #ensureEmailAvailable(String, Long)}. Na atualização, o email
 * do próprio usuário é ignorado na verificação de duplicidade.</p>
 * 
 * <h2>Auditoria</h2>
 * <p>Todas as operações de escrita (criar, atualizar, deletar) são
 * registradas via {@link AuditLogService#recordDomainEvent} com:</p>
 * <ul>
 *   <li>Ator: email do usuário autenticado ou "system"</li>
 *   <li>Ação: USER_CREATED, USER_UPDATED, USER_DELETED</li>
 *   <li>Entidade: tipo "User" com ID do registro</li>
 * </ul>
 * 
 * <h2>Exemplo de Uso</h2>
 * <pre>{@code
 * @Inject
 * UserService userService;
 * 
 * // Criar novo usuário
 * UserRequestDTO dto = new UserRequestDTO();
 * dto.setName("João Silva");
 * dto.setEmail("joao@example.com");
 * dto.setPassword("senha123");
 * dto.setProfileId(1L);
 * userService.save(dto);
 * 
 * // Buscar usuário
 * User user = userService.findById(1L);
 * 
 * // Atualizar (senha opcional)
 * dto.setPassword(null); // mantém senha atual
 * userService.update(1L, dto);
 * 
 * // Remover
 * userService.delete(1L);
 * }</pre>
 * 
 * @author Sistema de Gerenciamento
 * @version 1.0
 * @since 1.0
 * @see User
 * @see UserDao
 * @see UserRequestDTO
 * @see AuditLogService
 */
@ApplicationScoped
public class UserService {

    /**
     * DAO para operações de persistência de usuários.
     * Injetado pelo CDI para acesso à camada de dados.
     */
    @Inject
    UserDao userDao;

    /**
     * DAO para operações de persistência de perfis de usuário.
     * Usado para resolver e validar o perfil associado ao usuário.
     */
    @Inject
    UserProfileDao userProfileDao;

    /**
     * Serviço de auditoria para registro de eventos de domínio.
     * Registra todas as operações de escrita (CRUD) para rastreabilidade.
     */
    @Inject
    AuditLogService auditLogService;

    /**
     * Identidade de segurança do usuário autenticado.
     * Usada para determinar o ator responsável pelas ações auditadas.
     */
    @Inject
    SecurityIdentity securityIdentity;

    /**
     * Lista todos os usuários cadastrados no sistema.
     * 
     * <p>Retorna a lista completa de usuários sem paginação.
     * Para grandes volumes de dados, considere implementar paginação.</p>
     * 
     * @return lista de todos os usuários; nunca {@code null}, pode ser vazia
     */
    public List<User> listAll() {
        return userDao.listAll();
    }

    /**
     * Cria um novo usuário no sistema.
     * 
     * <p>Processo de criação:</p>
     * <ol>
     *   <li>Valida requisitos de senha (obrigatória, mínimo 6 caracteres)</li>
     *   <li>Verifica se email já está em uso</li>
     *   <li>Cria entidade com dados do DTO</li>
     *   <li>Aplica hash bcrypt na senha</li>
     *   <li>Resolve e associa perfil de usuário</li>
     *   <li>Persiste no banco de dados</li>
     *   <li>Registra evento de auditoria USER_CREATED</li>
     * </ol>
     * 
     * @param userRequestDTO dados do novo usuário; não pode ser {@code null}
     * @throws BadRequestException se senha for nula, em branco ou menor que 6 caracteres
     * @throws WebApplicationException com status CONFLICT (409) se email já existir
     * @throws BadRequestException se profileId for {@code null}
     * @throws NotFoundException se perfil não existir
     */
    @Transactional
    public void save(UserRequestDTO userRequestDTO) {
        validatePassword(userRequestDTO.getPassword());
        ensureEmailAvailable(userRequestDTO.getEmail(), null);

        User user = new User();
        user.setName(userRequestDTO.getName());
        user.setEmail(userRequestDTO.getEmail());
        user.setPassword(BcryptUtil.bcryptHash(userRequestDTO.getPassword()));

        UserProfile profile = resolveProfile(userRequestDTO.getProfileId());
        user.setProfile(profile);

        userDao.persist(user);
        userDao.flush();

        auditLogService.recordDomainEvent(
                currentActor(),
                "USER_CREATED",
                "/users",
                "User",
                user.getId() != null ? user.getId().toString() : null,
                "Usuário criado: " + sanitize(user.getEmail()),
                Response.Status.CREATED.getStatusCode()
        );
    }

    /**
     * Busca um usuário pelo seu identificador único.
     * 
     * @param id identificador do usuário; não pode ser {@code null}
     * @return usuário encontrado; nunca {@code null}
     * @throws NotFoundException se nenhum usuário com o ID especificado existir
     */
    public User findById(Long id) {
        User user = userDao.findById(id);
        if (user == null) {
            throw new NotFoundException("User not found");
        }
        return user;
    }

    /**
     * Atualiza um usuário existente.
     * 
     * <p>Processo de atualização:</p>
     * <ol>
     *   <li>Busca usuário existente (lança exceção se não encontrado)</li>
     *   <li>Atualiza nome</li>
     *   <li>Verifica unicidade do novo email (ignorando o próprio usuário)</li>
     *   <li>Atualiza email</li>
     *   <li>Se senha fornecida (não nula e não vazia), valida e atualiza com hash</li>
     *   <li>Resolve e atualiza perfil</li>
     *   <li>Registra evento de auditoria USER_UPDATED</li>
     * </ol>
     * 
     * <p><strong>Nota:</strong> A senha só é atualizada se um novo valor for fornecido.
     * Para manter a senha atual, passe {@code null} ou string vazia.</p>
     * 
     * @param id identificador do usuário a atualizar
     * @param userRequestDTO novos dados do usuário
     * @throws NotFoundException se usuário não existir
     * @throws WebApplicationException com CONFLICT se novo email já estiver em uso
     * @throws BadRequestException se nova senha não atender requisitos
     * @throws NotFoundException se novo perfil não existir
     */
    @Transactional
    public void update(Long id, UserRequestDTO userRequestDTO) {
        User user = findById(id);
        if (user == null) {
            throw new NotFoundException("User not found");
        }
        user.setName(userRequestDTO.getName());
        ensureEmailAvailable(userRequestDTO.getEmail(), id);
        user.setEmail(userRequestDTO.getEmail());
        if (userRequestDTO.getPassword() != null && !userRequestDTO.getPassword().isEmpty()) {
            validatePassword(userRequestDTO.getPassword());
            user.setPassword(BcryptUtil.bcryptHash(userRequestDTO.getPassword()));
        }

        UserProfile profile = resolveProfile(userRequestDTO.getProfileId());
        user.setProfile(profile);

        userDao.persist(user);
        auditLogService.recordDomainEvent(
                currentActor(),
                "USER_UPDATED",
                "/users/" + id,
                "User",
                user.getId() != null ? user.getId().toString() : null,
                "Usuário atualizado: " + sanitize(user.getEmail()),
                Response.Status.OK.getStatusCode()
        );
    }

    /**
     * Remove um usuário do sistema.
     * 
     * <p>A exclusão é física (hard delete). Após esta operação,
     * o usuário não poderá mais fazer login no sistema.</p>
     * 
     * @param id identificador do usuário a remover
     * @throws NotFoundException se usuário não existir
     */
    @Transactional
    public void delete(Long id) {
        boolean removed = userDao.deleteById(id);
        if (!removed) {
            throw new NotFoundException("User not found");
        }

        auditLogService.recordDomainEvent(
                currentActor(),
                "USER_DELETED",
                "/users/" + id + "/delete",
                "User",
                id != null ? id.toString() : null,
                "Usuário removido",
                Response.Status.OK.getStatusCode()
        );
    }

    /**
     * Resolve e valida o perfil de usuário pelo ID.
     * 
     * @param profileId identificador do perfil
     * @return perfil encontrado; nunca {@code null}
     * @throws BadRequestException se profileId for {@code null}
     * @throws NotFoundException se perfil não existir
     */
    private UserProfile resolveProfile(Long profileId) {
        if (profileId == null) {
            throw new BadRequestException("Profile is required");
        }
        UserProfile profile = userProfileDao.findById(profileId);
        if (profile == null) {
            throw new NotFoundException("Profile not found");
        }
        return profile;
    }

    /**
     * Verifica se o email está disponível para uso.
     * 
     * <p>Na criação (idToIgnore = null), qualquer email existente causa conflito.
     * Na atualização, o email do próprio usuário é permitido.</p>
     * 
     * @param email email a verificar
     * @param idToIgnore ID do usuário atual (para ignorar na verificação) ou {@code null}
     * @throws WebApplicationException com status CONFLICT se email já estiver em uso
     */
    private void ensureEmailAvailable(String email, Long idToIgnore) {
        userDao.findByEmail(email).ifPresent(existing -> {
            if (idToIgnore == null || !existing.getId().equals(idToIgnore)) {
                throw new WebApplicationException("Email já está em uso", Response.Status.CONFLICT);
            }
        });
    }

    /**
     * Valida os requisitos de segurança da senha.
     * 
     * @param password senha a validar
     * @throws BadRequestException se senha for nula, em branco ou menor que 6 caracteres
     */
    private void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new BadRequestException("Senha é obrigatória");
        }
        if (password.length() < 6) {
            throw new BadRequestException("Senha deve ter pelo menos 6 caracteres");
        }
    }

    /**
     * Obtém o identificador do ator atual (usuário autenticado).
     * 
     * @return email do usuário autenticado ou "system" se não autenticado
     */
    private String currentActor() {
        if (securityIdentity != null && !securityIdentity.isAnonymous() && securityIdentity.getPrincipal() != null) {
            return securityIdentity.getPrincipal().getName();
        }
        return "system";
    }

    /**
     * Sanitiza um valor para uso seguro em logs de auditoria.
     * 
     * <p>Trunca valores longos para evitar overflow em campos de log.</p>
     * 
     * @param value valor a sanitizar
     * @return valor truncado em 120 caracteres ou string vazia se nulo
     */
    private String sanitize(String value) {
        if (value == null) {
            return "";
        }
        return value.length() > 120 ? value.substring(0, 120) : value;
    }
}
