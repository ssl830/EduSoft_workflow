package org.example.edusoft.service.practice;

import java.util.List;
import java.util.stream.Collectors;

import org.example.edusoft.entity.practice.*;
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

    public Result<List<Answer>> getPendingAnswers(Long practiceId, Long submissionId) {
        List<Question> questionList = practiceQuestionMapper.findByPracticeId(practiceId);
        List<Long> questionIds = questionList.stream()
            .filter(q -> q.getType() != Question.QuestionType.singlechoice)
            .map(Question::getId)
            .collect(Collectors.toList());

        return Result.ok(
            answerMapper.findByQuestionIdsAndSubmissionId(questionIds, submissionId),
            "获取待评分答案成功"
        );
    }
}