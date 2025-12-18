package com.robsonbs.dao;

import com.robsonbs.model.Note;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

/**
 * Repositório de acesso a dados (DAO) para a entidade {@link Note}.
 * 
 * <p>Esta classe implementa o padrão Data Access Object utilizando o Quarkus Panache,
 * fornecendo operações de persistência para notas/anotações do sistema.</p>
 * 
 * <h2>Ownership e Privacidade</h2>
 * <p>O sistema implementa o conceito de "ownership" onde cada nota pertence
 * a um único usuário. Este DAO fornece métodos para filtrar notas por
 * proprietário, garantindo que usuários só acessem suas próprias notas.</p>
 * 
 * <h2>Padrão de Arquitetura</h2>
 * <pre>
 * Controller → Service → DAO → Database
 *                 ↓
 *         Validação de Ownership
 * </pre>
 * 
 * <h2>Métodos Herdados do Panache</h2>
 * <ul>
 *   <li>{@code persist(entity)} - Persiste nova nota</li>
 *   <li>{@code findById(id)} - Busca por ID</li>
 *   <li>{@code listAll()} - Lista todas as notas</li>
 *   <li>{@code delete(entity)} - Remove nota</li>
 *   <li>{@code count()} - Conta notas</li>
 * </ul>
 * 
 * <h2>Exemplo de Uso</h2>
 * <pre>{@code
 * @Inject
 * NoteDao noteDao;
 * 
 * // Buscar notas do usuário
 * List<Note> userNotes = noteDao.findByOwnerId(userId);
 * 
 * // Persistir nova nota
 * noteDao.persist(newNote);
 * }</pre>
 * 
 * @author Sistema de Gestão de Notas e Tarefas
 * @version 1.0
 * @since 2025-12-01
 * @see Note
 * @see com.robsonbs.service.NoteService
 * @see PanacheRepository
 */
@ApplicationScoped
public class NoteDao implements PanacheRepository<Note> {

    /**
     * Lista todas as notas pertencentes a um usuário específico.
     * 
     * <p>Este método é fundamental para implementar o conceito de "ownership",
     * garantindo que cada usuário só visualize suas próprias notas.</p>
     * 
     * <p>A query utiliza navegação de propriedade JPA ({@code owner.id})
     * para filtrar pelo ID do proprietário.</p>
     * 
     * @param ownerId o ID do usuário proprietário das notas
     * @return lista de notas do usuário (pode ser vazia, nunca {@code null})
     * 
     * @see com.robsonbs.service.NoteService#findByOwner(String)
     */
    public List<Note> findByOwnerId(Long ownerId) {
        return list("owner.id", ownerId);
    }
}
