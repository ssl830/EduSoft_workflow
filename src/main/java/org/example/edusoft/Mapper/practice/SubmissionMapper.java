package org.example.edusoft.mapper.practice;

import org.apache.ibatis.annotations.*;
import org.example.edusoft.entity.practice.Submission;

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
}