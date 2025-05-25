package org.example.edusoft.service.discussion.impl;

import org.example.edusoft.entity.discussion.Discussion;
import org.example.edusoft.mapper.discussion.DiscussionMapper;
import org.example.edusoft.service.discussion.DiscussionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class DiscussionServiceImpl implements DiscussionService {
    
    @Autowired
    private DiscussionMapper discussionMapper;
    
    @Override
    @Transactional
    public Discussion createDiscussion(Discussion discussion) {
        discussionMapper.insert(discussion);
        return discussion;
    }
    
    @Override
    @Transactional
    public Discussion updateDiscussion(Discussion discussion) {
        discussionMapper.updateById(discussion);
        return discussion;
    }
    
    @Override
    @Transactional
    public void deleteDiscussion(Long id) {
        discussionMapper.deleteById(id);
    }
    
    @Override
    public Discussion getDiscussion(Long id) {
        return discussionMapper.selectById(id);
    }
    
    @Override
    public List<Discussion> getDiscussionsByCourse(Long courseId) {
        return discussionMapper.findByCourseId(courseId);
    }
    
    @Override
    public List<Discussion> getDiscussionsByClass(Long classId) {
        return discussionMapper.findByClassId(classId);
    }
    
    @Override
    public List<Discussion> getDiscussionsByCreator(Long creatorId) {
        return discussionMapper.findByCreatorId(creatorId);
    }
    
    @Override
    public List<Discussion> getDiscussionsByCourseAndClass(Long courseId, Long classId) {
        return discussionMapper.findByCourseAndClass(courseId, classId);
    }
    
    @Override
    @Transactional
    public void incrementViewCount(Long id) {
        discussionMapper.incrementViewCount(id);
    }
    
    @Override
    @Transactional
    public void updatePinnedStatus(Long id, Boolean isPinned) {
        discussionMapper.updatePinnedStatus(id, isPinned);
    }
    
    @Override
    @Transactional
    public void updateClosedStatus(Long id, Boolean isClosed) {
        discussionMapper.updateClosedStatus(id, isClosed);
    }
    
    @Override
    public int countDiscussionsByCourse(Long courseId) {
        return discussionMapper.countByCourseId(courseId);
    }
    
    @Override
    public int countDiscussionsByClass(Long classId) {
        return discussionMapper.countByClassId(classId);
    }
} 