package com.rublin.rublinmart.dto;

import com.rublin.rublinmart.model.User;
import java.sql.Timestamp;

public class UserResponseDTO {
    private Long id;
    private String name;
    private String email;
    private String role;
    private Timestamp createdAt;

    public UserResponseDTO() {}

    public UserResponseDTO(User user) {
        if (user != null) {
            this.id = user.getId();
            this.name = user.getName();
            this.email = user.getEmail();
            this.role = user.getRole();
            this.createdAt = user.getCreatedAt();
        }
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public Timestamp getCreatedAt() { return createdAt; }
}
