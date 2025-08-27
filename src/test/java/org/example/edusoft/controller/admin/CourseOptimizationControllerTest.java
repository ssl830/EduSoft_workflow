package org.example.edusoft.controller.admin;

import java.util.HashMap;
import java.util.Map;

import org.example.edusoft.service.admin.CourseOptimizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class CourseOptimizationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CourseOptimizationService courseOptimizationService;

    @InjectMocks
    private CourseOptimizationController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    // 正例：全部参数合法，服务返回建议
    @Test
    void testGetOptimizationSuggestions_success() throws Exception {
        Map<String, Object> input = new HashMap<>();
        input.put("courseId", 1L);
        input.put("sectionId", 2L);
        input.put("averageScore", 80.5);
        input.put("errorRate", 0.1);
        input.put("studentCount", 30);

        Map<String, Object> mockSuggestions = new HashMap<>();
        mockSuggestions.put("advice", "Increase interactive activities");

        when(courseOptimizationService.generateOptimizationSuggestions(
                eq(1L), eq(2L), eq(80.5), eq(0.1), eq(30)
        )).thenReturn(mockSuggestions);

        String body = "{\n" +
                "  \"courseId\": 1,\n" +
                "  \"sectionId\": 2,\n" +
                "  \"averageScore\": 80.5,\n" +
                "  \"errorRate\": 0.1,\n" +
                "  \"studentCount\": 30\n" +
                "}";

        mockMvc.perform(post("/api/admin/dashboard/optimize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.advice").value("Increase interactive activities"));
    }

    // 反例：请求参数缺失，导致NumberFormatException
    @Test
    void testGetOptimizationSuggestions_invalidRequest() throws Exception {
        String body = "{\n" +
                "  \"courseId\": 1,\n" +
                "  \"sectionId\": 2,\n" +
                "  \"averageScore\": 80.5\n" +
                "}"; // 缺少 errorRate 和 studentCount

        mockMvc.perform(post("/api/admin/dashboard/optimize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
                
    }
}