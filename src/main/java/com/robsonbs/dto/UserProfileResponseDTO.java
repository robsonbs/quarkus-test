package com.robsonbs.dto;

import com.robsonbs.model.UserProfile;

/**
 * Data Transfer Object (DTO) para retornar dados de perfil para a camada de apresentação.
 * 
 * <p>Este DTO é utilizado para transferir dados de perfil de usuário para templates Qute
 * e respostas de API. Inclui informação agregada sobre a quantidade de usuários
 * associados ao perfil.</p>
 * 
 * <h2>Informação Agregada</h2>
 * <p>O campo {@code usersCount} fornece a contagem de usuários que possuem
 * este perfil, útil para:</p>
 * <ul>
 *   <li>Exibir estatísticas na listagem de perfis</li>
 *   <li>Validar antes de excluir (não permitir exclusão se há usuários)</li>
 *   <li>Análise de uso do sistema</li>
 * </ul>
 * 
 * <h2>Campos Expostos</h2>
 * <table border="1">
 *   <tr><th>Campo</th><th>Tipo</th><th>Descrição</th></tr>
 *   <tr><td>id</td><td>Long</td><td>Identificador único do perfil</td></tr>
 *   <tr><td>name</td><td>String</td><td>Nome do perfil (role)</td></tr>
 *   <tr><td>usersCount</td><td>long</td><td>Quantidade de usuários com este perfil</td></tr>
 * </table>
 * 
 * <h2>Exemplo de Uso</h2>
 * <pre>{@code
 * // No Service
 * public List<UserProfileResponseDTO> findAll() {
 *     return profileDao.listAll().stream()
 *         .map(p -> new UserProfileResponseDTO(p, userDao.count("profile", p)))
 *         .toList();
 * }
 * 
 * // No Template Qute
 * {#for profile in profiles}
 *     <tr>
 *         <td>{profile.name}</td>
 *         <td>{profile.usersCount} usuários</td>
 *     </tr>
 * {/for}
 * }</pre>
 * 
 * @author Sistema de Gestão de Notas e Tarefas
 * @version 1.0
 * @since 2025-12-01
 * @see UserProfile
 * @see UserProfileRequestDTO
 * @see com.robsonbs.service.UserProfileService
 */
public class UserProfileResponseDTO {
    
    /** Identificador único do perfil. */
    private Long id;
    
    /** Nome do perfil (role). */
    private String name;
    
    /** Quantidade de usuários associados a este perfil. */
    private long usersCount;

    /**
     * Construtor que converte uma entidade {@link UserProfile} para DTO.
     * 
     * @param profile    a entidade UserProfile a ser convertida
     * @param usersCount quantidade de usuários com este perfil
     */
    public UserProfileResponseDTO(UserProfile profile, long usersCount) {
        this.id = profile.getId();
        this.name = profile.getName();
        this.usersCount = usersCount;
    }

    /**
     * Obtém o ID do perfil.
     * @return o identificador único
     */
    public Long getId() {
        return id;
    }

    /**
     * Obtém o nome do perfil.
     * @return o nome (role)
     */
    public String getName() {
        return name;
    }

    /**
     * Obtém a quantidade de usuários com este perfil.
     * @return a contagem de usuários
     */
    public long getUsersCount() {
        return usersCount;
    }
}
