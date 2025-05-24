package org.example.edusoft.service.discussion.impl;

import org.example.edusoft.entity.discussion.DiscussionReply;
import org.example.edusoft.mapper.discussion.DiscussionMapper;
import org.example.edusoft.mapper.discussion.DiscussionReplyMapper;
import org.example.edusoft.service.discussion.DiscussionReplyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class DiscussionReplyServiceImpl implements DiscussionReplyService {
    
    @Autowired
    private DiscussionReplyMapper replyMapper;
    
    @Autowired
    private DiscussionMapper discussionMapper;
    
    @Override
    @Transactional
    public DiscussionReply createReply(DiscussionReply reply) {
        replyMapper.insert(reply);
        // 更新讨论的回复数
        discussionMapper.incrementReplyCount(reply.getDiscussionId());
        return reply;
    }
    
    @Override
    @Transactional
    public DiscussionReply updateReply(DiscussionReply reply) {
        replyMapper.updateById(reply);
        return reply;
    }
    
    @Override
    @Transactional
    public void deleteReply(Long id) {
        DiscussionReply reply = replyMapper.selectById(id);
        if (reply != null) {
            replyMapper.deleteById(id);
            // 更新讨论的回复数
            discussionMapper.decrementReplyCount(reply.getDiscussionId());
        }
    }
    
    @Override
    public DiscussionReply getReply(Long id) {
        return replyMapper.selectById(id);
    }
    
    @Override
    public List<DiscussionReply> getRepliesByDiscussion(Long discussionId) {
        return replyMapper.findByDiscussionId(discussionId);
    }
    
    @Override
    public List<DiscussionReply> getRepliesByUser(Long userId) {
        return replyMapper.findByUserId(userId);
    }
    
    @Override
    public List<DiscussionReply> getTopLevelReplies(Long discussionId) {
        return replyMapper.findTopLevelReplies(discussionId);
    }
    
    @Override
    public List<DiscussionReply> getRepliesByParent(Long parentReplyId) {
        return replyMapper.findRepliesByParentId(parentReplyId);
    }
    
    @Override
    public List<DiscussionReply> getTeacherReplies(Long discussionId) {
        return replyMapper.findTeacherReplies(discussionId);
    }
    
    @Override
    public int countRepliesByDiscussion(Long discussionId) {
        return replyMapper.countByDiscussionId(discussionId);
    }
    
    @Override
    public int countRepliesByUser(Long userId) {
        return replyMapper.countByUserId(userId);
    }
    
    @Override
    @Transactional
    public void deleteRepliesByDiscussion(Long discussionId) {
        replyMapper.deleteByDiscussionId(discussionId);
    }
} 