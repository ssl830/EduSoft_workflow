package org.example.edusoft.controller.discussion;

import org.example.edusoft.entity.discussion.DiscussionReply;
import org.example.edusoft.entity.user.User;
import org.example.edusoft.entity.discussion.Discussion;
import org.example.edusoft.service.discussion.DiscussionReplyService;
import org.example.edusoft.service.user.UserService;
import org.example.edusoft.service.discussion.DiscussionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DiscussionReplyControllerTest {

    @InjectMocks
    private DiscussionReplyController controller;
    @Mock
    private DiscussionReplyService discussionReplyService;
    @Mock
    private UserService userService;
    @Mock
    private DiscussionService discussionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // createReply 正例
    @Test
    void testCreateReply_success() {
        try (MockedStatic<cn.dev33.satoken.stp.StpUtil> stp = mockStatic(cn.dev33.satoken.stp.StpUtil.class)) {
            stp.when(cn.dev33.satoken.stp.StpUtil::isLogin).thenReturn(true);
            stp.when(cn.dev33.satoken.stp.StpUtil::getLoginIdAsLong).thenReturn(1L);
            User user = new User();
            user.setUserId("u1");
            user.setRole(User.UserRole.student);
            when(userService.findById(1L)).thenReturn(user);
            Discussion discussion = new Discussion();
            discussion.setIsClosed(false);
            when(discussionService.getDiscussion(10L)).thenReturn(discussion);
            DiscussionReply reply = new DiscussionReply();
            when(discussionReplyService.createReply(any())).thenReturn(reply);

            ResponseEntity<?> resp = controller.createReply(10L, null, new DiscussionReply());
            assertEquals(200, resp.getStatusCodeValue());
            assertTrue(resp.getBody() instanceof DiscussionReply);
        }
    }
    // createReply 反例（未登录）
    @Test
    void testCreateReply_notLogin() {
        try (MockedStatic<cn.dev33.satoken.stp.StpUtil> stp = mockStatic(cn.dev33.satoken.stp.StpUtil.class)) {
            stp.when(cn.dev33.satoken.stp.StpUtil::isLogin).thenReturn(false);
            ResponseEntity<?> resp = controller.createReply(10L, null, new DiscussionReply());
            assertEquals(401, resp.getStatusCodeValue());
        }
    }

    // updateReply 正例
    @Test
    void testUpdateReply_success() {
        try (MockedStatic<cn.dev33.satoken.stp.StpUtil> stp = mockStatic(cn.dev33.satoken.stp.StpUtil.class)) {
            stp.when(cn.dev33.satoken.stp.StpUtil::isLogin).thenReturn(true);
            stp.when(cn.dev33.satoken.stp.StpUtil::getLoginIdAsLong).thenReturn(1L);
            User user = new User();
            user.setUserId("u2");
            when(userService.findById(1L)).thenReturn(user);
            DiscussionReply exist = new DiscussionReply();
            exist.setUserId(1L);
            exist.setDiscussionId(20L);
            when(discussionReplyService.getReply(5L)).thenReturn(exist);
            when(discussionReplyService.updateReply(any())).thenReturn(exist);

            ResponseEntity<?> resp = controller.updateReply(5L, new DiscussionReply());
            assertEquals(200, resp.getStatusCodeValue());
        }
    }
    // updateReply 反例（非本人）
    @Test
    void testUpdateReply_forbidden() {
        try (MockedStatic<cn.dev33.satoken.stp.StpUtil> stp = mockStatic(cn.dev33.satoken.stp.StpUtil.class)) {
            stp.when(cn.dev33.satoken.stp.StpUtil::isLogin).thenReturn(true);
            stp.when(cn.dev33.satoken.stp.StpUtil::getLoginIdAsLong).thenReturn(2L);
            User user = new User();
            when(userService.findById(2L)).thenReturn(user);
            DiscussionReply exist = new DiscussionReply();
            exist.setUserId(1L);
            when(discussionReplyService.getReply(5L)).thenReturn(exist);

            ResponseEntity<?> resp = controller.updateReply(5L, new DiscussionReply());
            assertEquals(403, resp.getStatusCodeValue());
        }
    }

    // deleteReply 正例
    @Test
    void testDeleteReply_success() {
        try (MockedStatic<cn.dev33.satoken.stp.StpUtil> stp = mockStatic(cn.dev33.satoken.stp.StpUtil.class)) {
            stp.when(cn.dev33.satoken.stp.StpUtil::isLogin).thenReturn(true);
            stp.when(cn.dev33.satoken.stp.StpUtil::getLoginIdAsLong).thenReturn(1L);
            DiscussionReply reply = new DiscussionReply();
            reply.setUserId(1L);
            when(discussionReplyService.getReply(7L)).thenReturn(reply);
            doNothing().when(discussionReplyService).deleteReply(7L);

            ResponseEntity<?> resp = controller.deleteReply(7L);
            assertEquals(200, resp.getStatusCodeValue());
        }
    }
    // deleteReply 反例（未找到）
    @Test
    void testDeleteReply_notFound() {
        try (MockedStatic<cn.dev33.satoken.stp.StpUtil> stp = mockStatic(cn.dev33.satoken.stp.StpUtil.class)) {
            stp.when(cn.dev33.satoken.stp.StpUtil::isLogin).thenReturn(true);
            when(discussionReplyService.getReply(7L)).thenReturn(null);

            ResponseEntity<?> resp = controller.deleteReply(7L);
            assertEquals(404, resp.getStatusCodeValue());
        }
    }

    // getReply 正例
    @Test
    void testGetReply_success() {
        DiscussionReply reply = new DiscussionReply();
        when(discussionReplyService.getReply(10L)).thenReturn(reply);

        ResponseEntity<DiscussionReply> resp = controller.getReply(10L);
        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(reply, resp.getBody());
    }
    // getReply 反例
    @Test
    void testGetReply_notFound() {
        when(discussionReplyService.getReply(10L)).thenReturn(null);

        ResponseEntity<DiscussionReply> resp = controller.getReply(10L);
        assertEquals(404, resp.getStatusCodeValue());
    }

    // getRepliesByDiscussion 正例
    @Test
    void testGetRepliesByDiscussion_success() {
        List<DiscussionReply> list = List.of(new DiscussionReply());
        when(discussionReplyService.getRepliesByDiscussion(11L)).thenReturn(list);

        ResponseEntity<List<DiscussionReply>> resp = controller.getRepliesByDiscussion(11L);
        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(list, resp.getBody());
    }
    // getRepliesByDiscussion 反例（空）
    @Test
    void testGetRepliesByDiscussion_empty() {
        when(discussionReplyService.getRepliesByDiscussion(11L)).thenReturn(Collections.emptyList());
        ResponseEntity<List<DiscussionReply>> resp = controller.getRepliesByDiscussion(11L);
        assertEquals(200, resp.getStatusCodeValue());
        assertTrue(resp.getBody().isEmpty());
    }

    // getRepliesByUser 正例
    @Test
    void testGetRepliesByUser_success() {
        List<DiscussionReply> list = List.of(new DiscussionReply());
        when(discussionReplyService.getRepliesByUser(22L)).thenReturn(list);

        ResponseEntity<List<DiscussionReply>> resp = controller.getRepliesByUser(22L);
        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(list, resp.getBody());
    }
    // getRepliesByUser 反例（空）
    @Test
    void testGetRepliesByUser_empty() {
        when(discussionReplyService.getRepliesByUser(22L)).thenReturn(Collections.emptyList());
        ResponseEntity<List<DiscussionReply>> resp = controller.getRepliesByUser(22L);
        assertEquals(200, resp.getStatusCodeValue());
        assertTrue(resp.getBody().isEmpty());
    }

    // getTopLevelReplies 正例
    @Test
    void testGetTopLevelReplies_success() {
        List<DiscussionReply> list = List.of(new DiscussionReply());
        when(discussionReplyService.getTopLevelReplies(33L)).thenReturn(list);

        ResponseEntity<List<DiscussionReply>> resp = controller.getTopLevelReplies(33L);
        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(list, resp.getBody());
    }
    // getTopLevelReplies 反例
    @Test
    void testGetTopLevelReplies_empty() {
        when(discussionReplyService.getTopLevelReplies(33L)).thenReturn(Collections.emptyList());
        ResponseEntity<List<DiscussionReply>> resp = controller.getTopLevelReplies(33L);
        assertEquals(200, resp.getStatusCodeValue());
        assertTrue(resp.getBody().isEmpty());
    }

    // getRepliesByParent 正例
    @Test
    void testGetRepliesByParent_success() {
        List<DiscussionReply> list = List.of(new DiscussionReply());
        when(discussionReplyService.getRepliesByParent(44L)).thenReturn(list);

        ResponseEntity<List<DiscussionReply>> resp = controller.getRepliesByParent(44L);
        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(list, resp.getBody());
    }
    // getRepliesByParent 反例
    @Test
    void testGetRepliesByParent_empty() {
        when(discussionReplyService.getRepliesByParent(44L)).thenReturn(Collections.emptyList());
        ResponseEntity<List<DiscussionReply>> resp = controller.getRepliesByParent(44L);
        assertEquals(200, resp.getStatusCodeValue());
        assertTrue(resp.getBody().isEmpty());
    }

    // getTeacherReplies 正例
    @Test
    void testGetTeacherReplies_success() {
        List<DiscussionReply> list = List.of(new DiscussionReply());
        when(discussionReplyService.getTeacherReplies(55L)).thenReturn(list);

        ResponseEntity<List<DiscussionReply>> resp = controller.getTeacherReplies(55L);
        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(list, resp.getBody());
    }
    // getTeacherReplies 反例
    @Test
    void testGetTeacherReplies_empty() {
        when(discussionReplyService.getTeacherReplies(55L)).thenReturn(Collections.emptyList());
        ResponseEntity<List<DiscussionReply>> resp = controller.getTeacherReplies(55L);
        assertEquals(200, resp.getStatusCodeValue());
        assertTrue(resp.getBody().isEmpty());
    }

    // countRepliesByDiscussion 正例
    @Test
    void testCountRepliesByDiscussion_success() {
        when(discussionReplyService.countRepliesByDiscussion(66L)).thenReturn(100);
        ResponseEntity<Integer> resp = controller.countRepliesByDiscussion(66L);
        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(100, resp.getBody());
    }
    // countRepliesByDiscussion 反例
    @Test
    void testCountRepliesByDiscussion_zero() {
        when(discussionReplyService.countRepliesByDiscussion(66L)).thenReturn(0);
        ResponseEntity<Integer> resp = controller.countRepliesByDiscussion(66L);
        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(0, resp.getBody());
    }

    // countRepliesByUser 正例
    @Test
    void testCountRepliesByUser_success() {
        when(discussionReplyService.countRepliesByUser(77L)).thenReturn(8);
        ResponseEntity<Integer> resp = controller.countRepliesByUser(77L);
        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(8, resp.getBody());
    }
    // countRepliesByUser 反例
    @Test
    void testCountRepliesByUser_zero() {
        when(discussionReplyService.countRepliesByUser(77L)).thenReturn(0);
        ResponseEntity<Integer> resp = controller.countRepliesByUser(77L);
        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(0, resp.getBody());
    }

    // deleteRepliesByDiscussion 正例
    @Test
    void testDeleteRepliesByDiscussion_success() {
        try (MockedStatic<cn.dev33.satoken.stp.StpUtil> stp = mockStatic(cn.dev33.satoken.stp.StpUtil.class)) {
            stp.when(cn.dev33.satoken.stp.StpUtil::isLogin).thenReturn(true);
            stp.when(cn.dev33.satoken.stp.StpUtil::getLoginIdAsLong).thenReturn(1L);
            User user = new User();
            user.setRole(User.UserRole.teacher);
            when(userService.findById(1L)).thenReturn(user);
            Discussion discussion = new Discussion();
            when(discussionService.getDiscussion(88L)).thenReturn(discussion);
            doNothing().when(discussionReplyService).deleteRepliesByDiscussion(88L);

            ResponseEntity<?> resp = controller.deleteRepliesByDiscussion(88L);
            assertEquals(200, resp.getStatusCodeValue());
        }
    }
    // deleteRepliesByDiscussion 反例（非老师）
    @Test
    void testDeleteRepliesByDiscussion_notTeacher() {
        try (MockedStatic<cn.dev33.satoken.stp.StpUtil> stp = mockStatic(cn.dev33.satoken.stp.StpUtil.class)) {
            stp.when(cn.dev33.satoken.stp.StpUtil::isLogin).thenReturn(true);
            stp.when(cn.dev33.satoken.stp.StpUtil::getLoginIdAsLong).thenReturn(1L);
            User user = new User();
            user.setRole(User.UserRole.student);
            when(userService.findById(1L)).thenReturn(user);

            ResponseEntity<?> resp = controller.deleteRepliesByDiscussion(88L);
            assertEquals(403, resp.getStatusCodeValue());
        }
    }
}