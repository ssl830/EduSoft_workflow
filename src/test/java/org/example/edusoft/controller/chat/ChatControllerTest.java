package org.example.edusoft.controller.chat;

import java.util.List;
import java.util.Map;

import org.example.edusoft.service.chat.ChatManagementService;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

class ChatControllerTest {

    @InjectMocks
    private ChatController chatController;

    @Mock
    private ChatManagementService chatManagementService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // getChatSessions 正例
    @Test
    void testGetChatSessions_success() {
        List<Map<String, Object>> sessions = List.of(Map.of("id", 1L));
        when(chatManagementService.getUserChatSessions()).thenReturn(sessions);

        Map<String, Object> result = chatController.getChatSessions();
        assertEquals(200, result.get("code"));
        assertEquals("success", result.get("message"));
        assertEquals(sessions, result.get("data"));
    }

    // getChatSessions 反例
    @Test
    void testGetChatSessions_exception() {
        when(chatManagementService.getUserChatSessions()).thenThrow(new RuntimeException("fail"));
        Map<String, Object> result = chatController.getChatSessions();
        assertEquals(500, result.get("code"));
        assertTrue(result.get("message").toString().contains("fail"));
    }

    // createChatSession 正例
    @Test
    void testCreateChatSession_success() {
        Map<String, Object> req = Map.of("courseName", "Math", "courseId", 2L);
        when(chatManagementService.createNewChatSession("Math", 2L)).thenReturn(123L);

        Map<String, Object> result = chatController.createChatSession(req);
        assertEquals(200, result.get("code"));
        assertEquals(123L, ((Map)result.get("data")).get("sessionId"));
    }

    // createChatSession 反例
    @Test
    void testCreateChatSession_exception() {
        Map<String, Object> req = Map.of("courseName", "Math", "courseId", 2L);
        when(chatManagementService.createNewChatSession(anyString(), any())).thenThrow(new RuntimeException("fail"));
        Map<String, Object> result = chatController.createChatSession(req);
        assertEquals(500, result.get("code"));
        assertTrue(result.get("message").toString().contains("fail"));
    }

    // getSessionDetail 正例
    @Test
    void testGetSessionDetail_success() {
        Map<String, Object> detail = Map.of("messages", List.of());
        when(chatManagementService.getSessionDetail(1L)).thenReturn(detail);

        Map<String, Object> result = chatController.getSessionDetail(1L);
        assertEquals(200, result.get("code"));
        assertEquals(detail, result.get("data"));
    }

    // getSessionDetail 反例
    @Test
    void testGetSessionDetail_exception() {
        when(chatManagementService.getSessionDetail(1L)).thenThrow(new RuntimeException("fail"));
        Map<String, Object> result = chatController.getSessionDetail(1L);
        assertEquals(500, result.get("code"));
        assertTrue(result.get("message").toString().contains("fail"));
    }

    // deleteChatSession 正例
    @Test
    void testDeleteChatSession_success() {
        Map<String, Object> result = chatController.deleteChatSession(1L);
        assertEquals(200, result.get("code"));
        assertEquals("删除成功", result.get("message"));
    }

    // deleteChatSession 反例
    @Test
    void testDeleteChatSession_exception() {
        doThrow(new RuntimeException("fail")).when(chatManagementService).deleteChatSession(1L);
        Map<String, Object> result = chatController.deleteChatSession(1L);
        assertEquals(500, result.get("code"));
        assertTrue(result.get("message").toString().contains("fail"));
    }
}