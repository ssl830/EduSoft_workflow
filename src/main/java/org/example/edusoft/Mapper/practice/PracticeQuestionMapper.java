package org.example.edusoft.mapper.practice;

import java.util.List;

import org.apache.ibatis.annotations.*;
import org.example.edusoft.entity.practice.*;

@Mapper
public interface PracticeQuestionMapper {
    @Select("SELECT q.* FROM PracticeQuestion pq JOIN Question q ON pq.question_id = q.id WHERE pq.practice_id = #{practiceId}")
    List<Question> findByPracticeId(Long practiceId);

    @Select("SELECT * FROM PracticeQuestion pq WHERE pq.practice_id = #{practiceId}")
    List<PracticeQuestion> findpqByPracticeId(Long practiceId);
}
