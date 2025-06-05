package org.example.edusoft.service.practice;

import org.example.edusoft.mapper.practice.*;
import org.example.edusoft.entity.practice.*;
import org.example.edusoft.common.domain.*;


import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class SubmissionService {

    @Autowired
    private PracticeQuestionMapper practiceQuestionMapper;

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private AnswerMapper answerMapper;

    @Autowired
    private SubmissionMapper submissionMapper;

    public Result<Integer> autoJudgeAnswers(Long practiceId, Long studentId, Map<Long, String> answers) {
        // 获取练习题目
        List<PracticeQuestion> questions = practiceQuestionMapper.findpqByPracticeId(practiceId);

        // 创建提交记录
        Submission submission = new Submission();
        submission.setPracticeId(practiceId);
        submission.setStudentId(studentId);
        submission.setIsJudged(0);
        submission.setScore(0);
        submissionMapper.insert(submission);

        int totalScore = 0;

        for (PracticeQuestion pq : questions) {
            Long qId = pq.getQuestionId();
            Question question = questionMapper.selectById(qId);
            String userAnswer = answers.getOrDefault(qId, "");

            Answer answer = new Answer();
            answer.setSubmissionId(submission.getId());
            answer.setQuestionId(qId);
            answer.setAnswerText(userAnswer);

            if (question.getType() == Question.QuestionType.singlechoice) {
                boolean correct = userAnswer.equals(question.getAnswer());
                answer.setCorrect(correct);
                answer.setScore(correct ? pq.getScore() : 0);
                answer.setIsJudged(true);
                totalScore += answer.getScore();
            } else {
                answer.setScore(0);
                answer.setIsJudged(false);
                submission.setIsJudged(submission.getIsJudged() + 1);
            }

            answerMapper.insert(answer);
        }

        submission.setScore(totalScore);
        submissionMapper.update(submission);

        return Result.ok(totalScore, "自动评分完成");
    }
}
