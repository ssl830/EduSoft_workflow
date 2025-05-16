package org.example.edusoft.entity;
import jakarta.validation.constraints.*;

// 创建一个DTO类来接收更新请求
public class UserUpdate{
    @Size(max = 100, message = "姓名长度不能超过100个字符")
    private String name;

    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    private String email;

    // getter和setter
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}