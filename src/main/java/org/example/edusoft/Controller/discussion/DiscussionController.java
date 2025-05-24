package org.example.edusoft.controller.discussion;

import org.example.edusoft.entity.discussion.Discussion;
import org.example.edusoft.service.discussion.DiscussionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;

@RestController
@RequestMapping("/api/discussions")
public class DiscussionController {
    
    @Autowired
    private DiscussionService discussionService;
    
    @PostMapping
    @SaCheckLogin
    public ResponseEntity<Discussion> createDiscussion(@Valid @RequestBody Discussion discussion) {
        discussion.setCreatorId(StpUtil.getLoginIdAsLong());
        return ResponseEntity.ok(discussionService.createDiscussion(discussion));
    }
    
    @PutMapping("/{id}")
    @SaCheckLogin
    public ResponseEntity<Discussion> updateDiscussion(
            @PathVariable Long id,
            @Valid @RequestBody Discussion discussion) {
        // 验证是否是讨论创建者
        Discussion existingDiscussion = discussionService.getDiscussion(id);
        if (existingDiscussion == null) {
            return ResponseEntity.notFound().build();
        }
        if (!existingDiscussion.getCreatorId().equals(StpUtil.getLoginIdAsLong())) {
            return ResponseEntity.status(403).build();
        }
        discussion.setId(id);
        return ResponseEntity.ok(discussionService.updateDiscussion(discussion));
    }
    
    @DeleteMapping("/{id}")
    @SaCheckLogin
    public ResponseEntity<Void> deleteDiscussion(@PathVariable Long id) {
        // 验证是否是讨论创建者
        Discussion discussion = discussionService.getDiscussion(id);
        if (discussion == null) {
            return ResponseEntity.notFound().build();
        }
        if (!discussion.getCreatorId().equals(StpUtil.getLoginIdAsLong())) {
            return ResponseEntity.status(403).build();
        }
        discussionService.deleteDiscussion(id);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/{id}")
    @SaCheckLogin
    public ResponseEntity<Discussion> getDiscussion(@PathVariable Long id) {
        Discussion discussion = discussionService.getDiscussion(id);
        if (discussion != null) {
            discussionService.incrementViewCount(id);
            return ResponseEntity.ok(discussion);
        }
        return ResponseEntity.notFound().build();
    }
    
    @GetMapping("/course/{courseId}")
    @SaCheckLogin
    public ResponseEntity<List<Discussion>> getDiscussionsByCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(discussionService.getDiscussionsByCourse(courseId));
    }
    
    @GetMapping("/class/{classId}")
    @SaCheckLogin
    public ResponseEntity<List<Discussion>> getDiscussionsByClass(@PathVariable Long classId) {
        return ResponseEntity.ok(discussionService.getDiscussionsByClass(classId));
    }
    
    @GetMapping("/creator/{creatorId}")
    @SaCheckLogin
    public ResponseEntity<List<Discussion>> getDiscussionsByCreator(@PathVariable Long creatorId) {
        return ResponseEntity.ok(discussionService.getDiscussionsByCreator(creatorId));
    }
    
    @GetMapping("/course/{courseId}/class/{classId}")
    @SaCheckLogin
    public ResponseEntity<List<Discussion>> getDiscussionsByCourseAndClass(
            @PathVariable Long courseId,
            @PathVariable Long classId) {
        return ResponseEntity.ok(discussionService.getDiscussionsByCourseAndClass(courseId, classId));
    }
    
    @PutMapping("/{id}/pin")
    @SaCheckRole("teacher")
    public ResponseEntity<Void> updatePinnedStatus(
            @PathVariable Long id,
            @RequestParam Boolean isPinned) {
        discussionService.updatePinnedStatus(id, isPinned);
        return ResponseEntity.ok().build();
    }
    
    @PutMapping("/{id}/close")
    @SaCheckRole("teacher")
    public ResponseEntity<Void> updateClosedStatus(
            @PathVariable Long id,
            @RequestParam Boolean isClosed) {
        discussionService.updateClosedStatus(id, isClosed);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/course/{courseId}/count")
    @SaCheckLogin
    public ResponseEntity<Integer> countDiscussionsByCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(discussionService.countDiscussionsByCourse(courseId));
    }
    
    @GetMapping("/class/{classId}/count")
    @SaCheckLogin
    public ResponseEntity<Integer> countDiscussionsByClass(@PathVariable Long classId) {
        return ResponseEntity.ok(discussionService.countDiscussionsByClass(classId));
    }
} 