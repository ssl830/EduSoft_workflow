package org.example.edusoft.service.practice1.impl;

import org.example.edusoft.entity.practice.*;
import org.example.edusoft.mapper.practice1.PracticeMapper;
import org.example.edusoft.mapper.practice1.QuestionMapper;
import org.example.edusoft.service.practice1.PracticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class PracticeServiceImpl implements PracticeService {

    @Autowired
    private PracticeMapper practiceMapper;
    @Autowired
    private QuestionMapper questionMapper;

    @Override
    public void favoriteQuestion(Long studentId, Long questionId) {
        // 检查题目是否已收藏
        if (!practiceMapper.isQuestionFavorited(studentId, questionId)) {
            practiceMapper.insertFavoriteQuestion(studentId, questionId);
        }
    }

    @Override
    public void unfavoriteQuestion(Long studentId, Long questionId) {
        practiceMapper.deleteFavoriteQuestion(studentId, questionId);
    }

    @Override
    public List<Map<String, Object>> getFavoriteQuestions(Long studentId) {
        return practiceMapper.findFavoriteQuestions(studentId);
    }

    @Override
    public void addWrongQuestion(Long studentId, Long questionId, String wrongAnswer) {
        // 从数据库获取题目信息，包括正确答案
        Question question = questionMapper.findById(questionId);
        if (question == null) {
            throw new RuntimeException("题目不存在");
        }

        // 检查是否已存在该错题
        if (practiceMapper.existsWrongQuestion(studentId, questionId)) {
            // 如果存在，更新错误次数和最后错误时间
            practiceMapper.updateWrongQuestion(studentId, questionId, wrongAnswer, question.getAnswer());
        } else {
            // 如果不存在，新增错题记录
            practiceMapper.insertWrongQuestion(studentId, questionId, wrongAnswer, question.getAnswer());
        }
    }

    @Override
    public List<Map<String, Object>> getWrongQuestions(Long studentId) {
        return practiceMapper.findWrongQuestions(studentId);
    }

    @Override
    public List<Map<String, Object>> getWrongQuestionsByCourse(Long studentId, Long courseId) {
        return practiceMapper.findWrongQuestionsByCourse(studentId, courseId);
    }

    @Override
    public void removeWrongQuestion(Long studentId, Long questionId) {
        practiceMapper.deleteWrongQuestion(studentId, questionId);
    }

    @Override
    public List<Map<String, Object>> getCoursePractices(Long studentId, Long courseId) {
        // 先查询学生所在班级
        Long classId = practiceMapper.findClassIdByUserAndCourse(studentId, courseId);
        if (classId == null) {
            throw new RuntimeException("未找到学生所在班级");
        }

        // 根据班级ID和课程ID查询练习，同时传入studentId
        return practiceMapper.findCoursePractices(courseId, classId, studentId);
    }

}