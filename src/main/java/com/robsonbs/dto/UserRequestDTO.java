package com.robsonbs.dto;

import org.jboss.resteasy.reactive.RestForm;

/**
 * Data Transfer Object (DTO) para receber dados de criação ou atualização de usuário.
 * 
 * <p>Este DTO é utilizado para capturar dados enviados via formulário HTML
 * nas operações de criação e edição de usuários. A anotação {@code @RestForm}
 * permite o binding automático de campos de formulário.</p>
 * 
 * <h2>Padrão DTO no Projeto</h2>
 * <p>Seguindo as boas práticas de arquitetura, este projeto separa:</p>
 * <ul>
 *   <li><strong>RequestDTO:</strong> Dados de entrada (formulários, JSON)</li>
 *   <li><strong>ResponseDTO:</strong> Dados de saída (templates, API)</li>
 *   <li><strong>Entity:</strong> Representação de banco de dados</li>
 * </ul>
 * 
 * <h2>Campos do Formulário</h2>
 * <table border="1">
 *   <tr><th>Campo</th><th>Tipo</th><th>Obrigatório</th><th>Descrição</th></tr>
 *   <tr><td>name</td><td>String</td><td>Sim</td><td>Nome completo do usuário</td></tr>
 *   <tr><td>email</td><td>String</td><td>Sim</td><td>E-mail único (login)</td></tr>
 *   <tr><td>password</td><td>String</td><td>Criação: Sim</td><td>Senha em texto plano</td></tr>
 *   <tr><td>profileId</td><td>Long</td><td>Não</td><td>ID do perfil de acesso</td></tr>
 * </table>
 * 
 * <h2>Validações</h2>
 * <p>As validações de negócio são realizadas no {@link com.robsonbs.service.UserService}:</p>
 * <ul>
 *   <li>Nome não pode ser vazio</li>
 *   <li>E-mail deve ser único no sistema</li>
 *   <li>Senha é obrigatória na criação (opcional na atualização)</li>
 * </ul>
 * 
 * <h2>Exemplo de Uso</h2>
 * <pre>{@code
 * // No Controller
 * @POST
 * public Response create(@BeanParam UserRequestDTO dto) {
 *     UserResponseDTO created = userService.create(dto);
 *     return Response.seeOther(URI.create("/users?success=Usuário criado")).build();
 * }
 * }</pre>
 * 
 * @author Sistema de Gestão de Notas e Tarefas
 * @version 1.0
 * @since 2025-12-01
 * @see UserResponseDTO
 * @see com.robsonbs.service.UserService
 * @see com.robsonbs.controller.UserController
 */
public class UserRequestDTO {
    
    /**
     * Nome completo do usuário.
     * <p>Campo obrigatório, capturado do formulário HTML.</p>
     */
    @RestForm
    private String name;
    
    /**
     * Endereço de e-mail do usuário.
     * <p>Usado como identificador de login. Deve ser único no sistema.</p>
     */
    @RestForm
    private String email;
    
    /**
     * Senha do usuário em texto plano.
     * <p><strong>Nota:</strong> A senha será hasheada com BCrypt no Service
     * antes de ser persistida. Na atualização, se vazio, mantém a senha atual.</p>
     */
    @RestForm
    private String password;
    
    /**
     * ID do perfil de acesso a ser associado ao usuário.
     * <p>Referência para {@link com.robsonbs.model.UserProfile}.
     * Define as permissões do usuário (ADMIN, USER, etc.).</p>
     */
    @RestForm
    private Long profileId;

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
     * Obtém a senha em texto plano.
     * @return a senha (será hasheada antes de persistir)
     */
    public String getPassword() {
        return password;
    }

    /**
     * Define a senha do usuário.
     * @param password a senha em texto plano
     */
    public void setPassword(String password) {
        this.password = password;
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
}
