package org.example.edusoft.mapper.classroom;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.example.edusoft.entity.classroom.Class;
import java.util.List;
import java.util.Map;

@Mapper
public interface ClassMapper extends BaseMapper<Class> {
    
    @Select("SELECT c.* FROM Class c " +
            "LEFT JOIN Course co ON c.course_id = co.id " +
            "WHERE co.teacher_id = #{teacherId}")
    List<Class> getClassesByTeacherId(Long teacherId);
    
    @Select("SELECT c.* FROM Class c " +
            "LEFT JOIN ClassUser cu ON c.id = cu.class_id " +
            "WHERE cu.user_id = #{studentId}")
    List<Class> getClassesByStudentId(Long studentId);

    @Select("SELECT DISTINCT c.* FROM Class c " +
            "LEFT JOIN Course co ON c.course_id = co.id " +
            "LEFT JOIN ClassUser cu ON c.id = cu.class_id " +
            "WHERE co.teacher_id = #{userId} OR cu.user_id = #{userId}")
    List<Class> getClassesByUserId(Long userId);

    @Select("SELECT c.*, co.name as course_name FROM Class c " +
            "LEFT JOIN Course co ON c.course_id = co.id " +
            "WHERE c.id = #{id}")
    Map<String, Object> getClassDetailById(Long id);
} 