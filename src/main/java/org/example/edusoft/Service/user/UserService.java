package org.example.edusoft.service.user;
import org.example.edusoft.entity.user.User;

public interface UserService {

    User findById(Long id);
    User findByUserId(String userId);
    void save(User user);
    void deactivateAccount(Long id);
       
}


