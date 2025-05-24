package org.example.edusoft.entity.record;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import javax.persistence.*;

@NoArgsConstructor  // 添加无参构造函数
@AllArgsConstructor // 添加全参构造函数
@Entity
@Data
public class PracticeRecord {
    private Long id;
    private Long practiceId;
    private Long studentId;
    private LocalDateTime submittedAt;
    private Integer score;
    private String feedback;
    
    // 关联属性
    private String practiceTitle;
    private List<AnswerDetail> answers;
    private String courseName;    // 添加课程名称
    private String className;     // 添加班级名称
}