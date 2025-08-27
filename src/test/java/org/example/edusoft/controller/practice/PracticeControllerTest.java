package org.example.edusoft.controller.practice;

import org.example.edusoft.common.Result;
import org.example.edusoft.entity.practice.Practice;
import org.example.edusoft.entity.practice.Question;
import org.example.edusoft.entity.practice.PracticeListDTO;
import org.example.edusoft.service.practice.PracticeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PracticeControllerTest {

    @Mock
    private PracticeService practiceService;

    @InjectMocks
    private PracticeController practiceController;

    @BeforeEach
    void setUp() {
        // 重置所有mock对象
        reset(practiceService);
    }

    // createPractice
    @Test
    void testCreatePractice_Success() {
        Practice practice = new Practice();
        practice.setId(123L);
        when(practiceService.createPractice(any())).thenReturn(practice);

        Result<Map<String, Object>> result = practiceController.createPractice(practice);
        assertEquals(200, result.getCode());
        assertEquals(123L, result.getData().get("practiceId"));
    }

    @Test
    void testCreatePractice_Failure() {
        Practice practice = new Practice();
        when(practiceService.createPractice(any())).thenThrow(new RuntimeException("fail"));
        Result<Map<String, Object>> result = practiceController.createPractice(practice);
        assertEquals(500, result.getCode());
        assertTrue(result.getMessage().contains("fail"));
    }

    // updatePractice
    @Test
    void testUpdatePractice_Success() {
        Practice practice = new Practice();
        practice.setId(1L);
        when(practiceService.updatePractice(any())).thenReturn(practice);

        Result<Practice> result = practiceController.updatePractice(1L, practice);
        assertEquals(200, result.getCode());
        assertEquals(practice, result.getData());
    }

    @Test
    void testUpdatePractice_Failure() {
        Practice practice = new Practice();
        when(practiceService.updatePractice(any())).thenThrow(new RuntimeException("fail"));
        Result<Practice> result = practiceController.updatePractice(1L, practice);
        assertEquals(500, result.getCode());
        assertTrue(result.getMessage().contains("fail"));
    }

    // getPracticeList
    @Test
    void testGetPracticeList_Success() {
        List<Practice> list = Collections.singletonList(new Practice());
        when(practiceService.getPracticeList(1L)).thenReturn(list);

        Result<List<Practice>> result = practiceController.getPracticeList(1L);
        assertEquals(200, result.getCode());
        assertEquals(list, result.getData());
    }

    @Test
    void testGetPracticeList_Failure() {
        when(practiceService.getPracticeList(1L)).thenThrow(new RuntimeException("fail"));
        Result<List<Practice>> result = practiceController.getPracticeList(1L);
        assertEquals(500, result.getCode());
        assertTrue(result.getMessage().contains("fail"));
    }

    // getPracticeDetail
    @Test
    void testGetPracticeDetail_Success() {
        Practice practice = new Practice();
        when(practiceService.getPracticeDetail(2L)).thenReturn(practice);

        Result<Practice> result = practiceController.getPracticeDetail(2L);
        assertEquals(200, result.getCode());
        assertEquals(practice, result.getData());
    }

    @Test
    void testGetPracticeDetail_Failure() {
        when(practiceService.getPracticeDetail(2L)).thenThrow(new RuntimeException("fail"));
        Result<Practice> result = practiceController.getPracticeDetail(2L);
        assertEquals(500, result.getCode());
        assertTrue(result.getMessage().contains("fail"));
    }

    // deletePractice
    @Test
    void testDeletePractice_Success() {
        doNothing().when(practiceService).deletePractice(3L);
        Result<Void> result = practiceController.deletePractice(3L);
        assertEquals(200, result.getCode());
    }

    @Test
    void testDeletePractice_Failure() {
        doThrow(new RuntimeException("fail")).when(practiceService).deletePractice(3L);
        Result<Void> result = practiceController.deletePractice(3L);
        assertEquals(500, result.getCode());
        assertTrue(result.getMessage().contains("fail"));
    }

    // addQuestionToPractice
    @Test
    void testAddQuestionToPractice_Success() {
        doNothing().when(practiceService).addQuestionToPractice(1L, 2L, 5);
        Result<Void> result = practiceController.addQuestionToPractice(1L, 2L, 5);
        assertEquals(200, result.getCode());
    }

    @Test
    void testAddQuestionToPractice_Failure() {
        doThrow(new RuntimeException("fail")).when(practiceService).addQuestionToPractice(1L, 2L, 5);
        Result<Void> result = practiceController.addQuestionToPractice(1L, 2L, 5);
        assertEquals(500, result.getCode());
        assertTrue(result.getMessage().contains("fail"));
    }

    // removeQuestionFromPractice
    @Test
    void testRemoveQuestionFromPractice_Success() {
        doNothing().when(practiceService).removeQuestionFromPractice(1L, 2L);
        Result<Void> result = practiceController.removeQuestionFromPractice(1L, 2L);
        assertEquals(200, result.getCode());
    }

    @Test
    void testRemoveQuestionFromPractice_Failure() {
        doThrow(new RuntimeException("fail")).when(practiceService).removeQuestionFromPractice(1L, 2L);
        Result<Void> result = practiceController.removeQuestionFromPractice(1L, 2L);
        assertEquals(500, result.getCode());
        assertTrue(result.getMessage().contains("fail"));
    }

    // getPracticeQuestions
    @Test
    void testGetPracticeQuestions_Success() {
        List<Question> questions = Collections.singletonList(new Question());
        when(practiceService.getPracticeQuestions(4L)).thenReturn(questions);

        Result<List<Question>> result = practiceController.getPracticeQuestions(4L);
        assertEquals(200, result.getCode());
        assertEquals(questions, result.getData());
    }

    @Test
    void testGetPracticeQuestions_Failure() {
        when(practiceService.getPracticeQuestions(4L)).thenThrow(new RuntimeException("fail"));
        Result<List<Question>> result = practiceController.getPracticeQuestions(4L);
        assertEquals(500, result.getCode());
        assertTrue(result.getMessage().contains("fail"));
    }

    // getStudentPracticeList
    @Test
    void testGetStudentPracticeList_Success() {
        List<PracticeListDTO> list = new ArrayList<>();
        when(practiceService.getStudentPracticeList(1L, 2L)).thenReturn(list);
        Result<List<PracticeListDTO>> result = practiceController.getStudentPracticeList(1L, 2L);
        assertEquals(200, result.getCode());
        assertEquals(list, result.getData());
    }

    @Test
    void testGetStudentPracticeList_IllegalArgument() {
        when(practiceService.getStudentPracticeList(1L, 2L)).thenThrow(new IllegalArgumentException("参数错误"));
        Result<List<PracticeListDTO>> result = practiceController.getStudentPracticeList(1L, 2L);
        assertEquals(400, result.getCode());
        assertTrue(result.getMessage().contains("参数错误"));
    }

    @Test
    void testGetStudentPracticeList_Failure() {
        when(practiceService.getStudentPracticeList(1L, 2L)).thenThrow(new RuntimeException("fail"));
        Result<List<PracticeListDTO>> result = practiceController.getStudentPracticeList(1L, 2L);
        assertEquals(500, result.getCode());
        assertTrue(result.getMessage().contains("fail"));
    }

    // getPracticeListForTeacher
    @Test
    void testGetPracticeListForTeacher_Success() {
        List<Practice> list = new ArrayList<>();
        when(practiceService.getPracticeList(2L)).thenReturn(list);
        Result<List<Practice>> result = practiceController.getPracticeListForTeacher(2L);
        assertEquals(200, result.getCode());
        assertEquals(list, result.getData());
    }

    @Test
    void testGetPracticeListForTeacher_Failure() {
        when(practiceService.getPracticeList(2L)).thenThrow(new RuntimeException("fail"));
        Result<List<Practice>> result = practiceController.getPracticeListForTeacher(2L);
        assertEquals(500, result.getCode());
        assertTrue(result.getMessage().contains("fail"));
    }

    // updateQuestionScore
    @Test
    void testUpdateQuestionScore_Success() {
        doNothing().when(practiceService).addQuestionToPractice(1L, 2L, 10);
        Result<Void> result = practiceController.updateQuestionScore(1L, 2L, 10);
        assertEquals(200, result.getCode());
    }

    @Test
    void testUpdateQuestionScore_Failure() {
        doThrow(new RuntimeException("fail")).when(practiceService).addQuestionToPractice(1L, 2L, 10);
        Result<Void> result = practiceController.updateQuestionScore(1L, 2L, 10);
        assertEquals(500, result.getCode());
        assertTrue(result.getMessage().contains("fail"));
    }

    // getPracticeStats
    @Test
    void testGetPracticeStats() {
        Map<String, Object> stats = new HashMap<>();
        when(practiceService.getSubmissionStats(10L)).thenReturn(stats);
        Result<Map<String, Object>> result = practiceController.getPracticeStats(10L);
        assertEquals(200, result.getCode());
        assertEquals(stats, result.getData());
    }

    // updateScoreRate
    @Test
    void testUpdateScoreRate_Success() {
        doNothing().when(practiceService).updateScoreRateAfterDeadline(3L);
        Result<String> result = practiceController.updateScoreRate(3L);
        assertEquals(200, result.getCode());
        assertEquals("OK", result.getData());
    }

    @Test
    void testUpdateScoreRate_Failure() {
        doThrow(new RuntimeException("fail")).when(practiceService).updateScoreRateAfterDeadline(3L);
        Result<String> result = practiceController.updateScoreRate(3L);
        assertEquals(500, result.getCode());
        assertTrue(result.getMessage().contains("fail"));
    }

    // autoUpdateScoreRateForAllPractices
    @Test
    void testAutoUpdateScoreRateForAllPractices() {
        List<Long> ids = Arrays.asList(1L, 2L);
        when(practiceService.getAllEndedPracticeIds()).thenReturn(ids);
        doNothing().when(practiceService).updateScoreRateAfterDeadline(anyLong());
        practiceController.autoUpdateScoreRateForAllPractices();
        verify(practiceService, times(2)).updateScoreRateAfterDeadline(anyLong());
    }
}
