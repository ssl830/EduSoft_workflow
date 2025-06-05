package org.example.edusoft.entity.classroom;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("ClassUser")
public class ClassUser {
    private Long classId;
    private Long userId;
    private LocalDateTime joinedAt;
} 