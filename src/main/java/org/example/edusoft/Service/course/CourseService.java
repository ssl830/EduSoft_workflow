package org.example.edusoft.service.course;

import org.example.edusoft.entity.course.Course;
import java.util.List;

public interface CourseService {
    // 创建课程
    Course createCourse(Course course);
    
    // 获取课程列表（根据用户角色返回不同结果）
    List<Course> getCoursesByUserId(Long userId);
    
    // 获取课程详情
    Course getCourseById(Long id);
    
    // 更新课程信息
    Course updateCourse(Course course);
    
    // 删除课程
    boolean deleteCourse(Long id);
} 