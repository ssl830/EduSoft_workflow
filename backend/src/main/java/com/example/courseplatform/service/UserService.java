package com.example.courseplatform.service;

import com.example.courseplatform.entity.User;

public interface UserService {
    User getUserByToken(String token);
} 