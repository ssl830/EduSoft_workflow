package org.example.edusoft.entity.practice;
import lombok.Data;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import javax.persistence.*;

@NoArgsConstructor  // 添加无参构造函数
@AllArgsConstructor // 添加全参构造函数
@Entity
@Data
public class Question {
    private Long id;
    private String content;        // 题目内容
    private String type;          // 题目类型（SINGLE_CHOICE, MULTIPLE_CHOICE, TRUE_FALSE, FILL_BLANK）
    private String options;       // 选项，JSON格式存储
    private String answer;        // 正确答案
    private Long courseId;        // 所属课程ID
    private Long sectionId;       // 所属章节ID
    private String analysis;      // 题目解析
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
