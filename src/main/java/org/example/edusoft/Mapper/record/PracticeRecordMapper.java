package org.example.edusoft.mapper.record;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;
import org.example.edusoft.entity.record.*;
@Mapper
public interface PracticeRecordMapper {
    @Select("""
        SELECT s.*, p.title as practice_title
        FROM Submission s
        LEFT JOIN Practice p ON s.practice_id = p.id
        WHERE s.student_id = #{studentId}
        ORDER BY s.submitted_at DESC
    """)
    List<PracticeRecord> findPracticeRecords(@Param("studentId") Long studentId);
    
    @Select("""
        SELECT a.*, q.content as question_content
        FROM Answer a
        LEFT JOIN Question q ON a.question_id = q.id
        WHERE a.submission_id = #{submissionId}
    """)
    List<AnswerDetail> findAnswerDetails(@Param("submissionId") Long submissionId);

    @Select("""
        SELECT COUNT(DISTINCT student_id) 
        FROM Submission
        WHERE practice_id = #{practiceId}
    """)
    int getTotalStudentsInPractice(@Param("practiceId") Long practiceId);
    
    @Select("""
        SELECT 
            CASE 
                WHEN score >= 90 THEN '90-100'
                WHEN score >= 80 THEN '80-89'
                WHEN score >= 70 THEN '70-79'
                WHEN score >= 60 THEN '60-69'
                ELSE '0-59'
            END as score_range,
            COUNT(*) as count
        FROM Submission
        WHERE practice_id = #{practiceId}
        GROUP BY 
            CASE 
                WHEN score >= 90 THEN '90-100'
                WHEN score >= 80 THEN '80-89'
                WHEN score >= 70 THEN '70-79'
                WHEN score >= 60 THEN '60-69'
                ELSE '0-59'
            END
        ORDER BY score_range DESC
    """)
    List<Map<String, Object>> getScoreDistribution(@Param("practiceId") Long practiceId);

    @Select("""
        SELECT s.*, p.title as practice_title,
               c.name as course_name, cl.name as class_name
        FROM Submission s
        LEFT JOIN Practice p ON s.practice_id = p.id
        LEFT JOIN Course c ON p.course_id = c.id
        LEFT JOIN ClassUser cu ON s.student_id = cu.user_id
        LEFT JOIN Class cl ON cu.class_id = cl.id
        WHERE s.practice_id = #{practiceId} 
        AND s.student_id = #{studentId}
    """)
    PracticeRecord findPracticeRecord(@Param("practiceId") Long practiceId, 
                                    @Param("studentId") Long studentId);

    @Select("""
        SELECT COUNT(*) + 1 
        FROM Submission s1
        WHERE s1.practice_id = #{practiceId}
        AND s1.score > (
            SELECT score 
            FROM Submission s2
            WHERE s2.practice_id = #{practiceId}
            AND s2.student_id = #{studentId}
        )
    """)
    int getPracticeRank(@Param("practiceId") Long practiceId, 
                       @Param("studentId") Long studentId);

    @Select("""
        SELECT 
            CASE 
                WHEN score >= 90 THEN '90-100'
                WHEN score >= 80 THEN '80-89'
                WHEN score >= 70 THEN '70-79'
                WHEN score >= 60 THEN '60-69'
                ELSE '0-59'
            END as score_range,
            COUNT(*) as count,
            ROUND(COUNT(*) * 100.0 / SUM(COUNT(*)) OVER(), 2) as percentage
        FROM practice_submission
        WHERE practice_id = #{practiceId}
        AND class_id = (
            SELECT class_id 
            FROM practice_submission
            WHERE practice_id = #{practiceId}
            AND student_id = #{studentId}
            LIMIT 1
        )
        GROUP BY 
            CASE 
                WHEN score >= 90 THEN '90-100'
                WHEN score >= 80 THEN '80-89'
                WHEN score >= 70 THEN '70-79'
                WHEN score >= 60 THEN '60-69'
                ELSE '0-59'
            END
        ORDER BY score_range DESC
    """)
    List<Map<String, Object>> getScoreDistribution(@Param("practiceId") Long practiceId,
                                                 @Param("studentId") Long studentId);
    
    @Select("""
        SELECT 
            AVG(score) as average_score,
            MAX(score) as highest_score,
            MIN(score) as lowest_score,
            COUNT(*) as total_submissions
        FROM practice_submission
        WHERE practice_id = #{practiceId}
        AND class_id = (
            SELECT class_id 
            FROM practice_submission
            WHERE practice_id = #{practiceId}
            AND student_id = #{studentId}
            LIMIT 1
        )
    """)
    Map<String, Object> getPracticeStats(@Param("practiceId") Long practiceId,
                                       @Param("studentId") Long studentId);
}