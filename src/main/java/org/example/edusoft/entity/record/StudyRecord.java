package org.example.edusoft.entity.record;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import javax.persistence.*;

@NoArgsConstructor  // 添加无参构造函数
@AllArgsConstructor // 添加全参构造函数
@Entity
@Data
public class StudyRecord {
    private Long id;
    private Long studentId;
    private Long courseId;
    private Long sectionId;
    private Boolean completed;
    private LocalDateTime completedAt;
    
    // 关联属性
    private String courseName;
    private String sectionTitle;
}