package org.example.edusoft.mapper.practice;

import org.apache.ibatis.annotations.*;
import java.util.List;
import java.util.Map;

@Mapper
public interface PracticeMapper {

    // 检查题目是否已收藏
    @Select("SELECT COUNT(*) FROM FavoriteQuestion WHERE student_id = #{studentId} AND question_id = #{questionId}")
    boolean isQuestionFavorited(@Param("studentId") Long studentId, @Param("questionId") Long questionId);

    // 添加收藏题目
    @Insert("INSERT INTO FavoriteQuestion (student_id, question_id) VALUES (#{studentId}, #{questionId})")
    void insertFavoriteQuestion(@Param("studentId") Long studentId, @Param("questionId") Long questionId);

    // 删除收藏题目
    @Delete("DELETE FROM FavoriteQuestion WHERE student_id = #{studentId} AND question_id = #{questionId}")
    void deleteFavoriteQuestion(@Param("studentId") Long studentId, @Param("questionId") Long questionId);

    // 获取收藏的题目列表
    @Select("""
                SELECT
                    q.id,
                    q.content,
                    q.type,
                    q.options,
                    q.answer,
                    c.name as course_name,
                    cs.title as section_title,
                    p.title as practice_title
                FROM FavoriteQuestion fq
                JOIN Question q ON fq.question_id = q.id
                LEFT JOIN Course c ON q.course_id = c.id
                LEFT JOIN CourseSection cs ON q.section_id = cs.id
                LEFT JOIN PracticeQuestion pq ON q.id = pq.question_id
                LEFT JOIN Practice p ON pq.practice_id = p.id
                WHERE fq.student_id = #{studentId}
                ORDER BY q.created_at DESC
            """)
    List<Map<String, Object>> findFavoriteQuestions(@Param("studentId") Long studentId);

    // 检查错题是否存在
    @Select("SELECT COUNT(*) FROM WrongQuestion WHERE student_id = #{studentId} AND question_id = #{questionId}")
    boolean existsWrongQuestion(@Param("studentId") Long studentId, @Param("questionId") Long questionId);

    // 添加错题
    @Insert("""
                INSERT INTO WrongQuestion
                (student_id, question_id, wrong_answer, correct_answer)
                VALUES
                (#{studentId}, #{questionId}, #{wrongAnswer}, #{correctAnswer})
            """)
    void insertWrongQuestion(
            @Param("studentId") Long studentId,
            @Param("questionId") Long questionId,
            @Param("wrongAnswer") String wrongAnswer,
            @Param("correctAnswer") String correctAnswer);

    // 更新错题
    @Update("""
                UPDATE WrongQuestion
                SET wrong_answer = #{wrongAnswer},
                    correct_answer = #{correctAnswer},
                    wrong_count = wrong_count + 1,
                    last_wrong_time = CURRENT_TIMESTAMP
                WHERE student_id = #{studentId} AND question_id = #{questionId}
            """)
    void updateWrongQuestion(
            @Param("studentId") Long studentId,
            @Param("questionId") Long questionId,
            @Param("wrongAnswer") String wrongAnswer,
            @Param("correctAnswer") String correctAnswer);

    // 删除错题
    @Delete("DELETE FROM WrongQuestion WHERE student_id = #{studentId} AND question_id = #{questionId}")
    void deleteWrongQuestion(@Param("studentId") Long studentId, @Param("questionId") Long questionId);

    // 获取错题列表
    @Select("""
                SELECT
                    wq.id,
                    wq.wrong_answer,
                    wq.correct_answer,
                    wq.wrong_count,
                    wq.last_wrong_time,
                    q.content,
                    q.type,
                    q.options,
                    c.name as course_name,
                    cs.title as section_title,
                    p.title as practice_title
                FROM WrongQuestion wq
                JOIN Question q ON wq.question_id = q.id
                LEFT JOIN Course c ON q.course_id = c.id
                LEFT JOIN CourseSection cs ON q.section_id = cs.id
                LEFT JOIN PracticeQuestion pq ON q.id = pq.question_id
                LEFT JOIN Practice p ON pq.practice_id = p.id
                WHERE wq.student_id = #{studentId}
                ORDER BY wq.last_wrong_time DESC
            """)
    List<Map<String, Object>> findWrongQuestions(@Param("studentId") Long studentId);

    // 获取某个课程的错题列表
    @Select("""
                SELECT
                    wq.id,
                    wq.wrong_answer,
                    wq.correct_answer,
                    wq.wrong_count,
                    wq.last_wrong_time,
                    q.content,
                    q.type,
                    q.options,
                    c.name as course_name,
                    cs.title as section_title,
                    p.title as practice_title
                FROM WrongQuestion wq
                JOIN Question q ON wq.question_id = q.id
                LEFT JOIN Course c ON q.course_id = c.id
                LEFT JOIN CourseSection cs ON q.section_id = cs.id
                LEFT JOIN PracticeQuestion pq ON q.id = pq.question_id
                LEFT JOIN Practice p ON pq.practice_id = p.id
                WHERE wq.student_id = #{studentId}
                AND q.course_id = #{courseId}
                ORDER BY wq.last_wrong_time DESC
            """)
    List<Map<String, Object>> findWrongQuestionsByCourse(
            @Param("studentId") Long studentId,
            @Param("courseId") Long courseId);

    @Select("""
                SELECT
                    p.id,
                    p.title,
                    p.start_time,
                    p.end_time,
                    p.allow_multiple_submission,
                    p.created_at,
                    (
                        SELECT COUNT(*)
                        FROM PracticeQuestion pq
                        WHERE pq.practice_id = p.id
                    ) as question_count,
                    (
                        SELECT COUNT(*)
                        FROM Submission s
                        WHERE s.practice_id = p.id
                        AND s.student_id = #{studentId}
                    ) as submission_count,
                    (
                        SELECT s.score
                        FROM Submission s
                        WHERE s.practice_id = p.id
                        AND s.student_id = #{studentId}
                        ORDER BY s.submitted_at DESC
                        LIMIT 1
                    ) as last_score
                FROM Practice p
                WHERE p.course_id = #{courseId}
                ORDER BY p.created_at DESC
            """)
    List<Map<String, Object>> findCoursePractices(
            @Param("courseId") Long courseId,
            @Param("studentId") Long studentId);
}