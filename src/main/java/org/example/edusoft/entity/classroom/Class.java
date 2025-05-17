package org.example.edusoft.entity.classroom;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("Class")
public class Class {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long courseId;
    private String name;
    private String classCode;  // 班级暗号，用于学生加入
} 