package org.example.edusoft.mapper.practice;

import java.util.List; 
import org.apache.ibatis.annotations.*;
import org.example.edusoft.entity.practice.*;


@Mapper
public interface AnswerMapper {
    @Insert({
        "INSERT INTO Answer(submission_id, question_id, answer_text, is_judged, correct, score)",
        "VALUES(#{submissionId}, #{questionId}, #{answerText}, #{isJudged}, #{correct}, #{score})"
    })
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Answer answer);

    @Update({
        "UPDATE Answer SET answer_text=#{answerText}, is_judged=#{isJudged},",
        "correct=#{correct}, score=#{score} WHERE id=#{id}"
    })
    void update(Answer answer);

    @Select("SELECT * FROM Answer WHERE submission_id = #{submissionId}")
    List<Answer> findBySubmissionId(Long submissionId);

    @Select("SELECT * FROM Answer WHERE question_id IN (#{questionIds}) AND submission_id = #{submissionId}")
    List<Answer> findByQuestionIdsAndSubmissionId(@Param("questionIds") List<Long> questionIds,
                                                  @Param("submissionId") Long submissionId);

    @Update({
        "UPDATE Answer SET score=#{score}, is_judged=#{isJudged} WHERE id=#{id}"
    })
    void updateScoreAndJudgment(Answer answer);
}