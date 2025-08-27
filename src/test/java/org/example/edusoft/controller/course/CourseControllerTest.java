package org.example.edusoft.controller.course;

import java.util.List;

import org.example.edusoft.common.Result;
import org.example.edusoft.entity.course.Course;
import org.example.edusoft.entity.course.CourseDetailDTO;
import org.example.edusoft.service.course.CourseService;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

class CourseControllerTest {

    @InjectMocks
    private CourseController courseController;

    @Mock
    private CourseService courseService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // createCourse 正例
    @Test
    void testCreateCourse_success() {
        Course course = new Course();
        when(courseService.createCourse(course)).thenReturn(course);

        Result<Course> result = courseController.createCourse(course);
        assertEquals(200, result.getCode());
        assertEquals(course, result.getData());
    }

    // createCourse 反例
    @Test
    void testCreateCourse_fail() {
        Course course = new Course();
        when(courseService.createCourse(course)).thenThrow(new RuntimeException("fail"));
        Result<Course> result = courseController.createCourse(course);
        assertEquals(500, result.getCode());
    }

    // getCoursesByUserId 正例
    @Test
    void testGetCoursesByUserId_success() {
        List<CourseDetailDTO> list = List.of(new CourseDetailDTO());
        when(courseService.getCourseDetailsByUserId(1L)).thenReturn(list);
        Result<List<CourseDetailDTO>> result = courseController.getCoursesByUserId(1L);
        assertEquals(200, result.getCode());
        assertEquals(list, result.getData());
    }

    // getCoursesByUserId 反例
    @Test
    void testGetCoursesByUserId_fail() {
        when(courseService.getCourseDetailsByUserId(1L)).thenThrow(new RuntimeException("fail"));
        Result<List<CourseDetailDTO>> result = courseController.getCoursesByUserId(1L);
        assertEquals(500, result.getCode());
    }

    // getCourseById 正例
    @Test
    void testGetCourseById_success() {
        CourseDetailDTO dto = new CourseDetailDTO();
        when(courseService.getCourseDetailById(1L)).thenReturn(dto);
        Result<CourseDetailDTO> result = courseController.getCourseById(1L);
        assertEquals(200, result.getCode());
        assertEquals(dto, result.getData());
    }

    // getCourseById 反例
    @Test
    void testGetCourseById_fail() {
        when(courseService.getCourseDetailById(1L)).thenThrow(new RuntimeException("fail"));
        Result<CourseDetailDTO> result = courseController.getCourseById(1L);
        assertEquals(500, result.getCode());
    }

    // updateCourse 正例
    @Test
    void testUpdateCourse_success() {
        Course course = new Course();
        when(courseService.updateCourse(course)).thenReturn(course);
        Result<Course> result = courseController.updateCourse(1L, course);
        assertEquals(200, result.getCode());
        assertEquals(course, result.getData());
    }

    // updateCourse 反例
    @Test
    void testUpdateCourse_fail() {
        Course course = new Course();
        when(courseService.updateCourse(course)).thenThrow(new RuntimeException("fail"));
        Result<Course> result = courseController.updateCourse(1L, course);
        assertEquals(500, result.getCode());
    }

    // deleteCourse 正例
    @Test
    void testDeleteCourse_success() {
        when(courseService.deleteCourse(1L)).thenReturn(true);
        Result<Boolean> result = courseController.deleteCourse(1L);
        assertEquals(200, result.getCode());
        assertTrue(result.getData());
    }

    // deleteCourse 反例
    @Test
    void testDeleteCourse_fail() {
        when(courseService.deleteCourse(1L)).thenThrow(new RuntimeException("fail"));
        Result<Boolean> result = courseController.deleteCourse(1L);
        assertEquals(500, result.getCode());
    }

    // getAllCourses 正例
    @Test
    void testGetAllCourses_success() {
        List<CourseDetailDTO> list = List.of(new CourseDetailDTO());
        when(courseService.getAllCourses()).thenReturn(list);
        Result<List<CourseDetailDTO>> result = courseController.getAllCourses();
        assertEquals(200, result.getCode());
        assertEquals(list, result.getData());
    }

    // getAllCourses 反例
    @Test
    void testGetAllCourses_fail() {
        when(courseService.getAllCourses()).thenThrow(new RuntimeException("fail"));
        Result<List<CourseDetailDTO>> result = courseController.getAllCourses();
        assertEquals(500, result.getCode());
    }
}