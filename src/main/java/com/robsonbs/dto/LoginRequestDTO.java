package com.robsonbs.dto;

import org.jboss.resteasy.reactive.RestForm;

/**
 * Data Transfer Object (DTO) para receber credenciais de login.
 * 
 * <p>Este DTO é utilizado para capturar dados de autenticação enviados
 * via formulário HTML na página de login.</p>
 * 
 * <h2>Fluxo de Autenticação</h2>
 * <p>Embora este DTO exista, o Quarkus Elytron JDBC utiliza o endpoint
 * padrão {@code /j_security_check} para autenticação Form-based.
 * Este DTO pode ser usado para:</p>
 * <ul>
 *   <li>Validação customizada antes do login</li>
 *   <li>Pré-processamento de credenciais</li>
 *   <li>Logging de tentativas de login</li>
 * </ul>
 * 
 * <h2>Campos do Formulário</h2>
 * <table border="1">
 *   <tr><th>Campo</th><th>Tipo</th><th>Descrição</th></tr>
 *   <tr><td>email</td><td>String</td><td>E-mail de login (username)</td></tr>
 *   <tr><td>password</td><td>String</td><td>Senha em texto plano</td></tr>
 * </table>
 * 
 * <h2>Segurança</h2>
 * <p><strong>IMPORTANTE:</strong> A senha é transmitida em texto plano
 * sobre HTTPS. O Elytron compara com o hash BCrypt armazenado no banco.</p>
 * 
 * <h2>Integração com Elytron</h2>
 * <p>O mapeamento padrão do Elytron espera:</p>
 * <ul>
 *   <li>{@code j_username} - Campo de username</li>
 *   <li>{@code j_password} - Campo de senha</li>
 * </ul>
 * 
 * @author Sistema de Gestão de Notas e Tarefas
 * @version 1.0
 * @since 2025-12-01
 * @see com.robsonbs.controller.LoginController
 * @see com.robsonbs.service.AuthService
 */
public class LoginRequestDTO {
    
    /**
     * Endereço de e-mail usado como identificador de login.
     */
    @RestForm
    private String email;
    
    /**
     * Senha do usuário em texto plano.
     * <p><strong>Nota:</strong> Será comparada com o hash BCrypt armazenado.</p>
     */
    @RestForm
    private String password;

    /**
     * Obtém o e-mail de login.
     * @return o endereço de e-mail
     */
    public String getEmail() {
        return email;
    }

    /**
     * Define o e-mail de login.
     * @param email o endereço de e-mail
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Obtém a senha em texto plano.
     * @return a senha
     */
    public String getPassword() {
        return password;
    }

    /**
     * Define a senha.
     * @param password a senha em texto plano
     */
    public void setPassword(String password) {
        this.password = password;
    }
}
