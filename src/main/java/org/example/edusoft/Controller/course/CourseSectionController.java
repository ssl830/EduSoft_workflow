package org.example.edusoft.controller.course;

import org.example.edusoft.entity.course.CourseSection;
import org.example.edusoft.service.course.CourseSectionService;
import org.example.edusoft.common.Result;
import org.example.edusoft.exception.CourseSectionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/courses/sections")
public class CourseSectionController {
    @Autowired
    private CourseSectionService courseSectionService;

    @PostMapping
    public Result<CourseSection> createSection(@RequestBody CourseSection section) {
        try {
            CourseSection createdSection = courseSectionService.createSection(section);
            return Result.success(createdSection);
        } catch (CourseSectionException e) {
            return Result.error(e.getCode() + ": " + e.getMessage());
        } catch (Exception e) {
            return Result.error("系统错误：" + e.getMessage());
        }
    }

    @GetMapping("/course/{courseId}")
    public Result<List<CourseSection>> getSectionsByCourseId(@PathVariable Long courseId) {
        try {
            List<CourseSection> sections = courseSectionService.getSectionsByCourseId(courseId);
            return Result.success(sections);
        } catch (CourseSectionException e) {
            return Result.error(e.getCode() + ": " + e.getMessage());
        } catch (Exception e) {
            return Result.error("系统错误：" + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result<CourseSection> updateSection(@PathVariable Long id, @RequestBody CourseSection section) {
        try {
            section.setId(id);
            CourseSection updatedSection = courseSectionService.updateSection(section);
            return Result.success(updatedSection);
        } catch (CourseSectionException e) {
            return Result.error(e.getCode() + ": " + e.getMessage());
        } catch (Exception e) {
            return Result.error("系统错误：" + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteSection(@PathVariable Long id) {
        try {
            boolean result = courseSectionService.deleteSection(id);
            return Result.success(result);
        } catch (CourseSectionException e) {
            return Result.error(e.getCode() + ": " + e.getMessage());
        } catch (Exception e) {
            return Result.error("系统错误：" + e.getMessage());
        }
    }
} 