package org.example.edusoft.mapper.practice1;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.example.edusoft.entity.practice.*;
@Mapper
public interface QuestionMapper {
    @Select("SELECT * FROM Question WHERE id = #{id}")
    Question findById(Long id);
}