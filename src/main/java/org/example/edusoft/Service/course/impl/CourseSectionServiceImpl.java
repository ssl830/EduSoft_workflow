package org.example.edusoft.service.course.impl;

import org.example.edusoft.entity.course.CourseSection;
import org.example.edusoft.mapper.course.CourseSectionMapper;
import org.example.edusoft.service.course.CourseSectionService;
import org.example.edusoft.exception.CourseSectionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.util.List;

@Service
public class CourseSectionServiceImpl implements CourseSectionService {
    @Autowired
    private CourseSectionMapper courseSectionMapper;

    @Override
    @Transactional
    public CourseSection createSection(CourseSection section) {
        // 参数检查
        if (section == null) {
            throw new CourseSectionException("SECTION_001", "章节信息不能为空");
        }
        if (section.getCourseId() == null) {
            throw new CourseSectionException("SECTION_002", "课程ID不能为空");
        }
        if (!StringUtils.hasText(section.getTitle())) {
            throw new CourseSectionException("SECTION_003", "章节标题不能为空");
        }

        // 获取当前课程的最大排序号
        List<CourseSection> sections = courseSectionMapper.getSectionsByCourseId(section.getCourseId());
        int maxSortOrder = sections.stream()
                .mapToInt(CourseSection::getSortOrder)
                .max()
                .orElse(0);
        section.setSortOrder(maxSortOrder + 1);
        
        try {
            courseSectionMapper.insert(section);
            return section;
        } catch (Exception e) {
            throw new CourseSectionException("SECTION_004", "创建章节失败：" + e.getMessage());
        }
    }

    @Override
    public List<CourseSection> getSectionsByCourseId(Long courseId) {
        if (courseId == null) {
            throw new CourseSectionException("SECTION_005", "课程ID不能为空");
        }
        try {
            return courseSectionMapper.getSectionsByCourseId(courseId);
        } catch (Exception e) {
            throw new CourseSectionException("SECTION_006", "获取章节列表失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public CourseSection updateSection(CourseSection section) {
        // 参数检查
        if (section == null) {
            throw new CourseSectionException("SECTION_007", "章节信息不能为空");
        }
        if (section.getId() == null) {
            throw new CourseSectionException("SECTION_008", "章节ID不能为空");
        }
        if (!StringUtils.hasText(section.getTitle())) {
            throw new CourseSectionException("SECTION_009", "章节标题不能为空");
        }

        // 检查章节是否存在
        CourseSection existingSection = courseSectionMapper.selectById(section.getId());
        if (existingSection == null) {
            throw new CourseSectionException("SECTION_010", "章节不存在");
        }

        try {
            courseSectionMapper.updateById(section);
            return section;
        } catch (Exception e) {
            throw new CourseSectionException("SECTION_011", "更新章节失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public boolean deleteSection(Long id) {
        if (id == null) {
            throw new CourseSectionException("SECTION_012", "章节ID不能为空");
        }

        // 检查章节是否存在
        CourseSection existingSection = courseSectionMapper.selectById(id);
        if (existingSection == null) {
            throw new CourseSectionException("SECTION_013", "章节不存在");
        }

        try {
            return courseSectionMapper.deleteById(id) > 0;
        } catch (Exception e) {
            throw new CourseSectionException("SECTION_014", "删除章节失败：" + e.getMessage());
        }
    }
} 