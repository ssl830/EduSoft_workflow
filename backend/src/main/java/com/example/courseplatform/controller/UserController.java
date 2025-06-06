package com.example.courseplatform.controller;

import com.example.courseplatform.common.Result;
import com.example.courseplatform.entity.User;
import com.example.courseplatform.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*", allowCredentials = "true")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/info")
    public Result<User> getUserInfo(@RequestHeader("Authorization") String token) {
        try {
            User user = userService.getUserByToken(token);
            if (user != null) {
                return Result.success(user);
            } else {
                return Result.error("用户未登录或token已过期");
            }
        } catch (Exception e) {
            return Result.error("获取用户信息失败：" + e.getMessage());
        }
    }
} 