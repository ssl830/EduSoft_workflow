package org.example.edusoft.mapper.record;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import org.example.edusoft.entity.record.StudyRecord; // Make sure this path matches your actual StudyRecord location

@Mapper
public interface StudyRecordMapper {
    @Select("""
        SELECT p.*, c.name as course_name, cs.title as section_title 
        FROM Progress p
        LEFT JOIN Course c ON p.course_id = c.id
        LEFT JOIN CourseSection cs ON p.section_id = cs.id
        WHERE p.student_id = #{studentId}
        ORDER BY p.completed_at DESC
    """)
    List<StudyRecord> findStudyRecords(@Param("studentId") Long studentId);
    
    @Select("""
        SELECT COUNT(*) as rank
        FROM (
            SELECT student_id, SUM(score) as total_score
            FROM Submission
            WHERE practice_id = #{practiceId}
            GROUP BY student_id
            HAVING SUM(score) > (
                SELECT SUM(score)
                FROM Submission
                WHERE practice_id = #{practiceId}
                AND student_id = #{studentId}
            )
        ) t
    """)
    int getPracticeRank(@Param("practiceId") Long practiceId, @Param("studentId") Long studentId);
}
