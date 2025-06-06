package org.example.edusoft.mapper.classroom;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.example.edusoft.entity.classroom.Class;
import org.example.edusoft.entity.classroom.ClassDetailDTO;
import java.util.List;

@Mapper
public interface ClassMapper extends BaseMapper<Class> {
    
    @Select("SELECT c.id, c.course_id as courseId, co.name as courseName, " +
            "c.name as className, c.class_code as classCode " +
            "FROM Class c " +
            "LEFT JOIN Course co ON c.course_id = co.id " +
            "WHERE c.id = #{id}")
    ClassDetailDTO getClassDetailById(Long id);

    @Select("SELECT DISTINCT c.id, c.course_id as courseId, co.name as courseName, " +
            "co.teacher_id as teacherId, c.name as className, c.class_code as classCode " +
            "FROM Class c " +
            "LEFT JOIN Course co ON c.course_id = co.id " +
            "LEFT JOIN ClassUser cu ON c.id = cu.class_id " +
            "WHERE co.teacher_id = #{userId} OR cu.user_id = #{userId}")
    List<ClassDetailDTO> getClassesByUserId(Long userId);

    @Select("SELECT c.* FROM Class c " +
            "LEFT JOIN Course co ON c.course_id = co.id " +
            "WHERE co.teacher_id = #{teacherId}")
    List<Class> getClassesByTeacherId(Long teacherId);
    
    @Select("SELECT c.* FROM Class c " +
            "LEFT JOIN ClassUser cu ON c.id = cu.class_id " +
            "WHERE cu.user_id = #{studentId}")
    List<Class> getClassesByStudentId(Long studentId);
} 