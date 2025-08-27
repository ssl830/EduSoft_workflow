package org.example.edusoft.controller.ai;

import org.example.edusoft.dto.ai.TeachingContentRequest;
import org.example.edusoft.dto.ai.TeachingContentResponse;
import org.example.edusoft.service.ai.TeachingContentService;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

class TeachingContentControllerTest {

    @InjectMocks
    private TeachingContentController teachingContentController;

    @Mock
    private TeachingContentService teachingContentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // 正例：service 正常返回
    @Test
    void testGenerateTeachingContent_success() {
        TeachingContentRequest req = new TeachingContentRequest();
        TeachingContentResponse resp = new TeachingContentResponse();
        when(teachingContentService.generateTeachingContent(req)).thenReturn(resp);

        TeachingContentResponse result = teachingContentController.generateTeachingContent(req);
        assertNotNull(result);
        assertSame(resp, result);
    }

    // 反例：service 抛出异常
    @Test
    void testGenerateTeachingContent_serviceThrows() {
        TeachingContentRequest req = new TeachingContentRequest();
        when(teachingContentService.generateTeachingContent(req)).thenThrow(new RuntimeException("fail"));

        assertThrows(RuntimeException.class, () -> 
            teachingContentController.generateTeachingContent(req)
        );
    }
}