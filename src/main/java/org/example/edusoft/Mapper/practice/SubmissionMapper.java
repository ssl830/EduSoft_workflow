package org.example.edusoft.mapper.practice;

import org.apache.ibatis.annotations.*;
import org.example.edusoft.entity.practice.Submission;
import java.util.List;

@Mapper
public interface SubmissionMapper {
    @Insert({
        "INSERT INTO Submission(practice_id, student_id, score, is_judged)",
        "VALUES(#{practiceId}, #{studentId}, #{score}, #{isJudged})"
    })
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Submission submission);

    @Update({
        "UPDATE Submission SET score=#{score}, is_judged=#{isJudged}",
        "WHERE id=#{id}"
    })
    void update(Submission submission);

    @Select("SELECT * FROM Submission WHERE id = #{id}")
    Submission selectById(Long id);

    @Select("SELECT * FROM Submission WHERE practice_id = #{practiceId}")
    List<Submission> findByPracticeId(Long practiceId);
}