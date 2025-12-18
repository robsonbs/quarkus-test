package com.robsonbs.dto;

import jakarta.validation.constraints.NotBlank;
import org.jboss.resteasy.reactive.RestForm;

/**
 * Data Transfer Object (DTO) para receber dados de criação ou atualização de perfil.
 * 
 * <p>Este DTO é utilizado para capturar dados enviados via formulário HTML
 * nas operações de criação e edição de perfis de usuário (roles).</p>
 * 
 * <h2>Validação</h2>
 * <p>O campo {@code name} possui validação Bean Validation:</p>
 * <ul>
 *   <li>{@code @NotBlank} - Nome é obrigatório e não pode ser apenas espaços</li>
 * </ul>
 * <p>Validações adicionais (unicidade) são realizadas no 
 * {@link com.robsonbs.service.UserProfileService}.</p>
 * 
 * <h2>Convenção de Nomenclatura</h2>
 * <p>Os nomes de perfil seguem a convenção UPPER_CASE:</p>
 * <ul>
 *   <li>ADMIN - Administrador com acesso total</li>
 *   <li>USER - Usuário padrão com acesso limitado</li>
 *   <li>MANAGER - Gerente (se aplicável)</li>
 * </ul>
 * 
 * <h2>Exemplo de Uso</h2>
 * <pre>{@code
 * // No Controller
 * @POST
 * public Response create(@BeanParam UserProfileRequestDTO dto) {
 *     UserProfileResponseDTO created = profileService.create(dto);
 *     return Response.seeOther(URI.create("/profiles?success=Perfil criado")).build();
 * }
 * }</pre>
 * 
 * @author Sistema de Gestão de Notas e Tarefas
 * @version 1.0
 * @since 2025-12-01
 * @see UserProfileResponseDTO
 * @see com.robsonbs.model.UserProfile
 * @see com.robsonbs.service.UserProfileService
 */
public class UserProfileRequestDTO {

    /**
     * Nome do perfil (role).
     * <p>Campo obrigatório. Deve ser único no sistema.
     * Recomenda-se usar UPPER_CASE (ex: ADMIN, USER).</p>
     */
    @RestForm
    @NotBlank(message = "Nome do perfil é obrigatório")
    private String name;

    /**
     * Obtém o nome do perfil.
     * @return o nome do perfil
     */
    public String getName() {
        return name;
    }

    /**
     * Define o nome do perfil.
     * @param name o nome do perfil (recomendado: UPPER_CASE)
     */
    public void setName(String name) {
        this.name = name;
    }
}
