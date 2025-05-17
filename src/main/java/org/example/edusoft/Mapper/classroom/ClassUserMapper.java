package org.example.edusoft.mapper.classroom;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.example.edusoft.entity.classroom.ClassUser;
import java.util.List;

@Mapper
public interface ClassUserMapper extends BaseMapper<ClassUser> {
    
    @Select("SELECT cu.* FROM ClassUser cu " +
            "WHERE cu.class_id = #{classId}")
    List<ClassUser> getClassUsersByClassId(Long classId);
    
    @Select("SELECT COUNT(*) FROM ClassUser " +
            "WHERE class_id = #{classId} AND user_id = #{userId}")
    int checkUserInClass(Long classId, Long userId);
} 