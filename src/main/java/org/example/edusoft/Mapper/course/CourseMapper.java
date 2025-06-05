package org.example.edusoft.mapper.course;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.example.edusoft.entity.course.Course;
import java.util.List;

@Mapper
public interface CourseMapper extends BaseMapper<Course> {
    
    @Select("SELECT c.* FROM Course c " +
            "LEFT JOIN ClassUser cu ON c.id = cu.class_id " +
            "WHERE c.teacher_id = #{userId} OR cu.user_id = #{userId}")
    List<Course> getCoursesByUserId(Long userId);
} 