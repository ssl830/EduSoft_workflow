package org.example.edusoft.controller.practice;

import org.example.edusoft.common.domain.Result;
import org.example.edusoft.dto.practice.SubmissionRequest;
import org.example.edusoft.service.practice.SubmissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubmissionControllerTest {

    @Mock
    private SubmissionService submissionService;

    @InjectMocks
    private SubmissionController submissionController;

    @BeforeEach
    void setUp() {
        // 重置所有mock对象
        reset(submissionService);
    }

    // 正例：提交练习答案成功
    @Test
    void testSubmitPractice_Success() {
        SubmissionRequest request = new SubmissionRequest();
        request.setPracticeId(1L);
        request.setStudentId(2L);
        request.setAnswers(Collections.emptyList());

        Result<Long> mockResult = Result.ok(123L, "提交成功");
        when(submissionService.submitAndAutoJudge(1L, 2L, Collections.emptyList())).thenReturn(mockResult);

        Result<Long> result = submissionController.submitPractice(request);
        assertEquals(200, result.getCode());
        assertEquals(123L, result.getData());
    }

    // 反例：提交练习答案失败
    @Test
    void testSubmitPractice_Failure() {
        SubmissionRequest request = new SubmissionRequest();
        request.setPracticeId(1L);
        request.setStudentId(2L);
        request.setAnswers(Collections.emptyList());

        Result<Long> mockResult = Result.error("提交失败");
        when(submissionService.submitAndAutoJudge(1L, 2L, Collections.emptyList())).thenReturn(mockResult);

        Result<Long> result = submissionController.submitPractice(request);
        assertNotEquals(200, result.getCode());
        assertTrue(result.getMsg().contains("提交失败"));
    }
}