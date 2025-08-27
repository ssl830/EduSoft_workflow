package org.example.edusoft.controller.ai;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.example.edusoft.service.ai.AiAssistantService;
import org.example.edusoft.service.selfpractice.SelfPracticeService;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import cn.dev33.satoken.stp.StpUtil;

class AiAssistantControllerTest {

    @InjectMocks
    private AiAssistantController controller;

    @Mock
    private AiAssistantService aiAssistantService;

    @Mock
    private SelfPracticeService selfPracticeService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    // uploadEmbeddingFile 正例
    @Test
    void testUploadEmbeddingFile_success() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "hello".getBytes());
        Map<String, Object> mockResp = Map.of("status", "ok");
        when(aiAssistantService.uploadEmbeddingFile(any(), any())).thenReturn(mockResp);

        Map<String, Object> resp = controller.uploadEmbeddingFile(file, "1");
        assertEquals("ok", resp.get("status"));
    }

    // uploadEmbeddingFile 反例
    @Test
    void testUploadEmbeddingFile_exception() {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "hello".getBytes());
        when(aiAssistantService.uploadEmbeddingFile(any(), any())).thenThrow(new RuntimeException("upload error"));

        assertThrows(RuntimeException.class, () -> controller.uploadEmbeddingFile(file, "1"));
    }

    // generateTeachingContent 正例
    @Test
    void testGenerateTeachingContent_success() {
        Map<String, Object> req = Map.of("question", "hi");
        Map<String, Object> mockResp = Map.of("result", "answer");
        when(aiAssistantService.generateTeachingContent(req)).thenReturn(mockResp);

        Map<String, Object> resp = controller.generateTeachingContent(req);
        assertEquals("answer", resp.get("result"));
    }

    // generateTeachingContent 反例
    @Test
    void testGenerateTeachingContent_exception() {
        Map<String, Object> req = Map.of("question", "hi");
        when(aiAssistantService.generateTeachingContent(req)).thenThrow(new RuntimeException("gen error"));

        assertThrows(RuntimeException.class, () -> controller.generateTeachingContent(req));
    }

    // generateStudentExercise 正例
    @Test
    void testGenerateStudentExercise_success() {
        Map<String, Object> req = Map.of("student", "A");
        Map<String, Object> body = new HashMap<>();
        body.put("exercises", List.of("Q1"));
        when(aiAssistantService.generateStudentExercise(req)).thenReturn(body);

        try (MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            stp.when(StpUtil::isLogin).thenReturn(true);
            stp.when(StpUtil::getLoginIdAsLong).thenReturn(1L);
            when(selfPracticeService.saveGeneratedPractice(eq(1L), any())).thenReturn(100L);

            Map<String, Object> resp = controller.generateStudentExercise(req);
            assertTrue(resp.containsKey("practiceId"));
            assertEquals(100L, resp.get("practiceId"));
        }
    }

    // generateStudentExercise 反例（无exercises字段）
    @Test
    void testGenerateStudentExercise_invalidData() {
        Map<String, Object> req = Map.of("student", "A");
        Map<String, Object> body = new HashMap<>();
        when(aiAssistantService.generateStudentExercise(req)).thenReturn(body);

        Map<String, Object> resp = controller.generateStudentExercise(req);
        assertEquals("error", resp.get("status"));
    }

    // analyzeExercise 正例
    @Test
    void testAnalyzeExercise_success() {
        Map<String, Object> req = Map.of("practiceId", 123L);
        Map<String, Object> mockResp = Map.of("analysis", "done");
        when(aiAssistantService.analyzeExercise(123L)).thenReturn(mockResp);

        Map<String, Object> resp = controller.analyzeExercise(req);
        assertEquals("done", resp.get("analysis"));
    }

    // analyzeExercise 反例
    @Test
    void testAnalyzeExercise_noPracticeId() {
        Map<String, Object> req = new HashMap<>();
        Map<String, Object> resp = controller.analyzeExercise(req);
        assertEquals("fail", resp.get("status"));
    }

    // listKnowledgeBases 正例
    @Test
    void testListKnowledgeBases_success() {
        List<String> mockList = List.of("kb1", "kb2");
        when(aiAssistantService.listKnowledgeBases()).thenReturn(mockList);

        List<String> resp = controller.listKnowledgeBases();
        assertEquals(2, resp.size());
        assertTrue(resp.contains("kb1"));
    }

    // listKnowledgeBases 反例
    @Test
    void testListKnowledgeBases_exception() {
        when(aiAssistantService.listKnowledgeBases()).thenThrow(new RuntimeException("fail"));
        assertThrows(RuntimeException.class, () -> controller.listKnowledgeBases());
    }
}