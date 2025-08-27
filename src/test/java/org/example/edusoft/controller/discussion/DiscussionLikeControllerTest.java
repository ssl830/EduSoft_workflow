package org.example.edusoft.controller.discussion;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.example.edusoft.entity.discussion.DiscussionLike;
import org.example.edusoft.service.discussion.DiscussionLikeService;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

class DiscussionLikeControllerTest {

    @InjectMocks
    private DiscussionLikeController controller;

    @Mock
    private DiscussionLikeService discussionLikeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // likeDiscussion 正例
    @Test
    void testLikeDiscussion_success() {
        try (MockedStatic<cn.dev33.satoken.stp.StpUtil> stp = mockStatic(cn.dev33.satoken.stp.StpUtil.class)) {
            stp.when(cn.dev33.satoken.stp.StpUtil::isLogin).thenReturn(true);
            stp.when(cn.dev33.satoken.stp.StpUtil::getLoginIdAsLong).thenReturn(1L);
            DiscussionLike like = new DiscussionLike();
            when(discussionLikeService.likeDiscussion(2L, 1L)).thenReturn(like);

            ResponseEntity<?> resp = controller.likeDiscussion(2L);
            assertEquals(200, resp.getStatusCodeValue());
            assertEquals(like, resp.getBody());
        }
    }
    // likeDiscussion 反例（未登录）
    @Test
    void testLikeDiscussion_notLogin() {
        try (MockedStatic<cn.dev33.satoken.stp.StpUtil> stp = mockStatic(cn.dev33.satoken.stp.StpUtil.class)) {
            stp.when(cn.dev33.satoken.stp.StpUtil::isLogin).thenReturn(false);
            ResponseEntity<?> resp = controller.likeDiscussion(2L);
            assertEquals(401, resp.getStatusCodeValue());
        }
    }

    // unlikeDiscussion 正例
    @Test
    void testUnlikeDiscussion_success() {
        try (MockedStatic<cn.dev33.satoken.stp.StpUtil> stp = mockStatic(cn.dev33.satoken.stp.StpUtil.class)) {
            stp.when(cn.dev33.satoken.stp.StpUtil::isLogin).thenReturn(true);
            stp.when(cn.dev33.satoken.stp.StpUtil::getLoginIdAsLong).thenReturn(1L);

            doNothing().when(discussionLikeService).unlikeDiscussion(2L, 1L);

            ResponseEntity<?> resp = controller.unlikeDiscussion(2L);
            assertEquals(200, resp.getStatusCodeValue());
            assertTrue(((Map<?, ?>)resp.getBody()).get("message").toString().contains("取消点赞成功"));
        }
    }
    // unlikeDiscussion 反例（未登录）
    @Test
    void testUnlikeDiscussion_notLogin() {
        try (MockedStatic<cn.dev33.satoken.stp.StpUtil> stp = mockStatic(cn.dev33.satoken.stp.StpUtil.class)) {
            stp.when(cn.dev33.satoken.stp.StpUtil::isLogin).thenReturn(false);
            ResponseEntity<?> resp = controller.unlikeDiscussion(2L);
            assertEquals(401, resp.getStatusCodeValue());
        }
    }

    // getLikesByDiscussion 正例
    @Test
    void testGetLikesByDiscussion_success() {
        List<DiscussionLike> list = List.of(new DiscussionLike());
        when(discussionLikeService.getLikesByDiscussion(3L)).thenReturn(list);

        ResponseEntity<List<DiscussionLike>> resp = controller.getLikesByDiscussion(3L);
        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(list, resp.getBody());
    }
    // getLikesByDiscussion 反例（空列表也算反例）
    @Test
    void testGetLikesByDiscussion_empty() {
        when(discussionLikeService.getLikesByDiscussion(3L)).thenReturn(Collections.emptyList());
        ResponseEntity<List<DiscussionLike>> resp = controller.getLikesByDiscussion(3L);
        assertEquals(200, resp.getStatusCodeValue());
        assertTrue(resp.getBody().isEmpty());
    }

    // getLikesByUser 正例
    @Test
    void testGetLikesByUser_success() {
        List<DiscussionLike> list = List.of(new DiscussionLike());
        when(discussionLikeService.getLikesByUser(4L)).thenReturn(list);

        ResponseEntity<List<DiscussionLike>> resp = controller.getLikesByUser(4L);
        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(list, resp.getBody());
    }
    // getLikesByUser 反例（空列表）
    @Test
    void testGetLikesByUser_empty() {
        when(discussionLikeService.getLikesByUser(4L)).thenReturn(Collections.emptyList());
        ResponseEntity<List<DiscussionLike>> resp = controller.getLikesByUser(4L);
        assertEquals(200, resp.getStatusCodeValue());
        assertTrue(resp.getBody().isEmpty());
    }

    // countLikesByDiscussion 正例
    @Test
    void testCountLikesByDiscussion_success() {
        when(discussionLikeService.countLikesByDiscussion(5L)).thenReturn(7);
        ResponseEntity<Integer> resp = controller.countLikesByDiscussion(5L);
        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(7, resp.getBody());
    }
    // countLikesByDiscussion 反例（0也算反例）
    @Test
    void testCountLikesByDiscussion_zero() {
        when(discussionLikeService.countLikesByDiscussion(5L)).thenReturn(0);
        ResponseEntity<Integer> resp = controller.countLikesByDiscussion(5L);
        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(0, resp.getBody());
    }

    // countLikesByUser 正例
    @Test
    void testCountLikesByUser_success() {
        when(discussionLikeService.countLikesByUser(6L)).thenReturn(2);
        ResponseEntity<Integer> resp = controller.countLikesByUser(6L);
        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(2, resp.getBody());
    }
    // countLikesByUser 反例（0也算反例）
    @Test
    void testCountLikesByUser_zero() {
        when(discussionLikeService.countLikesByUser(6L)).thenReturn(0);
        ResponseEntity<Integer> resp = controller.countLikesByUser(6L);
        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(0, resp.getBody());
    }

    // hasLiked 正例
    @Test
    void testHasLiked_true() {
        try (MockedStatic<cn.dev33.satoken.stp.StpUtil> stp = mockStatic(cn.dev33.satoken.stp.StpUtil.class)) {
            stp.when(cn.dev33.satoken.stp.StpUtil::getLoginIdAsLong).thenReturn(10L);
            when(discussionLikeService.hasLiked(100L, 10L)).thenReturn(true);

            ResponseEntity<Boolean> resp = controller.hasLiked(100L);
            assertEquals(200, resp.getStatusCodeValue());
            assertTrue(resp.getBody());
        }
    }
    // hasLiked 反例
    @Test
    void testHasLiked_false() {
        try (MockedStatic<cn.dev33.satoken.stp.StpUtil> stp = mockStatic(cn.dev33.satoken.stp.StpUtil.class)) {
            stp.when(cn.dev33.satoken.stp.StpUtil::getLoginIdAsLong).thenReturn(10L);
            when(discussionLikeService.hasLiked(100L, 10L)).thenReturn(false);

            ResponseEntity<Boolean> resp = controller.hasLiked(100L);
            assertEquals(200, resp.getStatusCodeValue());
            assertFalse(resp.getBody());
        }
    }
}