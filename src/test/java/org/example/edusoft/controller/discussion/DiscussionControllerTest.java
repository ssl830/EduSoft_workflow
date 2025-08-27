package org.example.edusoft.controller.discussion;

import org.example.edusoft.entity.discussion.Discussion;
import org.example.edusoft.entity.user.User;
import org.example.edusoft.service.discussion.DiscussionService;
import org.example.edusoft.service.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DiscussionControllerTest {

    @InjectMocks
    private DiscussionController controller;

    @Mock
    private DiscussionService discussionService;
    @Mock
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // createDiscussion 正例
    @Test
    void testCreateDiscussion_success() {
        try (MockedStatic<cn.dev33.satoken.stp.StpUtil> stp = mockStatic(cn.dev33.satoken.stp.StpUtil.class)) {
            stp.when(cn.dev33.satoken.stp.StpUtil::isLogin).thenReturn(true);
            stp.when(cn.dev33.satoken.stp.StpUtil::getLoginIdAsLong).thenReturn(1L);
            User user = new User();
            user.setUserId("1001");
            when(userService.findById(1L)).thenReturn(user);
            Discussion discussion = new Discussion();
            when(discussionService.createDiscussion(any())).thenReturn(discussion);

            ResponseEntity<?> resp = controller.createDiscussion(10L, 20L, new Discussion());
            assertEquals(200, resp.getStatusCodeValue());
            assertTrue(resp.getBody() instanceof Discussion);
        }
    }
    // createDiscussion 反例（未登录）
    @Test
    void testCreateDiscussion_notLogin() {
        try (MockedStatic<cn.dev33.satoken.stp.StpUtil> stp = mockStatic(cn.dev33.satoken.stp.StpUtil.class)) {
            stp.when(cn.dev33.satoken.stp.StpUtil::isLogin).thenReturn(false);
            ResponseEntity<?> resp = controller.createDiscussion(10L, 20L, new Discussion());
            assertEquals(401, resp.getStatusCodeValue());
        }
    }

    // updateDiscussion 正例
    @Test
    void testUpdateDiscussion_success() {
        try (MockedStatic<cn.dev33.satoken.stp.StpUtil> stp = mockStatic(cn.dev33.satoken.stp.StpUtil.class)) {
            stp.when(cn.dev33.satoken.stp.StpUtil::isLogin).thenReturn(true);
            stp.when(cn.dev33.satoken.stp.StpUtil::getLoginIdAsLong).thenReturn(1L);
            Discussion exist = new Discussion();
            exist.setCreatorId(1L);
            exist.setClassId(2L);
            exist.setCourseId(3L);
            when(discussionService.getDiscussion(5L)).thenReturn(exist);
            when(discussionService.updateDiscussion(any())).thenReturn(exist);

            ResponseEntity<?> resp = controller.updateDiscussion(5L, new Discussion());
            assertEquals(200, resp.getStatusCodeValue());
        }
    }
    // updateDiscussion 反例（非本人）
    @Test
    void testUpdateDiscussion_forbidden() {
        try (MockedStatic<cn.dev33.satoken.stp.StpUtil> stp = mockStatic(cn.dev33.satoken.stp.StpUtil.class)) {
            stp.when(cn.dev33.satoken.stp.StpUtil::isLogin).thenReturn(true);
            stp.when(cn.dev33.satoken.stp.StpUtil::getLoginIdAsLong).thenReturn(2L);
            Discussion exist = new Discussion();
            exist.setCreatorId(1L);
            when(discussionService.getDiscussion(5L)).thenReturn(exist);

            ResponseEntity<?> resp = controller.updateDiscussion(5L, new Discussion());
            assertEquals(403, resp.getStatusCodeValue());
        }
    }

    // deleteDiscussion 正例
    @Test
    void testDeleteDiscussion_success() {
        try (MockedStatic<cn.dev33.satoken.stp.StpUtil> stp = mockStatic(cn.dev33.satoken.stp.StpUtil.class)) {
            stp.when(cn.dev33.satoken.stp.StpUtil::isLogin).thenReturn(true);
            stp.when(cn.dev33.satoken.stp.StpUtil::getLoginIdAsLong).thenReturn(1L);
            Discussion exist = new Discussion();
            exist.setCreatorId(1L);
            when(discussionService.getDiscussion(5L)).thenReturn(exist);

            ResponseEntity<?> resp = controller.deleteDiscussion(5L);
            assertEquals(200, resp.getStatusCodeValue());
        }
    }
    // deleteDiscussion 反例（未找到）
    @Test
    void testDeleteDiscussion_notFound() {
        try (MockedStatic<cn.dev33.satoken.stp.StpUtil> stp = mockStatic(cn.dev33.satoken.stp.StpUtil.class)) {
            stp.when(cn.dev33.satoken.stp.StpUtil::isLogin).thenReturn(true);
            when(discussionService.getDiscussion(5L)).thenReturn(null);

            ResponseEntity<?> resp = controller.deleteDiscussion(5L);
            assertEquals(404, resp.getStatusCodeValue());
        }
    }

    // getDiscussion 正例
    @Test
    void testGetDiscussion_success() {
        try (MockedStatic<cn.dev33.satoken.stp.StpUtil> stp = mockStatic(cn.dev33.satoken.stp.StpUtil.class)) {
            stp.when(cn.dev33.satoken.stp.StpUtil::isLogin).thenReturn(true);
            Discussion exist = new Discussion();
            when(discussionService.getDiscussion(1L)).thenReturn(exist);

            ResponseEntity<?> resp = controller.getDiscussion(1L);
            assertEquals(200, resp.getStatusCodeValue());
        }
    }
    // getDiscussion 反例（不存在）
    @Test
    void testGetDiscussion_notFound() {
        try (MockedStatic<cn.dev33.satoken.stp.StpUtil> stp = mockStatic(cn.dev33.satoken.stp.StpUtil.class)) {
            stp.when(cn.dev33.satoken.stp.StpUtil::isLogin).thenReturn(true);
            when(discussionService.getDiscussion(1L)).thenReturn(null);

            ResponseEntity<?> resp = controller.getDiscussion(1L);
            assertEquals(404, resp.getStatusCodeValue());
        }
    }

    // getDiscussionsByCourse 正例
    @Test
    void testGetDiscussionsByCourse_success() {
        when(discussionService.getDiscussionsByCourse(1L)).thenReturn(List.of(new Discussion()));
        ResponseEntity<List<Discussion>> resp = controller.getDiscussionsByCourse(1L);
        assertEquals(200, resp.getStatusCodeValue());
        assertFalse(resp.getBody().isEmpty());
    }
    // getDiscussionsByCourse 反例
    @Test
    void testGetDiscussionsByCourse_empty() {
        when(discussionService.getDiscussionsByCourse(1L)).thenReturn(Collections.emptyList());
        ResponseEntity<List<Discussion>> resp = controller.getDiscussionsByCourse(1L);
        assertEquals(200, resp.getStatusCodeValue());
        assertTrue(resp.getBody().isEmpty());
    }

    // getDiscussionsByClass 正例
    @Test
    void testGetDiscussionsByClass_success() {
        when(discussionService.getDiscussionsByClass(1L)).thenReturn(List.of(new Discussion()));
        ResponseEntity<List<Discussion>> resp = controller.getDiscussionsByClass(1L);
        assertEquals(200, resp.getStatusCodeValue());
        assertFalse(resp.getBody().isEmpty());
    }
    // getDiscussionsByClass 反例
    @Test
    void testGetDiscussionsByClass_empty() {
        when(discussionService.getDiscussionsByClass(1L)).thenReturn(Collections.emptyList());
        ResponseEntity<List<Discussion>> resp = controller.getDiscussionsByClass(1L);
        assertEquals(200, resp.getStatusCodeValue());
        assertTrue(resp.getBody().isEmpty());
    }

    // getDiscussionsByCreator 正例
    @Test
    void testGetDiscussionsByCreator_success() {
        when(discussionService.getDiscussionsByCreator(2L)).thenReturn(List.of(new Discussion()));
        ResponseEntity<List<Discussion>> resp = controller.getDiscussionsByCreator(2L);
        assertEquals(200, resp.getStatusCodeValue());
        assertFalse(resp.getBody().isEmpty());
    }
    // getDiscussionsByCreator 反例
    @Test
    void testGetDiscussionsByCreator_empty() {
        when(discussionService.getDiscussionsByCreator(2L)).thenReturn(Collections.emptyList());
        ResponseEntity<List<Discussion>> resp = controller.getDiscussionsByCreator(2L);
        assertEquals(200, resp.getStatusCodeValue());
        assertTrue(resp.getBody().isEmpty());
    }

    // getDiscussionsByCourseAndClass 正例
    @Test
    void testGetDiscussionsByCourseAndClass_success() {
        when(discussionService.getDiscussionsByCourseAndClass(3L, 4L)).thenReturn(List.of(new Discussion()));
        ResponseEntity<List<Discussion>> resp = controller.getDiscussionsByCourseAndClass(3L, 4L);
        assertEquals(200, resp.getStatusCodeValue());
        assertFalse(resp.getBody().isEmpty());
    }
    // getDiscussionsByCourseAndClass 反例
    @Test
    void testGetDiscussionsByCourseAndClass_empty() {
        when(discussionService.getDiscussionsByCourseAndClass(3L, 4L)).thenReturn(Collections.emptyList());
        ResponseEntity<List<Discussion>> resp = controller.getDiscussionsByCourseAndClass(3L, 4L);
        assertEquals(200, resp.getStatusCodeValue());
        assertTrue(resp.getBody().isEmpty());
    }

    // updatePinnedStatus 正例
    @Test
    void testUpdatePinnedStatus_success() {
        User teacher = new User();
        teacher.setRole(User.UserRole.teacher);
        when(userService.findById(1L)).thenReturn(teacher);
        when(discussionService.getDiscussion(10L)).thenReturn(new Discussion());

        try (MockedStatic<cn.dev33.satoken.stp.StpUtil> stp = mockStatic(cn.dev33.satoken.stp.StpUtil.class)) {
            stp.when(cn.dev33.satoken.stp.StpUtil::getLoginIdAsLong).thenReturn(1L);

            ResponseEntity<?> resp = controller.updatePinnedStatus(10L, true);
            assertEquals(200, resp.getStatusCodeValue());
        }
    }
    // updatePinnedStatus 反例（非教师）
    @Test
    void testUpdatePinnedStatus_notTeacher() {
        User user = new User();
        user.setRole(User.UserRole.student);
        when(userService.findById(1L)).thenReturn(user);
        try (MockedStatic<cn.dev33.satoken.stp.StpUtil> stp = mockStatic(cn.dev33.satoken.stp.StpUtil.class)) {
            stp.when(cn.dev33.satoken.stp.StpUtil::getLoginIdAsLong).thenReturn(1L);

            ResponseEntity<?> resp = controller.updatePinnedStatus(10L, true);
            assertEquals(403, resp.getStatusCodeValue());
        }
    }

    // updateClosedStatus 正例
    @Test
    void testUpdateClosedStatus_success() {
        User teacher = new User();
        teacher.setRole(User.UserRole.teacher);
        when(userService.findById(1L)).thenReturn(teacher);
        when(discussionService.getDiscussion(10L)).thenReturn(new Discussion());

        try (MockedStatic<cn.dev33.satoken.stp.StpUtil> stp = mockStatic(cn.dev33.satoken.stp.StpUtil.class)) {
            stp.when(cn.dev33.satoken.stp.StpUtil::getLoginIdAsLong).thenReturn(1L);

            ResponseEntity<?> resp = controller.updateClosedStatus(10L, true);
            assertEquals(200, resp.getStatusCodeValue());
        }
    }
    // updateClosedStatus 反例（用户不存在）
    @Test
    void testUpdateClosedStatus_userNotFound() {
        when(userService.findById(1L)).thenReturn(null);
        try (MockedStatic<cn.dev33.satoken.stp.StpUtil> stp = mockStatic(cn.dev33.satoken.stp.StpUtil.class)) {
            stp.when(cn.dev33.satoken.stp.StpUtil::getLoginIdAsLong).thenReturn(1L);

            ResponseEntity<?> resp = controller.updateClosedStatus(10L, true);
            assertEquals(404, resp.getStatusCodeValue());
        }
    }

    // countDiscussionsByCourse 正例
    @Test
    void testCountDiscussionsByCourse_success() {
        when(discussionService.countDiscussionsByCourse(1L)).thenReturn(7);
        ResponseEntity<Integer> resp = controller.countDiscussionsByCourse(1L);
        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(7, resp.getBody());
    }
    // countDiscussionsByClass 正例
    @Test
    void testCountDiscussionsByClass_success() {
        when(discussionService.countDiscussionsByClass(2L)).thenReturn(4);
        ResponseEntity<Integer> resp = controller.countDiscussionsByClass(2L);
        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(4, resp.getBody());
    }
}