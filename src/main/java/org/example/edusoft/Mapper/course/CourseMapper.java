package org.example.edusoft.mapper.course;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.example.edusoft.entity.course.Course;
import org.example.edusoft.entity.course.CourseDetailDTO;
import org.example.edusoft.entity.course.CourseSection;
import java.util.List;

@Mapper
public interface CourseMapper extends BaseMapper<Course> {
    
    @Select("SELECT c.* FROM Course c " +
            "LEFT JOIN ClassUser cu ON c.id = cu.class_id " +
            "WHERE c.teacher_id = #{userId} OR cu.user_id = #{userId}")
    List<Course> getCoursesByUserId(Long userId);

    @Select("""
            SELECT 
                c.*,
                u.username as teacherName,
                (SELECT COUNT(DISTINCT cu.user_id) 
                 FROM ClassUser cu 
                 JOIN Class cl ON cu.class_id = cl.id 
                 WHERE cl.course_id = c.id) as studentCount,
                (SELECT COUNT(*) FROM Practice p WHERE p.course_id = c.id) as practiceCount,
                (SELECT COUNT(*) FROM Homework h 
                 JOIN Class cl ON h.class_id = cl.id 
                 WHERE cl.course_id = c.id) as homeworkCount,
                0 as resourceCount
            FROM Course c
            LEFT JOIN User u ON c.teacher_id = u.id
            WHERE c.id = #{courseId}
            """)
    CourseDetailDTO getCourseDetailById(Long courseId);

    @Select("SELECT * FROM CourseSection WHERE course_id = #{courseId} ORDER BY sort_order")
    List<CourseSection> getSectionsByCourseId(Long courseId);

    @Select("""
            SELECT 
                c.*,
                u.username as teacherName,
                (SELECT COUNT(DISTINCT cu.user_id) 
                 FROM ClassUser cu 
                 JOIN Class cl ON cu.class_id = cl.id 
                 WHERE cl.course_id = c.id) as studentCount,
                (SELECT COUNT(*) FROM Practice p WHERE p.course_id = c.id) as practiceCount,
                (SELECT COUNT(*) FROM Homework h 
                 JOIN Class cl ON h.class_id = cl.id 
                 WHERE cl.course_id = c.id) as homeworkCount,
                0 as resourceCount
            FROM Course c
            LEFT JOIN User u ON c.teacher_id = u.id
            WHERE c.teacher_id = #{userId} OR EXISTS (
                SELECT 1 FROM ClassUser cu 
                JOIN Class cl ON cu.class_id = cl.id 
                WHERE cl.course_id = c.id AND cu.user_id = #{userId}
            )
            """)
    List<CourseDetailDTO> getCourseDetailsByUserId(Long userId);
} 