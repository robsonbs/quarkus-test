package com.robsonbs.dto;

import com.robsonbs.model.UserProfile;

public class UserProfileResponseDTO {
    private Long id;
    private String name;
    private long usersCount;

    public UserProfileResponseDTO(UserProfile profile, long usersCount) {
        this.id = profile.getId();
        this.name = profile.getName();
        this.usersCount = usersCount;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getUsersCount() {
        return usersCount;
    }
}
