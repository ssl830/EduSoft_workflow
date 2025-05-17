package org.example.edusoft.entity.course;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("Course")
public class Course {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long teacherId;
    private String name;
    private String code;
    private String outline;
    private String objective;
    private String assessment;
    private LocalDateTime createdAt;
} 