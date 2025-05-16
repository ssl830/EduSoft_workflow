package org.example.edusoft.mapper;
import org.example.edusoft.entity.*;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper {
    @Select("SELECT * FROM user WHERE username = #{username}")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "username", column = "username"),
        @Result(property = "passwordHash", column = "password_hash"),
        @Result(property = "role", column = "role"),
        @Result(property = "name", column = "name"),
        @Result(property = "email", column = "email"),
        @Result(property = "userid", column = "userid"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at")
    })
    User findByUserName(@Param("username") String username);

    @Select("SELECT * FROM User WHERE id = #{id}")
    User findById(Long id);

    @Select("SELECT * FROM user WHERE userid = #{userid}")
    User findByUserid(@Param("userid") String userid);

    @Insert("INSERT INTO User (username, userid, password_hash, role, name, email, created_at, updated_at) " +
           "VALUES (#{username},#{userid}, #{passwordHash}, #{role}, #{name}, #{email}, " +
           "CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(User user);

    @Update("UPDATE User SET username = #{username}, userid=#{userid}, password_hash = #{passwordHash}, " +
            "role = #{role}, name = #{name}, email = #{email} " +
            "WHERE id = #{id}")
    void update(User user);
}