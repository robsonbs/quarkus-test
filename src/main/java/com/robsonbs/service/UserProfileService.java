package com.robsonbs.service;

import com.robsonbs.dao.UserDao;
import com.robsonbs.dao.UserProfileDao;
import com.robsonbs.dto.UserProfileRequestDTO;
import com.robsonbs.dto.UserProfileResponseDTO;
import com.robsonbs.model.UserProfile;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import com.robsonbs.service.AuditLogService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Serviço de negócio para gerenciamento de perfis de usuário.
 * 
 * <p>Esta classe implementa a camada de regras de negócio para operações
 * CRUD de perfis ({@link UserProfile}), que definem os papéis e permissões
 * dos usuários no sistema.</p>
 * 
 * <h2>Responsabilidades</h2>
 * <ul>
 *   <li><strong>Criação:</strong> Validação e normalização de nome, verificação
 *       de unicidade, persistência e auditoria</li>
 *   <li><strong>Atualização:</strong> Revalidação de nome único, atualização
 *       com auditoria</li>
 *   <li><strong>Exclusão:</strong> Verificação de uso (perfis em uso não podem
 *       ser excluídos), remoção com auditoria</li>
 *   <li><strong>Consulta:</strong> Listagem com contagem de usuários por perfil</li>
 * </ul>
 * 
 * <h2>Normalização de Nomes</h2>
 * <p>Os nomes de perfis são automaticamente:</p>
 * <ul>
 *   <li>Validados (não podem ser nulos ou em branco)</li>
 *   <li>Trimados (espaços removidos das extremidades)</li>
 *   <li>Convertidos para MAIÚSCULAS</li>
 * </ul>
 * 
 * <h2>Proteção de Integridade</h2>
 * <p>Perfis com usuários associados não podem ser excluídos. Esta validação
 * previne a quebra de integridade referencial e garante que usuários não
 * fiquem sem perfil definido.</p>
 * 
 * <h2>Auditoria</h2>
 * <p>Eventos registrados:</p>
 * <ul>
 *   <li>{@code PROFILE_CREATED} - Criação de novo perfil</li>
 *   <li>{@code PROFILE_UPDATED} - Atualização de perfil existente</li>
 *   <li>{@code PROFILE_DELETED} - Remoção de perfil</li>
 * </ul>
 * 
 * <h2>Exemplo de Uso</h2>
 * <pre>{@code
 * @Inject
 * UserProfileService profileService;
 * 
 * // Listar todos os perfis com contagem de usuários
 * List<UserProfileResponseDTO> profiles = profileService.listAll();
 * 
 * // Criar novo perfil
 * UserProfileRequestDTO dto = new UserProfileRequestDTO();
 * dto.setName("gerente");  // será salvo como "GERENTE"
 * profileService.create(dto);
 * 
 * // Listar para seleção em combo box
 * List<UserProfileResponseDTO> options = profileService.listForSelection();
 * }</pre>
 * 
 * @author Sistema de Gerenciamento
 * @version 1.0
 * @since 1.0
 * @see UserProfile
 * @see UserProfileDao
 * @see UserProfileRequestDTO
 * @see UserProfileResponseDTO
 */
@ApplicationScoped
public class UserProfileService {

    /**
     * DAO para operações de persistência de perfis.
     */
    @Inject
    UserProfileDao userProfileDao;

    /**
     * DAO para operações de usuários.
     * Usado para contagem de usuários por perfil.
     */
    @Inject
    UserDao userDao;

    /**
     * Serviço de auditoria para registro de eventos de domínio.
     */
    @Inject
    AuditLogService auditLogService;

    /**
     * Identidade de segurança do usuário autenticado.
     */
    @Inject
    SecurityIdentity securityIdentity;

    /**
     * Lista todos os perfis com contagem de usuários associados.
     * 
     * <p>Retorna DTOs de resposta que incluem o número de usuários
     * vinculados a cada perfil, útil para exibir estatísticas na interface.</p>
     * 
     * @return lista de perfis com contagem; nunca {@code null}
     */
    public List<UserProfileResponseDTO> listAll() {
        return userProfileDao.listAll().stream()
                .map(profile -> new UserProfileResponseDTO(profile, countUsers(profile)))
                .collect(Collectors.toList());
    }

    /**
     * Lista todos os perfis ordenados alfabeticamente por nome.
     * 
     * <p>Ideal para exibição em listas ordenadas ou relatórios.</p>
     * 
     * @return lista de perfis ordenada por nome (case-insensitive)
     */
    public List<UserProfileResponseDTO> listAllOrdered() {
        return userProfileDao.listAll().stream()
            .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
                .map(profile -> new UserProfileResponseDTO(profile, countUsers(profile)))
                .collect(Collectors.toList());
    }

    /**
     * Cria um novo perfil de usuário.
     * 
     * <p>O nome é sanitizado (trim + uppercase) e validado quanto à unicidade
     * antes da persistência. Um evento de auditoria é registrado.</p>
     * 
     * @param requestDTO dados do novo perfil
     * @throws BadRequestException se nome for nulo ou em branco
     * @throws WebApplicationException com CONFLICT se nome já existir
     */
    @Transactional
    public void create(UserProfileRequestDTO requestDTO) {
        String name = sanitizeName(requestDTO.getName());
        ensureNameAvailable(name, null);

        UserProfile profile = new UserProfile();
        profile.setName(name);
        userProfileDao.persist(profile);

        auditLogService.recordDomainEvent(
            currentActor(),
            "PROFILE_CREATED",
            "/profiles",
            "UserProfile",
            profile.getId() != null ? profile.getId().toString() : null,
            "Perfil criado: " + name,
            Response.Status.CREATED.getStatusCode()
        );
    }

    /**
     * Busca a entidade perfil pelo ID.
     * 
     * <p>Método interno para operações que precisam da entidade JPA
     * em vez do DTO de resposta.</p>
     * 
     * @param id identificador do perfil
     * @return entidade perfil encontrada
     * @throws NotFoundException se perfil não existir
     */
    public UserProfile findEntityById(Long id) {
        UserProfile profile = userProfileDao.findById(id);
        if (profile == null) {
            throw new NotFoundException("Perfil não encontrado");
        }
        return profile;
    }

    /**
     * Busca um perfil pelo ID e retorna como DTO.
     * 
     * <p>Inclui a contagem de usuários associados ao perfil.</p>
     * 
     * @param id identificador do perfil
     * @return DTO com dados do perfil e contagem de usuários
     * @throws NotFoundException se perfil não existir
     */
    public UserProfileResponseDTO findById(Long id) {
        UserProfile profile = findEntityById(id);
        return new UserProfileResponseDTO(profile, countUsers(profile));
    }

    /**
     * Atualiza um perfil existente.
     * 
     * <p>O novo nome é sanitizado e validado quanto à unicidade
     * (ignorando o próprio perfil na verificação).</p>
     * 
     * @param id identificador do perfil a atualizar
     * @param requestDTO novos dados do perfil
     * @throws NotFoundException se perfil não existir
     * @throws BadRequestException se novo nome for inválido
     * @throws WebApplicationException com CONFLICT se novo nome já existir
     */
    @Transactional
    public void update(Long id, UserProfileRequestDTO requestDTO) {
        UserProfile profile = findEntityById(id);
        String name = sanitizeName(requestDTO.getName());
        ensureNameAvailable(name, id);
        profile.setName(name);

        auditLogService.recordDomainEvent(
            currentActor(),
            "PROFILE_UPDATED",
            "/profiles/" + id,
            "UserProfile",
            profile.getId() != null ? profile.getId().toString() : null,
            "Perfil atualizado: " + name,
            Response.Status.OK.getStatusCode()
        );
    }

    /**
     * Remove um perfil do sistema.
     * 
     * <p><strong>Restrição:</strong> Perfis com usuários associados não podem
     * ser excluídos. Primeiro é necessário migrar ou remover os usuários.</p>
     * 
     * @param id identificador do perfil a remover
     * @throws NotFoundException se perfil não existir
     * @throws WebApplicationException com CONFLICT se perfil tiver usuários associados
     */
    @Transactional
    public void delete(Long id) {
        UserProfile profile = findEntityById(id);
        long usage = countUsers(profile);
        if (usage > 0) {
            throw new WebApplicationException("Não é possível remover perfis com usuários associados", Response.Status.CONFLICT);
        }
        userProfileDao.delete(profile);

        auditLogService.recordDomainEvent(
                currentActor(),
                "PROFILE_DELETED",
                "/profiles/" + id + "/delete",
                "UserProfile",
                id != null ? id.toString() : null,
                "Perfil removido",
                Response.Status.OK.getStatusCode()
        );
    }

    /**
     * Lista perfis para seleção em formulários.
     * 
     * <p>Retorna a mesma lista que {@link #listAllOrdered()}, ideal
     * para popular combo boxes e selects em formulários.</p>
     * 
     * @return lista de perfis ordenada alfabeticamente
     */
    public List<UserProfileResponseDTO> listForSelection() {
        return listAllOrdered();
    }

    /**
     * Conta o número de usuários associados a um perfil.
     * 
     * @param profile perfil a consultar
     * @return quantidade de usuários com este perfil
     */
    private long countUsers(UserProfile profile) {
        return userDao.count("profile", profile);
    }

    /**
     * Verifica se o nome está disponível para uso.
     * 
     * <p>Na criação (id = null), qualquer nome existente causa conflito.
     * Na atualização, o nome do próprio perfil é permitido.</p>
     * 
     * @param name nome a verificar
     * @param id ID do perfil atual (para ignorar) ou {@code null}
     * @throws WebApplicationException com CONFLICT se nome já estiver em uso
     */
    private void ensureNameAvailable(String name, Long id) {
        userProfileDao.findByName(name).ifPresent(existing -> {
            if (id == null || !existing.getId().equals(id)) {
                throw new WebApplicationException("Já existe um perfil com esse nome", Response.Status.CONFLICT);
            }
        });
    }

    /**
     * Sanitiza e valida o nome do perfil.
     * 
     * <p>Aplica trim e conversão para maiúsculas, validando que
     * o nome não seja nulo ou em branco.</p>
     * 
     * @param name nome a sanitizar
     * @return nome sanitizado em maiúsculas
     * @throws BadRequestException se nome for nulo ou em branco
     */
    private String sanitizeName(String name) {
        if (name == null || name.isBlank()) {
            throw new BadRequestException("Nome do perfil é obrigatório");
        }
        return name.trim().toUpperCase();
    }

    /**
     * Obtém o identificador do ator atual (usuário autenticado).
     * 
     * @return email do usuário autenticado ou "system"
     */
    private String currentActor() {
        if (securityIdentity != null && !securityIdentity.isAnonymous() && securityIdentity.getPrincipal() != null) {
            return securityIdentity.getPrincipal().getName();
        }
        return "system";
    }
}
