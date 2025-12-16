package com.robsonbs.dao;

import com.robsonbs.model.UserProfile;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

@ApplicationScoped
public class UserProfileDao implements PanacheRepository<UserProfile> {

	public Optional<UserProfile> findByName(String name) {
		return find("name", name).firstResultOptional();
	}
}
