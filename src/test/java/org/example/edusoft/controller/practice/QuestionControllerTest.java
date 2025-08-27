package org.example.edusoft.controller.practice;

import org.example.edusoft.entity.practice.Question;
import org.example.edusoft.entity.practice.QuestionListDTO;
import org.example.edusoft.service.practice.QuestionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class QuestionControllerTest {

    @Mock
    private QuestionService questionService;

    @InjectMocks
    private QuestionController questionController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // createQuestion
    @Test
    void testCreateQuestion_Success() {
        Question q = new Question();
        q.setId(10L);
        when(questionService.createQuestion(any(Question.class))).thenReturn(q);

        ResponseEntity<Map<String, Object>> response = questionController.createQuestion(q);
        assertEquals(200, response.getBody().get("code"));
        Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
        assertEquals(10L, data.get("questionId"));
    }

    @Test
    void testCreateQuestion_Failure() {
        Question q = new Question();
        when(questionService.createQuestion(any(Question.class))).thenThrow(new RuntimeException("fail"));

        try {
            questionController.createQuestion(q);
            fail("Should have thrown");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("fail"));
        }
    }

    // updateQuestion
    @Test
    void testUpdateQuestion_Success() {
        Question q = new Question();
        q.setId(2L);
        when(questionService.updateQuestion(any(Question.class))).thenReturn(q);

        ResponseEntity<Map<String, Object>> response = questionController.updateQuestion(2L, q);
        assertEquals(200, response.getBody().get("code"));
        assertEquals(q, response.getBody().get("data"));
    }

    @Test
    void testUpdateQuestion_Failure() {
        Question q = new Question();
        when(questionService.updateQuestion(any(Question.class))).thenThrow(new RuntimeException("fail"));

        try {
            questionController.updateQuestion(2L, q);
            fail("Should have thrown");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("fail"));
        }
    }

    // getQuestionList
    @Test
    void testGetQuestionList_Success_All() {
        List<QuestionListDTO> list = Collections.singletonList(new QuestionListDTO());
        when(questionService.getAllQuestions()).thenReturn(list);

        ResponseEntity<Map<String, Object>> response = questionController.getQuestionList(null);
        assertEquals(200, response.getBody().get("code"));
        Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
        assertEquals(list, data.get("questions"));
    }

    @Test
    void testGetQuestionList_Success_ByCourse() {
        List<QuestionListDTO> list = Collections.singletonList(new QuestionListDTO());
        when(questionService.getQuestionListByCourse(1L)).thenReturn(list);

        ResponseEntity<Map<String, Object>> response = questionController.getQuestionList(1L);
        assertEquals(200, response.getBody().get("code"));
        Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
        assertEquals(list, data.get("questions"));
    }

    @Test
    void testGetQuestionList_Failure() {
        when(questionService.getAllQuestions()).thenThrow(new RuntimeException("fail"));
        try {
            questionController.getQuestionList(null);
            fail("Should have thrown");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("fail"));
        }
    }

    // getQuestionDetail
    @Test
    void testGetQuestionDetail_Success() {
        Question q = new Question();
        when(questionService.getQuestionDetail(2L)).thenReturn(q);

        ResponseEntity<Map<String, Object>> response = questionController.getQuestionDetail(2L);
        assertEquals(200, response.getBody().get("code"));
        assertEquals(q, response.getBody().get("data"));
    }

    @Test
    void testGetQuestionDetail_Failure() {
        when(questionService.getQuestionDetail(2L)).thenThrow(new RuntimeException("fail"));
        try {
            questionController.getQuestionDetail(2L);
            fail("Should have thrown");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("fail"));
        }
    }

    // deleteQuestion
    @Test
    void testDeleteQuestion_Success() {
        doNothing().when(questionService).deleteQuestion(4L);
        ResponseEntity<Map<String, Object>> response = questionController.deleteQuestion(4L);
        assertEquals(200, response.getBody().get("code"));
        Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
        assertEquals(4L, data.get("questionId"));
    }

    @Test
    void testDeleteQuestion_Failure() {
        doThrow(new RuntimeException("fail")).when(questionService).deleteQuestion(4L);
        try {
            questionController.deleteQuestion(4L);
            fail("Should have thrown");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("fail"));
        }
    }

    // importQuestionsToPractice
    @Test
    void testImportQuestionsToPractice_Success() {
        Map<String, Object> req = new HashMap<>();
        req.put("practiceId", 5);
        req.put("questionIds", Arrays.asList(1, 2, 3));
        req.put("scores", Arrays.asList(2, 2, 2));
        doNothing().when(questionService).importQuestionsToPractice(eq(5L), anyList(), anyList());

        ResponseEntity<Map<String, Object>> response = questionController.importQuestionsToPractice(req);
        assertEquals(200, response.getBody().get("code"));
        Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
        assertEquals(5L, data.get("practiceId"));
        assertEquals(3, data.get("questionCount"));
    }

    @Test
    void testImportQuestionsToPractice_Failure() {
        Map<String, Object> req = new HashMap<>();
        req.put("practiceId", 5);
        req.put("questionIds", Arrays.asList(1, 2, 3));
        req.put("scores", Arrays.asList(2, 2, 2));
        doThrow(new RuntimeException("fail")).when(questionService).importQuestionsToPractice(eq(5L), anyList(), anyList());

        try {
            questionController.importQuestionsToPractice(req);
            fail("Should have thrown");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("fail"));
        }
    }

    // getQuestionsBySection
    @Test
    void testGetQuestionsBySection_Success() {
        List<Question> list = Collections.singletonList(new Question());
        when(questionService.getQuestionsBySection(1L, 2L)).thenReturn(list);

        ResponseEntity<Map<String, Object>> response = questionController.getQuestionsBySection(1L, 2L);
        assertEquals(200, response.getBody().get("code"));
        assertEquals(list, response.getBody().get("data"));
    }

    @Test
    void testGetQuestionsBySection_Failure() {
        when(questionService.getQuestionsBySection(1L, 2L)).thenThrow(new RuntimeException("fail"));
        try {
            questionController.getQuestionsBySection(1L, 2L);
            fail("Should have thrown");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("fail"));
        }
    }

    // batchCreateQuestions
    @Test
    void testBatchCreateQuestions_Success() {
        List<Question> questions = Arrays.asList(new Question());
        when(questionService.batchCreateQuestions(questions)).thenReturn(questions);

        ResponseEntity<Map<String, Object>> response = questionController.batchCreateQuestions(questions);
        assertEquals(200, response.getBody().get("code"));
        assertEquals(questions, response.getBody().get("data"));
    }

    @Test
    void testBatchCreateQuestions_Failure() {
        List<Question> questions = Arrays.asList(new Question());
        when(questionService.batchCreateQuestions(questions)).thenThrow(new RuntimeException("fail"));

        try {
            questionController.batchCreateQuestions(questions);
            fail("Should have thrown");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("fail"));
        }
    }
}