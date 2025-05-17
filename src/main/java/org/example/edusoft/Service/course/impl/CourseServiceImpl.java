package org.example.edusoft.service.course.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.edusoft.entity.course.Course;
import org.example.edusoft.mapper.course.CourseMapper;
import org.example.edusoft.service.course.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CourseServiceImpl implements CourseService {

    @Autowired
    private CourseMapper courseMapper;

    @Override
    @Transactional
    public Course createCourse(Course course) {
        courseMapper.insert(course);
        return course;
    }

    @Override
    public List<Course> getCoursesByUserId(Long userId) {
        return courseMapper.getCoursesByUserId(userId);
    }

    @Override
    public Course getCourseById(Long id) {
        return courseMapper.selectById(id);
    }

    @Override
    @Transactional
    public Course updateCourse(Course course) {
        courseMapper.updateById(course);
        return course;
    }

    @Override
    @Transactional
    public boolean deleteCourse(Long id) {
        return courseMapper.deleteById(id) > 0;
    }
} 