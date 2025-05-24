package org.example.edusoft.service.practice;

import java.util.List;
import java.util.Map;

public interface PracticeService {
    // 收藏题目
    void favoriteQuestion(Long studentId, Long questionId);
    
    // 取消收藏题目
    void unfavoriteQuestion(Long studentId, Long questionId);
    
    // 获取收藏的题目列表
    List<Map<String, Object>> getFavoriteQuestions(Long studentId);
 
    // 添加错题
    void addWrongQuestion(Long studentId, Long questionId, String wrongAnswer);
    
    // 获取错题列表
    List<Map<String, Object>> getWrongQuestions(Long studentId);
    
    // 获取某个课程的错题列表
    List<Map<String, Object>> getWrongQuestionsByCourse(Long studentId, Long courseId);
    
    // 删除错题
    void removeWrongQuestion(Long studentId, Long questionId);

    // 获取课程的所有练习
    List<Map<String, Object>> getCoursePractices(Long studentId, Long courseId);

} 