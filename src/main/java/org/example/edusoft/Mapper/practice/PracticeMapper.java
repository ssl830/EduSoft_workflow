package org.example.edusoft.mapper.practice;

import org.apache.ibatis.annotations.*;
import org.example.edusoft.entity.practice.Practice;
import java.util.List;

@Mapper
public interface PracticeMapper {
    
    @Insert("INSERT INTO Practice (course_id, class_id, title, start_time, end_time, allow_multiple_submission, created_by) " +
            "VALUES (#{courseId}, #{classId}, #{title}, #{startTime}, #{endTime}, #{allowMultipleSubmission}, #{createdBy})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void createPractice(Practice practice);

    @Update("UPDATE Practice SET title = #{title}, start_time = #{startTime}, end_time = #{endTime}, " +
            "allow_multiple_submission = #{allowMultipleSubmission} WHERE id = #{id}")
    void updatePractice(Practice practice);

    @Select("SELECT * FROM Practice WHERE course_id = #{courseId} AND class_id = #{classId} " +
            "ORDER BY created_at DESC LIMIT #{offset}, #{size}")
    List<Practice> getPracticeList(@Param("courseId") Long courseId, 
                                  @Param("classId") Long classId,
                                  @Param("offset") Integer offset,
                                  @Param("size") Integer size);

    @Select("SELECT * FROM Practice WHERE id = #{id}")
    Practice getPracticeById(Long id);

    @Delete("DELETE FROM Practice WHERE id = #{id}")
    void deletePractice(Long id);

    @Select("SELECT COUNT(*) FROM Practice WHERE course_id = #{courseId} AND class_id = #{classId}")
    int getPracticeCount(@Param("courseId") Long courseId, @Param("classId") Long classId);
} 