package com.robsonbs.controller;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

/**
 * Controller responsável pela página de login do sistema.
 * 
 * <p>Este controller serve a página de autenticação onde usuários
 * inserem suas credenciais (email e senha) para acessar o sistema.
 * A autenticação em si é gerenciada pelo Quarkus Elytron Security.</p>
 * 
 * <h2>Fluxo de Autenticação</h2>
 * <ol>
 *   <li>Usuário acessa {@code GET /login}</li>
 *   <li>Preenche formulário com email e senha</li>
 *   <li>Formulário envia {@code POST /j_security_check} (Elytron)</li>
 *   <li>Se sucesso: redireciona para página original ou home</li>
 *   <li>Se falha: redireciona para {@code /login?error=true}</li>
 * </ol>
 * 
 * <h2>Parâmetros de Query</h2>
 * <ul>
 *   <li><strong>error:</strong> Indica falha de autenticação</li>
 *   <li><strong>success:</strong> Mensagem de sucesso (ex: após logout)</li>
 * </ul>
 * 
 * <h2>Características</h2>
 * <ul>
 *   <li><strong>Rota:</strong> {@code GET /login}</li>
 *   <li><strong>Acesso:</strong> Público</li>
 *   <li><strong>Template:</strong> {@code templates/login.html}</li>
 * </ul>
 * 
 * <h2>Integração com Elytron</h2>
 * <p>O formulário de login deve enviar para {@code /j_security_check}
 * com campos {@code j_username} e {@code j_password} para que o
 * mecanismo de autenticação FORM do Quarkus processe as credenciais.</p>
 * 
 * @author Sistema de Segurança
 * @version 1.0
 * @since 1.0
 * @see LogoutController
 */
@Path("/login")
public class LoginController {

    /**
     * Template Qute para a página de login.
     * Corresponde ao arquivo {@code templates/login.html}.
     */
    @jakarta.inject.Inject
    Template login;

    /**
     * Renderiza a página de login.
     * 
     * <p>Processa parâmetros de query para exibir mensagens de erro
     * (autenticação falhou) ou sucesso (logout realizado).</p>
     * 
     * @param error parâmetro indicando erro de autenticação
     * @param success mensagem de sucesso para exibir (ex: "Logout realizado")
     * @return instância do template com dados de erro/sucesso
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance login(@QueryParam("error") String error,
                                  @QueryParam("success") String success) {
        boolean hasError = error != null && !error.isBlank();
        String successMessage = (success != null && !success.isBlank()) ? success : null;
        return login
                .data("error", hasError)
                .data("success", successMessage);
    }
}
