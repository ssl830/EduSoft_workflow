package com.example.courseplatform.entity;

import lombok.Data;

@Data
public class User {
    private Long id;
    private String userId;
    private String username;
    private String passwordHash;
    private String role;
    private String email;
    private String avatar;
    private String bio;
    private String createdAt;
    private String updatedAt;
} 