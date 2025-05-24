package org.example.edusoft.entity.record;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import javax.persistence.*;

@NoArgsConstructor  // 添加无参构造函数
@AllArgsConstructor // 添加全参构造函数
@Entity
@Data
public class QuestionRecord {
    private Long id;
    private String content;          // 题目内容
    private String type;            // 题目类型
    private String options; 
    private String studentAnswer;    // 学生答案
    private String correctAnswer;    // 正确答案
    private Boolean isCorrect;       // 是否正确
    private Integer score;           // 得分
    private String feedback;         // 题目反馈
}