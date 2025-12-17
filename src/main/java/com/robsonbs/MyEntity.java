package com.robsonbs;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;


/**
 * Entidade JPA de exemplo demonstrando o padrão Active Record do Panache.
 * 
 * <p>Esta classe é gerada automaticamente pelo Quarkus como exemplo de
 * entidade JPA usando o padrão Active Record do Hibernate Panache.</p>
 * 
 * <h2>Herança PanacheEntity</h2>
 * <p>Ao estender {@link PanacheEntity}, a entidade ganha automaticamente:</p>
 * <ul>
 *   <li>Campo {@code id} do tipo {@code Long} (chave primária)</li>
 *   <li>Métodos estáticos: {@code persist()}, {@code listAll()}, {@code findById()}, etc.</li>
 *   <li>Geração automática de queries</li>
 * </ul>
 * 
 * <h2>Exemplo de Uso</h2>
 * <pre>{@code
 * // Criar e persistir
 * MyEntity entity = new MyEntity();
 * entity.field = "valor-1";
 * entity.persist();
 * 
 * // Consultar
 * List<MyEntity> all = MyEntity.listAll();
 * MyEntity found = MyEntity.findById(1L);
 * 
 * // Excluir
 * MyEntity.deleteById(1L);
 * }</pre>
 * 
 * <h2>Padrão Alternativo</h2>
 * <p>Para usar o padrão Repository (como as entidades principais deste projeto),
 * estenda {@code PanacheEntityBase} e crie um DAO separado.</p>
 * 
 * @author Quarkus Starter
 * @version 1.0
 * @since 1.0
 * @see PanacheEntity
 */
@Entity
public class MyEntity extends PanacheEntity {
    /**
     * Campo de exemplo da entidade.
     * Demonstra mapeamento simples de coluna.
     */
    public String field;
}
