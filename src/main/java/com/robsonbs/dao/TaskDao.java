package com.robsonbs.dao;

import com.robsonbs.model.Task;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class TaskDao implements PanacheRepository<Task> {

    public List<Task> findByOwnerId(Long ownerId) {
        return find("owner.id", ownerId).list();
    }
}
