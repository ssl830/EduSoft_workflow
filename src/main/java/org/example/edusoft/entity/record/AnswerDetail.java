package org.example.edusoft.entity.record;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import javax.persistence.*;

@Data
@NoArgsConstructor  // 添加无参构造函数
@AllArgsConstructor // 添加全参构造函数
@Entity
public class AnswerDetail {
    private Long questionId;
    private String questionContent;
    private String answerText;
    private Boolean correct;
    private Integer score;
}