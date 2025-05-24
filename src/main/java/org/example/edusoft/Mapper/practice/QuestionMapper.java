package org.example.edusoft.mapper.practice;

import java.util.List;

import org.apache.ibatis.annotations.*;
import org.example.edusoft.entity.practice.Question;


@Mapper
public interface QuestionMapper {
    @Select("SELECT * FROM Question WHERE id = #{id}")
    Question selectById(Long id);
}
