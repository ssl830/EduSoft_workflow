package org.example.edusoft.service.selfpractice.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.edusoft.entity.selfpractice.SelfPractice;
import org.example.edusoft.entity.selfpractice.SelfPracticeQuestion;
import org.example.edusoft.mapper.selfpractice.SelfPracticeMapper;
import org.example.edusoft.mapper.selfpractice.SelfPracticeQuestionMapper;
import org.example.edusoft.entity.practice.Question;
import org.example.edusoft.mapper.practice.QuestionMapper;
import org.example.edusoft.service.selfpractice.SelfPracticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.example.edusoft.entity.selfpractice.SelfSubmission;
import org.example.edusoft.mapper.selfpractice.SelfSubmissionMapper;
import org.example.edusoft.entity.selfpractice.SelfAnswer;
import org.example.edusoft.mapper.selfpractice.SelfAnswerMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class SelfPracticeServiceImpl extends ServiceImpl<SelfPracticeMapper, SelfPractice> implements SelfPracticeService {

    @Autowired
    private SelfPracticeQuestionMapper spqMapper;

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private SelfSubmissionMapper submissionMapper;

    @Autowired
    private SelfAnswerMapper answerMapper;

    @Override
    public Long saveGeneratedPractice(Long studentId, Map<String, Object> aiResult) {
        SelfPractice sp = new SelfPractice();
        sp.setStudentId(studentId);
        sp.setTitle("AI自测 " + LocalDateTime.now());
        sp.setCreatedAt(LocalDateTime.now());
        this.save(sp);

        // 兼容两种结构：直接包含 exercises，或包在 data.exercises
        List<Map<String, Object>> exercises = (List<Map<String, Object>>) aiResult.get("exercises");
        if (exercises == null && aiResult.get("data") instanceof Map) {
            exercises = (List<Map<String, Object>>) ((Map<?, ?>) aiResult.get("data")).get("exercises");
        }
        if (exercises == null) return sp.getId();
        int order = 1;
        for (Map<String, Object> ex : exercises) {
            Question q = new Question();
            q.setCreatorId(studentId);
            q.setType(Question.QuestionType.valueOf(ex.get("type").toString()));
            q.setContent(ex.get("question").toString());
            q.setAnswer(ex.get("answer").toString());
            q.setAnalysis(ex.getOrDefault("explanation", "").toString());
            if (ex.containsKey("options")) {
                List<String> opts = (List<String>) ex.get("options");
                q.setOptionsList(opts);
            }
            // 直接保存题目避免严格校验字段
            q.setCreatedAt(LocalDateTime.now());
            // 修复：学生自建题目，course_id设为-1，section_id设为1
            q.setCourseId(-1L);
            q.setSectionId(1L);
            questionMapper.createQuestion(q);

            // 将新题目的ID写回原始列表，便于前端提交作答时使用
            ex.put("id", q.getId());
            ex.put("score", 10);

            SelfPracticeQuestion spq = new SelfPracticeQuestion();
            spq.setSelfPracticeId(sp.getId());
            spq.setQuestionId(q.getId());
            spq.setSortOrder(order++);
            spq.setScore(10);
            spqMapper.insert(spq);
        }
        return sp.getId();
    }

    @Override
    public List<Map<String, Object>> getHistory(Long stuId) {
        // 查询学生所有提交记录，按提交时间倒序
        List<SelfSubmission> subs = submissionMapper.selectList(
                new LambdaQueryWrapper<SelfSubmission>()
                        .eq(SelfSubmission::getStudentId, stuId)
                        .orderByDesc(SelfSubmission::getSubmittedAt)
        );
        if (subs == null || subs.isEmpty()) return java.util.Collections.emptyList();
        java.util.List<Map<String, Object>> list = new java.util.ArrayList<>();
        for (SelfSubmission sub : subs) {
            SelfPractice sp = this.getById(sub.getSelfPracticeId());
            java.util.Map<String, Object> item = new java.util.HashMap<>();
            item.put("practiceId", sub.getSelfPracticeId());
            item.put("title", sp != null ? sp.getTitle() : "AI自测");
            item.put("submittedAt", sub.getSubmittedAt());
            item.put("score", sub.getScore());
            list.add(item);
        }
        return list;
    }

    @Override
    public List<Map<String, Object>> getDetail(Long stuId, Long practiceId) {
        // 获取学生在该练习的最新一次提交
        SelfSubmission sub = submissionMapper.selectOne(
                new LambdaQueryWrapper<SelfSubmission>()
                        .eq(SelfSubmission::getStudentId, stuId)
                        .eq(SelfSubmission::getSelfPracticeId, practiceId)
                        .orderByDesc(SelfSubmission::getSubmittedAt)
                        .last("limit 1")
        );
        if (sub == null) return java.util.Collections.emptyList();
        java.util.List<SelfAnswer> answers = answerMapper.selectList(
                new LambdaQueryWrapper<SelfAnswer>()
                        .eq(SelfAnswer::getSubmissionId, sub.getId())
                        .orderByAsc(SelfAnswer::getSortOrder)
        );
        java.util.List<Map<String, Object>> result = new java.util.ArrayList<>();
        int idx = 1;
        for (SelfAnswer ans : answers) {
            Question q = questionMapper.findById(ans.getQuestionId());
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("order", idx++);
            map.put("questionId", ans.getQuestionId());
            map.put("question", q != null ? q.getContent() : "");
            map.put("options", q != null ? q.getOptionsList() : null);
            map.put("correctAnswer", q != null ? q.getAnswer() : "");
            map.put("studentAnswer", ans.getAnswerText());
            map.put("score", ans.getScore());
            map.put("correct", ans.getCorrect());
            result.add(map);
        }
        return result;
    }
} 