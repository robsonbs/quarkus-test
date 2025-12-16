package com.robsonbs.dao;

import com.robsonbs.model.Note;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class NoteDao implements PanacheRepository<Note> {

    public List<Note> findByOwnerId(Long ownerId) {
        return list("owner.id", ownerId);
    }
}
