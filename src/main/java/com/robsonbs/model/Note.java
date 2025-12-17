package com.robsonbs.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidade JPA que representa uma nota/anotação pessoal no sistema.
 * 
 * <p>Uma nota é uma unidade de informação textual pertencente a um usuário.
 * O sistema implementa o conceito de "ownership" - cada usuário só pode
 * visualizar e gerenciar suas próprias notas.</p>
 * 
 * <h2>Características Principais</h2>
 * <ul>
 *   <li><strong>Ownership:</strong> Cada nota pertence a um único usuário</li>
 *   <li><strong>Privacidade:</strong> Usuários só veem suas próprias notas</li>
 *   <li><strong>Conteúdo Rico:</strong> Suporta textos longos via campo TEXT</li>
 *   <li><strong>Auditoria:</strong> Timestamps de criação e atualização automáticos</li>
 * </ul>
 * 
 * <h2>Regras de Negócio</h2>
 * <ul>
 *   <li>Título é obrigatório e não pode ser vazio</li>
 *   <li>Conteúdo é opcional mas recomendado</li>
 *   <li>Apenas o proprietário pode editar ou excluir a nota</li>
 *   <li>Todas as operações são registradas no log de auditoria</li>
 * </ul>
 * 
 * <h2>Mapeamento de Banco de Dados</h2>
 * <pre>
 * CREATE TABLE notes (
 *     id BIGSERIAL PRIMARY KEY,
 *     title VARCHAR(255) NOT NULL,
 *     content TEXT,
 *     user_id BIGINT NOT NULL REFERENCES users(id),
 *     created_at TIMESTAMP NOT NULL,
 *     updated_at TIMESTAMP
 * );
 * </pre>
 * 
 * <h2>Exemplo de Uso</h2>
 * <pre>{@code
 * Note note = new Note();
 * note.setTitle("Reunião de projeto");
 * note.setContent("Discutir cronograma e entregas...");
 * note.setOwner(currentUser);
 * noteDao.persist(note);
 * }</pre>
 * 
 * @author Sistema de Gestão de Notas e Tarefas
 * @version 1.0
 * @since 2025-12-01
 * @see User
 * @see com.robsonbs.dao.NoteDao
 * @see com.robsonbs.service.NoteService
 */
@Entity
@Table(name = "notes")
public class Note {

    /**
     * Identificador único da nota, gerado automaticamente pelo banco de dados.
     * <p>Utiliza estratégia IDENTITY para geração automática de sequência.</p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Título da nota, campo obrigatório.
     * <p>Deve ser conciso e descritivo, representando o assunto principal
     * da anotação. Validações de conteúdo são realizadas no
     * {@link com.robsonbs.service.NoteService}.</p>
     */
    @Column(nullable = false)
    private String title;

    /**
     * Conteúdo detalhado da nota.
     * <p>Campo opcional que permite armazenar o texto completo da anotação.
     * Mapeado como TEXT no banco para suportar conteúdos extensos.</p>
     */
    @Column(columnDefinition = "TEXT")
    private String content;

    /**
     * Usuário proprietário da nota.
     * <p>Relacionamento Many-to-One com carregamento lazy para otimização.
     * Este campo implementa o conceito de "ownership" - apenas o proprietário
     * pode visualizar, editar ou excluir suas próprias notas.</p>
     * 
     * @see User
     * @see com.robsonbs.service.NoteService#findByOwner(String)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    /**
     * Data e hora de criação da nota.
     * <p>Preenchido automaticamente pelo callback {@link #onCreate()} e
     * não pode ser alterado após a persistência inicial (updatable = false).</p>
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Data e hora da última atualização da nota.
     * <p>Atualizado automaticamente pelo callback {@link #onUpdate()}
     * sempre que a entidade é modificada.</p>
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Callback JPA executado antes da persistência inicial.
     * <p>Define automaticamente o timestamp de criação com o momento atual.</p>
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /**
     * Callback JPA executado antes de cada atualização.
     * <p>Atualiza o timestamp de última modificação.</p>
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Obtém o identificador único da nota.
     * @return o ID da nota ou {@code null} se ainda não persistida
     */
    public Long getId() {
        return id;
    }

    /**
     * Define o identificador da nota.
     * <p><strong>Nota:</strong> Normalmente não deve ser chamado diretamente,
     * pois o ID é gerado automaticamente pelo banco de dados.</p>
     * @param id o novo ID da nota
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Obtém o título da nota.
     * @return o título da nota
     */
    public String getTitle() {
        return title;
    }

    /**
     * Define o título da nota.
     * @param title o novo título (não pode ser nulo ou vazio)
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Obtém o conteúdo detalhado da nota.
     * @return o conteúdo ou {@code null} se não definido
     */
    public String getContent() {
        return content;
    }

    /**
     * Define o conteúdo detalhado da nota.
     * @param content o novo conteúdo
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Obtém o usuário proprietário da nota.
     * @return o usuário proprietário
     */
    public User getOwner() {
        return owner;
    }

    /**
     * Define o usuário proprietário da nota.
     * @param owner o novo proprietário (não pode ser nulo)
     */
    public void setOwner(User owner) {
        this.owner = owner;
    }

    /**
     * Obtém a data e hora de criação da nota.
     * @return o timestamp de criação
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Define a data e hora de criação da nota.
     * <p><strong>Nota:</strong> Normalmente preenchido automaticamente
     * pelo callback {@link #onCreate()}.</p>
     * @param createdAt o timestamp de criação
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Obtém a data e hora da última atualização.
     * @return o timestamp de atualização ou {@code null} se nunca atualizada
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Define a data e hora da última atualização.
     * <p><strong>Nota:</strong> Normalmente atualizado automaticamente
     * pelo callback {@link #onUpdate()}.</p>
     * @param updatedAt o timestamp de atualização
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
