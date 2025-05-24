package org.example.edusoft.mapper.practice;

import org.apache.ibatis.annotations.*;
import org.example.edusoft.entity.practice.Question;
import java.util.List;

@Mapper
public interface QuestionMapper {

    @Select("SELECT * FROM Question WHERE id = #{id}")
    Question selectById(Long id);
    
    @Insert("INSERT INTO Question (creator_id, type, content, analysis, options, answer, course_id, section_id) " +
            "VALUES (#{creatorId}, #{type}, #{content}, #{analysis}, #{options}, #{answer}, #{courseId}, #{sectionId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void createQuestion(Question question);

    @Select("SELECT * FROM Question WHERE course_id = #{courseId} " +
            "ORDER BY created_at DESC LIMIT #{offset}, #{size}")
    List<Question> getQuestionList(@Param("courseId") Long courseId,
                                  @Param("offset") Integer offset,
                                  @Param("size") Integer size);

    @Select("SELECT * FROM Question WHERE id = #{id}")
    Question getQuestionById(Long id);

    @Select("SELECT * FROM Question WHERE course_id = #{courseId} AND section_id = #{sectionId}")
    List<Question> getQuestionsBySection(@Param("courseId") Long courseId, @Param("sectionId") Long sectionId);

    @Update("UPDATE Question SET content = #{content}, type = #{type}, options = #{options}, " +
            "answer = #{answer}, analysis = #{analysis}, section_id = #{sectionId} WHERE id = #{id}")
    void updateQuestion(Question question);

    @Delete("DELETE FROM Question WHERE id = #{id}")
    void deleteQuestion(Long id);

    @Select("SELECT COUNT(*) FROM Question WHERE course_id = #{courseId}")
    int getQuestionCount(Long courseId);

    @Insert("INSERT INTO PracticeQuestion (practice_id, question_id, score) VALUES (#{practiceId}, #{questionId}, #{score})")
    void addQuestionToPractice(@Param("practiceId") Long practiceId,
                             @Param("questionId") Long questionId,
                             @Param("score") Integer score);

    @Delete("DELETE FROM PracticeQuestion WHERE practice_id = #{practiceId} AND question_id = #{questionId}")
    void removeQuestionFromPractice(@Param("practiceId") Long practiceId, @Param("questionId") Long questionId);

    @Delete("DELETE FROM PracticeQuestion WHERE practice_id = #{practiceId}")
    void removeAllQuestionsFromPractice(@Param("practiceId") Long practiceId);

    @Select("SELECT q.*, pq.score FROM Question q " +
            "JOIN PracticeQuestion pq ON q.id = pq.question_id " +
            "WHERE pq.practice_id = #{practiceId}")
    List<Question> getQuestionsByPractice(Long practiceId);



    @Select("SELECT * FROM Question WHERE id = #{id}")
    Question findById(Long id);
} 
