package org.example.edusoft.service.user.impl;
import org.springframework.stereotype.Service;
import org.example.edusoft.mapper.UserMapper;
import org.example.edusoft.service.user.UserService;
import org.example.edusoft.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public User findByUsername(String username) {
        return userMapper.findByUserName(username);
    }
    @Override
    public User findById(Long id) {
        return userMapper.findById(id);
    }
    @Override
    public User findByUserid(String userid) {
        return userMapper.findByUserid(userid);
    }
    @Override
    public void save(User user) {
        if (user.getId() == null) {
            userMapper.insert(user);
        } else {
            userMapper.update(user);
        }
    }
}