package org.example.edusoft.service.practice;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.ArrayList;

import org.example.edusoft.entity.practice.*;
import org.example.edusoft.dto.practice.QuestionAnswerDTO;
import org.example.edusoft.dto.practice.SubmissionAnswersDTO;
import org.example.edusoft.common.domain.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.*;

import org.example.edusoft.mapper.practice.*;
import org.example.edusoft.common.domain.*;
 
@Service
public class ManualJudgeService {

    @Autowired
    private AnswerMapper answerMapper;

    @Autowired
    private SubmissionMapper submissionMapper;

    @Autowired
    private PracticeQuestionMapper practiceQuestionMapper;

    public Result<Void> manualJudge(List<Answer> answers) {
        for (Answer ans : answers) {
            answerMapper.updateScoreAndJudgment(ans);

            Submission submission = submissionMapper.selectById(ans.getSubmissionId());
            submission.setScore(submission.getScore() + ans.getScore());

            if (ans.getIsJudged()) {
                submission.setIsJudged(submission.getIsJudged() - 1);
            }

            submissionMapper.update(submission);
        }

        return Result.ok(null, "评分更新成功");
    }

    public Result<List<SubmissionAnswersDTO>> getPendingAnswers(Long practiceId, Long submissionId) {
        // 1. 获取练习中的所有非单选题
        List<Question> questionList = practiceQuestionMapper.findByPracticeId(practiceId);
        List<Long> questionIds = questionList.stream()
            .filter(q -> q.getType() != Question.QuestionType.singlechoice)
            .map(Question::getId)
            .collect(Collectors.toList());

        if (questionIds.isEmpty()) {
            return Result.ok(new ArrayList<>(), "获取待评分答案成功");
        }

        // 2. 获取需要评分的提交记录
        List<Answer> answers;
        if (submissionId != null) {
            // 如果指定了submissionId，只获取该提交的答案
            answers = answerMapper.findunjudgedByQuestionIdsAndSubmissionId(questionIds, submissionId);
        } else {
            // 获取所有有未评分答案的提交记录
            List<Submission> submissions = submissionMapper.findByPracticeIdWithUnjudgedAnswers(practiceId);
            if (submissions.isEmpty()) {
                return Result.ok(new ArrayList<>(), "获取待评分答案成功");
            }
            
            List<Long> submissionIds = submissions.stream()
                .map(Submission::getId)
                .collect(Collectors.toList());
            
            // 获取这些提交记录的所有答案
            answers = answerMapper.findunjudgedByQuestionIdsAndSubmissionIds(questionIds, submissionIds);
        }

        // 3. 获取题目分数信息
        List<PracticeQuestion> practiceQuestions = practiceQuestionMapper.findpqByPracticeId(practiceId);
        Map<Long, Integer> questionScores = practiceQuestions.stream()
            .collect(Collectors.toMap(PracticeQuestion::getQuestionId, PracticeQuestion::getScore));

        // 4. 按提交记录分组答案
        Map<Long, List<Answer>> answersBySubmission = answers.stream()
            .collect(Collectors.groupingBy(Answer::getSubmissionId));

        // 5. 转换为DTO
        List<SubmissionAnswersDTO> result = new ArrayList<>();
        answersBySubmission.forEach((subId, subAnswers) -> {
            SubmissionAnswersDTO submissionDTO = new SubmissionAnswersDTO();
            submissionDTO.setId(subId);
            submissionDTO.setSubmissionId(subId);
            
            List<QuestionAnswerDTO> questionAnswers = subAnswers.stream()
                .map(answer -> {
                    QuestionAnswerDTO dto = new QuestionAnswerDTO();
                    dto.setQuestionId(answer.getQuestionId());
                    dto.setAnswerText(answer.getAnswerText());
                    dto.setIsJudged(answer.getIsJudged());
                    dto.setCorrect(answer.getCorrect());
                    dto.setScore(answer.getScore());
                    dto.setMaxScore(questionScores.get(answer.getQuestionId()));
                    return dto;
                })
                .collect(Collectors.toList());
            
            submissionDTO.setQuestion(questionAnswers);
            result.add(submissionDTO);
        });

        return Result.ok(result, "获取待评分答案成功");
    }
}