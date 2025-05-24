package org.example.edusoft.service.course;

import org.example.edusoft.entity.course.CourseSection;
import java.util.List;

public interface CourseSectionService {
    CourseSection createSection(CourseSection section);
    List<CourseSection> getSectionsByCourseId(Long courseId);
    CourseSection updateSection(CourseSection section);
    boolean deleteSection(Long id);
} 