package org.example.edusoft.controller.discussion;

import org.example.edusoft.entity.discussion.DiscussionReply;
import org.example.edusoft.service.discussion.DiscussionReplyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import org.example.edusoft.service.user.UserService;

@RestController
@RequestMapping("/api/discussion-replies")
public class DiscussionReplyController {
    
    @Autowired
    private DiscussionReplyService discussionReplyService;
    
    @Autowired
    private UserService userService;
    
    @PostMapping
    @SaCheckLogin
    public ResponseEntity<DiscussionReply> createReply(@Valid @RequestBody DiscussionReply reply) {
        // 如果是老师回复，验证身份
        if (reply.getIsTeacherReply() != null && reply.getIsTeacherReply()) {
            if (!StpUtil.hasRole("teacher")) {
                return ResponseEntity.status(403).build();
            }
        }
        reply.setUserId(StpUtil.getLoginIdAsLong());
        return ResponseEntity.ok(discussionReplyService.createReply(reply));
    }
    
    @PutMapping("/{id}")
    @SaCheckLogin
    public ResponseEntity<DiscussionReply> updateReply(
            @PathVariable Long id,
            @Valid @RequestBody DiscussionReply reply) {
        // 验证是否是回复创建者
        DiscussionReply existingReply = discussionReplyService.getReply(id);
        if (existingReply == null) {
            return ResponseEntity.notFound().build();
        }
        if (!existingReply.getUserId().equals(StpUtil.getLoginIdAsLong())) {
            return ResponseEntity.status(403).build();
        }
        reply.setId(id);
        return ResponseEntity.ok(discussionReplyService.updateReply(reply));
    }
    
    @DeleteMapping("/{id}")
    @SaCheckLogin
    public ResponseEntity<Void> deleteReply(@PathVariable Long id) {
        // 验证是否是回复创建者
        DiscussionReply reply = discussionReplyService.getReply(id);
        if (reply == null) {
            return ResponseEntity.notFound().build();
        }
        if (!reply.getUserId().equals(StpUtil.getLoginIdAsLong())) {
            return ResponseEntity.status(403).build();
        }
        discussionReplyService.deleteReply(id);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/{id}")
    @SaCheckLogin
    public ResponseEntity<DiscussionReply> getReply(@PathVariable Long id) {
        DiscussionReply reply = discussionReplyService.getReply(id);
        if (reply != null) {
            return ResponseEntity.ok(reply);
        }
        return ResponseEntity.notFound().build();
    }
    
    @GetMapping("/discussion/{discussionId}")
    @SaCheckLogin
    public ResponseEntity<List<DiscussionReply>> getRepliesByDiscussion(@PathVariable Long discussionId) {
        return ResponseEntity.ok(discussionReplyService.getRepliesByDiscussion(discussionId));
    }
    
    @GetMapping("/user/{userId}")
    @SaCheckLogin
    public ResponseEntity<List<DiscussionReply>> getRepliesByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(discussionReplyService.getRepliesByUser(userId));
    }
    
    @GetMapping("/discussion/{discussionId}/top-level")
    @SaCheckLogin
    public ResponseEntity<List<DiscussionReply>> getTopLevelReplies(@PathVariable Long discussionId) {
        return ResponseEntity.ok(discussionReplyService.getTopLevelReplies(discussionId));
    }
    
    @GetMapping("/parent/{parentReplyId}")
    @SaCheckLogin
    public ResponseEntity<List<DiscussionReply>> getRepliesByParent(@PathVariable Long parentReplyId) {
        return ResponseEntity.ok(discussionReplyService.getRepliesByParent(parentReplyId));
    }
    
    @GetMapping("/discussion/{discussionId}/teacher")
    @SaCheckLogin
    public ResponseEntity<List<DiscussionReply>> getTeacherReplies(@PathVariable Long discussionId) {
        return ResponseEntity.ok(discussionReplyService.getTeacherReplies(discussionId));
    }
    
    @GetMapping("/discussion/{discussionId}/count")
    @SaCheckLogin
    public ResponseEntity<Integer> countRepliesByDiscussion(@PathVariable Long discussionId) {
        return ResponseEntity.ok(discussionReplyService.countRepliesByDiscussion(discussionId));
    }
    
    @GetMapping("/user/{userId}/count")
    @SaCheckLogin
    public ResponseEntity<Integer> countRepliesByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(discussionReplyService.countRepliesByUser(userId));
    }
    
    @DeleteMapping("/discussion/{discussionId}")
    @SaCheckRole("teacher")
    public ResponseEntity<Void> deleteRepliesByDiscussion(@PathVariable Long discussionId) {
        discussionReplyService.deleteRepliesByDiscussion(discussionId);
        return ResponseEntity.ok().build();
    }
} 