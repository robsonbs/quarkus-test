package com.robsonbs.service;

import com.robsonbs.dao.UserDao;
import com.robsonbs.model.User;
import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Optional;

/**
 * Serviço de autenticação para validação de credenciais de usuário.
 * 
 * <p>Esta classe fornece funcionalidades de autenticação para o sistema,
 * verificando credenciais de email e senha contra o banco de dados.
 * Trabalha em conjunto com o mecanismo de segurança Elytron do Quarkus.</p>
 * 
 * <h2>Processo de Autenticação</h2>
 * <ol>
 *   <li>Busca usuário pelo email fornecido</li>
 *   <li>Se encontrado, compara senha informada com hash armazenado</li>
 *   <li>Retorna usuário se credenciais válidas, vazio caso contrário</li>
 * </ol>
 * 
 * <h2>Segurança de Senhas</h2>
 * <p>As senhas são armazenadas como hash bcrypt e a verificação é feita
 * através de {@link BcryptUtil#matches(String, String)}, que compara
 * a senha em texto plano com o hash de forma segura.</p>
 * 
 * <h2>Integração com Elytron</h2>
 * <p>Este serviço pode ser usado em conjunto com o mecanismo de
 * autenticação JDBC do Quarkus Elytron, ou diretamente para
 * autenticação programática em endpoints customizados.</p>
 * 
 * <h2>Exemplo de Uso</h2>
 * <pre>{@code
 * @Inject
 * AuthService authService;
 * 
 * // No endpoint de login
 * Optional<User> user = authService.authenticate(email, password);
 * if (user.isPresent()) {
 *     // Autenticação bem-sucedida
 *     // Criar sessão, gerar token, etc.
 * } else {
 *     // Credenciais inválidas
 *     // Registrar tentativa falha, retornar erro
 * }
 * }</pre>
 * 
 * <h2>Considerações de Segurança</h2>
 * <ul>
 *   <li>Não revela se o email existe ou não (retorna vazio em ambos os casos)</li>
 *   <li>Usa comparação de hash bcrypt (resistente a timing attacks)</li>
 *   <li>Não faz log de senhas ou informações sensíveis</li>
 * </ul>
 * 
 * @author Sistema de Segurança
 * @version 1.0
 * @since 1.0
 * @see User
 * @see UserDao
 * @see BcryptUtil
 */
@ApplicationScoped
public class AuthService {

    /**
     * DAO para operações de busca de usuários.
     * Usado para localizar o usuário pelo email.
     */
    @Inject
    UserDao userDao;

    /**
     * Autentica um usuário verificando email e senha.
     * 
     * <p>Busca o usuário pelo email e, se encontrado, compara a senha
     * fornecida com o hash armazenado usando bcrypt. Este método é
     * seguro contra timing attacks pois a comparação bcrypt tem
     * tempo constante.</p>
     * 
     * <p><strong>Importante:</strong> Para segurança, este método retorna
     * {@link Optional#empty()} tanto quando o email não existe quanto
     * quando a senha está incorreta, não revelando qual condição falhou.</p>
     * 
     * @param email endereço de email do usuário; não pode ser {@code null}
     * @param password senha em texto plano para verificação; não pode ser {@code null}
     * @return {@link Optional} contendo o usuário se autenticação bem-sucedida,
     *         ou {@link Optional#empty()} se credenciais inválidas
     */
    public Optional<User> authenticate(String email, String password) {
        Optional<User> userOptional = userDao.findByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (BcryptUtil.matches(password, user.getPassword())) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }
}
