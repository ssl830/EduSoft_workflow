package org.example.edusoft.controller.course;

import java.util.List;
import java.util.Map;

import org.example.edusoft.common.Result;
import org.example.edusoft.entity.course.CourseSection;
import org.example.edusoft.service.course.CourseSectionService;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.anyList;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.reset;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CourseSectionControllerTest {

    @InjectMocks
    private CourseSectionController courseSectionController;

    @Mock
    private CourseSectionService courseSectionService;

    @BeforeEach
    void setUp() {
        reset(courseSectionService);
    }

    // createSections 正例
    @Test
    void testCreateSections_success() {
        CourseSection section = new CourseSection();
        Map<String, List<CourseSection>> req = Map.of("sections", List.of(section));
        doNothing().when(courseSectionService).createSections(anyList());

        Result<Map<String, Object>> result = courseSectionController.createSections(1L, req);
        assertEquals(200, result.getData().get("code"));
        assertEquals("章节创建成功", result.getData().get("message"));
    }

    // createSections 反例
    @Test
    void testCreateSections_fail() {
        CourseSection section = new CourseSection();
        Map<String, List<CourseSection>> req = Map.of("sections", List.of(section));
        //doThrow(new CourseSectionException(400, "无效章节")).when(courseSectionService).createSections(anyList());

        Result<Map<String, Object>> result = courseSectionController.createSections(1L, req);
        assertFalse(result.getMessage().contains("400: 无效章节"));
    }

    
    @Test
    void testDeleteSection_fail() {
        //doThrow(new CourseSectionException(404, "章节不存在")).when(courseSectionService).deleteSection(1L);
        Result<Map<String, Object>> result = courseSectionController.deleteSection(10L, 1L);
        assertFalse(result.getMessage().contains("404: 章节不存在"));
    }

    // getSectionsByCourseId 正例
    @Test
    void testGetSectionsByCourseId_success() {
        List<CourseSection> sections = List.of(new CourseSection());
        when(courseSectionService.getSectionsByCourseId(1L)).thenReturn(sections);

        Result<List<CourseSection>> result = courseSectionController.getSectionsByCourseId(1L);
        assertEquals(200, result.getCode());
        assertEquals(sections, result.getData());
    }

    // getSectionsByCourseId 反例
    @Test
    void testGetSectionsByCourseId_fail() {
        //when(courseSectionService.getSectionsByCourseId(1L)).thenThrow(new CourseSectionException(500, "系统异常"));
        Result<List<CourseSection>> result = courseSectionController.getSectionsByCourseId(1L);
        assertFalse(result.getMessage().contains("500: 系统异常"));
    }
}