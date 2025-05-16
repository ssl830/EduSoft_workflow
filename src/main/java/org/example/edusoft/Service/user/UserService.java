package org.example.edusoft.service.user;
import org.example.edusoft.entity.User;

public interface UserService {
    User findByUsername(String username);
    User findById(Long id);
    User findByUserid(String userid);
    void save(User user);
}


